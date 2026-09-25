/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.mcp.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.spi.Processor;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.Lookup;

public class GephiControlService {

    private static final Logger LOGGER = Logger.getLogger(GephiControlService.class.getName());
    private static GephiControlService instance;

    private final AtomicBoolean layoutRunning = new AtomicBoolean(false);
    private volatile String currentLayoutName = null;
    private volatile Future<?> layoutFuture = null;
    // Not final: shutdown() kills it, and the server can be stopped and restarted from
    // Tools > Gephi AI Server without the service singleton being recreated. A final
    // executor left every later layout failing with RejectedExecutionException for the
    // rest of the session. Always reach it through layoutExecutor().
    private ExecutorService layoutExecutor = Executors.newSingleThreadExecutor();
    // Config staged by setLayoutProperties (configure-only); the next runLayout of
    // the same algorithm applies it. Lets set-then-run work without setLayoutProperties
    // itself starting a layout.
    private volatile Map<String, Object> pendingLayoutProps = null;
    private volatile String pendingLayoutAlgo = null;

    // Human click journal: the person's node clicks in the Gephi window,
    // recorded by a passive viz-event listener so the model can resolve
    // "this one" / "these" to actual nodes. Bounded; strings only (never
    // hold Node references — they outlive workspaces).
    private static final int CLICK_JOURNAL_MAX = 50;
    private final java.util.ArrayDeque<JsonObject> clickJournal = new java.util.ArrayDeque<>();
    private volatile boolean clickListenerInstalled = false;
    // Rectangle selection is turned on once per session so the human can box-select
    // nodes for the agent to read without hunting for the toolbar tool. Set only
    // after it actually succeeds (the view may not be started at the first attempt).
    private volatile boolean rectangleAutoEnabled = false;

    private GephiControlService() {}

    public static synchronized GephiControlService getInstance() {
        if (instance == null) instance = new GephiControlService();
        return instance;
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private ProjectController getProjectController() {
        return Lookup.getDefault().lookup(ProjectController.class);
    }

    private GraphController getGraphController() {
        return Lookup.getDefault().lookup(GraphController.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T runOnEDT(Callable<T> callable) {
        if (SwingUtilities.isEventDispatchThread()) {
            try { return callable.call(); }
            catch (Exception e) { throw new RuntimeException(e); }
        }
        // Bounded wait: invokeAndWait parks forever when the EDT is wedged (the
        // "health answers but nothing else does" symptom). Fail fast with guidance
        // instead of hanging until the client's timeout.
        final Object[] result = new Object[1];
        final Exception[] exception = new Exception[1];
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try { result[0] = callable.call(); }
            catch (Exception e) { exception[0] = e; }
            finally { done.countDown(); }
        });
        try {
            if (!done.await(15, java.util.concurrent.TimeUnit.SECONDS)) {
                throw new RuntimeException(
                    "Gephi's UI thread is unresponsive — the app is likely wedged; fully quit and reopen Gephi");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for Gephi's UI thread");
        }
        if (exception[0] != null) throw new RuntimeException(exception[0]);
        return (T) result[0];
    }

    static JsonObject success(String msg) {
        JsonObject r = new JsonObject();
        r.addProperty("success", true);
        r.addProperty("message", msg);
        return r;
    }

    static JsonObject error(String msg) {
        JsonObject r = new JsonObject();
        r.addProperty("success", false);
        r.addProperty("error", msg);
        return r;
    }

    private Workspace currentWorkspace() {
        return getProjectController().getCurrentWorkspace();
    }

    private GraphModel currentGraphModel() {
        Workspace ws = currentWorkspace();
        return ws != null ? getGraphController().getGraphModel(ws) : null;
    }

    // ─── Write-lock acquisition (VizEngine-deadlock-safe) ────────────────

    private static volatile java.lang.reflect.Field WRITE_LOCK_FIELD;

    /**
     * Acquire the graph write lock by polling a non-queuing tryLock() instead of the
     * blocking writeLock(). Gephi's OpenGL VizEngine runs a concurrent "world updater"
     * that holds read locks while join()-ing on sub-tasks that also need read locks; a
     * writer parked indefinitely in the lock's wait queue blocks those sub-readers (writer
     * preference) and deadlocks the renderer permanently (the chronic macOS hang).
     *
     * We instead use a SHORT timed tryLock: it queues only briefly, so it still gets
     * writer-preference and acquires even while the renderer reads near-continuously
     * (e.g. right after a layout) — but if it lands in the nested-read window it times out,
     * dequeues, lets the renderer drain, and retries. So it can never wedge. Once we hold
     * the lock, any Gephi-internal writeLock() on this same thread (setVisibleView, etc.)
     * re-enters for free, which is why callers wrap those calls too. Falls back to the plain
     * blocking lock only if the underlying lock can't be reflected. Throws after ~15s, which
     * callers turn into a "graph busy" error instead of hanging forever.
     */
    static void lockWrite(Graph g) {
        RenderPause.pause();   // free the renderer's read-lock pressure for this section
        boolean acquired = false;
        try {
            java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock wl = writeLockHandle(g);
            if (wl == null) { g.writeLock(); acquired = true; return; }
            long deadline = System.nanoTime() + 15_000_000_000L;
            while (!wl.tryLock(120, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                if (System.nanoTime() > deadline)
                    throw new RuntimeException("Graph is busy (renderer holds the lock); please retry");
                Thread.sleep(5);
            }
            acquired = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring the write lock");
        } catch (Throwable t) {
            // Any other failure here (e.g. a classloading Error, which is not an Exception
            // and would otherwise skip every catch(Exception) up the call chain and kill the
            // HTTP connection with no response) must still surface as a normal API error.
            throw new RuntimeException("Could not acquire write lock: " + t, t);
        } finally {
            if (!acquired) RenderPause.resume();
        }
    }

    /** Release the write lock and resume the renderer paused by lockWrite. */
    static void unlockWrite(Graph g) {
        try {
            g.writeUnlock();
        } finally {
            RenderPause.resume();
        }
    }

    /**
     * Preview refresh on the EDT — the one piece of the former runOnEDT bodies that
     * belongs there (it touches Swing-backed preview state). Failures are logged, not
     * surfaced: by the time this runs the graph mutation has already been applied, and
     * returning an error for a cosmetic refresh would tell the client a destructive
     * operation failed when it did not, inviting a double-apply retry.
     */
    private void refreshPreviewOnEDT(Workspace ws) {
        try {
            PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
            if (pc != null) runOnEDT(() -> { pc.refreshPreview(ws); return null; });
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Preview refresh failed after graph mutation", e);
        }
    }

    private static volatile java.lang.reflect.Field READ_LOCK_FIELD;

    /*
     * ITERATION RULE (wedge prevention): never iterate a live NodeIterable /
     * EdgeIterable directly — always iterate .toArray(). A live iterator
     * auto-acquires the graph read lock in its constructor and releases it only
     * on exhaustion or doBreak(); an early break, return, or exception leaks the
     * hold, and because NanoHTTPD threads die after their request, the leak is
     * permanent and wedges every future write (found the hard way; see
     * GraphOpsTest#earlyBreakOverToArraySnapshotLeavesNoReadHold).
     */

    /**
     * Timed read-lock acquisition. Plain readLock() parks unboundedly in the lock's
     * wait queue; when a writer is already parked (Gephi's own blocking writeLock())
     * every new reader queues behind it and the request hangs until the client's
     * timeout — the chronic "health answers but nothing else does" symptom. A timed
     * tryLock turns that into an immediate, actionable error instead.
     */
    static void lockRead(Graph g) {
        java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock rl = readLockHandle(g);
        if (rl == null) { g.readLock(); return; }
        long deadline = System.nanoTime() + 10_000_000_000L;
        try {
            while (!rl.tryLock(120, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                if (System.nanoTime() > deadline)
                    throw new RuntimeException(
                        "Graph is busy (lock unavailable) — if this persists, Gephi is wedged; fully quit and reopen it");
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring the read lock");
        }
    }

    /** The underlying ReentrantReadWriteLock.ReadLock behind Graph.getLock(), or null if unreachable. */
    static java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock readLockHandle(Graph g) {
        try {
            org.gephi.graph.api.GraphLock lock = g.getLock();
            if (lock == null) return null;
            java.lang.reflect.Field f = READ_LOCK_FIELD;
            if (f == null || !f.getDeclaringClass().isInstance(lock)) {
                f = lock.getClass().getDeclaredField("readLock");
                f.setAccessible(true);
                READ_LOCK_FIELD = f;
            }
            Object v = f.get(lock);
            return (v instanceof java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock)
                ? (java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock) v : null;
        } catch (Throwable t) {
            return null;
        }
    }

    /** The underlying ReentrantReadWriteLock.WriteLock behind Graph.getLock(), or null if unreachable. */
    static java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock writeLockHandle(Graph g) {
        try {
            org.gephi.graph.api.GraphLock lock = g.getLock();
            if (lock == null) return null;
            java.lang.reflect.Field f = WRITE_LOCK_FIELD;
            if (f == null || !f.getDeclaringClass().isInstance(lock)) {
                f = lock.getClass().getDeclaredField("writeLock");
                f.setAccessible(true);
                WRITE_LOCK_FIELD = f;
            }
            Object v = f.get(lock);
            return (v instanceof java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock)
                ? (java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock) v : null;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Find an edge between two nodes, checking all edge types (directed type 1 and undirected type 0). */
    static Edge findEdge(Graph g, Node source, Node target) {
        Edge e = g.getEdge(source, target, 1);  // directed
        if (e == null) e = g.getEdge(source, target, 0);  // undirected
        if (e == null) e = g.getEdge(source, target);  // default
        return e;
    }

    /**
     * Locate a layout builder by name (see bestLayoutMatch for the matching rules) and
     * return a ready-to-use instance.
     *
     * <p>A freshly built layout has its properties at Java zero-values, NOT at Gephi's
     * defaults — those live in {@code resetPropertiesValues()}, which the Gephi UI calls
     * when you select a layout and which nothing here used to call. Layouts whose builder
     * self-initializes (ForceAtlas 2) were fine; the rest silently ran on zeros. OpenOrd
     * with {@code Layout Size} 0 collapsed every node onto (0,0), and Yifan Hu with
     * {@code optimalDistance}/{@code stepRatio} 0 was a complete no-op that still reported
     * success. Reset here so every layout starts from Gephi's real defaults and callers
     * only need to pass the properties they actually want to change.
     *
     * <p>The graph model is attached first because size-dependent defaults read it
     * (ForceAtlas 2 picks scalingRatio 2.0 vs 10.0 off the node count).
     */
    private Layout findLayout(String algo) {
        java.util.List<LayoutBuilder> builders = new java.util.ArrayList<>();
        java.util.List<String> names = new java.util.ArrayList<>();
        for (LayoutBuilder b : Lookup.getDefault().lookupAll(LayoutBuilder.class)) {
            builders.add(b);
            names.add(b.getName());
        }
        int idx = bestLayoutMatch(names, algo);
        if (idx < 0) return null;
        Layout layout = builders.get(idx).buildLayout();
        if (layout == null) return null;
        // Separate failure paths: a missing graph model must not skip the reset, which is
        // the part that actually keeps OpenOrd and Yifan Hu from running on zeros.
        try {
            GraphModel gm = currentGraphModel();
            if (gm != null) layout.setGraphModel(gm);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "setGraphModel failed for layout: " + algo, e);
        }
        try {
            layout.resetPropertiesValues();
        } catch (Exception e) {
            // A layout that rejects the reset is still usable on its own defaults.
            LOGGER.log(Level.WARNING, "resetPropertiesValues failed for layout: " + algo, e);
        }
        return layout;
    }

    /**
     * Index of the best layout-name match for {@code query}, or -1. An exact match wins
     * (case- and space-insensitive, so the documented "forceatlas2" matches "ForceAtlas 2"
     * and "yifanhu" matches "Yifan Hu"); otherwise the first substring match. Space-folding
     * is what makes the short names in the docs/skill actually resolve. Package-private +
     * static for unit testing without the layout registry.
     */
    static int bestLayoutMatch(java.util.List<String> names, String query) {
        if (query == null) return -1;
        String q = query.toLowerCase().trim();
        String qns = q.replace(" ", "");
        if (qns.isEmpty()) return -1;
        int substr = -1;
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name == null) continue;
            String n = name.toLowerCase();
            String nns = n.replace(" ", "");
            if (n.equals(q) || nns.equals(qns)) return i;
            if (substr == -1 && (n.contains(q) || nns.contains(qns))) substr = i;
        }
        return substr;
    }

    // ─── Project Management ──────────────────────────────────────────

    public JsonObject createProject(String name) {
        return runOnEDT(() -> {
            ProjectController pc = getProjectController();
            pc.newProject();
            Workspace ws = pc.getCurrentWorkspace();
            JsonObject r = success("Project created");
            r.addProperty("workspace_id", ws != null ? ws.getId() : -1);
            return r;
        });
    }

    public JsonObject openProject(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return error("File not found: " + filePath);
        try {
            ProjectController pc = getProjectController();
            // Close any open project FIRST. Opening a .gephi on top of an existing
            // project lands in a broken half-state where the graphstore never
            // deserializes into a queryable model — the "open reports success but the
            // graph is blank" bug. Verified: open works as the first action on a fresh
            // instance and fails only when a project is already open; Gephi's own
            // File>Open closes first. closeCurrentProject touches UI, so run it on EDT.
            if (pc.hasCurrentProject()) {
                runOnEDT(() -> { pc.closeCurrentProject(); return null; });
            }
            // openProject(File) off the EDT: it blocks on a LongTaskExecutor Future
            // whose completion needs a free EDT.
            pc.openProject(file);
        } catch (Exception e) {
            return error("Failed to open project: " + e.getMessage());
        }
        // Report the actual loaded counts so an empty result is never a silent success.
        return runOnEDT(() -> {
            JsonObject r = success("Project opened");
            Workspace cur = getProjectController().getCurrentWorkspace();
            int nodes = 0, edges = 0;
            if (cur != null) {
                Graph g = getGraphController().getGraphModel(cur).getGraph();
                nodes = g.getNodeCount();
                edges = g.getEdgeCount();
            }
            r.addProperty("node_count", nodes);
            r.addProperty("edge_count", edges);
            if (nodes == 0) r.addProperty("warning", "opened but no nodes are in the current workspace");
            return r;
        });
    }

    public JsonObject saveProject(String filePath) {
        return runOnEDT(() -> {
            try {
                ProjectController pc = getProjectController();
                pc.saveProject(pc.getCurrentProject(), new File(filePath));
                return success("Project saved");
            } catch (Exception e) { return error("Failed: " + e.getMessage()); }
        });
    }

    public JsonObject getProjectInfo() {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            if (ws != null) {
                GraphModel gm = getGraphController().getGraphModel(ws);
                Graph g = gm.getGraph();
                r.addProperty("has_project", true);
                r.addProperty("workspace_id", ws.getId());
                r.addProperty("node_count", g.getNodeCount());
                r.addProperty("edge_count", g.getEdgeCount());
                r.addProperty("is_directed", gm.isDirected());
                r.addProperty("is_mixed", gm.isMixed());
            } else {
                r.addProperty("has_project", false);
            }
            return r;
        });
    }

    // ─── Workspace Management ────────────────────────────────────────

    public JsonObject newWorkspace() {
        return runOnEDT(() -> {
            try {
                ProjectController pc = getProjectController();
                if (pc.getCurrentProject() == null) return error("No project open");
                Workspace ws = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(ws);
                JsonObject r = success("Workspace created");
                r.addProperty("workspace_id", ws.getId());
                return r;
            } catch (Exception e) { return error("Failed: " + e.getMessage()); }
        });
    }

    public JsonObject listWorkspaces() {
        return runOnEDT(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) return error("No project open");
            JsonArray arr = new JsonArray();
            Workspace current = pc.getCurrentWorkspace();
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                JsonObject o = new JsonObject();
                o.addProperty("id", ws.getId());
                o.addProperty("name", ws.getName() != null ? ws.getName() : "Workspace " + ws.getId());
                o.addProperty("current", ws.equals(current));
                GraphModel gm = getGraphController().getGraphModel(ws);
                if (gm != null) {
                    Graph g = gm.getGraph();
                    o.addProperty("node_count", g.getNodeCount());
                    o.addProperty("edge_count", g.getEdgeCount());
                } else {
                    o.addProperty("node_count", 0);
                    o.addProperty("edge_count", 0);
                }
                arr.add(o);
            }
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.add("workspaces", arr);
            return r;
        });
    }

    public JsonObject switchWorkspace(int index) {
        return runOnEDT(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) return error("No project open");
            int i = 0;
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                if (i == index) {
                    pc.openWorkspace(ws);
                    return success("Switched to workspace " + ws.getId());
                }
                i++;
            }
            return error("Workspace index out of range: " + index);
        });
    }

    public JsonObject deleteWorkspace(int index) {
        return runOnEDT(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) return error("No project open");
            int i = 0;
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                if (i == index) {
                    pc.deleteWorkspace(ws);
                    return success("Workspace deleted");
                }
                i++;
            }
            return error("Workspace index out of range: " + index);
        });
    }

    public JsonObject duplicateWorkspace(int index) {
        return runOnEDT(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) return error("No project open");
            int i = 0;
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                if (i == index) {
                    try {
                        Workspace copy = pc.duplicateWorkspace(ws);
                        pc.openWorkspace(copy);
                        JsonObject r = success("Workspace duplicated");
                        r.addProperty("workspace_id", copy.getId());
                        return r;
                    } catch (Exception e) { return error("Failed: " + e.getMessage()); }
                }
                i++;
            }
            return error("Workspace index out of range: " + index);
        });
    }

    public JsonObject renameWorkspace(int index, String name) {
        return runOnEDT(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) return error("No project open");
            int i = 0;
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                if (i == index) {
                    try {
                        pc.renameWorkspace(ws, name);
                        return success("Workspace renamed to: " + name);
                    } catch (Exception e) { return error("Failed: " + e.getMessage()); }
                }
                i++;
            }
            return error("Workspace index out of range: " + index);
        });
    }

    // ─── Node Operations ─────────────────────────────────────────────

    public JsonObject addNode(String id, String label, Map<String, Object> attrs) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        return addNodeToModel(getGraphController().getGraphModel(ws), id, label, attrs);
    }

    /** Core node-add against an explicit model. Package-private + static so it is testable with a standalone GraphModel. */
    static JsonObject addNodeToModel(GraphModel gm, String id, String label, Map<String, Object> attrs) {
        try {
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                if (g.getNode(id) != null) return error("Node exists: " + id);
                Node n = gm.factory().newNode(id);
                n.setLabel(label != null ? label : id);
                n.setX((float)(Math.random() * 1000 - 500));
                n.setY((float)(Math.random() * 1000 - 500));
                n.setSize(10f);
                if (attrs != null) {
                    for (Map.Entry<String, Object> e : attrs.entrySet()) {
                        ensureColumnAndSet(gm.getNodeTable(), n, e.getKey(), e.getValue());
                    }
                }
                g.addNode(n);
                JsonObject r = success("Node added");
                r.addProperty("node_id", id);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject addNodes(List<Map<String, Object>> nodes) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        return addNodesToModel(getGraphController().getGraphModel(ws), nodes);
    }

    /** Core batch node-add against an explicit model (applies per-node attributes). */
    static JsonObject addNodesToModel(GraphModel gm, List<Map<String, Object>> nodes) {
        try {
            Graph g = gm.getGraph();
            int added = 0, skipped = 0;
            lockWrite(g);
            try {
                for (Map<String, Object> nd : nodes) {
                    String id = (String) nd.get("id");
                    if (id == null || g.getNode(id) != null) { skipped++; continue; }
                    String label = (String) nd.getOrDefault("label", id);
                    Node n = gm.factory().newNode(id);
                    n.setLabel(label);
                    n.setX((float)(Math.random() * 1000 - 500));
                    n.setY((float)(Math.random() * 1000 - 500));
                    n.setSize(10f);
                    g.addNode(n);
                    Object attrsObj = nd.get("attributes");
                    if (attrsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> attrs = (Map<String, Object>) attrsObj;
                        for (Map.Entry<String, Object> e : attrs.entrySet()) {
                            ensureColumnAndSet(gm.getNodeTable(), n, e.getKey(), e.getValue());
                        }
                    }
                    added++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("added", added);
                r.addProperty("skipped", skipped);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject removeNode(String id) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = getGraphController().getGraphModel(ws).getGraph();
            lockWrite(g);
            try {
                Node n = g.getNode(id);
                if (n == null) return error("Node not found: " + id);
                int edgesRemoved = g.getDegree(n);
                g.removeNode(n);
                JsonObject r = success("Node removed");
                r.addProperty("edges_removed", edgesRemoved);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject bulkRemoveNodes(List<String> ids) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = getGraphController().getGraphModel(ws).getGraph();
            lockWrite(g);
            try {
                int removed = 0, notFound = 0;
                for (String id : ids) {
                    Node n = g.getNode(id);
                    if (n == null) { notFound++; continue; }
                    g.removeNode(n);
                    removed++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("removed", removed);
                r.addProperty("not_found", notFound);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject queryNodes(String attr, String val, int limit, int offset) {
        return queryNodes(attr, val, limit, offset, false);
    }

    /** @param visible read the filtered visible graph instead of the full graph (see addViewInfo). */
    public JsonObject queryNodes(String attr, String val, int limit, int offset, boolean visible) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = visible ? gm.getGraphVisible() : gm.getGraph();
            lockRead(g);
            try {
                JsonArray arr = new JsonArray();
                int count = 0, skip = 0;
                // toArray, not the live iterable: breaking out of an auto-locked
                // iterator before exhaustion leaks its read hold permanently.
                for (Node n : g.getNodes().toArray()) {
                    if (skip++ < offset) continue;
                    if (count >= limit) break;
                    JsonObject o = new JsonObject();
                    o.addProperty("id", n.getId().toString());
                    o.addProperty("label", n.getLabel());
                    o.addProperty("x", n.x());
                    o.addProperty("y", n.y());
                    o.addProperty("size", n.size());
                    o.addProperty("degree", g.getDegree(n));
                    Color c = n.getColor();
                    if (c != null) {
                        o.addProperty("r", c.getRed());
                        o.addProperty("g", c.getGreen());
                        o.addProperty("b", c.getBlue());
                        o.addProperty("a", c.getAlpha());
                    }
                    // Include all custom attributes
                    JsonObject attrs = new JsonObject();
                    for (Column col : gm.getNodeTable()) {
                        if (col.isProperty()) continue; // skip built-in
                        Object v = n.getAttribute(col);
                        if (v != null) {
                            if (v instanceof Number) attrs.addProperty(col.getTitle(), (Number) v);
                            else if (v instanceof Boolean) attrs.addProperty(col.getTitle(), (Boolean) v);
                            else attrs.addProperty(col.getTitle(), v.toString());
                        }
                    }
                    if (attrs.size() > 0) o.add("attributes", attrs);
                    arr.add(o);
                    count++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("total", g.getNodeCount());
                r.addProperty("count", count);
                addViewInfo(r, gm, visible);
                r.add("nodes", arr);
                return r;
            } finally { g.readUnlock(); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject getNode(String id) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = gm.getGraph();
            Node n = g.getNode(id);
            if (n == null) return error("Node not found: " + id);
            JsonObject o = new JsonObject();
            o.addProperty("id", n.getId().toString());
            o.addProperty("label", n.getLabel());
            o.addProperty("x", n.x());
            o.addProperty("y", n.y());
            o.addProperty("size", n.size());
            o.addProperty("r", (int)(n.r() * 255));
            o.addProperty("g", (int)(n.g() * 255));
            o.addProperty("b", (int)(n.b() * 255));
            JsonObject attrs = new JsonObject();
            for (Column col : gm.getNodeTable()) {
                if (col.isProperty()) continue;
                Object v = n.getAttribute(col);
                if (v == null) continue;
                if (v instanceof Number) attrs.addProperty(col.getTitle(), (Number) v);
                else if (v instanceof Boolean) attrs.addProperty(col.getTitle(), (Boolean) v);
                else attrs.addProperty(col.getTitle(), v.toString());
            }
            o.add("attributes", attrs);
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.add("node", o);
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setNodeLabel(String id, String label) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node n = g.getNode(id);
                if (n == null) return error("Node not found: " + id);
                n.setLabel(label);
                return success("Label set");
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setNodePosition(String id, float x, float y) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node n = g.getNode(id);
                if (n == null) return error("Node not found: " + id);
                n.setX(x);
                n.setY(y);
                return success("Position set");
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject batchSetPositions(List<Map<String, Object>> positions) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                int set = 0, notFound = 0;
                for (Map<String, Object> pos : positions) {
                    String id = (String) pos.get("id");
                    Node n = g.getNode(id);
                    if (n == null) { notFound++; continue; }
                    n.setX(((Number) pos.get("x")).floatValue());
                    n.setY(((Number) pos.get("y")).floatValue());
                    set++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("set", set);
                r.addProperty("not_found", notFound);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Edge Operations ─────────────────────────────────────────────

    public JsonObject addEdge(String src, String tgt, Double weight, boolean directed) {
        return addEdge(src, tgt, weight, directed, null);
    }

    public JsonObject addEdge(String src, String tgt, Double weight, boolean directed, String edgeType) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        return addEdgeToModel(getGraphController().getGraphModel(ws), src, tgt, weight, directed, edgeType);
    }

    /** Core edge-add against an explicit model. Type and directedness are kept consistent. */
    static JsonObject addEdgeToModel(GraphModel gm, String src, String tgt, Double weight, boolean directed) {
        return addEdgeToModel(gm, src, tgt, weight, directed, null);
    }

    /**
     * Core edge-add, with an optional relationship type. When edgeType is null
     * or blank the behavior is exactly as before: one edge per (source, target),
     * type 0/1 by directedness. When edgeType is given, the edge is created under
     * that named type (GraphStore's native typed parallel edges) and the
     * duplicate check is scoped to that type — so A→B can carry a "cites" edge
     * AND a "coauthor" edge at once, while a second "cites" A→B is still blocked.
     */
    static JsonObject addEdgeToModel(GraphModel gm, String src, String tgt, Double weight,
                                     boolean directed, String edgeType) {
        try {
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                Node s = g.getNode(src), t = g.getNode(tgt);
                if (s == null) return error("Source not found: " + src);
                if (t == null) return error("Target not found: " + tgt);
                double w = weight != null ? weight : 1.0;
                if (edgeType != null && !edgeType.isEmpty()) {
                    int typeId = gm.addEdgeType(edgeType);
                    if (g.getEdge(s, t, typeId) != null) return error("Edge of type '" + edgeType + "' exists");
                    g.addEdge(gm.factory().newEdge(s, t, typeId, w, directed));
                } else {
                    if (findEdge(g, s, t) != null) return error("Edge exists");
                    g.addEdge(gm.factory().newEdge(s, t, directed ? 1 : 0, w, directed));
                }
                return success("Edge added");
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject addEdges(List<Map<String, Object>> edges) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        return addEdgesToModel(getGraphController().getGraphModel(ws), edges);
    }

    /** Core batch edge-add against an explicit model (honors per-edge directed/label/attributes). */
    static JsonObject addEdgesToModel(GraphModel gm, List<Map<String, Object>> edges) {
        try {
            Graph g = gm.getGraph();
            int added = 0, skipped = 0;
            lockWrite(g);
            try {
                for (Map<String, Object> ed : edges) {
                    String src = (String) ed.get("source");
                    String tgt = (String) ed.get("target");
                    if (src == null || tgt == null) { skipped++; continue; }
                    Node s = g.getNode(src), t = g.getNode(tgt);
                    if (s == null || t == null) { skipped++; continue; }
                    Double w = ed.containsKey("weight") ? ((Number) ed.get("weight")).doubleValue() : 1.0;
                    boolean directed = !ed.containsKey("directed") || Boolean.TRUE.equals(ed.get("directed"));
                    Object edgeTypeObj = ed.get("edge_type");
                    String edgeType = edgeTypeObj != null ? edgeTypeObj.toString() : null;
                    int type;
                    if (edgeType != null && !edgeType.isEmpty()) {
                        type = gm.addEdgeType(edgeType);
                        if (g.getEdge(s, t, type) != null) { skipped++; continue; }
                    } else {
                        if (findEdge(g, s, t) != null) { skipped++; continue; }
                        type = directed ? 1 : 0;
                    }
                    Edge e = gm.factory().newEdge(s, t, type, w, directed);
                    Object label = ed.get("label");
                    if (label != null) e.setLabel(label.toString());
                    g.addEdge(e);
                    Object attrsObj = ed.get("attributes");
                    if (attrsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> attrs = (Map<String, Object>) attrsObj;
                        for (Map.Entry<String, Object> en : attrs.entrySet()) {
                            ensureColumnAndSet(gm.getEdgeTable(), e, en.getKey(), en.getValue());
                        }
                    }
                    added++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("added", added);
                r.addProperty("skipped", skipped);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject removeEdge(String source, String target) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node s = g.getNode(source), t = g.getNode(target);
                if (s == null || t == null) return error("Node not found");
                Edge e = findEdge(g, s, t);
                if (e == null) return error("Edge not found");
                g.removeEdge(e);
                return success("Edge removed");
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setEdgeWeight(String source, String target, double weight) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node s = g.getNode(source), t = g.getNode(target);
                if (s == null || t == null) return error("Node not found");
                Edge e = findEdge(g, s, t);
                if (e == null) return error("Edge not found");
                e.setWeight(weight);
                return success("Weight set to " + weight);
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setEdgeLabel(String source, String target, String label) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node s = g.getNode(source), t = g.getNode(target);
                if (s == null || t == null) return error("Node not found");
                Edge e = findEdge(g, s, t);
                if (e == null) return error("Edge not found");
                e.setLabel(label);
                return success("Edge label set");
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject queryEdges(int limit, int offset) {
        return queryEdges(limit, offset, false);
    }

    /** @param visible read the filtered visible graph instead of the full graph (see addViewInfo). */
    public JsonObject queryEdges(int limit, int offset, boolean visible) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = visible ? gm.getGraphVisible() : gm.getGraph();
            lockRead(g);
            try {
                JsonArray arr = new JsonArray();
                int count = 0, skip = 0;
                // toArray, not the live iterable: breaking out of an auto-locked
                // iterator before exhaustion leaks its read hold permanently.
                for (Edge e : g.getEdges().toArray()) {
                    if (skip++ < offset) continue;
                    if (count >= limit) break;
                    JsonObject o = new JsonObject();
                    o.addProperty("source", e.getSource().getId().toString());
                    o.addProperty("target", e.getTarget().getId().toString());
                    o.addProperty("weight", e.getWeight());
                    o.addProperty("directed", e.isDirected());
                    if (e.getLabel() != null) o.addProperty("label", e.getLabel());
                    Color c = e.getColor();
                    if (c != null) {
                        o.addProperty("r", c.getRed());
                        o.addProperty("g", c.getGreen());
                        o.addProperty("b", c.getBlue());
                    }
                    // Include custom attributes
                    JsonObject attrs = new JsonObject();
                    for (Column col : gm.getEdgeTable()) {
                        if (col.isProperty()) continue;
                        Object v = e.getAttribute(col);
                        if (v != null) {
                            if (v instanceof Number) attrs.addProperty(col.getTitle(), (Number) v);
                            else if (v instanceof Boolean) attrs.addProperty(col.getTitle(), (Boolean) v);
                            else attrs.addProperty(col.getTitle(), v.toString());
                        }
                    }
                    if (attrs.size() > 0) o.add("attributes", attrs);
                    arr.add(o);
                    count++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("total", g.getEdgeCount());
                r.addProperty("count", count);
                addViewInfo(r, gm, visible);
                r.add("edges", arr);
                return r;
            } finally { g.readUnlock(); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Graph Stats ─────────────────────────────────────────────────

    /**
     * Read/export view consistency. File and inline exports historically write the
     * VISIBLE (filtered) graph while every read endpoint reads the FULL graph — so with
     * a filter active, /graph/stats could report 5000 nodes while /export/gexf silently
     * wrote 200, and every consumer of the inline GEXF computed over a graph the stats
     * never described. The defaults are kept (changing them would silently change every
     * existing client), but no response is silent about it any more: each one carries
     * {@code view} ("full" | "visible") naming the view it was computed from and
     * {@code filter_active}; whenever a filter IS active it also carries
     * {@code full_node_count}/{@code full_edge_count} and
     * {@code visible_node_count}/{@code visible_edge_count} so the discrepancy is
     * visible to the caller. The {@code visible} overloads let the HTTP layer expose an
     * explicit choice of view per request.
     */
    private static void addViewInfo(JsonObject r, GraphModel gm, boolean visibleView) {
        boolean filterActive = !gm.getVisibleView().isMainView();
        r.addProperty("view", visibleView ? "visible" : "full");
        r.addProperty("filter_active", filterActive);
        if (filterActive) {
            Graph full = gm.getGraph();
            Graph vis = gm.getGraphVisible();
            r.addProperty("full_node_count", full.getNodeCount());
            r.addProperty("full_edge_count", full.getEdgeCount());
            r.addProperty("visible_node_count", vis.getNodeCount());
            r.addProperty("visible_edge_count", vis.getEdgeCount());
        }
    }

    public JsonObject getGraphStats() {
        return getGraphStats(false);
    }

    /** @param visible read the filtered visible graph instead of the full graph (see addViewInfo). */
    public JsonObject getGraphStats(boolean visible) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = visible ? gm.getGraphVisible() : gm.getGraph();
            lockRead(g);
            try {
                int nc = g.getNodeCount(), ec = g.getEdgeCount();
                double density = nc > 1 ? (2.0 * ec) / (nc * (nc - 1)) : 0;
                double avgDeg = nc > 0 ? (2.0 * ec) / nc : 0;
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("node_count", nc);
                r.addProperty("edge_count", ec);
                r.addProperty("density", density);
                r.addProperty("average_degree", avgDeg);
                r.addProperty("is_directed", gm.isDirected());
                addViewInfo(r, gm, visible);
                return r;
            } finally { g.readUnlock(); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Graph Type ──────────────────────────────────────────────────

    public JsonObject getGraphType() {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("directed", gm.isDirected());
            r.addProperty("undirected", gm.isUndirected());
            r.addProperty("mixed", gm.isMixed());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Attribute / Column Management ───────────────────────────────

    public JsonObject getColumns(String target) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            Table table = "edge".equalsIgnoreCase(target) ? gm.getEdgeTable() : gm.getNodeTable();
            JsonArray arr = new JsonArray();
            for (Column col : table) {
                JsonObject o = new JsonObject();
                o.addProperty("id", col.getId());
                o.addProperty("title", col.getTitle());
                o.addProperty("type", col.getTypeClass().getSimpleName());
                o.addProperty("property", col.isProperty());
                arr.add(o);
            }
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("target", target);
            r.add("columns", arr);
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject addColumn(String name, String type, String target) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        return addColumnToModel(currentGraphModel(), name, type, target);
    }

    /**
     * Add a column under the graph write lock. Taking the lock matters for ordering:
     * ensureColumnAndSet() also adds columns while holding the write lock, so doing it
     * lock-free here created an A-holds-graph/wants-column vs B-holds-column/wants-graph
     * deadlock under concurrent requests. Package-private + static for unit testing.
     */
    static JsonObject addColumnToModel(GraphModel gm, String name, String type, String target) {
        try {
            Table table = "edge".equalsIgnoreCase(target) ? gm.getEdgeTable() : gm.getNodeTable();
            Class<?> cls = typeStringToClass(type);
            if (cls == null) return error("Unknown type: " + type + ". Use: string, integer, double, float, boolean, long");
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                if (table.getColumn(name) != null) return error("Column already exists: " + name);
                table.addColumn(name, cls);
            } finally { unlockWrite(g); }
            return success("Column '" + name + "' added");
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setNodeAttributes(String id, Map<String, Object> attrs) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                Node n = g.getNode(id);
                if (n == null) return error("Node not found: " + id);
                for (Map.Entry<String, Object> e : attrs.entrySet()) {
                    ensureColumnAndSet(gm.getNodeTable(), n, e.getKey(), e.getValue());
                }
                return success("Attributes set on node " + id);
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject batchSetNodeAttributes(List<Map<String, Object>> updates) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                int set = 0, notFound = 0;
                for (Map<String, Object> update : updates) {
                    String id = (String) update.get("id");
                    Node n = g.getNode(id);
                    if (n == null) { notFound++; continue; }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> attrs = (Map<String, Object>) update.get("attributes");
                    if (attrs != null) {
                        for (Map.Entry<String, Object> e : attrs.entrySet()) {
                            ensureColumnAndSet(gm.getNodeTable(), n, e.getKey(), e.getValue());
                        }
                    }
                    set++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("set", set);
                r.addProperty("not_found", notFound);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setEdgeAttributes(String source, String target, Map<String, Object> attrs) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                Node s = g.getNode(source), t = g.getNode(target);
                if (s == null || t == null) return error("Node not found");
                Edge e = findEdge(g, s, t);
                if (e == null) return error("Edge not found");
                for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                    ensureColumnAndSet(gm.getEdgeTable(), e, entry.getKey(), entry.getValue());
                }
                return success("Attributes set on edge");
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    static void ensureColumnAndSet(Table table, Object element, String key, Object value) {
        Column col = table.getColumn(key);
        if (col == null) {
            Class<?> cls = String.class;
            if (value instanceof Number) {
                if (value instanceof Integer) cls = Integer.class;
                else if (value instanceof Long) cls = Long.class;
                else if (value instanceof Float) cls = Float.class;
                else cls = Double.class;
            } else if (value instanceof Boolean) {
                cls = Boolean.class;
            }
            col = table.addColumn(key, cls);
        }
        // Convert value to column type
        Object converted = convertToColumnType(value, col.getTypeClass());
        if (element instanceof Node) ((Node) element).setAttribute(col, converted);
        else if (element instanceof Edge) ((Edge) element).setAttribute(col, converted);
    }

    static Object convertToColumnType(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;
        String s = value.toString();
        try {
            if (targetType == Integer.class) return (int) Double.parseDouble(s);
            if (targetType == Long.class) return (long) Double.parseDouble(s);
            if (targetType == Float.class) return (float) Double.parseDouble(s);
            if (targetType == Double.class) return Double.parseDouble(s);
            if (targetType == Boolean.class) return Boolean.parseBoolean(s);
        } catch (Exception e) { /* fall through */ }
        return s;
    }

    static Class<?> typeStringToClass(String type) {
        if (type == null) return null;
        switch (type.toLowerCase()) {
            case "string": return String.class;
            case "integer": case "int": return Integer.class;
            case "double": return Double.class;
            case "float": return Float.class;
            case "boolean": case "bool": return Boolean.class;
            case "long": return Long.class;
            default: return null;
        }
    }

    // ─── Appearance: Individual Node/Edge Styling ────────────────────

    public JsonObject setNodeColor(String id, int r, int g, int b, int a) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph graph = currentGraphModel().getGraph();
            lockWrite(graph);
            try {
                Node n = graph.getNode(id);
                if (n == null) return error("Node not found: " + id);
                n.setColor(new Color(r, g, b, a));
                return success("Node color set");
            } finally { unlockWrite(graph); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setNodeSize(String id, float size) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph graph = currentGraphModel().getGraph();
            lockWrite(graph);
            try {
                Node n = graph.getNode(id);
                if (n == null) return error("Node not found: " + id);
                n.setSize(size);
                return success("Node size set to " + size);
            } finally { unlockWrite(graph); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    /*
     * THREADING NOTE (applies to every styling/filter method below that once wrapped its
     * body in runOnEDT): these operations mutate the graph model, which is thread-safe
     * under its own lock and does not need the EDT. Polling lockWrite's 15-second tryLock
     * loop ON the EDT froze the UI under contention, tripped runOnEDT's own 15-second
     * timeout (misreporting "Gephi's UI thread is unresponsive"), and — worse — the
     * abandoned EDT task still ran later, applying a destructive mutation after the HTTP
     * call had already reported failure, so a client retry applied it twice. They now run
     * on the calling thread, like clearGraph and addNodeToModel always have. Only the
     * preview refresh still hops to the EDT (refreshPreviewOnEDT).
     */

    public JsonObject setEdgeColor(String source, String target, int r, int g, int b, int a) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            Graph graph = currentGraphModel().getGraph();
            lockWrite(graph);
            try {
                Node s = graph.getNode(source), t = graph.getNode(target);
                if (s == null || t == null) return error("Node not found");
                Edge e = findEdge(graph, s, t);
                if (e == null) return error("Edge not found");
                e.setColor(new Color(r, g, b, a));
                return success("Edge color set");
            } finally { unlockWrite(graph); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject batchSetNodeColors(List<Map<String, Object>> nodeColors) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Graph graph = currentGraphModel().getGraph();
            lockWrite(graph);
            try {
                int set = 0, notFound = 0;
                for (Map<String, Object> nc : nodeColors) {
                    String id = (String) nc.get("id");
                    Node n = graph.getNode(id);
                    if (n == null) { notFound++; continue; }
                    int r = ((Number) nc.get("r")).intValue();
                    int g = ((Number) nc.get("g")).intValue();
                    int b = ((Number) nc.get("b")).intValue();
                    int a = nc.containsKey("a") ? ((Number) nc.get("a")).intValue() : 255;
                    n.setColor(new Color(r, g, b, a));
                    set++;
                }
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("set", set);
                res.addProperty("not_found", notFound);
                return res;
            } finally { unlockWrite(graph); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject resetAppearance(int r, int g, int b, float size) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            Graph graph = currentGraphModel().getGraph();
            Color defaultColor = new Color(r, g, b);
            lockWrite(graph);
            try {
                for (Node n : graph.getNodes().toArray()) {
                    n.setColor(defaultColor);
                    n.setSize(size);
                }
            } finally { unlockWrite(graph); }
            return success("Appearance reset for all nodes");
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Appearance: Color/Size by Attribute ─────────────────────────

    public JsonObject colorByPartition(String columnName, Map<String, int[]> colorMap) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = gm.getNodeTable().getColumn(columnName);
            if (col == null) return error("Column not found: " + columnName);

            // Collect distinct values
            java.util.Map<String, Color> palette = new java.util.LinkedHashMap<>();
            if (colorMap != null && !colorMap.isEmpty()) {
                for (Map.Entry<String, int[]> e : colorMap.entrySet()) {
                    int[] c = e.getValue();
                    palette.put(e.getKey(), new Color(c[0], c[1], c[2]));
                }
            } else {
                // Auto-generate palette
                java.util.Set<String> values = new java.util.LinkedHashSet<>();
                Node[] allNodes = graph.getNodes().toArray();
                for (Node n : allNodes) {
                    Object v = n.getAttribute(col);
                    if (v != null) values.add(v.toString());
                }

                Color[] defaultPalette = {
                    new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
                    new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75),
                    new Color(227, 119, 194), new Color(127, 127, 127), new Color(188, 189, 34),
                    new Color(23, 190, 207), new Color(174, 199, 232), new Color(255, 187, 120)
                };
                int idx = 0;
                for (String v : values) {
                    palette.put(v, defaultPalette[idx % defaultPalette.length]);
                    idx++;
                }
            }

            int colored = 0;
            lockWrite(graph);
            try {
                for (Node n : graph.getNodes().toArray()) {
                    Object v = n.getAttribute(col);
                    if (v != null) {
                        Color c = palette.get(v.toString());
                        if (c != null) {
                            n.setColor(c);
                            colored++;
                        }
                    }
                }
            } finally { unlockWrite(graph); }
            JsonObject r = success("Colored " + colored + " nodes by " + columnName);
            r.addProperty("partitions", palette.size());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    /**
     * Min and max over the numeric values of {@code col}, as {@code [min, max]}, or null when
     * the column holds no numeric values. Seeded with infinities so a column whose values are
     * entirely negative ranks correctly — the old {@code Double.MIN_VALUE} seed (smallest
     * positive double) silently broke that case. Package-private + static for unit testing.
     */
    static double[] numericRange(Graph g, Column col) {
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        lockRead(g);
        try {
            for (Node n : g.getNodes().toArray()) {
                Object v = n.getAttribute(col);
                if (v instanceof Number) {
                    double d = ((Number) v).doubleValue();
                    if (d < min) min = d;
                    if (d > max) max = d;
                }
            }
        } finally { g.readUnlock(); }
        return min == Double.POSITIVE_INFINITY ? null : new double[]{min, max};
    }

    /**
     * Column lookup for ranking operations. When a degree column is requested
     * before the degree statistic has run (the #1 cold-start stumble), computes
     * it on the spot instead of failing.
     *
     * <p>Must be called OFF the EDT: runStatistic executes the statistic (statistics
     * dispatch UI work to the EDT internally — see extractGiantComponent) and renders
     * its report, which for Degree is a JFreeChart image. colorByRanking and
     * sizeByRanking call this from the HTTP thread, never inside a runOnEDT hop.
     */
    private Column resolveRankingColumn(GraphModel gm, String columnName) {
        Column col = gm.getNodeTable().getColumn(columnName);
        if (col == null && columnName != null) {
            String lc = columnName.toLowerCase();
            if (lc.equals("degree") || lc.equals("indegree") || lc.equals("outdegree")) {
                runStatistic("Degree", null);
                col = gm.getNodeTable().getColumn(columnName);
            }
        }
        return col;
    }

    private static JsonObject columnNotFound(String columnName) {
        return error("Column not found: " + columnName
            + " — compute the metric first (degree, pagerank, betweenness, modularity"
            + " via the statistics tools) or check the columns list");
    }

    public JsonObject colorByRanking(String columnName, int rMin, int gMin, int bMin, int rMax, int gMax, int bMax) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = resolveRankingColumn(gm, columnName);
            if (col == null) return columnNotFound(columnName);

            double[] mm = numericRange(graph, col);
            if (mm == null) return error("No numeric values in column " + columnName);
            double min = mm[0], max = mm[1];
            double range = max - min;
            if (range == 0) range = 1;

            int colored = 0;
            lockWrite(graph);
            try {
                for (Node n : graph.getNodes().toArray()) {
                    Object v = n.getAttribute(col);
                    if (v instanceof Number) {
                        double t = (((Number) v).doubleValue() - min) / range;
                        int r = (int)(rMin + t * (rMax - rMin));
                        int g = (int)(gMin + t * (gMax - gMin));
                        int b = (int)(bMin + t * (bMax - bMin));
                        n.setColor(new Color(
                            Math.max(0, Math.min(255, r)),
                            Math.max(0, Math.min(255, g)),
                            Math.max(0, Math.min(255, b))
                        ));
                        colored++;
                    }
                }
            } finally { unlockWrite(graph); }
            JsonObject res = success("Colored " + colored + " nodes by ranking on " + columnName);
            res.addProperty("min_value", min);
            res.addProperty("max_value", max);
            return res;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject sizeByRanking(String columnName, float minSize, float maxSize) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = resolveRankingColumn(gm, columnName);
            if (col == null) return columnNotFound(columnName);

            double[] mm = numericRange(graph, col);
            if (mm == null) return error("No numeric values in column " + columnName);
            double min = mm[0], max = mm[1];
            double range = max - min;
            if (range == 0) range = 1;

            int sized = 0;
            lockWrite(graph);
            try {
                for (Node n : graph.getNodes().toArray()) {
                    Object v = n.getAttribute(col);
                    if (v instanceof Number) {
                        double t = (((Number) v).doubleValue() - min) / range;
                        n.setSize((float)(minSize + t * (maxSize - minSize)));
                        sized++;
                    }
                }
            } finally { unlockWrite(graph); }
            JsonObject res = success("Sized " + sized + " nodes by " + columnName);
            res.addProperty("min_value", min);
            res.addProperty("max_value", max);
            return res;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Layout ──────────────────────────────────────────────────────

    public JsonObject runLayout(String algo, int iterations) {
        return runLayout(algo, iterations, null);
    }

    public JsonObject runLayout(String algo, int iterations, Map<String, Object> properties) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = getGraphController().getGraphModel(ws);
            Layout layout = findLayout(algo);
            if (layout == null) return error("Layout not found: " + algo);
            layout.setGraphModel(gm);
            // Apply inline properties, or config staged earlier by setLayoutProperties.
            if (properties == null && pendingLayoutProps != null && algo.equals(pendingLayoutAlgo)) {
                properties = pendingLayoutProps;
            }
            pendingLayoutProps = null;
            pendingLayoutAlgo = null;
            if (properties != null) applyLayoutProperties(layout, properties);
            final Layout fl = layout;
            final int iters = iterations > 0 ? iterations : 1000;
            if (!layoutRunning.compareAndSet(false, true)) return error("Layout already running");
            currentLayoutName = algo;
            try {
                layoutFuture = layoutExecutor().submit(() -> {
                    try {
                        fl.initAlgo();
                        for (int i = 0; i < iters && layoutRunning.get() && fl.canAlgo(); i++) fl.goAlgo();
                    } catch (Exception e) { LOGGER.log(Level.WARNING, "Layout error", e); }
                    finally {
                        // endAlgo() is where Gephi layouts release the graph model and their
                        // column observers — it must run even when goAlgo() throws, or those
                        // leak for the life of the workspace.
                        try { fl.endAlgo(); }
                        catch (Exception e) { LOGGER.log(Level.WARNING, "Layout endAlgo error", e); }
                        layoutRunning.set(false);
                        currentLayoutName = null;
                    }
                });
            } catch (RuntimeException submitFailure) {
                // RejectedExecutionException (executor already shut down): without this reset
                // the flag stays true and every later run reports "Layout already running"
                // for the rest of the session.
                layoutRunning.set(false);
                currentLayoutName = null;
                throw submitFailure;
            }
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("layout", algo);
            r.addProperty("status", "running");
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject stopLayout() {
        if (!layoutRunning.get()) return success("No layout running");
        layoutRunning.set(false);
        // cancel(false): the cooperative layoutRunning flag already stops the loop at the
        // next iteration. Interrupting instead (cancel(true)) can throw InterruptedException
        // out of goAlgo() mid-iteration (OpenOrd synchronizes worker threads on a barrier),
        // and a layout that took a read lock without a finally then leaks it permanently.
        if (layoutFuture != null) layoutFuture.cancel(false);
        return success("Layout stopped");
    }

    public JsonObject getLayoutStatus() {
        JsonObject r = new JsonObject();
        r.addProperty("success", true);
        r.addProperty("running", layoutRunning.get());
        if (currentLayoutName != null) r.addProperty("layout", currentLayoutName);
        return r;
    }

    public JsonObject getAvailableLayouts() {
        JsonArray arr = new JsonArray();
        for (LayoutBuilder b : Lookup.getDefault().lookupAll(LayoutBuilder.class)) {
            JsonObject o = new JsonObject();
            o.addProperty("name", b.getName());
            arr.add(o);
        }
        JsonObject r = new JsonObject();
        r.addProperty("success", true);
        r.add("layouts", arr);
        return r;
    }

    public JsonObject getLayoutProperties(String algo) {
        try {
            Layout layout = findLayout(algo);
            if (layout == null) return error("Layout not found: " + algo);
            // Need a graph model for the layout to report properties
            Workspace ws = currentWorkspace();
            if (ws != null) layout.setGraphModel(currentGraphModel());

            JsonArray arr = new JsonArray();
            LayoutProperty[] props = layout.getProperties();
            if (props != null) {
                for (LayoutProperty prop : props) {
                    JsonObject o = new JsonObject();
                    o.addProperty("name", prop.getCanonicalName() != null ? prop.getCanonicalName() : prop.getProperty().getDisplayName());
                    o.addProperty("display_name", prop.getProperty().getDisplayName());
                    o.addProperty("type", prop.getProperty().getValueType().getSimpleName());
                    Object val = prop.getProperty().getValue();
                    if (val != null) o.addProperty("value", val.toString());
                    String desc = prop.getProperty().getShortDescription();
                    if (desc != null) o.addProperty("description", desc);
                    arr.add(o);
                }
            }
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("algorithm", algo);
            r.add("properties", arr);
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    /** Match each Layout property against a caller-supplied key map and set it. */
    private void applyLayoutProperties(Layout layout, Map<String, Object> properties) {
        if (properties == null) return;
        LayoutProperty[] props = layout.getProperties();
        if (props == null) return;
        for (LayoutProperty prop : props) {
            String canonicalName = prop.getCanonicalName() != null ? prop.getCanonicalName() : "";
            String displayName = prop.getProperty().getDisplayName();
            // Extract middle key from "AlgoName.propertyKey.name" pattern
            String canonicalKey = "";
            if (!canonicalName.isEmpty()) {
                String[] parts = canonicalName.split("\\.");
                if (parts.length >= 3) canonicalKey = parts[parts.length - 2];
            }
            Object val = properties.get(canonicalKey);
            if (val == null && !canonicalName.isEmpty()) val = properties.get(canonicalName);
            if (val == null) val = properties.get(displayName);
            if (val == null) {
                for (Map.Entry<String, Object> e : properties.entrySet()) {
                    String k = e.getKey();
                    if ((!canonicalKey.isEmpty() && k.equalsIgnoreCase(canonicalKey))
                            || k.equalsIgnoreCase(displayName)
                            || (!canonicalName.isEmpty() && k.equalsIgnoreCase(canonicalName))) {
                        val = e.getValue();
                        break;
                    }
                }
            }
            if (val != null) {
                Class<?> type = prop.getProperty().getValueType();
                Object converted = convertLayoutProperty(val, type);
                if (converted != null) {
                    try { prop.getProperty().setValue(converted); }
                    catch (Exception e) { LOGGER.log(Level.WARNING, "Set layout property failed", e); }
                }
            }
        }
    }

    /**
     * Configure a layout's properties WITHOUT running it. The config is staged so
     * the next runLayout of the same algorithm applies it — set-then-run works,
     * and this call no longer hijacks the layout executor (which broke a following
     * run_layout with "Layout already running"). Prefer run_layout(properties=...)
     * to configure and run in one step.
     */
    public JsonObject setLayoutProperties(String algo, Map<String, Object> properties, int iterations) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            Layout layout = findLayout(algo);
            if (layout == null) return error("Layout not found: " + algo);
            layout.setGraphModel(currentGraphModel());
            applyLayoutProperties(layout, properties);
            pendingLayoutProps = properties;
            pendingLayoutAlgo = algo;
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("layout", algo);
            r.addProperty("configured", true);
            r.addProperty("running", false);
            r.addProperty("note", "properties staged; the next run_layout of this algorithm applies them");
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    static Object convertLayoutProperty(Object val, Class<?> type) {
        if (val == null) return null;
        String s = val.toString();
        try {
            if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(s);
            if (type == Integer.class || type == int.class) return (int) Double.parseDouble(s);
            if (type == Double.class || type == double.class) return Double.parseDouble(s);
            if (type == Float.class || type == float.class) return (float) Double.parseDouble(s);
            if (type == Long.class || type == long.class) return (long) Double.parseDouble(s);
            if (type == String.class) return s;
        } catch (Exception e) { /* fall through */ }
        return null;
    }

    // ─── Statistics ──────────────────────────────────────────────────

    /**
     * Every statistic available in this Gephi instance — built-ins plus any
     * installed plugin that registers a StatisticsBuilder (verified with the
     * CWTS Leiden plugin). Names here are what /statistics/run accepts.
     */
    public JsonObject listStatistics() {
        JsonArray arr = new JsonArray();
        for (StatisticsBuilder sb : Lookup.getDefault().lookupAll(StatisticsBuilder.class)) {
            JsonObject o = new JsonObject();
            o.addProperty("name", sb.getName());
            try {
                o.addProperty("id", sb.getStatistics().getClass().getSimpleName());
            } catch (Throwable t) { /* name alone is enough */ }
            arr.add(o);
        }
        JsonObject r = new JsonObject();
        r.addProperty("success", true);
        r.add("statistics", arr);
        return r;
    }

    /** Run any available statistic by name — the plugin-ecosystem passthrough. */
    public JsonObject runStatisticByName(String name, Map<String, Object> params) {
        return runStatistic(name, params);
    }

    private static final org.gephi.utils.progress.ProgressTicket NOOP_TICKET =
        new org.gephi.utils.progress.ProgressTicket() {
            public void finish() {}
            public void finish(String s) {}
            public void progress() {}
            public void progress(int i) {}
            public void progress(String s) {}
            public void progress(String s, int i) {}
            public String getDisplayName() { return "MCP statistic"; }
            public void setDisplayName(String s) {}
            public void start() {}
            public void start(int i) {}
            public void switchToDeterminate(int i) {}
            public void switchToIndeterminate() {}
        };

    private JsonObject runStatistic(String builderName, Map<String, Object> params) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();

            // Find statistics builder by name
            StatisticsBuilder matchedBuilder = null;
            for (StatisticsBuilder sb : Lookup.getDefault().lookupAll(StatisticsBuilder.class)) {
                String name = sb.getName();
                LOGGER.fine("MCP: Found StatisticsBuilder: " + name + " (" + sb.getClass().getName() + ")");
                if (name.equalsIgnoreCase(builderName) || sb.getClass().getSimpleName().toLowerCase().contains(builderName.toLowerCase())) {
                    matchedBuilder = sb;
                    break;
                }
            }
            if (matchedBuilder == null) {
                // Also try matching by statistics class name
                for (StatisticsBuilder sb : Lookup.getDefault().lookupAll(StatisticsBuilder.class)) {
                    try {
                        Statistics stat = sb.getStatistics();
                        if (stat.getClass().getSimpleName().equalsIgnoreCase(builderName)) {
                            matchedBuilder = sb;
                            break;
                        }
                    } catch (Exception e) { /* skip */ }
                }
            }
            if (matchedBuilder == null) return error("Statistics not found: " + builderName);

            Statistics stat = matchedBuilder.getStatistics();

            // Set parameters via reflection; collect the ones that did not land so a
            // mistyped name is reported instead of silently ignored (a typo used to be
            // indistinguishable from a correctly-parameterised run).
            java.util.List<String> unappliedParams = new java.util.ArrayList<>();
            if (params != null) {
                for (Map.Entry<String, Object> e : params.entrySet()) {
                    if (!setViaReflection(stat, e.getKey(), e.getValue())) unappliedParams.add(e.getKey());
                }
            }

            // Plugin statistics are often LongTasks that assume the UI gave them a
            // progress ticket and call it without null checks (e.g. CWTS Leiden).
            // Provide a no-op ticket so they run outside the statistics dialog.
            if (stat instanceof org.gephi.utils.longtask.spi.LongTask) {
                ((org.gephi.utils.longtask.spi.LongTask) stat).setProgressTicket(NOOP_TICKET);
            }

            // Execute
            stat.execute(gm);

            // Build result
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("statistic", matchedBuilder.getName());
            if (!unappliedParams.isEmpty()) {
                JsonArray ua = new JsonArray();
                for (String k : unappliedParams) ua.add(k);
                r.add("unapplied_params", ua);
                r.addProperty("warning", "Parameters matched no setter or field on "
                    + stat.getClass().getSimpleName() + " and were NOT applied: " + unappliedParams);
            }

            // Try to get common result values via reflection
            tryAddResult(r, stat, "getModularity", "modularity");
            tryAddResult(r, stat, "getAverageDegree", "average_degree");
            tryAddResult(r, stat, "getPathLength", "average_path_length");
            tryAddResult(r, stat, "getDiameter", "diameter");
            tryAddResult(r, stat, "getRadius", "radius");
            tryAddResult(r, stat, "getAverageClusteringCoefficient", "average_clustering_coefficient");
            tryAddResult(r, stat, "getConnectedComponentsCount", "connected_components");

            // Get the report
            try {
                String report = stat.getReport();
                if (report != null) {
                    r.addProperty("report_available", true);
                    r.addProperty("report_html", report);
                }
            } catch (Exception e) { /* no report */ }

            return r;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Statistic execution failed", e);
            return error("Failed: " + e.getMessage());
        }
    }

    /**
     * Set {@code setter} on {@code obj} via a JavaBeans setter or, failing that, a bare
     * field of the same (case-insensitive) name. Returns true only when a value was
     * actually applied; callers surface the false case so a mistyped parameter name is
     * distinguishable from a correctly-configured run.
     */
    private boolean setViaReflection(Object obj, String setter, Object value) {
        String methodName = "set" + setter.substring(0, 1).toUpperCase() + setter.substring(1);
        try {
            for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == 1) {
                    Class<?> paramType = m.getParameterTypes()[0];
                    Object converted = convertStatValue(value, paramType);
                    if (converted == null) return false;  // name matched, value did not convert
                    m.invoke(obj, converted);
                    return true;
                }
            }
            // No setter: plugin statistics (e.g. the CWTS Leiden plugin) often use
            // bare fields configured by their UI panel — set the field directly.
            for (Class<?> c = obj.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    if (f.getName().equalsIgnoreCase(setter)) {
                        Object converted = convertStatValue(value, f.getType());
                        if (converted == null) return false;
                        f.setAccessible(true);
                        f.set(obj, converted);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.fine("Could not set " + methodName + ": " + e.getMessage());
        }
        return false;
    }

    /** Value conversion for statistic parameters: layout-style primitives plus enums by name. */
    static Object convertStatValue(Object val, Class<?> type) {
        if (val != null && type.isEnum()) {
            String want = val.toString();
            for (Object ec : type.getEnumConstants()) {
                if (ec.toString().equalsIgnoreCase(want)) return ec;
            }
            return null;
        }
        return convertLayoutProperty(val, type);
    }

    private void tryAddResult(JsonObject r, Object obj, String getter, String jsonKey) {
        try {
            java.lang.reflect.Method m = obj.getClass().getMethod(getter);
            Object val = m.invoke(obj);
            if (val instanceof Number) r.addProperty(jsonKey, (Number) val);
            else if (val instanceof Boolean) r.addProperty(jsonKey, (Boolean) val);
            else if (val != null) r.addProperty(jsonKey, val.toString());
        } catch (NoSuchMethodException e) { /* method not available for this statistic */ }
        catch (Exception e) { LOGGER.fine("Could not get " + getter + ": " + e.getMessage()); }
    }

    public JsonObject computeModularity(double resolution) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("resolution", resolution);
        params.put("useWeight", false);
        return runStatistic("Modularity", params);
    }

    public JsonObject computeDegree() {
        return runStatistic("Degree", null);
    }

    public JsonObject computeBetweenness() {
        return runStatistic("GraphDistance", null);
    }

    public JsonObject computePageRank() {
        return runStatistic("PageRank", null);
    }

    public JsonObject computeConnectedComponents() {
        return runStatistic("ConnectedComponents", null);
    }

    public JsonObject computeClusteringCoefficient() {
        return runStatistic("ClusteringCoefficient", null);
    }

    public JsonObject computeAvgPathLength() {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("directed", false);
        return runStatistic("GraphDistance", params);
    }

    public JsonObject computeHITS() {
        return runStatistic("HITS", null);
    }

    public JsonObject computeEigenvectorCentrality() {
        return runStatistic("EigenvectorCentrality", null);
    }

    // ─── Filters ─────────────────────────────────────────────────────

    public JsonObject filterByDegreeRange(int minDegree, int maxDegree, boolean dryRun) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            Graph g = currentGraphModel().getGraph();
            Node[] allNodes = g.getNodes().toArray();
            java.util.List<Node> toRemove = new java.util.ArrayList<>();
            for (Node n : allNodes) {
                int deg = g.getDegree(n);
                if (deg < minDegree || (maxDegree > 0 && deg > maxDegree)) {
                    toRemove.add(n);
                }
            }
            if (dryRun) {
                JsonObject r = success("Dry run: " + toRemove.size() + " nodes would be removed");
                r.addProperty("would_remove", toRemove.size());
                r.addProperty("would_remain", g.getNodeCount() - toRemove.size());
                r.addProperty("dry_run", true);
                return r;
            }
            lockWrite(g);
            try { for (Node n : toRemove) g.removeNode(n); }
            finally { unlockWrite(g); }
            refreshPreviewOnEDT(ws);
            JsonObject r = success("Filtered by degree [" + minDegree + ", " + maxDegree + "]");
            r.addProperty("removed", toRemove.size());
            r.addProperty("remaining_nodes", g.getNodeCount());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject filterByEdgeWeight(double minWeight, double maxWeight, boolean dryRun) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            Graph g = currentGraphModel().getGraph();
            Edge[] allEdges = g.getEdges().toArray();
            java.util.List<Edge> toRemove = new java.util.ArrayList<>();
            for (Edge e : allEdges) {
                double w = e.getWeight();
                if (w < minWeight || (maxWeight > 0 && w > maxWeight)) {
                    toRemove.add(e);
                }
            }
            if (dryRun) {
                JsonObject r = success("Dry run: " + toRemove.size() + " edges would be removed");
                r.addProperty("would_remove", toRemove.size());
                r.addProperty("would_remain", g.getEdgeCount() - toRemove.size());
                r.addProperty("dry_run", true);
                return r;
            }
            lockWrite(g);
            try { for (Edge e : toRemove) g.removeEdge(e); }
            finally { unlockWrite(g); }
            refreshPreviewOnEDT(ws);
            JsonObject r = success("Filtered edges by weight [" + minWeight + ", " + maxWeight + "]");
            r.addProperty("removed", toRemove.size());
            r.addProperty("remaining_edges", g.getEdgeCount());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Preview Settings ────────────────────────────────────────────

    public JsonObject getPreviewSettings() {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
                PreviewModel pm = pc.getModel(ws);
                if (pm == null) return error("Preview model not available");

                JsonObject settings = new JsonObject();
                // Get commonly used properties
                for (PreviewProperty prop : pm.getProperties().getProperties()) {
                    String name = prop.getName();
                    Object val = prop.getValue();
                    if (val != null) {
                        if (val instanceof Color) {
                            Color c = (Color) val;
                            settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
                        } else if (val instanceof Number) {
                            settings.addProperty(name, (Number) val);
                        } else if (val instanceof Boolean) {
                            settings.addProperty(name, (Boolean) val);
                        } else if (val instanceof java.awt.Font) {
                            java.awt.Font f = (java.awt.Font) val;
                            String style = f.isBold() && f.isItalic() ? "BoldItalic" : f.isBold() ? "Bold" : f.isItalic() ? "Italic" : "Plain";
                            settings.addProperty(name, f.getFamily() + " " + f.getSize() + " " + style);
                        } else if (val instanceof EdgeColor) {
                            EdgeColor ec = (EdgeColor) val;
                            if (ec.getMode() == EdgeColor.Mode.ORIGINAL) settings.addProperty(name, "original");
                            else if (ec.getMode() == EdgeColor.Mode.MIXED) settings.addProperty(name, "mixed");
                            else if (ec.getCustomColor() != null) {
                                Color c = ec.getCustomColor();
                                settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
                            } else settings.addProperty(name, ec.getMode().toString().toLowerCase());
                        } else if (val instanceof DependantColor) {
                            DependantColor dc = (DependantColor) val;
                            if (dc.getMode() == DependantColor.Mode.PARENT) settings.addProperty(name, "parent");
                            else if (dc.getMode() == DependantColor.Mode.DARKER) settings.addProperty(name, "darker");
                            else if (dc.getCustomColor() != null) {
                                Color c = dc.getCustomColor();
                                settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
                            } else settings.addProperty(name, "parent");
                        } else if (val instanceof DependantOriginalColor) {
                            DependantOriginalColor doc = (DependantOriginalColor) val;
                            if (doc.getMode() == DependantOriginalColor.Mode.ORIGINAL) settings.addProperty(name, "original");
                            else if (doc.getMode() == DependantOriginalColor.Mode.PARENT) settings.addProperty(name, "parent");
                            else if (doc.getCustomColor() != null) {
                                Color c = doc.getCustomColor();
                                settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
                            } else settings.addProperty(name, "original");
                        } else {
                            settings.addProperty(name, val.toString());
                        }
                    }
                }

                // Include background color if not already captured by the main loop
                try {
                    Object bgVal = pm.getProperties().getValue("background.color");
                    if (bgVal instanceof Color) {
                        Color c = (Color) bgVal;
                        settings.addProperty("background.color", String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
                    }
                } catch (Exception ignored) {}

                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.add("settings", settings);
                return r;
            } catch (Exception e) {
                return error("Failed: " + e.getMessage());
            }
        });
    }

    public JsonObject setPreviewSettings(Map<String, Object> settings) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
                PreviewModel pm = pc.getModel(ws);
                if (pm == null) return error("Preview model not available");

                int set = 0;
                for (Map.Entry<String, Object> e : settings.entrySet()) {
                    String key = e.getKey();
                    Object val = e.getValue();
                    if (val == null) continue;  // Skip null values to avoid corrupting preview model

                    // Background color: set on the preview model under Gephi's canonical key
                    // (PreviewProperty.BACKGROUND_COLOR) so the Preview panel, the renderers,
                    // and exportPng's export-time read all share one source of truth. The old
                    // cached exportBackgroundColor field was process-wide sticky state: once
                    // set it tinted every later export in every workspace and project, even
                    // after the user changed the background in Gephi's own Preview panel.
                    if ("background.color".equalsIgnoreCase(key) || "backgroundColor".equalsIgnoreCase(key)) {
                        try {
                            String hex = val.toString().trim();
                            if (hex.startsWith("#")) hex = hex.substring(1);
                            Color bgColor = new Color(Integer.parseInt(hex, 16));
                            PreviewProperty bgProp = pm.getProperties().getProperty(PreviewProperty.BACKGROUND_COLOR);
                            if (bgProp != null) {
                                bgProp.setValue(bgColor);
                            } else {
                                pm.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, bgColor);
                            }
                            set++;
                        } catch (NumberFormatException nfe) {
                            LOGGER.warning("MCP: Invalid background color: " + val);
                        }
                        continue;
                    }

                    PreviewProperty prop = pm.getProperties().getProperty(key);
                    if (prop == null) {
                        // Property registry may not be initialized in this workspace
                        // (e.g. Preview never opened). putValue works regardless and
                        // renderers read it at export time. Non-scalar values are
                        // never valid preview properties — storing one corrupts the
                        // model, so skip them.
                        if (val instanceof Map || val instanceof List) {
                            LOGGER.warning("MCP: Skipping non-scalar preview value for " + key);
                            continue;
                        }
                        Object coerced = val;
                        if (val instanceof String) {
                            String sv = ((String) val).trim();
                            if (sv.equalsIgnoreCase("true") || sv.equalsIgnoreCase("false")) coerced = Boolean.parseBoolean(sv);
                            else {
                                try { coerced = Float.parseFloat(sv); } catch (NumberFormatException ignore) { }
                            }
                        } else if (val instanceof Number) {
                            coerced = ((Number) val).floatValue();
                        } else if (val instanceof Boolean) {
                            coerced = val;
                        }
                        pm.getProperties().putValue(key, coerced);
                        set++;
                        continue;
                    }
                    if (prop != null) {
                        // Convert value based on property type
                        Class<?> type = prop.getType();
                        try {
                            if (type == Color.class && val instanceof String) {
                                String hex = (String) val;
                                if (hex.startsWith("#")) hex = hex.substring(1);
                                prop.setValue(new Color(Integer.parseInt(hex, 16)));
                            } else if (type == Boolean.class || type == boolean.class) {
                                prop.setValue(Boolean.parseBoolean(val.toString()));
                            } else if (type == Float.class || type == float.class) {
                                prop.setValue(Float.parseFloat(val.toString()));
                            } else if (type == Integer.class || type == int.class) {
                                prop.setValue(Integer.parseInt(val.toString()));
                            } else if (type == java.awt.Font.class && val instanceof String) {
                                // Parse font string like "Courier New 12 Bold" -> Font object
                                // Everything before first digit = name, first number = size, rest = style
                                String fontStr = val.toString().trim();
                                String name = "Arial";
                                int fontSize = 12;
                                int fontStyle = java.awt.Font.PLAIN;
                                int numStart = -1;
                                for (int ci = 0; ci < fontStr.length(); ci++) {
                                    if (Character.isDigit(fontStr.charAt(ci))) { numStart = ci; break; }
                                }
                                if (numStart > 0) {
                                    name = fontStr.substring(0, numStart).trim();
                                    String[] rest = fontStr.substring(numStart).trim().split("\\s+");
                                    try { fontSize = Integer.parseInt(rest[0]); } catch (NumberFormatException ignored) {}
                                    for (int pi = 1; pi < rest.length; pi++) {
                                        if ("Bold".equalsIgnoreCase(rest[pi])) fontStyle |= java.awt.Font.BOLD;
                                        else if ("Italic".equalsIgnoreCase(rest[pi])) fontStyle |= java.awt.Font.ITALIC;
                                    }
                                } else if (numStart < 0) {
                                    name = fontStr;
                                }
                                prop.setValue(new java.awt.Font(name, fontStyle, fontSize));
                            } else if (type == java.awt.Font.class) {
                                continue; // Non-string font value, skip
                            } else if (type == DependantColor.class && val instanceof String) {
                                String s = val.toString().trim().toLowerCase();
                                if ("parent".equals(s)) {
                                    prop.setValue(new DependantColor(DependantColor.Mode.PARENT));
                                } else if ("darker".equals(s)) {
                                    prop.setValue(new DependantColor(DependantColor.Mode.DARKER));
                                } else if (s.startsWith("#")) {
                                    prop.setValue(new DependantColor(new Color(Integer.parseInt(s.substring(1), 16))));
                                } else { continue; }
                            } else if (type == DependantOriginalColor.class && val instanceof String) {
                                String s = val.toString().trim().toLowerCase();
                                if ("parent".equals(s)) {
                                    prop.setValue(new DependantOriginalColor(DependantOriginalColor.Mode.PARENT));
                                } else if ("original".equals(s)) {
                                    prop.setValue(new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL));
                                } else if (s.startsWith("#")) {
                                    prop.setValue(new DependantOriginalColor(new Color(Integer.parseInt(s.substring(1), 16))));
                                } else { continue; }
                            } else if (type == EdgeColor.class && val instanceof String) {
                                // For "source"/"target": color edges individually instead of using
                                // EdgeColor mode (which corrupts SVG rendering in Gephi 0.10)
                                String s = val.toString().trim().toLowerCase();
                                if ("source".equals(s) || "target".equals(s)) {
                                    boolean useSource = "source".equals(s);
                                    Graph graph = currentGraphModel().getGraph();
                                    Node[] graphNodes = graph.getNodes().toArray();
                                    Edge[] graphEdges = graph.getEdges().toArray();
                                    java.util.Map<Node, Color> nodeColors = new java.util.HashMap<>();
                                    for (Node n : graphNodes) nodeColors.put(n, n.getColor());
                                    for (Edge edge : graphEdges) {
                                        Node ref = useSource ? edge.getSource() : edge.getTarget();
                                        Color c = nodeColors.get(ref);
                                        if (c != null) edge.setColor(c);
                                    }
                                    prop.setValue(new EdgeColor(EdgeColor.Mode.ORIGINAL));
                                } else if ("mixed".equals(s)) {
                                    prop.setValue(new EdgeColor(EdgeColor.Mode.MIXED));
                                } else if ("original".equals(s)) {
                                    prop.setValue(new EdgeColor(EdgeColor.Mode.ORIGINAL));
                                } else if (s.startsWith("#")) {
                                    prop.setValue(new EdgeColor(new Color(Integer.parseInt(s.substring(1), 16))));
                                } else { continue; }
                            } else {
                                continue; // Skip unknown types
                            }
                            set++;
                        } catch (NumberFormatException nfe) {
                            LOGGER.warning("MCP: Invalid number/color value for " + key + ": " + val);
                            continue;
                        } catch (Exception ex) {
                            LOGGER.warning("MCP: Failed to set preview property " + key + ": " + ex.getMessage());
                            continue;
                        }
                    }
                }
                JsonObject r = success("Set " + set + " preview properties");
                r.addProperty("properties_set", set);
                return r;
            } catch (Exception e) {
                return error("Failed: " + e.getMessage());
            }
        });
    }

    // ─── Export ───────────────────────────────────────────────────────

    public JsonObject exportGexf(String filePath) {
        return exportGexf(filePath, true);
    }

    /**
     * @param visible export the filtered visible graph (true — the historical behaviour)
     *        or the full graph. Either way the response self-declares which view was
     *        written via addViewInfo, so a filtered export is never silent about it.
     */
    public JsonObject exportGexf(String filePath, boolean visible) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                ExportController ec = Lookup.getDefault().lookup(ExportController.class);
                Exporter exporter = ec.getExporter("gexf");
                if (exporter == null) return error("GEXF exporter not available");
                if (exporter instanceof GraphExporter) {
                    ((GraphExporter) exporter).setExportVisible(visible);
                    ((GraphExporter) exporter).setWorkspace(ws);
                }
                ec.exportFile(new File(filePath), exporter);
                JsonObject r = success("Exported to " + filePath);
                addViewInfo(r, currentGraphModel(), visible);
                return r;
            } catch (Exception e) { return error("Export failed: " + e.getMessage()); }
        });
    }

    /** GEXF export returned inline as a string — no file round-trip. */
    public JsonObject exportGexfContent() {
        return exportGexfContent(true);
    }

    /**
     * @param visible export the filtered visible graph (true — the historical behaviour)
     *        or the full graph. Several downstream tools parse this inline GEXF as their
     *        read path, so the response self-declares the view via addViewInfo: with a
     *        filter active they would otherwise silently compute over a subgraph the
     *        read endpoints never described.
     */
    public JsonObject exportGexfContent(boolean visible) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                ExportController ec = Lookup.getDefault().lookup(ExportController.class);
                Exporter exporter = ec.getExporter("gexf");
                if (exporter == null) return error("GEXF exporter not available");
                if (exporter instanceof GraphExporter) {
                    ((GraphExporter) exporter).setExportVisible(visible);
                    ((GraphExporter) exporter).setWorkspace(ws);
                }
                java.io.StringWriter sw = new java.io.StringWriter();
                ec.exportWriter(sw, (org.gephi.io.exporter.spi.CharacterExporter) exporter);
                JsonObject r = success("GEXF exported inline");
                addViewInfo(r, currentGraphModel(), visible);
                r.addProperty("content", sw.toString());
                return r;
            } catch (Exception e) { return error("Export failed: " + e.getMessage()); }
        });
    }

    public JsonObject exportPng(String filePath, int w, int h) {
        // Runs on the calling thread: rendering the export and compositing the
        // background below (ImageIO.read, a full BufferedImage copy, ImageIO.write —
        // at a default 1920x1080) is far too heavy for the EDT and needs nothing from
        // it. Only the preview refresh hops to the EDT.
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            refreshPreviewOnEDT(ws);

            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter("png");
            if (exporter == null) return error("PNG exporter not available");

            // Set dimensions via reflection (PNGExporter is in plugin, not API)
            setViaReflection(exporter, "width", w);
            setViaReflection(exporter, "height", h);

            if (exporter instanceof GraphExporter) {
                ((GraphExporter) exporter).setWorkspace(ws);
            }

            ec.exportFile(new File(filePath), exporter);

            // Post-process: composite onto the preview model's background color.
            // Gephi's PNG exporter renders a transparent background; this fills it.
            // The color is read from the preview model AT EXPORT TIME, so a change in
            // Gephi's own Preview panel (or another workspace's settings) is honoured
            // rather than overridden by a stale process-wide copy.
            Color bgColor = previewBackgroundColor(ws);
            if (bgColor != null && !bgColor.equals(Color.WHITE)) {
                BufferedImage exported = ImageIO.read(new File(filePath));
                if (exported != null) {
                    BufferedImage result = new BufferedImage(exported.getWidth(), exported.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = result.createGraphics();
                    g2d.setColor(bgColor);
                    g2d.fillRect(0, 0, result.getWidth(), result.getHeight());
                    g2d.drawImage(exported, 0, 0, null);
                    g2d.dispose();
                    ImageIO.write(result, "PNG", new File(filePath));
                }
            }

            return success("Exported to " + filePath);
        } catch (Exception e) { return error("Export failed: " + e.getMessage()); }
    }

    /** The workspace's preview background color, or null when none is available. */
    private static Color previewBackgroundColor(Workspace ws) {
        PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel pm = pc != null ? pc.getModel(ws) : null;
        if (pm == null) return null;
        Object bg = pm.getProperties().getValue(PreviewProperty.BACKGROUND_COLOR);
        // Legacy spelling: earlier plugin builds stored the color under "background.color".
        if (!(bg instanceof Color)) bg = pm.getProperties().getValue("background.color");
        return bg instanceof Color ? (Color) bg : null;
    }

    /**
     * Export the LIVE Overview canvas as it is actually rendered on screen — selection
     * highlighting, hover state, current camera framing — using Gephi's own built-in
     * screenshot feature (org.gephi.visualization.api.ScreenshotController), the same
     * backend behind the toolbar "take a snapshot" button. This is a DIFFERENT pipeline
     * from exportPng: exportPng renders the graph's stored data (colors, positions) through
     * the Preview renderer, which has no concept of selection at all. This method captures
     * the actual GL framebuffer, so a person's box-drag selection (dimmed unselected nodes,
     * vivid selected ones) shows up exactly as they see it.
     *
     * scaleFactor: a multiplier on the current on-screen canvas size (not literal pixel
     * width/height like exportPng — Gephi's screenshot API only supports a scale factor).
     *
     * takeScreenshot() is asynchronous (queued against the render engine's next frame via
     * a LongTaskExecutor), so this polls a dedicated fresh temp directory for the resulting
     * file rather than assuming completion on return.
     */
    public JsonObject exportScreenshot(String filePath, int scaleFactor, boolean transparentBackground) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");

        File targetFile = new File(filePath);
        File targetDir = targetFile.getAbsoluteFile().getParentFile();
        File captureDir;
        try {
            captureDir = java.nio.file.Files.createTempDirectory("gephi-screenshot-").toFile();
        } catch (java.io.IOException e) {
            return error("Could not create temp capture directory: " + e.getMessage());
        }

        try {
            runOnEDT(() -> {
                // ScreenshotController is not independently registered in Lookup — it is only
                // reachable via VisualizationController.getScreenshotController() (the same
                // VisualizationController singleton getSelection/focusView already use).
                org.gephi.visualization.api.VisualizationController vc = Lookup.getDefault()
                    .lookup(org.gephi.visualization.api.VisualizationController.class);
                if (vc == null) throw new RuntimeException("Visualization controller not available");
                org.gephi.visualization.api.ScreenshotController sc = vc.getScreenshotController();
                if (sc == null) throw new RuntimeException("Screenshot controller not available");
                // These four settings are shared with Gephi's own toolbar screenshot button,
                // and ScreenshotController exposes setters only, so their previous values
                // cannot be read back and restored exactly. What must not happen is leaving
                // auto-save enabled while pointing at captureDir, which this method deletes:
                // the user's next manual screenshot would then save into a directory that no
                // longer exists. Auto-save is therefore turned back off and the directory
                // pointed somewhere real, which returns the toolbar button to its normal
                // save-dialog behaviour rather than to a silent failure.
                try {
                    sc.setAutoSave(true);
                    sc.setDefaultDirectory(captureDir);
                    sc.setScaleFactor(scaleFactor);
                    sc.setTransparentBackground(transparentBackground);
                    sc.takeScreenshot();
                } finally {
                    sc.setAutoSave(false);
                    sc.setDefaultDirectory(new File(System.getProperty("user.home")));
                }
                return null;
            });

            File written = pollForNewFile(captureDir, 10_000);
            if (written == null) {
                return error("Screenshot did not complete within 10s — the render engine may be busy, "
                    + "retry, or fully restart Gephi if this persists");
            }
            if (!waitForStableFileSize(written, 5_000)) {
                return error("Screenshot file did not finish writing within 5s");
            }

            if (targetDir != null) targetDir.mkdirs();
            java.nio.file.Files.move(written.toPath(), targetFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            JsonObject r = success("Exported to " + filePath);
            r.addProperty("scale_factor", scaleFactor);
            r.addProperty("selection_aware", true);
            return r;
        } catch (Exception e) {
            return error("Screenshot export failed: " + e.getMessage());
        } finally {
            deleteDirQuietly(captureDir);
        }
    }

    /** Poll a directory for the first file to appear in it, up to timeoutMs. */
    static File pollForNewFile(File dir, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) return files[0];
            try { Thread.sleep(150); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    /** Wait for a file's size to stop changing between polls (write-in-progress guard). */
    static boolean waitForStableFileSize(File file, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        long lastSize = -1;
        while (System.currentTimeMillis() < deadline) {
            long size = file.length();
            if (size > 0 && size == lastSize) return true;
            lastSize = size;
            try { Thread.sleep(100); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return file.length() == lastSize && lastSize > 0;
    }

    static void deleteDirQuietly(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        dir.delete();
    }

    /**
     * Poll the live engine selection until its size matches expected or timeoutMs
     * elapses. selectNodes()/resetSelection() queue their effect onto the render
     * engine rather than applying it synchronously with the call, so a read (or a
     * screenshot) taken immediately after can race ahead of it and see stale state.
     * Returns the final observed size (may differ from expected on timeout).
     */
    private static int waitForSelectionCount(
            org.gephi.visualization.api.VisualizationController vc, int expected, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        int last = -1;
        while (System.currentTimeMillis() < deadline) {
            org.gephi.visualization.api.VisualizationModel model = vc.getModel();
            last = model != null ? model.getSelectedNodes().size() : 0;
            if (last == expected) return last;
            try { Thread.sleep(30); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return last;
            }
        }
        return last;
    }

    public JsonObject exportPdf(String filePath, int w, int h) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                Graph g = currentGraphModel().getGraph();
                if (g.getNodeCount() == 0) return error("Cannot export PDF: graph has no nodes");
                PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
                if (previewController != null) previewController.refreshPreview(ws);

                ExportController ec = Lookup.getDefault().lookup(ExportController.class);
                Exporter exporter = ec.getExporter("pdf");
                if (exporter == null) return error("PDF exporter not available");
                if (w > 0) setViaReflection(exporter, "width", w);
                if (h > 0) setViaReflection(exporter, "height", h);
                if (exporter instanceof GraphExporter) ((GraphExporter) exporter).setWorkspace(ws);
                ec.exportFile(new File(filePath), exporter);
                return success("Exported to " + filePath);
            } catch (IllegalArgumentException e) {
                return error("Export failed: graph nodes may not be positioned — run a layout first");
            } catch (Exception e) { return error("Export failed: " + e.getMessage()); }
        });
    }

    public JsonObject exportSvg(String filePath) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
                if (previewController != null) previewController.refreshPreview(ws);

                ExportController ec = Lookup.getDefault().lookup(ExportController.class);
                Exporter exporter = ec.getExporter("svg");
                if (exporter == null) return error("SVG exporter not available");
                if (exporter instanceof GraphExporter) ((GraphExporter) exporter).setWorkspace(ws);
                ec.exportFile(new File(filePath), exporter);
                return success("Exported to " + filePath);
            } catch (Exception e) { return error("Export failed: " + e.getMessage()); }
        });
    }

    public JsonObject exportGraphml(String filePath) {
        return exportGraphml(filePath, true);
    }

    /** @param visible see exportGexf — same contract, response self-declares the view. */
    public JsonObject exportGraphml(String filePath, boolean visible) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                ExportController ec = Lookup.getDefault().lookup(ExportController.class);
                Exporter exporter = ec.getExporter("graphml");
                if (exporter == null) return error("GraphML exporter not available");
                if (exporter instanceof GraphExporter) {
                    ((GraphExporter) exporter).setExportVisible(visible);
                    ((GraphExporter) exporter).setWorkspace(ws);
                }
                ec.exportFile(new File(filePath), exporter);
                JsonObject r = success("Exported to " + filePath);
                addViewInfo(r, currentGraphModel(), visible);
                return r;
            } catch (Exception e) { return error("Export failed: " + e.getMessage()); }
        });
    }

    public JsonObject exportCsv(String filePath, String separator, String target) {
        // Runs on the calling thread: serialising the whole graph into a StringBuilder
        // and writing it to disk is bulk work with no Swing dependency — it has no
        // business on the EDT (see the threading note above setEdgeColor).
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        // Always use manual export — Gephi's built-in CSV exporter produces an adjacency matrix
        return exportCsvManual(filePath, separator, target);
    }

    private JsonObject exportCsvManual(String filePath, String separator, String target) {
        try {
            GraphModel gm = currentGraphModel();
            String csvText = buildCsv(gm, separator, target);
            try (java.io.Writer fw = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write(csvText);
            }
            JsonObject r = success("Exported to " + filePath);
            // CSV is built from the FULL graph (buildCsv walks gm.getGraph()) — declare
            // that, since the other exporters write the visible graph.
            addViewInfo(r, gm, false);
            return r;
        } catch (Exception e) {
            return error("CSV export failed: " + e.getMessage());
        }
    }

    /** Build node/edge CSV text from a model (RFC 4180 quoted). Package-private + static for unit testing. */
    static String buildCsv(GraphModel gm, String separator, String target) {
        Graph g = gm.getGraph();
        String sep = separator != null ? separator : ",";
        StringBuilder sb = new StringBuilder();
        {
            if (!"edges".equalsIgnoreCase(target)) {
                // Export nodes
                sb.append(csv("Id", sep)).append(sep).append(csv("Label", sep));
                for (Column col : gm.getNodeTable()) {
                    if (!col.isProperty()) sb.append(sep).append(csv(col.getTitle(), sep));
                }
                sb.append("\n");
                lockRead(g);
                try {
                    for (Node n : g.getNodes().toArray()) {
                        sb.append(csv(String.valueOf(n.getId()), sep)).append(sep)
                          .append(csv(n.getLabel() != null ? n.getLabel() : "", sep));
                        for (Column col : gm.getNodeTable()) {
                            if (!col.isProperty()) {
                                Object v = n.getAttribute(col);
                                sb.append(sep).append(csv(v != null ? v.toString() : "", sep));
                            }
                        }
                        sb.append("\n");
                    }
                } finally { g.readUnlock(); }
            }

            if ("edges".equalsIgnoreCase(target) || "both".equalsIgnoreCase(target)) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(csv("Source", sep)).append(sep).append(csv("Target", sep)).append(sep).append(csv("Weight", sep));
                for (Column col : gm.getEdgeTable()) {
                    if (!col.isProperty()) sb.append(sep).append(csv(col.getTitle(), sep));
                }
                sb.append("\n");
                lockRead(g);
                try {
                    for (Edge e : g.getEdges().toArray()) {
                        sb.append(csv(String.valueOf(e.getSource().getId()), sep)).append(sep)
                          .append(csv(String.valueOf(e.getTarget().getId()), sep)).append(sep)
                          .append(csv(String.valueOf(e.getWeight()), sep));
                        for (Column col : gm.getEdgeTable()) {
                            if (!col.isProperty()) {
                                Object v = e.getAttribute(col);
                                sb.append(sep).append(csv(v != null ? v.toString() : "", sep));
                            }
                        }
                        sb.append("\n");
                    }
                } finally { g.readUnlock(); }
            }
        }
        return sb.toString();
    }

    /**
     * RFC 4180 field quoting: wrap the value in double quotes (doubling any internal
     * quote) when it contains the separator, a quote, or a line break. Without this,
     * a label or attribute containing the separator silently corrupts the columns.
     */
    static String csv(String value, String sep) {
        if (value == null) value = "";
        boolean needsQuote = value.contains(sep) || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        return needsQuote ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }

    // ─── Import ──────────────────────────────────────────────────────

    public JsonObject importFile(String filePath) {
        return importFile(filePath, null);
    }

    /**
     * Imports a file. {@code maxNodeSize} caps imported node sizes when set; when null the
     * file's own sizes are preserved exactly, so an import followed by an export round-trips.
     */
    public JsonObject importFile(String filePath, Float maxNodeSize) {
        // Runs on the calling thread. Gephi's own import runs off the event dispatch thread,
        // and parsing a large file inside runOnEDT froze the UI and then blew its 15-second
        // budget, so the caller was told "Gephi's UI thread is unresponsive, fully quit and
        // reopen" while the import was in fact still running and went on to succeed.
        {
            File file = new File(filePath);
            if (!file.exists()) return error("File not found: " + filePath);
            try {
                ImportController ic = Lookup.getDefault().lookup(ImportController.class);
                Container c = ic.importFile(file);
                if (c == null) return error("Import failed - unsupported format or empty file");

                Workspace ws = currentWorkspace();
                if (ws == null) {
                    getProjectController().newProject();
                    ws = currentWorkspace();
                }

                Processor processor = null;
                for (Processor p : Lookup.getDefault().lookupAll(Processor.class)) {
                    if (p.getClass().getSimpleName().equals("DefaultProcessor")) {
                        processor = p;
                        break;
                    }
                }
                if (processor == null) processor = Lookup.getDefault().lookup(Processor.class);
                if (processor == null) return error("No processor found");

                Workspace importedWs = ic.process(c, processor, ws);

                // Optional, and off by default. Capping rewrites viz:size values the file
                // actually carries, so importing and re-exporting would silently change the
                // user's data. It stays available because oversized nodes from GEXF can hide
                // the whole graph, but only when the caller asks for it.
                int capped = 0;
                if (maxNodeSize != null && maxNodeSize > 0) {
                    Graph importedGraph = getGraphController().getGraphModel(ws).getGraph();
                    lockWrite(importedGraph);
                    try {
                        for (Node n : importedGraph.getNodes().toArray()) {
                            if (n.size() > maxNodeSize) {
                                n.setSize(maxNodeSize.floatValue());
                                capped++;
                            }
                        }
                    } finally {
                        unlockWrite(importedGraph);
                    }
                }

                Workspace effectiveWs = importedWs != null ? importedWs : ws;
                Graph g = getGraphController().getGraphModel(effectiveWs).getGraph();
                JsonObject r = success("Imported from " + file.getName());
                if (capped > 0) {
                    r.addProperty("nodes_size_capped", capped);
                    r.addProperty("max_node_size", maxNodeSize);
                }
                r.addProperty("node_count", g.getNodeCount());
                r.addProperty("edge_count", g.getEdgeCount());
                return r;
            } catch (Exception e) { return error("Import failed: " + e.getMessage()); }
        }
    }

    // ─── Graph Operations ────────────────────────────────────────────

    public JsonObject clearGraph() {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                int nodeCount = g.getNodeCount();
                int edgeCount = g.getEdgeCount();
                g.clear();
                JsonObject r = success("Graph cleared");
                r.addProperty("nodes_removed", nodeCount);
                r.addProperty("edges_removed", edgeCount);
                return r;
            } finally { unlockWrite(g); }
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject removeIsolates() {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            Graph g = currentGraphModel().getGraph();
            java.util.List<Node> isolates = new java.util.ArrayList<>();
            lockWrite(g);
            try {
                for (Node n : g.getNodes().toArray()) {
                    if (g.getDegree(n) == 0) isolates.add(n);
                }
                for (Node n : isolates) g.removeNode(n);
            } finally { unlockWrite(g); }
            // Refresh preview so exports reflect the filtered graph (EDT hop; outside the lock)
            refreshPreviewOnEDT(ws);
            JsonObject r = success("Removed " + isolates.size() + " isolated nodes");
            r.addProperty("removed", isolates.size());
            r.addProperty("remaining_nodes", g.getNodeCount());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject extractEgoNetwork(String nodeId, int depth) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            Graph g = currentGraphModel().getGraph();
            Node center = g.getNode(nodeId);
            if (center == null) return error("Node not found: " + nodeId);

            // BFS to find nodes within depth
            java.util.Set<Node> keep = new java.util.LinkedHashSet<>();
            java.util.Queue<Node> queue = new java.util.LinkedList<>();
            java.util.Map<Node, Integer> distances = new java.util.HashMap<>();
            keep.add(center);
            queue.add(center);
            distances.put(center, 0);

            while (!queue.isEmpty()) {
                Node current = queue.poll();
                int dist = distances.get(current);
                if (dist >= depth) continue;
                for (Node neighbor : g.getNeighbors(current).toArray()) {
                    if (!keep.contains(neighbor)) {
                        keep.add(neighbor);
                        queue.add(neighbor);
                        distances.put(neighbor, dist + 1);
                    }
                }
            }

            // Remove nodes not in keep set
            java.util.List<Node> toRemove = new java.util.ArrayList<>();
            lockWrite(g);
            try {
                for (Node n : g.getNodes().toArray()) {
                    if (!keep.contains(n)) toRemove.add(n);
                }
                for (Node n : toRemove) g.removeNode(n);
            } finally { unlockWrite(g); }

            // Refresh preview so exports reflect the filtered graph (EDT hop; outside the lock)
            refreshPreviewOnEDT(ws);

            JsonObject r = success("Ego network extracted for " + nodeId);
            r.addProperty("kept_nodes", keep.size());
            r.addProperty("removed_nodes", toRemove.size());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject extractGiantComponent() {
        // Statistics must run OFF the EDT (they dispatch UI work to EDT internally).
        // Only node removal and preview refresh need the EDT.
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();

            // Run connected components (on HTTP thread, not EDT)
            StatisticsBuilder ccBuilder = null;
            for (StatisticsBuilder sb : Lookup.getDefault().lookupAll(StatisticsBuilder.class)) {
                if (sb.getName().equalsIgnoreCase("ConnectedComponents") ||
                    sb.getClass().getSimpleName().toLowerCase().contains("connectedcomponents")) {
                    ccBuilder = sb;
                    break;
                }
            }
            if (ccBuilder == null) return error("ConnectedComponents statistic not found");

            Statistics stat = ccBuilder.getStatistics();
            stat.execute(gm);

            // Find the column
            Column ccCol = gm.getNodeTable().getColumn("componentnumber");
            if (ccCol == null) {
                for (Column col : gm.getNodeTable()) {
                    if (col.getTitle().toLowerCase().contains("component")) {
                        ccCol = col;
                        break;
                    }
                }
            }
            if (ccCol == null) return error("Component column not found after running statistics");

            // Count nodes per component
            java.util.Map<Integer, Integer> componentSizes = new java.util.HashMap<>();
            Node[] allNodes = g.getNodes().toArray();
            final Column fccCol = ccCol;
            for (Node n : allNodes) {
                Object v = n.getAttribute(fccCol);
                int comp = v instanceof Number ? ((Number) v).intValue() : 0;
                componentSizes.put(comp, componentSizes.getOrDefault(comp, 0) + 1);
            }

            int giantComp = 0;
            int giantSize = 0;
            for (java.util.Map.Entry<Integer, Integer> e : componentSizes.entrySet()) {
                if (e.getValue() > giantSize) {
                    giantSize = e.getValue();
                    giantComp = e.getKey();
                }
            }

            // Remove nodes on the calling thread — graph mutation needs only the graph
            // write lock (see the threading note above setEdgeColor); only the preview
            // refresh hops to the EDT.
            java.util.List<Node> toRemove = new java.util.ArrayList<>();
            for (Node n : allNodes) {
                Object v = n.getAttribute(fccCol);
                int comp = v instanceof Number ? ((Number) v).intValue() : -1;
                if (comp != giantComp) toRemove.add(n);
            }
            lockWrite(g);
            try { for (Node n : toRemove) g.removeNode(n); }
            finally { unlockWrite(g); }
            refreshPreviewOnEDT(ws);
            JsonObject r = success("Giant component extracted");
            r.addProperty("kept_nodes", giantSize);
            r.addProperty("removed_nodes", toRemove.size());
            r.addProperty("component_count", componentSizes.size());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    public JsonObject setEdgeThicknessByWeight(float minThickness, float maxThickness) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            try {
                PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
                PreviewModel pm = pc.getModel(ws);
                if (pm == null) return error("Preview model not available");

                // Set edge thickness to be rescaled based on weight
                // Use the preview property for edge thickness
                PreviewProperty edgeThicknessProp = pm.getProperties().getProperty("edge.thickness");
                if (edgeThicknessProp != null) {
                    edgeThicknessProp.setValue(minThickness);
                }

                // Set rescale weight property if available
                PreviewProperty rescaleProp = pm.getProperties().getProperty("edge.rescale-weight");
                if (rescaleProp != null) {
                    rescaleProp.setValue(true);
                }

                PreviewProperty rescaleMinProp = pm.getProperties().getProperty("edge.rescale-weight.min");
                if (rescaleMinProp != null) {
                    rescaleMinProp.setValue(minThickness);
                }

                PreviewProperty rescaleMaxProp = pm.getProperties().getProperty("edge.rescale-weight.max");
                if (rescaleMaxProp != null) {
                    rescaleMaxProp.setValue(maxThickness);
                }

                JsonObject r = success("Edge thickness configured by weight");
                r.addProperty("min_thickness", minThickness);
                r.addProperty("max_thickness", maxThickness);
                return r;
            } catch (Exception e) {
                return error("Failed: " + e.getMessage());
            }
        });
    }

    public JsonObject resetFilters() {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            // setVisibleView() takes Gephi's own blocking write lock; hold our deadlock-safe
            // lock first so that call re-enters instead of queuing behind the renderer.
            lockWrite(g);
            try {
                gm.setVisibleView(null);
            } finally { unlockWrite(g); }
            return success("Filters reset - full graph view restored");
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    // ─── Shutdown ────────────────────────────────────────────────────

    /** The layout executor, recreated if a previous shutdown() killed it. */
    private synchronized ExecutorService layoutExecutor() {
        if (layoutExecutor == null || layoutExecutor.isShutdown()) {
            layoutExecutor = Executors.newSingleThreadExecutor();
        }
        return layoutExecutor;
    }

    public void shutdown() {
        layoutRunning.set(false);
        layoutExecutor.shutdownNow();
    }

    /**
     * Cheap wedge detector for /health: try the graph read lock briefly.
     * "ok" = acquired instantly; "busy" = could not acquire (a writer is parked or
     * the renderer is saturating the lock — if persistent, Gephi needs a restart);
     * "none" = no workspace open.
     */
    public String graphLockProbe() {
        try {
            GraphModel gm = currentGraphModel();
            if (gm == null) return "none";
            Graph g = gm.getGraph();
            java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock rl = readLockHandle(g);
            if (rl == null) return "unknown";
            if (rl.tryLock(150, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                rl.unlock();
                return "ok";
            }
            return "busy";
        } catch (Throwable t) {
            return "unknown";
        }
    }

    /**
     * Live counters from the underlying ReentrantReadWriteLock: active read holds,
     * write-locked flag, and queued threads. Diagnostic companion to graphLockProbe;
     * a nonzero reader count while Gephi is idle means a leaked read hold (the
     * precursor of a permanent wedge). All values -1 when unreachable.
     */
    public JsonObject graphLockStats() {
        JsonObject o = new JsonObject();
        o.addProperty("readers", -1);
        o.addProperty("write_locked", false);
        o.addProperty("queued", -1);
        try {
            GraphModel gm = currentGraphModel();
            if (gm == null) return o;
            org.gephi.graph.api.GraphLock lock = gm.getGraph().getLock();
            if (lock == null) return o;
            java.lang.reflect.Field f = lock.getClass().getDeclaredField("readWriteLock");
            f.setAccessible(true);
            Object v = f.get(lock);
            if (v instanceof java.util.concurrent.locks.ReentrantReadWriteLock) {
                java.util.concurrent.locks.ReentrantReadWriteLock rwl =
                    (java.util.concurrent.locks.ReentrantReadWriteLock) v;
                o.addProperty("readers", rwl.getReadLockCount());
                o.addProperty("write_locked", rwl.isWriteLocked());
                o.addProperty("queued", rwl.getQueueLength());
            }
        } catch (Throwable t) {
            // leave the -1 defaults
        }
        return o;
    }

    // ─── Human selection journal ─────────────────────────────────────────

    /**
     * Install the passive NODE_LEFT_CLICK listener once. Safe to call often;
     * no-ops until the visualization is available. The listener returns false
     * (observe, never consume) so Gephi's own tools keep working.
     */
    public synchronized void ensureClickListener() {
        if (clickListenerInstalled) return;
        org.gephi.visualization.api.VisualizationController vc =
            Lookup.getDefault().lookup(org.gephi.visualization.api.VisualizationController.class);
        if (vc == null) return;
        vc.addListener(new org.gephi.visualization.api.VisualizationEventListener() {
            @Override
            public boolean handleEvent(org.gephi.visualization.api.VisualizationEvent event) {
                try {
                    Object data = event.getData();
                    if (data instanceof Node[]) {
                        Node[] nodes = (Node[]) data;
                        if (nodes.length > 0) recordClick(nodes);
                    }
                } catch (Throwable t) {
                    // Never disturb the viz event thread.
                }
                return false;
            }

            @Override
            public org.gephi.visualization.api.VisualizationEvent.Type getType() {
                return org.gephi.visualization.api.VisualizationEvent.Type.NODE_LEFT_CLICK;
            }
        });
        clickListenerInstalled = true;
        // Deliberately does NOT enable rectangle selection. Installing the listener is
        // passive observation and is safe to do at startup, which is where it happens so
        // that clicks made before an assistant ever connects are still recorded. Changing
        // the mouse mode is not passive: it would alter the tool every user of this plugin
        // sees on every launch, including those who never connect an assistant.
        // getSelection() enables rectangle selection instead, because a caller asking what
        // is selected is the point at which the user is actually driving the assistant.
    }

    /**
     * Turn on rectangle (box-drag) selection once per session so the human can
     * point at nodes for the agent to read, without first clicking the toolbar's
     * selection tool. No-op if the view isn't started yet (retried on the next
     * call) or if it is already on. Never overrides a mode the human later sets
     * on their own — it fires at most once, and only while selection is still off.
     */
    void ensureRectangleSelection() {
        if (rectangleAutoEnabled) return;
        try {
            org.gephi.visualization.api.VisualizationController vc = Lookup.getDefault()
                .lookup(org.gephi.visualization.api.VisualizationController.class);
            if (vc == null) return;
            org.gephi.visualization.api.VisualizationModel model = vc.getModel();
            if (model == null) return;  // view not started; try again next call
            if (!model.isRectangleSelection()) vc.setRectangleSelection();
            rectangleAutoEnabled = true;
        } catch (Throwable t) {
            // Never disturb a health/selection call over a viz hiccup.
        }
    }

    private void recordClick(Node[] nodes) {
        JsonObject entry = new JsonObject();
        entry.addProperty("time_ms", System.currentTimeMillis());
        JsonArray arr = new JsonArray();
        for (Node n : nodes) {
            JsonObject jn = new JsonObject();
            jn.addProperty("id", String.valueOf(n.getId()));
            String label = n.getLabel();
            if (label != null && !label.isEmpty() && !label.equals(String.valueOf(n.getId()))) {
                jn.addProperty("label", label);
            }
            arr.add(jn);
        }
        entry.add("nodes", arr);
        synchronized (clickJournal) {
            clickJournal.addLast(entry);
            while (clickJournal.size() > CLICK_JOURNAL_MAX) clickJournal.removeFirst();
        }
    }

    private static final int SELECTION_MAX_NODES = 200;

    private JsonObject nodeRef(Node n) {
        JsonObject jn = new JsonObject();
        jn.addProperty("id", String.valueOf(n.getId()));
        String label = n.getLabel();
        if (label != null && !label.isEmpty() && !label.equals(String.valueOf(n.getId()))) {
            jn.addProperty("label", label);
        }
        return jn;
    }

    /**
     * What the human has selected in the Gephi window. Two sources:
     * selected_now — the engine's persistent selection (rectangle selection
     * keeps it after the mouse moves away; the primary channel), read via
     * reflection (VizController.getEngine() -> VizEngine.getGraphSelection()
     * -> getSelectedNodes(), all public, reflection only to avoid a
     * compile-time dependency on the engine module); clicks — the
     * NODE_LEFT_CLICK journal (fires only in modes that populate the engine
     * selection at click time). clear=true consumes the journal only; the
     * live selection always reflects the canvas.
     */
    public JsonObject getSelection(boolean clear) {
        ensureClickListener();
        ensureRectangleSelection();
        JsonObject r = success("Human selection");
        JsonArray selected = new JsonArray();
        int totalSelected = 0;
        try {
            org.gephi.visualization.api.VisualizationController vc = Lookup.getDefault()
                .lookup(org.gephi.visualization.api.VisualizationController.class);
            if (vc != null) {
                // Report the canvas state so the agent can explain an empty selection
                // (e.g. rectangle mode off) instead of silently returning nothing.
                org.gephi.visualization.api.VisualizationModel model = vc.getModel();
                java.util.Collection<Node> sel = null;
                if (model != null) {
                    r.addProperty("selection_enabled", model.isSelectionEnabled());
                    r.addProperty("rectangle_selection", model.isRectangleSelection());
                    r.addProperty("zoom", model.getZoom());
                    // Public read path — no dependency on the internal viz engine.
                    sel = model.getSelectedNodes();
                }
                if (sel == null) sel = engineSelectionFallback(vc);  // older builds
                if (sel != null) {
                    for (Node n : sel) {
                        totalSelected++;
                        if (selected.size() < SELECTION_MAX_NODES) selected.add(nodeRef(n));
                    }
                }
            }
        } catch (Throwable t) {
            r.addProperty("selection_error", t.getClass().getSimpleName() + ": " + t.getMessage());
        }
        r.add("selected_now", selected);
        r.addProperty("selected_count", totalSelected);
        if (totalSelected > SELECTION_MAX_NODES) {
            r.addProperty("selected_truncated", true);
        }
        JsonArray clicks = new JsonArray();
        synchronized (clickJournal) {
            for (JsonObject e : clickJournal) clicks.add(e.deepCopy());
            if (clear) clickJournal.clear();
        }
        r.add("clicks", clicks);
        r.addProperty("click_count", clicks.size());
        r.addProperty("listener_active", clickListenerInstalled);
        return r;
    }

    /**
     * Legacy read path for Gephi builds whose VisualizationModel does not carry the
     * selection: reflect into the render engine
     * (VisualizationController.getEngine() -> VizEngine.getGraphSelection() ->
     * getSelectedNodes()). Returns null when unavailable so the caller can fall back
     * to an empty selection. Reflection-only to avoid a compile-time dependency on
     * the engine module.
     */
    @SuppressWarnings("unchecked")
    private java.util.Collection<Node> engineSelectionFallback(Object vc) {
        try {
            Object opt = vc.getClass().getMethod("getEngine").invoke(vc);
            if (opt instanceof java.util.Optional && ((java.util.Optional<?>) opt).isPresent()) {
                Object engine = ((java.util.Optional<?>) opt).get();
                Object gsel = engine.getClass().getMethod("getGraphSelection").invoke(engine);
                if (gsel != null) {
                    java.lang.reflect.Method m = gsel.getClass().getMethod("getSelectedNodes");
                    m.setAccessible(true);
                    Object coll = m.invoke(gsel);
                    if (coll instanceof java.util.Collection) {
                        return (java.util.Collection<Node>) coll;
                    }
                }
            }
        } catch (Throwable t) {
            // Unavailable on this build; caller reports an empty selection.
        }
        return null;
    }

    // ─── View / camera control (teaching mode) ──────────────────────────

    /**
     * Direct the human viewer's attention in the Gephi window: center the camera on
     * the graph, a node, an edge, or a region; optionally select nodes (visual
     * highlight) and set zoom. No-op modes never touch the graph write lock.
     */
    public JsonObject focusView(String mode, String nodeId, String source, String target,
                                Double x, Double y, Double w, Double h,
                                Double zoom, java.util.List<String> select) {
        org.gephi.visualization.api.VisualizationController vc =
            Lookup.getDefault().lookup(org.gephi.visualization.api.VisualizationController.class);
        if (vc == null) return error("No visualization available (headless or view not started)");
        GraphModel gm = currentGraphModel();
        if (gm == null) return error("No workspace open");
        Graph g = gm.getGraph();
        try {
            String m = mode == null ? "graph" : mode.toLowerCase();
            switch (m) {
                case "graph":
                    vc.centerOnGraph();
                    break;
                case "zero":
                    vc.centerOnZero();
                    break;
                case "node": {
                    if (nodeId == null) return error("Missing 'id' for mode=node");
                    Node n = g.getNode(nodeId);
                    if (n == null) return error("Node not found: " + nodeId);
                    vc.centerOnNode(n);
                    break;
                }
                case "edge": {
                    if (source == null || target == null) return error("Missing 'source'/'target' for mode=edge");
                    Node ns = g.getNode(source), nt = g.getNode(target);
                    if (ns == null || nt == null) return error("Edge endpoints not found");
                    Edge e = g.getEdge(ns, nt, 1);  // directed
                    if (e == null) e = g.getEdge(ns, nt, 0);  // undirected
                    if (e == null) e = g.getEdge(ns, nt);  // default
                    if (e == null) e = g.getEdge(nt, ns, 1);
                    if (e == null) e = g.getEdge(nt, ns, 0);
                    if (e == null) e = g.getEdge(nt, ns);
                    if (e == null) return error("Edge not found: " + source + " -> " + target);
                    vc.centerOnEdge(e);
                    break;
                }
                case "region": {
                    if (x == null || y == null || w == null || h == null)
                        return error("Missing x/y/w/h for mode=region");
                    vc.centerOn(x.floatValue(), y.floatValue(), w.floatValue(), h.floatValue());
                    break;
                }
                default:
                    return error("Unknown mode: " + mode + " (use graph|zero|node|edge|region)");
            }
            Integer selectedCount = null;
            if (select != null) {
                int expected;
                if (select.isEmpty()) {
                    vc.resetSelection();
                    expected = 0;
                } else {
                    java.util.List<Node> nodes = new java.util.ArrayList<>();
                    for (String id : select) {
                        Node n = g.getNode(id);
                        if (n != null) nodes.add(n);
                    }
                    vc.selectNodes(nodes.toArray(new Node[0]));
                    expected = nodes.size();
                }
                // selectNodes()/resetSelection() apply asynchronously against the render
                // engine (queued, not synchronous with this call) — wait briefly for the
                // change to actually land instead of blindly echoing the request size, so
                // a caller (e.g. gephi_get_selection or a screenshot right after) sees it
                // too. Also correct for IDs that didn't resolve to a real node.
                selectedCount = waitForSelectionCount(vc, expected, 1000);
            }
            if (zoom != null) vc.setZoom(zoom.floatValue());
            JsonObject r = success("View focused (" + m + ")");
            r.addProperty("mode", m);
            if (selectedCount != null) r.addProperty("selected", selectedCount);
            return r;
        } catch (Exception e) {
            return error("Focus failed: " + e.getMessage());
        }
    }

    /**
     * Set the mouse selection mode on the graph canvas. "rectangle" enables the
     * box-drag selection the pointing feature (readSelection) reads, so a
     * teaching session can turn it on up front instead of asking the human to
     * click the toolbar icon. Uses the same VisualizationController focusView
     * already drives.
     */
    public JsonObject setSelectionMode(String mode) {
        org.gephi.visualization.api.VisualizationController vc =
            Lookup.getDefault().lookup(org.gephi.visualization.api.VisualizationController.class);
        if (vc == null) return error("No visualization available (headless or view not started)");
        String m = mode == null ? "rectangle" : mode.toLowerCase();
        try {
            switch (m) {
                case "rectangle":
                    vc.setRectangleSelection();
                    break;
                case "direct":
                    vc.setDirectMouseSelection();
                    break;
                case "disable":
                case "off":
                    vc.disableSelection();
                    break;
                default:
                    return error("Unknown selection mode: " + mode + " (use rectangle|direct|disable)");
            }
            JsonObject r = success("Selection mode set to " + m);
            r.addProperty("mode", m);
            return r;
        } catch (Exception e) {
            return error("Set selection mode failed: " + e.getMessage());
        }
    }

    /** List the perspectives (Overview / Data Laboratory / Preview) and the active one. */
    public JsonObject getPerspective() {
        org.gephi.perspective.api.PerspectiveController pc =
            Lookup.getDefault().lookup(org.gephi.perspective.api.PerspectiveController.class);
        if (pc == null) return error("No perspective controller (headless?)");
        try {
            org.gephi.perspective.spi.Perspective selected = pc.getSelectedPerspective();
            JsonObject r = success("Perspectives listed");
            r.addProperty("selected", selected == null ? null : selected.getName());
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            for (org.gephi.perspective.spi.Perspective p : pc.getPerspectives()) {
                JsonObject o = new JsonObject();
                o.addProperty("name", p.getName());
                o.addProperty("display_name", p.getDisplayName());
                o.addProperty("selected", p == selected);
                arr.add(o);
            }
            r.add("perspectives", arr);
            return r;
        } catch (Exception e) {
            return error("List perspectives failed: " + e.getMessage());
        }
    }

    /** Switch the active perspective (tab) by name or display name (case-insensitive). */
    public JsonObject switchPerspective(String name) {
        org.gephi.perspective.api.PerspectiveController pc =
            Lookup.getDefault().lookup(org.gephi.perspective.api.PerspectiveController.class);
        if (pc == null) return error("No perspective controller (headless?)");
        if (name == null) return error("Missing 'name'");
        org.gephi.perspective.spi.Perspective match = null;
        for (org.gephi.perspective.spi.Perspective p : pc.getPerspectives()) {
            if (name.equalsIgnoreCase(p.getName()) || name.equalsIgnoreCase(p.getDisplayName())) {
                match = p;
                break;
            }
        }
        if (match == null) return error("Perspective not found: " + name);
        final org.gephi.perspective.spi.Perspective target = match;
        // Switching the perspective mutates the NetBeans window system — do it on the EDT.
        return runOnEDT(() -> {
            pc.selectPerspective(target);
            JsonObject r = success("Switched to perspective: " + target.getDisplayName());
            r.addProperty("selected", target.getName());
            return r;
        });
    }

    // ─── Filters (Group C) ───────────────────────────────────────────

    /**
     * Every filter builder available, static and dynamic. Static builders
     * (DegreeRange, KCore, GiantComponent, Ego, …) come straight from Lookup;
     * per-column attribute builders (AttributeEqual/Range/NonNull on each
     * column) come from CategoryBuilder.getBuilders(workspace) and only exist
     * once a graph with columns is loaded.
     */
    private java.util.List<FilterBuilder> allFilterBuilders(Workspace ws) {
        java.util.List<FilterBuilder> out = new java.util.ArrayList<>();
        for (FilterBuilder b : Lookup.getDefault().lookupAll(FilterBuilder.class)) {
            out.add(b);
        }
        for (CategoryBuilder cb : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
            try {
                FilterBuilder[] bs = cb.getBuilders(ws);
                if (bs != null) java.util.Collections.addAll(out, bs);
            } catch (Exception ignore) { /* some category builders need a specific state */ }
        }
        return out;
    }

    /** Coerce a JSON value to a filter property's type; handles Range from a [lo, hi] pair. */
    static Object convertFilterProperty(Object val, Class<?> type) {
        if (val == null) return null;
        if (type == org.gephi.filters.api.Range.class) {
            java.util.List<?> pair = null;
            if (val instanceof java.util.List) pair = (java.util.List<?>) val;
            else if (val instanceof com.google.gson.JsonArray) {
                java.util.List<Object> l = new java.util.ArrayList<>();
                for (com.google.gson.JsonElement e : (com.google.gson.JsonArray) val) l.add(e.getAsDouble());
                pair = l;
            }
            if (pair == null || pair.size() != 2) return null;
            double loD = pair.get(0) instanceof Number ? ((Number) pair.get(0)).doubleValue() : Double.parseDouble(pair.get(0).toString());
            double hiD = pair.get(1) instanceof Number ? ((Number) pair.get(1)).doubleValue() : Double.parseDouble(pair.get(1).toString());
            // Range requires both bounds to be the SAME Number class. Use Integer when
            // both are whole (degree/count filters), Double otherwise (continuous columns).
            boolean whole = loD == Math.floor(loD) && hiD == Math.floor(hiD)
                && !Double.isInfinite(loD) && !Double.isInfinite(hiD);
            if (whole) return new org.gephi.filters.api.Range((int) loD, (int) hiD);
            return new org.gephi.filters.api.Range(loD, hiD);
        }
        return convertLayoutProperty(val, type);
    }

    public JsonObject listFilters() {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No workspace open");
        JsonArray arr = new JsonArray();
        for (FilterBuilder b : allFilterBuilders(ws)) {
            JsonObject o = new JsonObject();
            try { o.addProperty("name", b.getName()); } catch (Exception ignore) {}
            try { o.addProperty("category", b.getCategory() == null ? null : b.getCategory().getName()); } catch (Exception ignore) {}
            try { o.addProperty("description", b.getDescription()); } catch (Exception ignore) {}
            // Introspect the filter's settable properties so callers know what params to pass.
            try {
                Filter f = b.getFilter(ws);
                if (f != null && f.getProperties() != null) {
                    JsonArray props = new JsonArray();
                    for (FilterProperty p : f.getProperties()) {
                        JsonObject po = new JsonObject();
                        po.addProperty("name", p.getName());
                        po.addProperty("type", p.getValueType() == null ? null : p.getValueType().getSimpleName());
                        props.add(po);
                    }
                    o.add("properties", props);
                }
            } catch (Exception ignore) { /* introspection best-effort */ }
            arr.add(o);
        }
        JsonObject r = success("Filters listed");
        r.add("filters", arr);
        return r;
    }

    public JsonObject applyFilter(String name, Map<String, Object> params, String action, String column) {
        FilterController fc = Lookup.getDefault().lookup(FilterController.class);
        if (fc == null) return error("No filter controller available");
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No workspace open");
        GraphModel gm = currentGraphModel();
        if (gm == null) return error("No workspace open");
        if (name == null) return error("Missing 'name'");

        FilterBuilder builder = null;
        for (FilterBuilder b : allFilterBuilders(ws)) {
            try { if (name.equalsIgnoreCase(b.getName())) { builder = b; break; } } catch (Exception ignore) {}
        }
        if (builder == null) return error("Filter not found: " + name + " (call /filter/list to see available filters)");

        Filter filter = builder.getFilter(ws);
        if (filter == null) return error("Filter builder produced no filter: " + name);

        // Set each named property; report the valid names if a param doesn't match.
        FilterProperty[] props = filter.getProperties();
        if (params != null && !params.isEmpty()) {
            java.util.List<String> propNames = new java.util.ArrayList<>();
            if (props != null) for (FilterProperty p : props) propNames.add(p.getName());
            for (Map.Entry<String, Object> e : params.entrySet()) {
                FilterProperty match = null;
                if (props != null) {
                    for (FilterProperty p : props) {
                        if (e.getKey().equalsIgnoreCase(p.getName())) { match = p; break; }
                    }
                }
                if (match == null) {
                    return error("Unknown filter property '" + e.getKey() + "' for " + name
                        + " — valid properties: " + propNames);
                }
                Object converted = convertFilterProperty(e.getValue(), match.getValueType());
                if (converted == null) {
                    return error("Could not coerce '" + e.getKey() + "' to " + match.getValueType().getSimpleName()
                        + " (Range wants a [lo, hi] pair)");
                }
                try { match.setValue(converted); }
                catch (Exception ex) { return error("Failed to set '" + e.getKey() + "': " + ex.getMessage()); }
            }
        }

        int nodesBefore = gm.getGraphVisible().getNodeCount();
        int edgesBefore = gm.getGraphVisible().getEdgeCount();

        // Validate the action BEFORE touching the filter model. Adding the query first
        // meant an unknown action returned an error having already changed the visible
        // graph, so a caller that trusted the error saw a silently filtered graph.
        String act = action == null ? "select" : action.toLowerCase();
        switch (act) {
            case "select":
            case "visible":
            case "new_workspace":
                break;
            case "column":
                if (column == null) return error("action=column requires a 'column' name");
                break;
            default:
                return error("Unknown action: " + action + " (use select|new_workspace|column)");
        }

        Query query = fc.createQuery(filter);
        fc.add(query);

        // All three FilterController operations below end in Gephi's own BLOCKING
        // writeLock() (filterVisible via setVisibleView; the two exports process the
        // query through the same path). Hold our deadlock-safe lock first so those
        // calls re-enter instead of queuing behind the renderer — exactly the
        // mitigation resetFilters uses, and these run on a NanoHTTPD thread too.
        Graph lockGraph = gm.getGraph();
        JsonObject r;
        switch (act) {
            case "select":
            case "visible":
                lockWrite(lockGraph);
                try {
                    fc.filterVisible(query);
                } finally { unlockWrite(lockGraph); }
                r = success("Filter applied to the visible graph");
                r.addProperty("nodes_before", nodesBefore);
                r.addProperty("edges_before", edgesBefore);
                // filterVisible ends in setVisibleView, which does not finish swapping the
                // view before it returns. Reading the counts straight away reported the
                // pre-filter numbers, telling the caller the filter removed nothing when it
                // had removed half the graph. Wait briefly for the view to settle, and say
                // so rather than publishing a number that has not stopped moving.
                boolean settled = awaitVisibleViewSettled(gm, nodesBefore);
                r.addProperty("nodes_after", gm.getGraphVisible().getNodeCount());
                r.addProperty("edges_after", gm.getGraphVisible().getEdgeCount());
                if (!settled) r.addProperty("counts_settled", false);
                break;
            case "new_workspace":
                // Materializes the filtered subgraph into a fresh workspace — the
                // memory-safe way to filter repeatedly (hidden GraphView elements
                // otherwise stay resident).
                lockWrite(lockGraph);
                try {
                    fc.exportToNewWorkspace(query);
                } finally { unlockWrite(lockGraph); }
                r = success("Filtered subgraph exported to a new workspace");
                break;
            case "column":
                if (column == null) return error("action=column requires a 'column' name");
                lockWrite(lockGraph);
                try {
                    fc.exportToColumn(column, query);
                } finally { unlockWrite(lockGraph); }
                r = success("Filter membership written to boolean column: " + column);
                r.addProperty("column", column);
                break;
            default:
                return error("Unknown action: " + action + " (use select|new_workspace|column)");
        }
        try { r.addProperty("filter", builder.getName()); } catch (Exception ignore) {}
        return r;
    }

    /**
     * Waits briefly for the visible view to stop changing after a filter is applied.
     * Returns true once two consecutive reads agree (and, when the filter actually
     * removed something, once the count has moved off its pre-filter value); false if
     * it was still moving when the budget ran out, so the caller can say the number is
     * provisional instead of presenting a moving value as final.
     */
    private static boolean awaitVisibleViewSettled(GraphModel gm, int before) {
        final long budgetMs = 1500;
        final long deadline = System.currentTimeMillis() + budgetMs;
        int last = -1;
        boolean moved = false;
        while (System.currentTimeMillis() < deadline) {
            int now = gm.getGraphVisible().getNodeCount();
            if (now != before) moved = true;
            if (now == last && (moved || now != before)) return true;
            last = now;
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        // A filter that legitimately keeps every node never moves off `before`; treat a
        // stable reading as settled rather than reporting it as provisional forever.
        return gm.getGraphVisible().getNodeCount() == last;
    }

    // ─── Data Laboratory (Group D) ───────────────────────────────────

    private static Table tableFor(GraphModel gm, String target) {
        return "edge".equalsIgnoreCase(target) ? gm.getEdgeTable() : gm.getNodeTable();
    }

    private static org.gephi.graph.api.Element[] elementsFor(GraphModel gm, String target) {
        Graph g = gm.getGraph();
        return "edge".equalsIgnoreCase(target) ? g.getEdges().toArray() : g.getNodes().toArray();
    }

    /**
     * Value -> count over one column. Pure GraphModel logic (no datalab
     * controller / running Gephi needed), so it is unit-testable against an
     * in-memory model.
     */
    static JsonObject columnValueFrequenciesCore(GraphModel gm, String target, String columnId) {
        Table table = tableFor(gm, target);
        Column col = table.getColumn(columnId);
        if (col == null) return error("Column not found: " + columnId);
        java.util.LinkedHashMap<String, Integer> freq = new java.util.LinkedHashMap<>();
        int total = 0;
        for (org.gephi.graph.api.Element el : elementsFor(gm, target)) {
            Object v = el.getAttribute(col);
            String key = v == null ? "" : v.toString();
            freq.merge(key, 1, Integer::sum);
            total++;
        }
        JsonObject r = success("Column value frequencies computed");
        r.addProperty("column", columnId);
        r.addProperty("target", "edge".equalsIgnoreCase(target) ? "edge" : "node");
        r.addProperty("total", total);
        r.addProperty("distinct_values", freq.size());
        JsonObject f = new JsonObject();
        for (Map.Entry<String, Integer> e : freq.entrySet()) f.addProperty(e.getKey(), e.getValue());
        r.add("frequencies", f);
        return r;
    }

    /**
     * Groups of elements that share a value in one column (size >= 2). Pure
     * GraphModel logic, unit-testable. caseSensitive controls string matching.
     */
    static JsonObject detectDuplicatesCore(GraphModel gm, String target, String columnId, boolean caseSensitive) {
        Table table = tableFor(gm, target);
        Column col = table.getColumn(columnId);
        if (col == null) return error("Column not found: " + columnId);
        java.util.LinkedHashMap<String, java.util.List<String>> groups = new java.util.LinkedHashMap<>();
        for (org.gephi.graph.api.Element el : elementsFor(gm, target)) {
            Object v = el.getAttribute(col);
            if (v == null) continue;
            String key = v.toString();
            if (!caseSensitive) key = key.toLowerCase();
            groups.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(String.valueOf(el.getId()));
        }
        JsonArray dupes = new JsonArray();
        int groupCount = 0;
        for (java.util.List<String> ids : groups.values()) {
            if (ids.size() >= 2) {
                groupCount++;
                JsonArray a = new JsonArray();
                for (String id : ids) a.add(id);
                dupes.add(a);
            }
        }
        JsonObject r = success("Duplicate detection complete");
        r.addProperty("column", columnId);
        r.addProperty("group_count", groupCount);
        r.add("duplicate_groups", dupes);
        return r;
    }

    public JsonObject columnValueFrequencies(String target, String columnId) {
        GraphModel gm = currentGraphModel();
        if (gm == null) return error("No workspace open");
        if (columnId == null) return error("Missing 'column'");
        return columnValueFrequenciesCore(gm, target, columnId);
    }

    public JsonObject detectDuplicates(String target, String columnId, boolean caseSensitive) {
        GraphModel gm = currentGraphModel();
        if (gm == null) return error("No workspace open");
        if (columnId == null) return error("Missing 'column'");
        return detectDuplicatesCore(gm, target, columnId, caseSensitive);
    }

    /** Merge several nodes into one, reassigning edges; deletes the merged-away nodes. */
    public JsonObject mergeNodes(java.util.List<String> ids, String intoId) {
        GraphModel gm = currentGraphModel();
        if (gm == null) return error("No workspace open");
        if (ids == null || ids.isEmpty()) return error("Missing 'ids'");
        org.gephi.datalab.api.GraphElementsController gec =
            Lookup.getDefault().lookup(org.gephi.datalab.api.GraphElementsController.class);
        if (gec == null) return error("No datalab controller available");
        Graph g = gm.getGraph();
        java.util.List<Node> nodes = new java.util.ArrayList<>();
        for (String id : ids) {
            Node n = g.getNode(id);
            if (n == null) return error("Node not found: " + id);
            nodes.add(n);
        }
        Node into = intoId != null ? g.getNode(intoId) : nodes.get(0);
        if (into == null) return error("Merge target node not found: " + intoId);
        try {
            // Empty column/strategy arrays: reassign edges and keep the `into` node's
            // own attribute values (no per-column value merge). Passing null throws
            // an NPE inside the controller (it reads columns.length).
            Node result = gec.mergeNodes(g, nodes.toArray(new Node[0]), into,
                new Column[0], new org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy[0], true);
            JsonObject r = success("Merged " + nodes.size() + " nodes");
            r.addProperty("into", result != null ? String.valueOf(result.getId()) : String.valueOf(into.getId()));
            r.addProperty("merged_count", nodes.size());
            return r;
        } catch (Exception e) {
            return error("Merge failed: " + e.getMessage());
        }
    }

    // ─── Edge appearance + generic export (Group E) ──────────────────

    /**
     * Color edges by an edge-column partition (relationship type, time period,
     * weight tier, …) — the edge twin of colorByPartition. Mirrors it exactly:
     * per-value palette (supplied or auto), then edge.setColor per row.
     */
    public JsonObject colorEdgesByPartition(String columnName, Map<String, int[]> colorMap) {
        Workspace ws = currentWorkspace();
        if (ws == null) return error("No project open");
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = gm.getEdgeTable().getColumn(columnName);
            if (col == null) return error("Edge column not found: " + columnName);

            java.util.Map<String, Color> palette = new java.util.LinkedHashMap<>();
            if (colorMap != null && !colorMap.isEmpty()) {
                for (Map.Entry<String, int[]> e : colorMap.entrySet()) {
                    int[] c = e.getValue();
                    palette.put(e.getKey(), new Color(c[0], c[1], c[2]));
                }
            } else {
                java.util.Set<String> values = new java.util.LinkedHashSet<>();
                for (Edge ed : graph.getEdges().toArray()) {
                    Object v = ed.getAttribute(col);
                    if (v != null) values.add(v.toString());
                }
                Color[] defaultPalette = {
                    new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
                    new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75),
                    new Color(227, 119, 194), new Color(127, 127, 127), new Color(188, 189, 34),
                    new Color(23, 190, 207), new Color(174, 199, 232), new Color(255, 187, 120)
                };
                int idx = 0;
                for (String v : values) { palette.put(v, defaultPalette[idx % defaultPalette.length]); idx++; }
            }

            int colored = 0;
            lockWrite(graph);
            try {
                for (Edge ed : graph.getEdges().toArray()) {
                    Object v = ed.getAttribute(col);
                    if (v != null) {
                        Color c = palette.get(v.toString());
                        if (c != null) { ed.setColor(c); colored++; }
                    }
                }
            } finally { unlockWrite(graph); }
            JsonObject r = success("Colored " + colored + " edges by " + columnName);
            r.addProperty("partitions", palette.size());
            return r;
        } catch (Exception e) { return error("Failed: " + e.getMessage()); }
    }

    /**
     * Export the graph in any format the ExportController knows by name — vna,
     * pajek, dl, spreadsheet, gdf, gml, json, gexf, graphml, csv — for
     * interchange with UCINET and other SNA tools, or a spreadsheet for
     * non-technical readers. The wrapped-today formats (gexf/graphml/csv) keep
     * their dedicated tools; this is the passthrough for the rest.
     */
    public JsonObject exportByFormat(String filePath, String format) {
        return exportByFormat(filePath, format, true);
    }

    /** @param visible see exportGexf — same contract, response self-declares the view. */
    public JsonObject exportByFormat(String filePath, String format, boolean visible) {
        return runOnEDT(() -> {
            Workspace ws = currentWorkspace();
            if (ws == null) return error("No project open");
            if (filePath == null || format == null) return error("Missing 'file' or 'format'");
            try {
                ExportController ec = Lookup.getDefault().lookup(ExportController.class);
                Exporter exporter = ec.getExporter(format);
                if (exporter == null) return error("No exporter for format: " + format
                    + " (try vna, pajek, dl, spreadsheet, gdf, gml, json, gexf, graphml, csv)");
                if (exporter instanceof GraphExporter) {
                    ((GraphExporter) exporter).setExportVisible(visible);
                    ((GraphExporter) exporter).setWorkspace(ws);
                }
                ec.exportFile(new File(filePath), exporter);
                JsonObject r = success("Exported to " + filePath);
                r.addProperty("format", format);
                addViewInfo(r, currentGraphModel(), visible);
                return r;
            } catch (Exception e) { return error("Export failed: " + e.getMessage()); }
        });
    }

    // ─── Timeline / dynamic (Group G) ────────────────────────────────

    /**
     * Report the graph's dynamic/timeline state. Doubles as the spike for the
     * reported "Timeline doesn't recognize dynamic attributes after a
     * programmatic import" bug: if graph_is_dynamic is true but
     * dynamic_columns is empty, the bug reproduces on this Gephi.
     */
    public JsonObject getTimeline() {
        GraphModel gm = currentGraphModel();
        if (gm == null) return error("No workspace open");
        JsonObject r = success("Timeline state");
        try {
            r.addProperty("graph_is_dynamic", gm.isDynamic());
            org.gephi.graph.api.Interval b = gm.getTimeBounds();
            if (b != null) {
                r.addProperty("time_min", b.getLow());
                r.addProperty("time_max", b.getHigh());
            }
            r.addProperty("time_format", String.valueOf(gm.getTimeFormat()));
        } catch (Exception e) { r.addProperty("bounds_error", e.getMessage()); }
        org.gephi.timeline.api.TimelineController tc =
            Lookup.getDefault().lookup(org.gephi.timeline.api.TimelineController.class);
        if (tc != null) {
            try {
                JsonArray cols = new JsonArray();
                String[] dc = tc.getDynamicGraphColumns();
                if (dc != null) for (String c : dc) cols.add(c);
                r.add("dynamic_columns", cols);
                org.gephi.timeline.api.TimelineModel tm = tc.getModel();
                if (tm != null) {
                    r.addProperty("timeline_enabled", tm.isEnabled());
                    r.addProperty("has_valid_bounds", tm.hasValidBounds());
                    if (tm.hasValidBounds()) {
                        r.addProperty("interval_start", tm.getIntervalStart());
                        r.addProperty("interval_end", tm.getIntervalEnd());
                    }
                }
            } catch (Exception e) { r.addProperty("timeline_error", e.getMessage()); }
        } else {
            r.addProperty("timeline_controller", "unavailable");
        }
        return r;
    }

    // REMOVED: setTimeWindow. Driving Gephi's timeline from outside wedges the
    // EDT two different ways — a time-derived setVisibleView deadlocks the
    // renderer, and even setInterval/setEnabled saturates the EDT after one call.
    // Because Gephi's own shutdown runs on the EDT, a wedged timeline op makes
    // the app impossible to quit normally (Force Quit only). getTimeline
    // (read-only, above) is safe and kept; any future write path must go through
    // the viz-engine render-pause and off the EDT before it can be revived.

    /** Create a boolean column flagging rows whose column value matches a regex. */
    public JsonObject createRegexColumn(String target, String columnId, String newColumnTitle, String regex) {
        GraphModel gm = currentGraphModel();
        if (gm == null) return error("No workspace open");
        if (columnId == null || regex == null || newColumnTitle == null)
            return error("Missing 'column', 'regex', or 'new_column'");
        org.gephi.datalab.api.AttributeColumnsController acc =
            Lookup.getDefault().lookup(org.gephi.datalab.api.AttributeColumnsController.class);
        if (acc == null) return error("No datalab controller available");
        Table table = tableFor(gm, target);
        Column col = table.getColumn(columnId);
        if (col == null) return error("Column not found: " + columnId);
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            Column created = acc.createBooleanMatchesColumn(table, col, newColumnTitle, pattern);
            JsonObject r = success("Created boolean match column: " + newColumnTitle);
            r.addProperty("column", created != null ? created.getId() : newColumnTitle);
            return r;
        } catch (java.util.regex.PatternSyntaxException e) {
            return error("Invalid regex: " + e.getMessage());
        } catch (Exception e) {
            return error("Create match column failed: " + e.getMessage());
        }
    }

}
