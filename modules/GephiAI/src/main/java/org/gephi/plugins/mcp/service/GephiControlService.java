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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.spi.Processor;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.Lookup;

public class GephiControlService {

    private static final Logger LOGGER = Logger.getLogger(GephiControlService.class.getName());
    private static GephiControlService instance;

    // Config staged by setLayoutProperties (configure-only); the next runLayout of
    // the same algorithm applies it. Lets set-then-run work without setLayoutProperties
    // itself starting a layout.
    private volatile Map<String, Object> pendingLayoutProps = null;
    private volatile String pendingLayoutAlgo = null;
    /** The last layout started: algorithm, iterations and the settings read back, for get_layout_status. */
    private volatile JsonObject lastLayoutRun = null;

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

    private GephiControlService() {
    }

    public static synchronized GephiControlService getInstance() {
        if (instance == null) {
            instance = new GephiControlService();
        }
        return instance;
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private ProjectController getProjectController() {
        return Lookup.getDefault().lookup(ProjectController.class);
    }

    private GraphController getGraphController() {
        return Lookup.getDefault().lookup(GraphController.class);
    }

    /**
     * Project and workspace changes (new project, open, close, new, switch, delete, duplicate,
     * rename) run on the calling HTTP thread, as Gephi's own interface runs them on a background
     * thread: made on the interface thread they freeze it while listeners run, and Gephi warns
     * that this will become an error.
     */
    private static <T> T onProjectThread(Callable<T> work) {
        try {
            return work.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T runOnEDT(Callable<T> callable) {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // Bounded wait: invokeAndWait parks forever when the EDT is wedged (the
        // "health answers but nothing else does" symptom). Fail fast with guidance
        // instead of hanging until the client's timeout.
        final Object[] result = new Object[1];
        final Exception[] exception = new Exception[1];
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                result[0] = callable.call();
            } catch (Exception e) {
                exception[0] = e;
            } finally {
                done.countDown();
            }
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
        if (exception[0] != null) {
            throw new RuntimeException(exception[0]);
        }
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

    /**
     * A request that failed with an exception: logs it with its stack trace, so a failure inside
     * Gephi leaves a trace that can be reported, and returns the error to the caller.
     */
    static JsonObject failure(String prefix, Exception e) {
        LOGGER.log(Level.WARNING, prefix + e.getMessage(), e);
        return error(prefix + e.getMessage());
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
     * Acquire the graph write lock with a bounded wait instead of the blocking writeLock().
     * If any thread leaves a read hold behind (an auto-locked iterator abandoned before it
     * finished, in this plugin or in Gephi, releases its hold only on exhaustion), a blocking
     * writeLock() waits forever and every reader queued behind it waits too, with no holder
     * left for a thread dump to show. Short timed tryLock() attempts, retried for up to ~15s,
     * turn that into a "graph busy" error the caller can report. GraphLock offers a timed
     * acquisition for reads only (a queued timed writer would stall new readers), so the
     * underlying WriteLock is reached by reflection; if it cannot be, this falls back to the
     * blocking lock. Once held, any Gephi-internal
     * writeLock() on this same thread (setVisibleView, etc.) re-enters for free, which is why
     * callers wrap those calls too.
     */
    static void lockWrite(Graph g) {
        try {
            java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock wl = writeLockHandle(g);
            if (wl == null) {
                g.writeLock();
                return;
            }
            long deadline = System.nanoTime() + 15_000_000_000L;
            while (!wl.tryLock(120, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                if (System.nanoTime() > deadline) {
                    throw new RuntimeException(
                        "Graph is busy (another task, such as a running statistic, holds the lock); please retry ("
                            + describeLock(g) + ")");
                }
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring the write lock");
        } catch (RuntimeException e) {
            throw e;  // the busy message above, which the caller shows as it is
        } catch (Throwable t) {
            // Any other failure here (e.g. a classloading Error, which is not an Exception
            // and would otherwise skip every catch(Exception) up the call chain and kill the
            // HTTP connection with no response) must still surface as a normal API error.
            throw new RuntimeException("Could not acquire write lock: " + t, t);
        }
    }

    /** Release the write lock taken by lockWrite. */
    static void unlockWrite(Graph g) {
        g.writeUnlock();
    }

    /*
     * ITERATION RULE (wedge prevention): never iterate a live NodeIterable /
     * EdgeIterable directly — always iterate .toArray(). A live iterator
     * auto-acquires the graph read lock in its constructor and releases it only
     * on exhaustion or doBreak(); an early break, return, or exception leaks the
     * hold, and since nothing else ever unlocks on that thread's behalf, the leak is
     * permanent and wedges every future write (found the hard way; see
     * GraphOpsTest#earlyBreakOverToArraySnapshotLeavesNoReadHold).
     */

    /**
     * Timed read-lock acquisition. Plain readLock() parks unboundedly in the lock's
     * wait queue; when a writer is already parked (Gephi's own blocking writeLock())
     * every new reader queues behind it and the request hangs until the client's
     * timeout, the chronic "health answers but nothing else does" symptom. A timed
     * tryReadLock turns that into an immediate, actionable error instead.
     */
    static void lockRead(Graph g) {
        lockRead(g, 10_000_000_000L);
    }

    /** lockRead with its overall wait, in nanoseconds; package-private for tests. */
    static void lockRead(Graph g, long timeoutNanos) {
        org.gephi.graph.api.GraphLock lock = g.getLock();
        long deadline = System.nanoTime() + timeoutNanos;
        try {
            while (!lock.tryReadLock(120, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                if (System.nanoTime() > deadline) {
                    throw new RuntimeException(
                        "Graph is busy (lock unavailable): if this persists, Gephi is wedged; fully quit and "
                            + "reopen it (" + describeLock(g) + ")");
                }
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring the read lock");
        }
    }

    /**
     * The graph lock's live state for a "busy" message, so a leaked read hold (read holds while
     * nothing runs) can be told from a long statistic. Never throws.
     */
    static String describeLock(Graph g) {
        try {
            org.gephi.graph.api.GraphLock lock = g.getLock();
            return lock.getReadLockCount() + " read holds, write lock "
                + (lock.isWriteLocked() ? "held" : "free") + ", " + lock.getQueueLength() + " threads waiting";
        } catch (Throwable t) {
            return "lock state unavailable";
        }
    }

    /** The underlying ReentrantReadWriteLock.WriteLock behind Graph.getLock(), or null if unreachable. */
    static java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock writeLockHandle(Graph g) {
        try {
            org.gephi.graph.api.GraphLock lock = g.getLock();
            if (lock == null) {
                return null;
            }
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
        if (e == null) {
            e = g.getEdge(source, target, 0);  // undirected
        }
        if (e == null) {
            e = g.getEdge(source, target);  // default
        }
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
        if (idx < 0) {
            return null;
        }
        Layout layout = builders.get(idx).buildLayout();
        if (layout == null) {
            return null;
        }
        // Separate failure paths: a missing graph model must not skip the reset, which is
        // the part that actually keeps OpenOrd and Yifan Hu from running on zeros.
        try {
            GraphModel gm = currentGraphModel();
            if (gm != null) {
                layout.setGraphModel(gm);
            }
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
        if (query == null) {
            return -1;
        }
        String q = query.toLowerCase().trim();
        String qns = q.replace(" ", "");
        if (qns.isEmpty()) {
            return -1;
        }
        int substr = -1;
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name == null) {
                continue;
            }
            String n = name.toLowerCase();
            String nns = n.replace(" ", "");
            if (n.equals(q) || nns.equals(qns)) {
                return i;
            }
            if (substr == -1 && (n.contains(q) || nns.contains(qns))) {
                substr = i;
            }
        }
        return substr;
    }

    // ─── Project Management ──────────────────────────────────────────

    /**
     * Empties Gephi's Filters panel before its project closes. Closed off the interface
     * thread, the project's filters are gone while the panel may still be drawing their
     * entries, which fails inside Gephi; with the list emptied first there is nothing to draw.
     * The project is being discarded, so nothing is lost.
     */
    private void clearFilterQueriesBeforeClosing() {
        ProjectController pc = getProjectController();
        if (!pc.hasCurrentProject()) {
            return;
        }
        try {
            runOnEDT(() -> {
                FilterController fc = Lookup.getDefault().lookup(FilterController.class);
                org.gephi.filters.api.FilterModel fm = fc == null ? null : fc.getModel();
                if (fm != null) {
                    for (Query q : fm.getQueries()) {
                        fc.remove(q);
                    }
                }
                return null;
            });
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not clear the filter list before closing the project", e);
        }
    }

    public JsonObject createProject(String name) {
        clearFilterQueriesBeforeClosing();
        return onProjectThread(() -> {
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
        if (!file.exists()) {
            return error("File not found: " + filePath);
        }
        try {
            ProjectController pc = getProjectController();
            // Close any open project FIRST. Opening a .gephi on top of an existing
            // project lands in a broken half-state where the graphstore never
            // deserializes into a queryable model — the "open reports success but the
            // graph is blank" bug. Verified: open works as the first action on a fresh
            // instance and fails only when a project is already open; Gephi's own
            // File>Open closes first.
            if (pc.hasCurrentProject()) {
                clearFilterQueriesBeforeClosing();
                onProjectThread(() -> {
                    pc.closeCurrentProject();
                    return null;
                });
            }
            // openProject(File) off the EDT: it blocks on a LongTaskExecutor Future
            // whose completion needs a free EDT.
            pc.openProject(file);
        } catch (Exception e) {
            return failure("Failed to open project: ", e);
        }
        // Report the actual loaded counts so an empty result is never a silent success.
        return runOnEDT(() -> {
            JsonObject r = success("Project opened");
            Workspace cur = getProjectController().getCurrentWorkspace();
            int nodes = 0;
            int edges = 0;
            if (cur != null) {
                Graph g = getGraphController().getGraphModel(cur).getGraph();
                nodes = g.getNodeCount();
                edges = g.getEdgeCount();
            }
            r.addProperty("node_count", nodes);
            r.addProperty("edge_count", edges);
            if (nodes == 0) {
                r.addProperty("warning", "opened but no nodes are in the current workspace");
            }
            return r;
        });
    }

    /**
     * Saves the project and reports success only once the file is on disk. Gephi writes the
     * file on the calling thread, so this runs off the interface thread to keep Gephi
     * responsive; a save that failed or was cancelled leaves no new file, and says so.
     */
    public JsonObject saveProject(String filePath) {
        try {
            ProjectController pc = getProjectController();
            Project project = pc.getCurrentProject();
            if (project == null) {
                return error("No project open");
            }
            File file = outputFile(filePath);
            // Checked first: a save that fails inside Gephi reports it in a dialog, which leaves
            // this call waiting until someone closes it.
            File dir = file.getParentFile();
            if (dir == null || !dir.isDirectory()) {
                return error("The folder " + dir + " does not exist and could not be created");
            }
            if (!dir.canWrite() || (file.exists() && !file.canWrite())) {
                return error("Gephi cannot write to " + file.getPath());
            }
            long before = file.isFile() ? file.lastModified() : -1;
            long started = System.currentTimeMillis();
            pc.saveProject(project, file);
            String problem = savedFileProblem(file, before, started);
            if (problem != null) {
                return error(problem);
            }
            JsonObject r = success("Project saved");
            r.addProperty("file", file.getPath());
            r.addProperty("bytes", file.length());
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /** Null when {@code file} holds a fresh save, else what went wrong. */
    static String savedFileProblem(File file, long modifiedBefore, long started) {
        String hint = " Gephi may have shown an error; check that the folder exists and is writable.";
        if (!file.isFile()) {
            return "The project was not saved: " + file.getPath() + " was not written." + hint;
        }
        if (file.length() == 0) {
            return "The project was not saved: " + file.getPath() + " is empty." + hint;
        }
        // File times can be as coarse as two seconds, so only a file older than that counts as unchanged.
        if (modifiedBefore >= 0 && file.lastModified() == modifiedBefore && modifiedBefore < started - 2000) {
            return "The project was not saved: " + file.getPath() + " is unchanged." + hint;
        }
        return null;
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
        return onProjectThread(() -> {
            try {
                ProjectController pc = getProjectController();
                if (pc.getCurrentProject() == null) {
                    return error("No project open");
                }
                Workspace ws = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(ws);
                JsonObject r = success("Workspace created");
                r.addProperty("workspace_id", ws.getId());
                return r;
            } catch (Exception e) {
                return failure("Failed: ", e);
            }
        });
    }

    public JsonObject listWorkspaces() {
        return runOnEDT(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) {
                return error("No project open");
            }
            JsonArray arr = new JsonArray();
            Workspace current = pc.getCurrentWorkspace();
            int index = 0;
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                JsonObject o = new JsonObject();
                // The position is what switch, delete, rename, duplicate and compare take; the Gephi
                // id is not, and the two diverge as soon as a workspace is deleted.
                o.addProperty("index", index++);
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
        return onProjectThread(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) {
                return error("No project open");
            }
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
        return onProjectThread(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) {
                return error("No project open");
            }
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
        return onProjectThread(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) {
                return error("No project open");
            }
            int i = 0;
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                if (i == index) {
                    try {
                        Workspace copy = pc.duplicateWorkspace(ws);
                        pc.openWorkspace(copy);
                        JsonObject r = success("Workspace duplicated");
                        r.addProperty("workspace_id", copy.getId());
                        return r;
                    } catch (Exception e) {
                        return failure("Failed: ", e);
                    }
                }
                i++;
            }
            return error("Workspace index out of range: " + index);
        });
    }

    public JsonObject renameWorkspace(int index, String name) {
        return onProjectThread(() -> {
            ProjectController pc = getProjectController();
            if (pc.getCurrentProject() == null) {
                return error("No project open");
            }
            int i = 0;
            for (Workspace ws : pc.getCurrentProject().getWorkspaces()) {
                if (i == index) {
                    try {
                        pc.renameWorkspace(ws, name);
                        return success("Workspace renamed to: " + name);
                    } catch (Exception e) {
                        return failure("Failed: ", e);
                    }
                }
                i++;
            }
            return error("Workspace index out of range: " + index);
        });
    }

    // ─── Node Operations ─────────────────────────────────────────────

    public JsonObject addNode(String id, String label, Map<String, Object> attrs) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        return addNodeToModel(getGraphController().getGraphModel(ws), id, label, attrs);
    }

    /**
     * The node with this id, or null. Ids arrive as text, but a graph imported with integer or
     * long ids keys its nodes by number, so when the text itself finds nothing it is read as the
     * graph's id type and looked up again.
     */
    static Node nodeById(Graph g, String id) {
        if (id == null) {
            return null;
        }
        Node n = g.getNode(id);
        if (n != null) {
            return n;
        }
        Object typed = typedNodeId(g.getModel().getConfiguration().getNodeIdType(), id);
        return typed == null ? null : g.getNode(typed);
    }

    /** The id to create a node with: the text as the graph's id type when the graph keys by number. */
    static Object nodeIdFor(GraphModel gm, String id) {
        Object typed = typedNodeId(gm.getConfiguration().getNodeIdType(), id);
        return typed != null ? typed : id;
    }

    /** Text read as a numeric id type, or null when the type is not numeric or the text not a number. */
    static Object typedNodeId(Class<?> type, String text) {
        try {
            if (type == Integer.class) {
                return Integer.valueOf(text.trim());
            }
            if (type == Long.class) {
                return Long.valueOf(text.trim());
            }
            if (type == Short.class) {
                return Short.valueOf(text.trim());
            }
            if (type == Byte.class) {
                return Byte.valueOf(text.trim());
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /**
     * Core node-add against an explicit model. Package-private + static so it is testable with a standalone
     * GraphModel.
     */
    static JsonObject addNodeToModel(GraphModel gm, String id, String label, Map<String, Object> attrs) {
        try {
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                if (nodeById(g, id) != null) {
                    return error("Node exists: " + id);
                }
                Node n = gm.factory().newNode(nodeIdFor(gm, id));
                n.setLabel(label != null ? label : id);
                n.setX((float) (Math.random() * 1000 - 500));
                n.setY((float) (Math.random() * 1000 - 500));
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
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject addNodes(List<Map<String, Object>> nodes) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        return addNodesToModel(getGraphController().getGraphModel(ws), nodes);
    }

    /** Core batch node-add against an explicit model (applies per-node attributes). */
    static JsonObject addNodesToModel(GraphModel gm, List<Map<String, Object>> nodes) {
        try {
            Graph g = gm.getGraph();
            int added = 0;
            int skipped = 0;
            lockWrite(g);
            try {
                for (Map<String, Object> nd : nodes) {
                    String id = (String) nd.get("id");
                    if (id == null || nodeById(g, id) != null) {
                        skipped++;
                        continue;
                    }
                    String label = (String) nd.getOrDefault("label", id);
                    Node n = gm.factory().newNode(nodeIdFor(gm, id));
                    n.setLabel(label);
                    n.setX((float) (Math.random() * 1000 - 500));
                    n.setY((float) (Math.random() * 1000 - 500));
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
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject removeNode(String id) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = getGraphController().getGraphModel(ws).getGraph();
            lockWrite(g);
            try {
                Node n = nodeById(g, id);
                if (n == null) {
                    return error("Node not found: " + id);
                }
                int edgesRemoved = g.getDegree(n);
                g.removeNode(n);
                JsonObject r = success("Node removed");
                r.addProperty("edges_removed", edgesRemoved);
                return r;
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject bulkRemoveNodes(List<String> ids) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = getGraphController().getGraphModel(ws).getGraph();
            lockWrite(g);
            try {
                int removed = 0;
                int notFound = 0;
                for (String id : ids) {
                    Node n = nodeById(g, id);
                    if (n == null) {
                        notFound++;
                        continue;
                    }
                    g.removeNode(n);
                    removed++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("removed", removed);
                r.addProperty("not_found", notFound);
                return r;
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject queryNodes(String attr, String val, int limit, int offset) {
        return queryNodes(attr, val, limit, offset, false);
    }

    public JsonObject queryNodes(String attr, String val, int limit, int offset, boolean visible) {
        return queryNodes(attr, val, null, null, null, limit, offset, visible);
    }

    /**
     * Lists nodes, optionally only those whose {@code column} matches a value search (see
     * nodeMatcher); {@code matches} then counts every match, not just the page returned.
     *
     * @param visible read the filtered visible graph instead of the full graph (see addViewInfo).
     */
    public JsonObject queryNodes(String column, String value, String contains, Double min, Double max,
        int limit, int offset, boolean visible) {
        return queryNodes(column, value, contains, min, max, limit, offset, visible, null, true, null);
    }

    /**
     * As above, and with {@code sortBy} (a column id or title, or "degree") the matching nodes
     * are ordered before paging, largest first unless {@code descending} is false. With
     * {@code columns} (comma-separated ids or titles) each node carries only those attributes
     * plus its id, label and degree, which keeps a long listing small.
     */
    public JsonObject queryNodes(String column, String value, String contains, Double min, Double max,
        int limit, int offset, boolean visible, String sortBy,
        boolean descending, String columns) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = visible ? gm.getGraphVisible() : gm.getGraph();
            java.util.function.Predicate<Node> keep = null;
            if (value != null || contains != null || min != null || max != null) {
                if (column == null) {
                    return error("Name the column to search with 'column'");
                }
                Column col = findColumn(gm.getNodeTable(), column);
                if (col == null) {
                    return error("Column not found: " + column);
                }
                keep = nodeMatcher(col, value, contains, min, max);
            }
            Column sortCol = null;
            if (sortBy != null && !"degree".equalsIgnoreCase(sortBy)) {
                sortCol = findColumn(gm.getNodeTable(), sortBy);
                if (sortCol == null) {
                    return error("Column not found for sort_by: " + sortBy);
                }
            }
            java.util.Set<String> wanted = wantedColumns(columns);
            lockRead(g);
            try {
                JsonArray arr = new JsonArray();
                int count = 0;
                int skip = 0;
                int matches = 0;
                // toArray, not the live iterable: breaking out of an auto-locked
                // iterator before exhaustion leaks its read hold permanently.
                List<Node> nodes = java.util.Arrays.asList(g.getNodes().toArray());
                if (sortBy != null) {
                    final Column sc = sortCol;
                    final Graph sg = g;
                    nodes = sortByValue(nodes, n -> sc == null ? (Object) sg.getDegree(n) : n.getAttribute(sc),
                        descending);
                }
                for (Node n : nodes) {
                    if (keep != null) {
                        if (!keep.test(n)) {
                            continue;
                        }
                        matches++;
                    }
                    if (skip++ < offset) {
                        continue;
                    }
                    if (count >= limit) {
                        if (keep == null) {
                            break;
                        }
                        continue;
                    }
                    JsonObject o = new JsonObject();
                    o.addProperty("id", n.getId().toString());
                    o.addProperty("label", n.getLabel());
                    if (wanted == null) {
                        o.addProperty("x", n.x());
                        o.addProperty("y", n.y());
                        o.addProperty("size", n.size());
                    }
                    o.addProperty("degree", g.getDegree(n));
                    Color c = n.getColor();
                    if (c != null && wanted == null) {
                        o.addProperty("r", c.getRed());
                        o.addProperty("g", c.getGreen());
                        o.addProperty("b", c.getBlue());
                        o.addProperty("a", c.getAlpha());
                    }
                    // Custom attributes: every one, or only those asked for
                    JsonObject attrs = new JsonObject();
                    for (Column col : gm.getNodeTable()) {
                        if (col.isProperty()) {
                            continue; // skip built-in
                        }
                        if (!isWanted(wanted, col.getId(), col.getTitle())) {
                            continue;
                        }
                        Object v = n.getAttribute(col);
                        if (v != null) {
                            if (v instanceof Number) {
                                attrs.addProperty(col.getTitle(), (Number) v);
                            } else if (v instanceof Boolean) {
                                attrs.addProperty(col.getTitle(), (Boolean) v);
                            } else {
                                attrs.addProperty(col.getTitle(), v.toString());
                            }
                        }
                    }
                    if (attrs.size() > 0) {
                        o.add("attributes", attrs);
                    }
                    arr.add(o);
                    count++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("total", g.getNodeCount());
                if (keep != null) {
                    r.addProperty("matches", matches);
                }
                if (sortBy != null) {
                    r.addProperty("sorted_by", sortCol == null ? "degree" : sortCol.getTitle());
                    r.addProperty("descending", descending);
                }
                r.addProperty("count", count);
                addViewInfo(r, gm, visible);
                r.add("nodes", arr);
                return r;
            } finally {
                g.readUnlock();
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /**
     * Items ordered by a value: numbers by size, anything else as text ignoring case. Items
     * without a value always come last, so a sorted first page is the top of the graph.
     */
    static <T> List<T> sortByValue(List<T> items, java.util.function.Function<T, Object> value,
        boolean descending) {
        java.util.Comparator<Object> byValue = (a, b) -> {
            if (a instanceof Number && b instanceof Number) {
                return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
            }
            return a.toString().compareToIgnoreCase(b.toString());
        };
        java.util.Comparator<Object> order = descending ? byValue.reversed() : byValue;
        List<T> sorted = new java.util.ArrayList<>(items);
        sorted.sort((x, y) -> {
            Object a = value.apply(x);
            Object b = value.apply(y);
            if (a == null || b == null) {
                return a == null ? (b == null ? 0 : 1) : -1;
            }
            return order.compare(a, b);
        });
        return sorted;
    }

    /** The requested attribute columns, lower-cased; null means every column. */
    static java.util.Set<String> wantedColumns(String columns) {
        if (columns == null || columns.isBlank()) {
            return null;
        }
        java.util.Set<String> wanted = new java.util.HashSet<>();
        for (String c : columns.split(",")) {
            if (!c.isBlank()) {
                wanted.add(c.trim().toLowerCase(java.util.Locale.ROOT));
            }
        }
        return wanted.isEmpty() ? null : wanted;
    }

    static boolean isWanted(java.util.Set<String> wanted, String id, String title) {
        return wanted == null || wanted.contains(id.toLowerCase(java.util.Locale.ROOT))
            || (title != null && wanted.contains(title.toLowerCase(java.util.Locale.ROOT)));
    }

    /**
     * Which nodes a value search keeps: {@code value} matches the whole value (text ignoring
     * case, numbers by value), {@code contains} a part of the text, and {@code min} / {@code max}
     * a numeric range. The column is found by id or by title. Null when no search was asked for.
     */
    static java.util.function.Predicate<Node> nodeMatcher(Column col, String value, String contains,
        Double min, Double max) {
        if (value == null && contains == null && min == null && max == null) {
            return null;
        }
        String needle = contains == null ? null : contains.toLowerCase(java.util.Locale.ROOT);
        return n -> {
            Object v = n.getAttribute(col);
            if (v == null) {
                return false;
            }
            if (value != null) {
                if (v instanceof Number) {
                    try {
                        if (((Number) v).doubleValue() != Double.parseDouble(value.trim())) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                } else if (!v.toString().equalsIgnoreCase(value)) {
                    return false;
                }
            }
            if (needle != null && !v.toString().toLowerCase(java.util.Locale.ROOT).contains(needle)) {
                return false;
            }
            if (min != null || max != null) {
                if (!(v instanceof Number)) {
                    return false;
                }
                double d = ((Number) v).doubleValue();
                if (min != null && d < min) {
                    return false;
                }
                if (max != null && d > max) {
                    return false;
                }
            }
            return true;
        };
    }

    public JsonObject getNode(String id) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = gm.getGraph();
            Node n = nodeById(g, id);
            if (n == null) {
                return error("Node not found: " + id);
            }
            JsonObject o = new JsonObject();
            o.addProperty("id", n.getId().toString());
            o.addProperty("label", n.getLabel());
            o.addProperty("x", n.x());
            o.addProperty("y", n.y());
            o.addProperty("size", n.size());
            o.addProperty("r", (int) (n.r() * 255));
            o.addProperty("g", (int) (n.g() * 255));
            o.addProperty("b", (int) (n.b() * 255));
            JsonObject attrs = new JsonObject();
            for (Column col : gm.getNodeTable()) {
                if (col.isProperty()) {
                    continue;
                }
                Object v = n.getAttribute(col);
                if (v == null) {
                    continue;
                }
                if (v instanceof Number) {
                    attrs.addProperty(col.getTitle(), (Number) v);
                } else if (v instanceof Boolean) {
                    attrs.addProperty(col.getTitle(), (Boolean) v);
                } else {
                    attrs.addProperty(col.getTitle(), v.toString());
                }
            }
            o.add("attributes", attrs);
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.add("node", o);
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setNodeLabel(String id, String label) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node n = nodeById(g, id);
                if (n == null) {
                    return error("Node not found: " + id);
                }
                n.setLabel(label);
                return success("Label set");
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setNodePosition(String id, float x, float y) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node n = nodeById(g, id);
                if (n == null) {
                    return error("Node not found: " + id);
                }
                n.setX(x);
                n.setY(y);
                return success("Position set");
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject batchSetPositions(List<Map<String, Object>> positions) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                int set = 0;
                int notFound = 0;
                for (Map<String, Object> pos : positions) {
                    String id = (String) pos.get("id");
                    Node n = nodeById(g, id);
                    if (n == null) {
                        notFound++;
                        continue;
                    }
                    n.setX(((Number) pos.get("x")).floatValue());
                    n.setY(((Number) pos.get("y")).floatValue());
                    set++;
                }
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("set", set);
                r.addProperty("not_found", notFound);
                return r;
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    // ─── Edge Operations ─────────────────────────────────────────────

    public JsonObject addEdge(String src, String tgt, Double weight, boolean directed) {
        return addEdge(src, tgt, weight, directed, null);
    }

    public JsonObject addEdge(String src, String tgt, Double weight, boolean directed, String edgeType) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
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
                Node s = nodeById(g, src);
                Node t = nodeById(g, tgt);
                if (s == null) {
                    return error("Source not found: " + src);
                }
                if (t == null) {
                    return error("Target not found: " + tgt);
                }
                double w = weight != null ? weight : 1.0;
                if (edgeType != null && !edgeType.isEmpty()) {
                    int typeId = gm.addEdgeType(edgeType);
                    if (g.getEdge(s, t, typeId) != null) {
                        return error("Edge of type '" + edgeType + "' exists");
                    }
                    g.addEdge(gm.factory().newEdge(s, t, typeId, w, directed));
                } else {
                    if (findEdge(g, s, t) != null) {
                        return error("Edge exists");
                    }
                    g.addEdge(gm.factory().newEdge(s, t, directed ? 1 : 0, w, directed));
                }
                return success("Edge added");
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject addEdges(List<Map<String, Object>> edges) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        return addEdgesToModel(getGraphController().getGraphModel(ws), edges);
    }

    /** Core batch edge-add against an explicit model (honors per-edge directed/label/attributes). */
    static JsonObject addEdgesToModel(GraphModel gm, List<Map<String, Object>> edges) {
        try {
            Graph g = gm.getGraph();
            int added = 0;
            int skipped = 0;
            lockWrite(g);
            try {
                for (Map<String, Object> ed : edges) {
                    String src = (String) ed.get("source");
                    String tgt = (String) ed.get("target");
                    if (src == null || tgt == null) {
                        skipped++;
                        continue;
                    }
                    Node s = nodeById(g, src);
                    Node t = nodeById(g, tgt);
                    if (s == null || t == null) {
                        skipped++;
                        continue;
                    }
                    Double w = ed.containsKey("weight") ? ((Number) ed.get("weight")).doubleValue() : 1.0;
                    boolean directed = !ed.containsKey("directed") || Boolean.TRUE.equals(ed.get("directed"));
                    Object edgeTypeObj = ed.get("edge_type");
                    String edgeType = edgeTypeObj != null ? edgeTypeObj.toString() : null;
                    int type;
                    if (edgeType != null && !edgeType.isEmpty()) {
                        type = gm.addEdgeType(edgeType);
                        if (g.getEdge(s, t, type) != null) {
                            skipped++;
                            continue;
                        }
                    } else {
                        if (findEdge(g, s, t) != null) {
                            skipped++;
                            continue;
                        }
                        type = directed ? 1 : 0;
                    }
                    Edge e = gm.factory().newEdge(s, t, type, w, directed);
                    Object label = ed.get("label");
                    if (label != null) {
                        e.setLabel(label.toString());
                    }
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
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject removeEdge(String source, String target) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node s = nodeById(g, source);
                Node t = nodeById(g, target);
                if (s == null || t == null) {
                    return error("Node not found");
                }
                Edge e = findEdge(g, s, t);
                if (e == null) {
                    return error("Edge not found");
                }
                g.removeEdge(e);
                return success("Edge removed");
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setEdgeWeight(String source, String target, double weight) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node s = nodeById(g, source);
                Node t = nodeById(g, target);
                if (s == null || t == null) {
                    return error("Node not found");
                }
                Edge e = findEdge(g, s, t);
                if (e == null) {
                    return error("Edge not found");
                }
                e.setWeight(weight);
                return success("Weight set to " + weight);
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setEdgeLabel(String source, String target, String label) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph g = currentGraphModel().getGraph();
            lockWrite(g);
            try {
                Node s = nodeById(g, source);
                Node t = nodeById(g, target);
                if (s == null || t == null) {
                    return error("Node not found");
                }
                Edge e = findEdge(g, s, t);
                if (e == null) {
                    return error("Edge not found");
                }
                e.setLabel(label);
                return success("Edge label set");
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject queryEdges(int limit, int offset) {
        return queryEdges(limit, offset, false);
    }

    /**
     * @param visible read the filtered visible graph instead of the full graph (see addViewInfo).
     */
    public JsonObject queryEdges(int limit, int offset, boolean visible) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = visible ? gm.getGraphVisible() : gm.getGraph();
            lockRead(g);
            try {
                JsonArray arr = new JsonArray();
                int count = 0;
                int skip = 0;
                // toArray, not the live iterable: breaking out of an auto-locked
                // iterator before exhaustion leaks its read hold permanently.
                for (Edge e : g.getEdges().toArray()) {
                    if (skip++ < offset) {
                        continue;
                    }
                    if (count >= limit) {
                        break;
                    }
                    JsonObject o = new JsonObject();
                    o.addProperty("source", e.getSource().getId().toString());
                    o.addProperty("target", e.getTarget().getId().toString());
                    o.addProperty("weight", e.getWeight());
                    o.addProperty("directed", e.isDirected());
                    if (e.getLabel() != null) {
                        o.addProperty("label", e.getLabel());
                    }
                    Color c = e.getColor();
                    if (c != null) {
                        o.addProperty("r", c.getRed());
                        o.addProperty("g", c.getGreen());
                        o.addProperty("b", c.getBlue());
                    }
                    // Include custom attributes
                    JsonObject attrs = new JsonObject();
                    for (Column col : gm.getEdgeTable()) {
                        if (col.isProperty()) {
                            continue;
                        }
                        Object v = e.getAttribute(col);
                        if (v != null) {
                            if (v instanceof Number) {
                                attrs.addProperty(col.getTitle(), (Number) v);
                            } else if (v instanceof Boolean) {
                                attrs.addProperty(col.getTitle(), (Boolean) v);
                            } else {
                                attrs.addProperty(col.getTitle(), v.toString());
                            }
                        }
                    }
                    if (attrs.size() > 0) {
                        o.add("attributes", attrs);
                    }
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
            } finally {
                g.readUnlock();
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
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

    /**
     * @param visible read the filtered visible graph instead of the full graph (see addViewInfo).
     */
    public JsonObject getGraphStats(boolean visible) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = getGraphController().getGraphModel(ws);
            Graph g = visible ? gm.getGraphVisible() : gm.getGraph();
            lockRead(g);
            try {
                int nc = g.getNodeCount();
                int ec = g.getEdgeCount();
                // As the Gephi Density and Average Degree statistics count them: a directed tie
                // fills one of n(n-1) ordered pairs and adds one to the mean in-degree and the mean
                // out-degree; an undirected tie fills one of n(n-1)/2 pairs and adds to two degrees.
                double ties = gm.isDirected() ? ec : 2.0 * ec;
                double density = nc > 1 ? ties / ((double) nc * (nc - 1)) : 0;
                double avgDeg = nc > 0 ? ties / nc : 0;
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("node_count", nc);
                r.addProperty("edge_count", ec);
                r.addProperty("density", density);
                r.addProperty("average_degree", avgDeg);
                r.addProperty("is_directed", gm.isDirected());
                addViewInfo(r, gm, visible);
                return r;
            } finally {
                g.readUnlock();
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    // ─── Graph Type ──────────────────────────────────────────────────

    public JsonObject getGraphType() {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = currentGraphModel();
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("directed", gm.isDirected());
            r.addProperty("undirected", gm.isUndirected());
            r.addProperty("mixed", gm.isMixed());
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    // ─── Attribute / Column Management ───────────────────────────────

    public JsonObject getColumns(String target) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
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
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject addColumn(String name, String type, String target) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
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
            if (cls == null) {
                return error("Unknown type: " + type + ". Use: string, integer, double, float, boolean, long");
            }
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                Column existing = findColumn(table, name);
                if (existing != null) {
                    return error("Column already exists: " + existing.getTitle() + " (id " + existing.getId() + ")");
                }
                table.addColumn(name, cls);
            } finally {
                unlockWrite(g);
            }
            return success("Column '" + name + "' added");
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setNodeAttributes(String id, Map<String, Object> attrs) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                Node n = nodeById(g, id);
                if (n == null) {
                    return error("Node not found: " + id);
                }
                for (Map.Entry<String, Object> e : attrs.entrySet()) {
                    ensureColumnAndSet(gm.getNodeTable(), n, e.getKey(), e.getValue());
                }
                return success("Attributes set on node " + id);
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject batchSetNodeAttributes(List<Map<String, Object>> updates) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                int set = 0;
                int notFound = 0;
                for (Map<String, Object> update : updates) {
                    String id = (String) update.get("id");
                    Node n = nodeById(g, id);
                    if (n == null) {
                        notFound++;
                        continue;
                    }
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
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setEdgeAttributes(String source, String target, Map<String, Object> attrs) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            lockWrite(g);
            try {
                Node s = nodeById(g, source);
                Node t = nodeById(g, target);
                if (s == null || t == null) {
                    return error("Node not found");
                }
                Edge e = findEdge(g, s, t);
                if (e == null) {
                    return error("Edge not found");
                }
                for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                    ensureColumnAndSet(gm.getEdgeTable(), e, entry.getKey(), entry.getValue());
                }
                return success("Attributes set on edge");
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    static void ensureColumnAndSet(Table table, Object element, String key, Object value) {
        Column col = findColumn(table, key);
        if (col == null) {
            Class<?> cls = String.class;
            if (value instanceof Number) {
                if (value instanceof Integer) {
                    cls = Integer.class;
                } else if (value instanceof Long) {
                    cls = Long.class;
                } else if (value instanceof Float) {
                    cls = Float.class;
                } else {
                    cls = Double.class;
                }
            } else if (value instanceof Boolean) {
                cls = Boolean.class;
            }
            col = table.addColumn(key, cls);
        }
        // Convert value to column type
        Object converted = convertToColumnType(value, col.getTypeClass());
        if (element instanceof Node) {
            ((Node) element).setAttribute(col, converted);
        } else if (element instanceof Edge) {
            ((Edge) element).setAttribute(col, converted);
        }
    }

    static Object convertToColumnType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        String s = value.toString();
        try {
            if (targetType == Integer.class) {
                return (int) Double.parseDouble(s);
            }
            if (targetType == Long.class) {
                return (long) Double.parseDouble(s);
            }
            if (targetType == Float.class) {
                return (float) Double.parseDouble(s);
            }
            if (targetType == Double.class) {
                return Double.parseDouble(s);
            }
            if (targetType == Boolean.class) {
                return Boolean.parseBoolean(s);
            }
        } catch (Exception e) { /* fall through */
        }
        return s;
    }

    static Class<?> typeStringToClass(String type) {
        if (type == null) {
            return null;
        }
        switch (type.toLowerCase()) {
            case "string":
                return String.class;
            case "integer":
            case "int":
                return Integer.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "boolean":
            case "bool":
                return Boolean.class;
            case "long":
                return Long.class;
            default:
                return null;
        }
    }

    // ─── Appearance: Individual Node/Edge Styling ────────────────────

    /**
     * Node color/size are plain fields on the Node object (NodeImpl.setColor/setSize just
     * write an int/float, no checkWriteLock) — unlike addNode/removeNode, which mutate
     * NodeStore's internal structure and do enforce the write lock. Gephi's own
     * AppearanceController.transform() (see colorByPartition/colorByRanking/sizeByRanking
     * below) sets these same properties on every node in the graph under nothing more than
     * a live NodeIterable's read lock, so a single-node set needs no write lock either.
     */
    public JsonObject setNodeColor(String id, int r, int g, int b, int a) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph graph = currentGraphModel().getGraph();
            Node n = nodeById(graph, id);
            if (n == null) {
                return error("Node not found: " + id);
            }
            n.setColor(new Color(r, g, b, a));
            return success("Node color set");
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setNodeSize(String id, float size) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph graph = currentGraphModel().getGraph();
            Node n = nodeById(graph, id);
            if (n == null) {
                return error("Node not found: " + id);
            }
            n.setSize(size);
            return success("Node size set to " + size);
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /*
     * THREADING NOTE (applies to every styling/filter method below that once wrapped its
     * body in runOnEDT): these operations mutate the graph model, which is thread-safe
     * under its own lock and does not need the EDT. Polling lockWrite's 15-second tryLock
     * loop ON the EDT froze the UI under contention, tripped runOnEDT's own 15-second
     * timeout (misreporting "Gephi's UI thread is unresponsive"), and — worse — the
     * abandoned EDT task still ran later, applying a destructive mutation after the HTTP
     * call had already reported failure, so a client retry applied it twice. They now run
     * on the calling thread, like clearGraph and addNodeToModel always have.
     */

    public JsonObject setEdgeColor(String source, String target, int r, int g, int b, int a) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            Graph graph = currentGraphModel().getGraph();
            lockWrite(graph);
            try {
                Node s = nodeById(graph, source);
                Node t = nodeById(graph, target);
                if (s == null || t == null) {
                    return error("Node not found");
                }
                Edge e = findEdge(graph, s, t);
                if (e == null) {
                    return error("Edge not found");
                }
                e.setColor(new Color(r, g, b, a));
                return success("Edge color set");
            } finally {
                unlockWrite(graph);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject batchSetNodeColors(List<Map<String, Object>> nodeColors) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            Graph graph = currentGraphModel().getGraph();
            int set = 0;
            int notFound = 0;
            for (Map<String, Object> nc : nodeColors) {
                String id = (String) nc.get("id");
                Node n = nodeById(graph, id);
                if (n == null) {
                    notFound++;
                    continue;
                }
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
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /** Every node to one colour, and to one size when {@code size} is not null. */
    public JsonObject resetAppearance(int r, int g, int b, Float size) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            Graph graph = currentGraphModel().getGraph();
            Color defaultColor = new Color(r, g, b);
            // toArray, not the live iterable: breaking out of an auto-locked iterator
            // before exhaustion leaks its read hold permanently (see ITERATION RULE above).
            for (Node n : graph.getNodes().toArray()) {
                n.setColor(defaultColor);
                if (size != null) {
                    n.setSize(size);
                }
            }
            return success(size != null ? "Colour and size reset for all nodes"
                : "Colour reset for all nodes; sizes kept");
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    // ─── Appearance: Color/Size by Attribute ─────────────────────────

    /**
     * A column by its id, or by the title users see in Gephi (any case) when no id matches.
     * Imported files often give columns internal ids such as "0" with a readable title.
     */
    static Column findColumn(org.gephi.graph.api.Table table, String name) {
        if (table == null || name == null) {
            return null;
        }
        Column byId = table.getColumn(name);
        if (byId != null) {
            return byId;
        }
        // Search a copy: iterating the table itself locks it until the loop runs to the end, so
        // returning from inside the loop would leave the table locked and wedge Gephi.
        for (Column c : table.toArray()) {
            if (name.equalsIgnoreCase(c.getId()) || name.equalsIgnoreCase(c.getTitle())) {
                return c;
            }
        }
        return null;
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /**
     * Give a partition the exact colours Gephi AI applied, matched by the value's text as the
     * colour map is keyed. Values Gephi AI left alone keep their colour in the panel. Returns how
     * many values were set.
     */
    static int applyPaletteToPartition(org.gephi.appearance.api.Partition partition, Graph graph,
        Map<String, Color> palette) {
        int set = 0;
        for (Object value : partition.getValues(graph)) {
            Color c = palette.get(String.valueOf(value));
            if (c != null) {
                partition.setColor(value, c);
                set++;
            }
        }
        return set;
    }

    /** How many visible nodes (or edges) have a value in {@code col} that {@code counts}. */
    static int countVisible(GraphModel gm, boolean edges, java.util.function.Predicate<Object> counts, Column col) {
        Graph visible = gm.getGraphVisible();
        lockRead(visible);
        try {
            int n = 0;
            for (org.gephi.graph.api.Element e : edges ? visible.getEdges().toArray() : visible.getNodes().toArray()) {
                if (counts.test(e.getAttribute(col))) {
                    n++;
                }
            }
            return n;
        } finally {
            visible.readUnlock();
        }
    }

    /** A two-stop colour ranking from the minimum colour to the maximum, as Gephi AI applies it. */
    static void configureRankingColor(org.gephi.appearance.plugin.RankingElementColorTransformer t,
        Color min, Color max) {
        t.setColors(new Color[]{min, max});
        t.setColorPositions(new float[]{0f, 1f});
    }

    /** A size ranking from the minimum size to the maximum, as Gephi AI applies it. */
    static void configureRankingSize(org.gephi.appearance.plugin.RankingSizeTransformer<?> t,
        float min, float max) {
        t.setMinSize(min);
        t.setMaxSize(max);
    }

    /**
     * Apply a colour or size the way the Appearance panel's Apply button does: take Gephi's
     * function for {@code col}, give it the colours or sizes through {@code configure}, and
     * transform the visible graph with it. The ranking and partition scales are global, so a
     * filter does not rescale the values. Returns the function, or null when Gephi offers no
     * such function for the column.
     */
    private org.gephi.appearance.api.Function applyAppearance(Workspace ws, Column col, boolean edges,
        Class<? extends org.gephi.appearance.spi.Transformer> transformer,
        java.util.function.Consumer<org.gephi.appearance.api.Function> configure) {
        org.gephi.appearance.api.AppearanceController ac =
            Lookup.getDefault().lookup(org.gephi.appearance.api.AppearanceController.class);
        org.gephi.appearance.api.AppearanceModel am = ac == null ? null : ac.getModel(ws);
        if (am == null) {
            return null;
        }
        org.gephi.appearance.api.Function f = edges
            ? am.getEdgeFunction(col, transformer) : am.getNodeFunction(col, transformer);
        if (f == null) {
            return null;
        }
        ac.setUseRankingLocalScale(false);
        ac.setUsePartitionLocalScale(false);
        configure.accept(f);
        ac.transform(f);
        return f;
    }

    private static JsonObject noAppearanceFunction(String what, Column col) {
        return error("Gephi's Appearance panel offers no " + what + " for column '" + col.getTitle() + "'");
    }

    /**
     * Set Gephi's Appearance panel to the node function Gephi AI just applied (nodes, the
     * transformer's category, its UI, the column), so the panel shows what was done and Apply
     * there reproduces it. Returns null when the panel shows it, or the reason it could not,
     * without failing the caller.
     */
    private String showInAppearancePanel(org.gephi.appearance.api.Function f) {
        try {
            org.gephi.desktop.appearance.AppearanceUIController ui =
                Lookup.getDefault().lookup(org.gephi.desktop.appearance.AppearanceUIController.class);
            if (ui == null) {
                return "Gephi's Appearance panel is not available";
            }
            onEdt(() -> {
                ui.setSelectedElementClass("nodes");
                ui.setSelectedCategory(f.getUI().getCategory());
                ui.setSelectedTransformerUI(f.getUI());
                // Selecting the function already shown does not refresh the panel, so clear it
                // first; otherwise the panel keeps the colours it had before.
                ui.setSelectedFunction(null);
                ui.setSelectedFunction(f);
            });
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Could not show the appearance in Gephi's Appearance panel", e);
            return "could not update Gephi's Appearance panel: " + e;
        }
    }

    /** Record in a response whether Gephi's Appearance panel now shows what was applied. */
    private static void reportPanel(JsonObject r, String problem) {
        r.addProperty("appearance_panel", problem == null);
        if (problem != null) {
            r.addProperty("appearance_panel_note", problem);
        }
    }

    /**
     * Eight colours validated for categorical use on light backgrounds (lightness band, chroma
     * floor, contrast), given to the largest groups first. On a map any two groups can touch, so
     * the order keeps every pair of the first five apart with normal vision and under simulated
     * red and green colour blindness; past five, groups need labels as well as colour. The skill
     * documents the same eight, with a variant for dark backgrounds.
     */
    static final Color[] BASE_PALETTE = {
        new Color(42, 120, 214), new Color(237, 161, 0), new Color(0, 131, 0),
        new Color(232, 123, 164), new Color(74, 58, 167), new Color(227, 73, 72),
        new Color(27, 175, 122), new Color(235, 104, 52)
    };

    /**
     * A colour per value, largest group first, so the most distinct colours go to the groups
     * that cover most of the map. Past the eight base colours every further group still gets
     * its own colour: hues step by the golden angle, and saturation and brightness alternate so
     * neighbouring hues stay apart.
     */
    static java.util.LinkedHashMap<String, Color> partitionPalette(Map<String, Integer> counts) {
        List<Map.Entry<String, Integer>> order = new java.util.ArrayList<>(counts.entrySet());
        order.sort((a, b) -> b.getValue().equals(a.getValue())
            ? a.getKey().compareTo(b.getKey()) : Integer.compare(b.getValue(), a.getValue()));
        java.util.LinkedHashMap<String, Color> palette = new java.util.LinkedHashMap<>();
        int i = 0;
        for (Map.Entry<String, Integer> e : order) {
            palette.put(e.getKey(), paletteColor(i++));
        }
        return palette;
    }

    static Color paletteColor(int i) {
        if (i < BASE_PALETTE.length) {
            return BASE_PALETTE[i];
        }
        int k = i - BASE_PALETTE.length;
        float hue = (float) ((0.13 + k * 0.618033988749895) % 1.0);
        float saturation = new float[] {0.55f, 0.85f, 0.40f}[k % 3];
        float brightness = new float[] {0.85f, 0.60f, 0.95f}[(k / 3) % 3];
        return Color.getHSBColor(hue, saturation, brightness);
    }

    /** Past this many groups, some pairs of colours are hard to tell apart where groups touch. */
    static final int DISTINCT_GROUPS = 5;

    static void addPaletteNote(JsonObject r, int groups) {
        if (groups > DISTINCT_GROUPS) {
            r.addProperty("palette_note", groups + " groups each have their own colour, but past "
                + DISTINCT_GROUPS + " some colours are hard to tell apart where groups touch,"
                + " especially for colour-blind readers. Label the groups as well, or colour only"
                + " the largest and leave the rest gray.");
        }
    }

    public JsonObject colorByPartition(String columnName, Map<String, int[]> colorMap) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = findColumn(gm.getNodeTable(), columnName);
            if (col == null) {
                return error("Column not found: " + columnName);
            }

            // Collect distinct values
            java.util.Map<String, Color> palette = new java.util.LinkedHashMap<>();
            if (colorMap != null && !colorMap.isEmpty()) {
                for (Map.Entry<String, int[]> e : colorMap.entrySet()) {
                    int[] c = e.getValue();
                    palette.put(e.getKey(), new Color(c[0], c[1], c[2]));
                }
            } else {
                java.util.Map<String, Integer> counts = new java.util.HashMap<>();
                for (Node n : graph.getNodes().toArray()) {
                    Object v = n.getAttribute(col);
                    if (v != null) {
                        counts.merge(v.toString(), 1, Integer::sum);
                    }
                }
                palette.putAll(partitionPalette(counts));
            }

            org.gephi.appearance.api.Function f = applyAppearance(ws, col, false,
                org.gephi.appearance.plugin.PartitionElementColorTransformer.class,
                fn -> applyPaletteToPartition(((org.gephi.appearance.api.PartitionFunction) fn).getPartition(),
                    fn.getGraph(), palette));
            if (f == null) {
                return noAppearanceFunction("partition colouring", col);
            }
            int colored = countVisible(gm, false, v -> v != null && palette.containsKey(v.toString()), col);
            JsonObject r = success("Colored " + colored + " nodes by " + columnName);
            r.addProperty("partitions", palette.size());
            addPaletteNote(r, palette.size());
            addViewInfo(r, gm, true);
            reportPanel(r, showInAppearancePanel(f));
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /**
     * Min and max over the numeric values of {@code col}, as {@code [min, max]}, or null when
     * the column holds no numeric values. Seeded with infinities so a column whose values are
     * entirely negative ranks correctly — the old {@code Double.MIN_VALUE} seed (smallest
     * positive double) silently broke that case. Package-private + static for unit testing.
     */
    static double[] numericRange(Graph g, Column col) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        lockRead(g);
        try {
            for (Node n : g.getNodes().toArray()) {
                Object v = n.getAttribute(col);
                if (v instanceof Number) {
                    double d = ((Number) v).doubleValue();
                    if (d < min) {
                        min = d;
                    }
                    if (d > max) {
                        max = d;
                    }
                }
            }
        } finally {
            g.readUnlock();
        }
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
        Column col = findColumn(gm.getNodeTable(), columnName);
        if (col == null && columnName != null) {
            String lc = columnName.toLowerCase();
            if (lc.equals("degree") || lc.equals("indegree") || lc.equals("outdegree")) {
                runStatistic("Degree", null);
                col = findColumn(gm.getNodeTable(), columnName);
            }
        }
        return col;
    }

    private static JsonObject columnNotFound(String columnName) {
        return error("Column not found: " + columnName
            + " — compute the metric first (degree, pagerank, betweenness, modularity"
            + " via the statistics tools) or check the columns list");
    }

    public JsonObject colorByRanking(String columnName, int minRed, int minGreen, int minBlue, int maxRed,
        int maxGreen, int maxBlue) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = resolveRankingColumn(gm, columnName);
            if (col == null) {
                return columnNotFound(columnName);
            }

            double[] mm = numericRange(graph, col);
            if (mm == null) {
                return error("No numeric values in column " + columnName);
            }
            final Color low = new Color(clamp255(minRed), clamp255(minGreen), clamp255(minBlue));
            final Color high = new Color(clamp255(maxRed), clamp255(maxGreen), clamp255(maxBlue));
            org.gephi.appearance.api.Function f = applyAppearance(ws, col, false,
                org.gephi.appearance.plugin.RankingElementColorTransformer.class,
                fn -> configureRankingColor(fn.getTransformer(), low, high));
            if (f == null) {
                return noAppearanceFunction("colour ranking", col);
            }
            int colored = countVisible(gm, false, v -> v instanceof Number, col);
            JsonObject res = success("Colored " + colored + " nodes by ranking on " + columnName);
            res.addProperty("min_value", mm[0]);
            res.addProperty("max_value", mm[1]);
            addViewInfo(res, gm, true);
            reportPanel(res, showInAppearancePanel(f));
            return res;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /**
     * The size a node with value {@code v} gets on a ranking from {@code min} to {@code max}.
     * With a {@code cap} below {@code max}, values at or above the cap get the largest size and
     * the rest spread over the whole range, so a few outliers cannot shrink every other node.
     */
    static float rankedSize(double v, double min, double max, Double cap, float minSize, float maxSize) {
        double top = cap != null && cap < max ? cap : max;
        double range = top - min;
        if (range <= 0) {
            range = 1;
        }
        double t = Math.min(1.0, Math.max(0.0, (v - min) / range));
        return (float) (minSize + t * (maxSize - minSize));
    }

    public JsonObject sizeByRanking(String columnName, float minSize, float maxSize) {
        return sizeByRanking(columnName, minSize, maxSize, null);
    }

    public JsonObject sizeByRanking(String columnName, float minSize, float maxSize, Double cap) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = resolveRankingColumn(gm, columnName);
            if (col == null) {
                return columnNotFound(columnName);
            }

            double[] mm = numericRange(graph, col);
            if (mm == null) {
                return error("No numeric values in column " + columnName);
            }
            double min = mm[0];
            double max = mm[1];
            boolean capped = cap != null && cap < max;

            if (!capped) {
                org.gephi.appearance.api.Function f = applyAppearance(ws, col, false,
                    org.gephi.appearance.plugin.RankingNodeSizeTransformer.class,
                    fn -> configureRankingSize(fn.getTransformer(), minSize, maxSize));
                if (f == null) {
                    return noAppearanceFunction("size ranking", col);
                }
                int sized = countVisible(gm, false, v -> v instanceof Number, col);
                JsonObject res = success("Sized " + sized + " nodes by " + columnName);
                res.addProperty("min_value", min);
                res.addProperty("max_value", max);
                addViewInfo(res, gm, true);
                reportPanel(res, showInAppearancePanel(f));
                return res;
            }

            // Gephi's size ranking has no cap, so a capped ranking is applied here directly.
            int sized = 0;
            int atCap = 0;
            lockWrite(graph);
            try {
                for (Node n : graph.getNodes().toArray()) {
                    Object v = n.getAttribute(col);
                    if (v instanceof Number) {
                        double value = ((Number) v).doubleValue();
                        n.setSize(rankedSize(value, min, max, cap, minSize, maxSize));
                        if (capped && value >= cap) {
                            atCap++;
                        }
                        sized++;
                    }
                }
            } finally {
                unlockWrite(graph);
            }
            JsonObject res = success("Sized " + sized + " nodes by " + columnName);
            res.addProperty("min_value", min);
            res.addProperty("max_value", max);
            res.addProperty("cap", cap);
            res.addProperty("nodes_at_cap", atCap);
            addViewInfo(res, gm, false);
            reportPanel(res, "Gephi's Appearance panel has no cap, so it was left as it was;"
                + " reapplying the ranking there would undo the cap.");
            return res;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    // ─── Layout ──────────────────────────────────────────────────────

    public JsonObject runLayout(String algo, int iterations) {
        return runLayout(algo, iterations, null);
    }

    public JsonObject runLayout(String algo, int iterations, Map<String, Object> properties) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = getGraphController().getGraphModel(ws);
            Layout layout = findLayout(algo);
            if (layout == null) {
                return error("Layout not found: " + algo);
            }
            layout.setGraphModel(gm);
            // Apply inline properties, or config staged earlier by setLayoutProperties.
            if (properties == null && pendingLayoutProps != null && algo.equals(pendingLayoutAlgo)) {
                properties = pendingLayoutProps;
            }
            pendingLayoutProps = null;
            pendingLayoutAlgo = null;
            org.gephi.layout.api.LayoutController lc = layoutController();
            if (lc == null) {
                return error("Gephi's layout controller is not available");
            }
            if (lc.getModel().isRunning()) {
                return error("Layout already running");
            }
            final int panelIters = iterations > 0 ? iterations : 1000;
            final java.util.List<String> unapplied =
                startLayoutThroughController(lc, layout, properties, panelIters, this::onEdt);
            JsonObject applied = appliedLayoutSettings(layout, properties);
            JsonObject lastRun = new JsonObject();
            lastRun.addProperty("layout", algo);
            lastRun.addProperty("iterations", panelIters);
            lastRun.add("applied", applied);
            lastRun.addProperty("applied_note", APPLIED_NOTE);
            lastLayoutRun = lastRun;
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("layout", algo);
            r.addProperty("status", "running");
            r.addProperty("iterations", panelIters);
            r.add("applied", applied.deepCopy());
            r.addProperty("applied_note", APPLIED_NOTE);
            reportUnapplied(r, unapplied, algo);
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject stopLayout() {
        org.gephi.layout.api.LayoutController lc = layoutController();
        if (lc == null || !lc.getModel().isRunning()) {
            return success("No layout running");
        }
        lc.stopLayout();
        return success("Layout stopped");
    }

    public JsonObject getLayoutStatus() {
        JsonObject r = new JsonObject();
        r.addProperty("success", true);
        org.gephi.layout.api.LayoutController lc = layoutController();
        boolean running = lc != null && lc.getModel().isRunning();
        r.addProperty("running", running);
        if (running && lc.getModel().getSelectedBuilder() != null) {
            r.addProperty("layout", lc.getModel().getSelectedBuilder().getName());
        }
        JsonObject lastRun = lastLayoutRun;
        if (lastRun != null) {
            r.add("last_run", lastRun.deepCopy());
        }
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
            if (layout == null) {
                return error("Layout not found: " + algo);
            }
            // Need a graph model for the layout to report properties
            Workspace ws = currentWorkspace();
            if (ws != null) {
                layout.setGraphModel(currentGraphModel());
            }

            JsonArray arr = new JsonArray();
            LayoutProperty[] props = layout.getProperties();
            if (props != null) {
                for (LayoutProperty prop : props) {
                    JsonObject o = new JsonObject();
                    String key = layoutPropertyKey(prop);
                    if (!key.isEmpty()) {
                        o.addProperty("key", key);
                    }
                    o.addProperty("name", prop.getCanonicalName() != null ? prop.getCanonicalName()
                        : prop.getProperty().getDisplayName());
                    o.addProperty("display_name", prop.getProperty().getDisplayName());
                    o.addProperty("type", prop.getProperty().getValueType().getSimpleName());
                    Object val = prop.getProperty().getValue();
                    if (val != null) {
                        o.addProperty("value", val.toString());
                    }
                    String desc = prop.getProperty().getShortDescription();
                    if (desc != null) {
                        o.addProperty("description", desc);
                    }
                    arr.add(o);
                }
            }
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("algorithm", algo);
            r.addProperty("values_are", "defaults of a fresh layout, never what a run or a staged setting"
                + " used; read applied in the gephi_run_layout result or last_run in gephi_get_layout_status."
                + " Pass the key to gephi_run_layout.");
            r.add("properties", arr);
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /**
     * Apply layout properties by canonical key, display name or full canonical name (any case).
     * Returns the keys that matched no property, in the order given, so a misspelled setting is
     * reported instead of the layout silently running on its default.
     */
    static java.util.List<String> applyLayoutProperties(Layout layout, Map<String, Object> properties) {
        java.util.List<String> unapplied = new java.util.ArrayList<>();
        if (properties == null) {
            return unapplied;
        }
        java.util.Set<String> matched = new java.util.HashSet<>();
        LayoutProperty[] props = layout.getProperties();
        if (props == null) {
            unapplied.addAll(properties.keySet());
            return unapplied;
        }
        for (LayoutProperty prop : props) {
            Object val = null;
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                String k = e.getKey();
                if (layoutPropertyMatches(prop, k)) {
                    matched.add(k);
                    if (val == null) {
                        val = e.getValue();
                    }
                }
            }
            if (val != null) {
                Class<?> type = prop.getProperty().getValueType();
                Object converted = convertLayoutProperty(val, type);
                if (converted != null) {
                    try {
                        prop.getProperty().setValue(converted);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Set layout property failed", e);
                    }
                }
            }
        }
        for (String k : properties.keySet()) {
            if (!matched.contains(k)) {
                unapplied.add(k);
            }
        }
        return unapplied;
    }

    /**
     * Said with every applied block. Agents took a bare applied block for the server repeating the
     * request and spent many calls checking it another way, including against the defaults that
     * gephi_get_layout_properties reports.
     */
    static final String APPLIED_NOTE = "These values were read back from the layout after the settings"
        + " were set, so they are what this run uses. gephi_get_layout_properties shows defaults only and"
        + " cannot confirm a setting.";

    /** The middle part of an "AlgoName.propertyKey.name" canonical name, or "" when there is none. */
    private static String layoutPropertyKey(LayoutProperty prop) {
        String canonicalName = prop.getCanonicalName() != null ? prop.getCanonicalName() : "";
        String[] parts = canonicalName.split("\\.");
        return parts.length >= 3 ? parts[parts.length - 2] : "";
    }

    /** Whether a key from the caller names this property: its key, display name or canonical name, any case. */
    private static boolean layoutPropertyMatches(LayoutProperty prop, String key) {
        String canonicalName = prop.getCanonicalName() != null ? prop.getCanonicalName() : "";
        String canonicalKey = layoutPropertyKey(prop);
        return (!canonicalKey.isEmpty() && key.equalsIgnoreCase(canonicalKey))
            || key.equalsIgnoreCase(prop.getProperty().getDisplayName())
            || (!canonicalName.isEmpty() && key.equalsIgnoreCase(canonicalName));
    }

    /**
     * The value each requested setting holds now, read back from the layout under the property
     * key (its display name when it has none), so a caller can confirm what a run used instead of
     * inferring it from the absence of unapplied_params. Keys that matched nothing are left out.
     */
    static JsonObject appliedLayoutSettings(Layout layout, Map<String, Object> properties) {
        JsonObject applied = new JsonObject();
        LayoutProperty[] props = layout.getProperties();
        if (properties == null || props == null) {
            return applied;
        }
        for (LayoutProperty prop : props) {
            boolean asked = false;
            for (String k : properties.keySet()) {
                asked |= layoutPropertyMatches(prop, k);
            }
            if (!asked) {
                continue;
            }
            String key = layoutPropertyKey(prop);
            if (key.isEmpty()) {
                key = prop.getProperty().getDisplayName();
            }
            Object value;
            try {
                value = prop.getProperty().getValue();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Reading layout property " + key + " failed", e);
                continue;
            }
            if (value instanceof Number) {
                applied.addProperty(key, (Number) value);
            } else if (value instanceof Boolean) {
                applied.addProperty(key, (Boolean) value);
            } else if (value != null) {
                applied.addProperty(key, value.toString());
            }
        }
        return applied;
    }

    /** Adds unapplied_params and a warning to a layout response when any key matched nothing. */
    private static void reportUnapplied(JsonObject r, java.util.List<String> unapplied, String algo) {
        if (unapplied == null || unapplied.isEmpty()) {
            return;
        }
        JsonArray ua = new JsonArray();
        for (String k : unapplied) {
            ua.add(k);
        }
        r.add("unapplied_params", ua);
        r.addProperty("warning", "These settings match no property of " + algo
            + " and were NOT applied: " + unapplied + ". Check the names with gephi_get_layout_properties.");
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
            if (ws == null) {
                return error("No project open");
            }
            Layout layout = findLayout(algo);
            if (layout == null) {
                return error("Layout not found: " + algo);
            }
            layout.setGraphModel(currentGraphModel());
            final java.util.List<String> unapplied = applyLayoutProperties(layout, properties);
            pendingLayoutProps = properties == null ? null : new java.util.LinkedHashMap<>(properties);
            pendingLayoutAlgo = algo;
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("layout", algo);
            r.addProperty("configured", true);
            r.addProperty("running", false);
            r.addProperty("note", "properties staged; the next run_layout of this algorithm applies them");
            r.add("staged", appliedLayoutSettings(layout, properties));
            reportUnapplied(r, unapplied, algo);
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    static Object convertLayoutProperty(Object val, Class<?> type) {
        if (val == null) {
            return null;
        }
        String s = val.toString();
        try {
            if (type == Boolean.class || type == boolean.class) {
                return Boolean.parseBoolean(s);
            }
            if (type == Integer.class || type == int.class) {
                return (int) Double.parseDouble(s);
            }
            if (type == Double.class || type == double.class) {
                return Double.parseDouble(s);
            }
            if (type == Float.class || type == float.class) {
                return (float) Double.parseDouble(s);
            }
            if (type == Long.class || type == long.class) {
                return (long) Double.parseDouble(s);
            }
            if (type == String.class) {
                return s;
            }
        } catch (Exception e) { /* fall through */
        }
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
            } catch (Throwable t) { /* name alone is enough */
            }
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
            public void finish() {
            }

            public void finish(String s) {
            }

            public void progress() {
            }

            public void progress(int i) {
            }

            public void progress(String s) {
            }

            public void progress(String s, int i) {
            }

            public String getDisplayName() {
                return "MCP statistic";
            }

            public void setDisplayName(String s) {
            }

            public void start() {
            }

            public void start(int i) {
            }

            public void switchToDeterminate(int i) {
            }

            public void switchToIndeterminate() {
            }
        };

    /** Cancels statistics that overrun their deadline. One daemon thread serves every run. */
    private static final java.util.concurrent.ScheduledExecutorService DEADLINES =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Gephi AI statistic deadline");
            t.setDaemon(true);
            return t;
        });

    /** The stack Gephi 0.11.4 gives its own statistic workers (gephi#3327). */
    static final long STATISTIC_STACK_BYTES = 128L * 1024 * 1024;

    /**
     * Runs {@code work} on a worker with Gephi's statistic stack size and waits for it, so a
     * statistic that recurses once per node of a long path (connected components on a deep
     * directed graph) does not overflow the request thread's small stack. Whatever it throws is
     * thrown here. If the request is interrupted, {@code cancel} stops the statistic, so it does
     * not go on writing columns after its request has given up.
     */
    static void runOnStatisticThread(Runnable work, Runnable cancel) {
        java.util.concurrent.atomic.AtomicReference<Throwable> thrown =
            new java.util.concurrent.atomic.AtomicReference<>();
        Thread worker = new Thread(null, () -> {
            try {
                work.run();
            } catch (Throwable t) {
                thrown.set(t);
            }
        }, "Gephi AI statistic", STATISTIC_STACK_BYTES);
        worker.setDaemon(true);
        worker.start();
        try {
            worker.join();
        } catch (InterruptedException e) {
            cancel.run();
            worker.interrupt();
            try {
                worker.join(STATISTIC_STOP_GRACE_MS);
            } catch (InterruptedException again) {
                // Already interrupted; the flag is restored below.
            }
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while a statistic was running; it was stopped");
        }
        Throwable t = thrown.get();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        if (t != null) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Run {@code work}, cancelling {@code task} if it is still running after {@code timeoutMs}.
     * Returns true when the deadline stopped it. A timeout of 0 or less runs without a deadline.
     * A statistic that outlasts its deadline is cancelled (statistic loops check the cancel flag),
     * so it releases the graph lock it holds instead of keeping every write waiting.
     */
    static boolean runWithDeadline(Runnable work, org.gephi.utils.longtask.spi.LongTask task,
        long timeoutMs) {
        if (timeoutMs <= 0) {
            work.run();
            return false;
        }
        java.util.concurrent.atomic.AtomicBoolean fired = new java.util.concurrent.atomic.AtomicBoolean();
        java.util.concurrent.ScheduledFuture<?> deadline = DEADLINES.schedule(() -> {
            fired.set(true);
            task.cancel();
        }, timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        try {
            work.run();
        } finally {
            deadline.cancel(false);
        }
        return fired.get();
    }

    /**
     * A dynamic statistic steps through the network's timeline window by window. On a network
     * with no time data there is no timeline to step through and Gephi's loop never ends, so it
     * is refused. Returns the reason, or null when the statistic can run.
     */
    private static String fmtTime(double t) {
        return t == Math.rint(t) ? String.valueOf((long) t) : String.valueOf(t);
    }

    static String dynamicStatisticProblem(Statistics stat, GraphModel gm) {
        if (!(stat instanceof org.gephi.statistics.spi.DynamicStatistics) || gm == null) {
            return null;
        }
        org.gephi.graph.api.Interval bounds = gm.isDynamic() ? gm.getTimeBounds() : null;
        if (bounds == null || Double.isInfinite(bounds.getLow()) || Double.isInfinite(bounds.getHigh())) {
            return "This is a dynamic statistic and needs a network with time data (timestamps or"
                + " intervals on nodes or edges). This network has none, so it was not run.";
        }
        // Gephi's dynamic statistics start with window 0 and tick 0, which its settings dialog
        // fills in. Gephi steps from the start by tick while a window fits, so tick 0 never ends
        // and a window wider than the span computes nothing.
        org.gephi.statistics.spi.DynamicStatistics dyn = (org.gephi.statistics.spi.DynamicStatistics) stat;
        double span = bounds.getHigh() - bounds.getLow();
        String range = " The network's time data runs from " + fmtTime(bounds.getLow()) + " to "
            + fmtTime(bounds.getHigh()) + "; pass params {\"window\": ..., \"tick\": ...} in those units.";
        if (!(dyn.getTick() > 0)) {
            return "This dynamic statistic needs a tick (the step between windows) greater than 0." + range;
        }
        if (dyn.getWindow() < 0 || dyn.getWindow() > span) {
            return "This dynamic statistic needs a window (the width of each time slice) between 0"
                + " and the network's time span." + range;
        }
        return null;
    }

    /** A statistic with no deadline is waited for this long before it is reported as still running. */
    static final long STATISTIC_WAIT_CAP_MS = 60L * 60 * 1000;

    /** How a wait for the panel's callback ended. */
    enum PanelWait { FINISHED, TIMED_OUT, LOST }

    /**
     * Waits for the panel's callback, for at most {@code waitMs}. The panel clears a statistic's
     * running mark just before it calls back; a mark that is gone with no callback after
     * {@link #PANEL_FINISH_GRACE_MS} means the run ended without reporting (Gephi sends a failure
     * in the panel's own completion code to its error handler, not to this listener), so the wait
     * ends there instead of at the one-hour cap.
     */
    static PanelWait awaitPanelRun(java.util.concurrent.CountDownLatch done, long waitMs, PanelRunMark mark)
        throws InterruptedException {
        final long deadline = System.nanoTime() + waitMs * 1_000_000L;
        while (true) {
            long leftMs = (deadline - System.nanoTime()) / 1_000_000L;
            if (leftMs <= 0) {
                return PanelWait.TIMED_OUT;
            }
            if (done.await(Math.min(leftMs, 1000), java.util.concurrent.TimeUnit.MILLISECONDS)) {
                return PanelWait.FINISHED;
            }
            if (!mark.isMarked()
                && !done.await(PANEL_FINISH_GRACE_MS, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                return PanelWait.LOST;
            }
        }
    }

    /** Hand-over states: not started yet, started on the interface thread, given up by the caller. */
    static final int HAND_OVER_PENDING = 0;
    static final int HAND_OVER_STARTED = 1;
    static final int HAND_OVER_ABANDONED = 2;

    /** How long a run whose running mark has gone may still report back. */
    static final long PANEL_FINISH_GRACE_MS = 2000;
    /** After a deadline cancels a statistic, how long to wait for it to actually stop. */
    static final long STATISTIC_STOP_GRACE_MS = 30_000;

    /** Statistics running now, by the name Gephi shows for them, so a stop request can reach them. */
    static final Map<Statistics, String> RUNNING_STATISTICS = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Records a statistic as running, unless a run of the same statistic is already under way.
     * Two runs of one statistic do the same work twice and hold the graph's read lock for both,
     * which keeps every write waiting longer. Returns null when claimed, or the error to report.
     */
    static String claimStatistic(Statistics stat, String name) {
        synchronized (RUNNING_STATISTICS) {
            if (RUNNING_STATISTICS.containsValue(name)) {
                return name + " is already running; wait for it to finish, or stop it with stop_statistic";
            }
            RUNNING_STATISTICS.put(stat, name);
            return null;
        }
    }

    /** Statistics stopped by request, so their run is reported as stopped rather than finished. */
    static final java.util.Set<Statistics> STOP_REQUESTED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * Stops every statistic Gephi AI started that is still running. A stopped run writes
     * nothing, so the columns keep their earlier values. Statistics that offer no way to stop
     * them are named, so the user knows to wait or to stop them from Gephi.
     */
    public JsonObject stopStatistics() {
        JsonArray stopped = new JsonArray();
        JsonArray unstoppable = new JsonArray();
        for (Map.Entry<Statistics, String> e : new java.util.ArrayList<>(RUNNING_STATISTICS.entrySet())) {
            if (e.getKey() instanceof org.gephi.utils.longtask.spi.LongTask) {
                STOP_REQUESTED.add(e.getKey());
                ((org.gephi.utils.longtask.spi.LongTask) e.getKey()).cancel();
                stopped.add(e.getValue());
            } else {
                unstoppable.add(e.getValue());
            }
        }
        JsonObject r = success(stopped.size() + unstoppable.size() == 0 ? "No statistic is running"
            : stopped.size() > 0 ? "Stopped " + stopped.size() + " statistic(s)"
            : "The running statistic cannot be stopped; it will finish on its own");
        r.add("stopped", stopped);
        if (unstoppable.size() > 0) {
            r.add("cannot_stop", unstoppable);
        }
        return r;
    }

    /**
     * The Statistics panel's controller, or null when the panel cannot run a statistic in this
     * workspace. The panel's controller is registered whenever Gephi's desktop modules are
     * loaded, but it runs nothing until the Statistics window has given the workspace its model;
     * before that its execute() returns at once and never reports back, so waiting on it would
     * hold the request for the full wait cap. The window adds that model to the workspace, so
     * its absence means the statistic has to run directly.
     */
    static org.gephi.desktop.statistics.api.StatisticsControllerUI statisticsPanelFor(Workspace ws,
        org.gephi.desktop.statistics.api.StatisticsControllerUI panel) {
        if (panel == null || ws == null
            || ws.getLookup().lookup(org.gephi.desktop.statistics.api.StatisticsModelUI.class) == null) {
            return null;
        }
        return panel;
    }

    /**
     * What the plugin can see of Gephi's running mark for one statistic. Gephi's Statistics panel
     * marks a statistic running inside {@code execute()}, before it hands it to the controller, and
     * clears the mark when the run ends, before it calls back.
     */
    interface PanelRunMark extends AutoCloseable {

        /** For callers that cannot observe the panel: always marked, so the panel is trusted. */
        PanelRunMark TRUSTED = new PanelRunMark() {
            @Override
            public boolean isMarked() {
                return true;
            }

            @Override
            public boolean wasEverMarked() {
                return true;
            }

            @Override
            public void close() {
            }
        };

        /** Whether the statistic is marked running now. */
        boolean isMarked();

        /** Whether it has been marked at any point since watching began. */
        boolean wasEverMarked();

        @Override
        void close();
    }

    /**
     * Whether this Statistics model shows this statistic as running. Its public interface shows
     * one running instance per statistic class; another instance of the same class (a run the
     * person started from the panel) is ambiguous, and the panel is trusted rather than risk a
     * second run. So is a model with no panel entry for the statistic.
     */
    static boolean panelIsRunning(org.gephi.desktop.statistics.api.StatisticsModelUI model, Statistics stat,
        java.util.Collection<? extends org.gephi.statistics.spi.StatisticsUI> uis) {
        if (model == null) {
            return false;
        }
        boolean checked = false;
        for (org.gephi.statistics.spi.StatisticsUI ui : uis) {
            if (ui.getStatisticsClass() != null && ui.getStatisticsClass().equals(stat.getClass())) {
                checked = true;
                if (model.getRunning(ui) != null) {
                    return true;
                }
            }
        }
        return !checked;
    }

    /** Statistics instances a Statistics model has shown as running, recorded by its one listener. */
    private static final java.util.Set<Statistics> SEEN_MARKED =
        java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>()));

    /** Statistics models that already carry the plugin's listener. */
    private static final java.util.Set<org.gephi.desktop.statistics.api.StatisticsModelUI> HOOKED =
        java.util.Collections.synchronizedSet(java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>()));

    /** Records the statistics a model now shows as running. */
    private static void recordMarked(org.gephi.desktop.statistics.api.StatisticsModelUI model,
        java.util.Collection<? extends org.gephi.statistics.spi.StatisticsUI> uis) {
        for (org.gephi.statistics.spi.StatisticsUI ui : uis) {
            Statistics running = model.getRunning(ui);
            if (running != null) {
                SEEN_MARKED.add(running);
            }
        }
    }

    /**
     * Watches every workspace's Statistics model for this statistic's running mark. The panel
     * marks the model it is bound to, which after a workspace change can be another workspace's,
     * so all of them are watched. Each model gets one listener, added once and never removed:
     * Gephi keeps listeners in a plain list that its statistic worker iterates, so adding and
     * removing one per run would race with it. The listener records every statistic it sees
     * marked, which catches a mark set and cleared between two checks.
     */
    static PanelRunMark watchPanel(Statistics stat,
        java.util.Collection<org.gephi.desktop.statistics.api.StatisticsModelUI> models,
        java.util.Collection<? extends org.gephi.statistics.spi.StatisticsUI> uis) {
        for (org.gephi.desktop.statistics.api.StatisticsModelUI m : models) {
            if (HOOKED.add(m)) {
                m.addChangeListener(e -> recordMarked(m, uis));
            }
        }
        final java.util.function.BooleanSupplier marked = () -> {
            for (org.gephi.desktop.statistics.api.StatisticsModelUI m : models) {
                if (panelIsRunning(m, stat, uis)) {
                    SEEN_MARKED.add(stat);
                    return true;
                }
            }
            return false;
        };
        return new PanelRunMark() {
            @Override
            public boolean isMarked() {
                return marked.getAsBoolean();
            }

            @Override
            public boolean wasEverMarked() {
                return marked.getAsBoolean() || SEEN_MARKED.contains(stat);
            }

            @Override
            public void close() {
                SEEN_MARKED.remove(stat);
            }
        };
    }

    /** {@link #watchPanel} over the current project's workspaces. */
    static PanelRunMark watchPanel(Statistics stat) {
        java.util.List<org.gephi.desktop.statistics.api.StatisticsModelUI> models = new java.util.ArrayList<>();
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc == null ? null : pc.getCurrentProject();
        if (project != null) {
            for (Workspace w : project.getWorkspaces()) {
                org.gephi.desktop.statistics.api.StatisticsModelUI m =
                    w.getLookup().lookup(org.gephi.desktop.statistics.api.StatisticsModelUI.class);
                if (m != null) {
                    models.add(m);
                }
            }
        }
        return watchPanel(stat, models, Lookup.getDefault().lookupAll(StatisticsUI.class));
    }

    /**
     * Run a statistic through Gephi's Statistics panel when the desktop interface is present, so
     * the panel shows it running, its result and its report, as if the user had clicked Run.
     * Without the panel (headless, tests) the statistic runs directly. Returns true when the
     * deadline stopped it; throws when Gephi reports a failure or the run never finishes.
     */
    static boolean executeStatistic(Statistics stat, GraphModel gm, long timeoutMs,
        org.gephi.desktop.statistics.api.StatisticsControllerUI panel,
        java.util.function.Consumer<Runnable> onEdt) throws InterruptedException {
        return executeStatistic(stat, gm, timeoutMs, panel, onEdt, s -> PanelRunMark.TRUSTED);
    }

    static boolean executeStatistic(Statistics stat, GraphModel gm, long timeoutMs,
        org.gephi.desktop.statistics.api.StatisticsControllerUI panel,
        java.util.function.Consumer<Runnable> onEdt,
        java.util.function.Function<Statistics, PanelRunMark> watch) throws InterruptedException {
        if (panel == null) {
            if (stat instanceof org.gephi.utils.longtask.spi.LongTask) {
                org.gephi.utils.longtask.spi.LongTask task = (org.gephi.utils.longtask.spi.LongTask) stat;
                return runWithDeadline(() -> runOnStatisticThread(() -> stat.execute(gm), task::cancel),
                    task, timeoutMs);
            }
            runOnStatisticThread(() -> stat.execute(gm), () -> { });
            return false;
        }
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> failure =
            new java.util.concurrent.atomic.AtomicReference<>();
        org.gephi.utils.longtask.api.LongTaskListener listener =
            new org.gephi.utils.longtask.api.LongTaskListener() {
                @Override
                public void taskFinished(org.gephi.utils.longtask.spi.LongTask task) {
                    done.countDown();
                }

                // Gephi 0.11.3+ reports a failed run here (and shows the user a dialog); earlier
                // versions never call it, and the wait below still ends at its limit.
                public void fatalError(Throwable t) {
                    failure.set(t);
                    done.countDown();
                }
            };
        // The watch is built on the interface thread right before execute(): the Statistics window
        // binds the panel to a newly selected workspace on that same thread, so whichever model
        // the panel marks is one being watched. If the hand-over times out before this step
        // starts, the step must not start the statistic later, behind the caller's back; if it
        // has already started, it is waited for.
        final java.util.concurrent.atomic.AtomicInteger handOver = new java.util.concurrent.atomic.AtomicInteger();
        final java.util.concurrent.atomic.AtomicReference<PanelRunMark> markRef =
            new java.util.concurrent.atomic.AtomicReference<>();
        final java.util.concurrent.CountDownLatch handedOver = new java.util.concurrent.CountDownLatch(1);
        Runnable onInterfaceThread = () -> {
            if (!handOver.compareAndSet(HAND_OVER_PENDING, HAND_OVER_STARTED)) {
                return;
            }
            try {
                markRef.set(watch.apply(stat));
                panel.execute(stat, listener);
            } finally {
                handedOver.countDown();
            }
        };
        try {
            onEdt.accept(onInterfaceThread);
        } catch (RuntimeException e) {
            if (handOver.compareAndSet(HAND_OVER_PENDING, HAND_OVER_ABANDONED)) {
                throw e;
            }
            if (!handedOver.await(STATISTIC_STOP_GRACE_MS, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                throw e;
            }
        }
        PanelRunMark mark = markRef.get();
        if (mark == null) {
            throw new IllegalStateException("Gephi's Statistics panel was not reached");
        }
        try {
            // The panel marks a statistic inside execute() before handing it over, so one never
            // marked was declined (its model not yet bound after a workspace change) and will
            // never run or report back. It is run directly instead.
            if (done.getCount() > 0 && !mark.wasEverMarked()) {
                LOGGER.info("Gephi's Statistics panel did not take " + stat.getClass().getSimpleName()
                    + "; running it directly");
                return executeStatistic(stat, gm, timeoutMs, null, onEdt, watch);
            }
            return awaitPanelResult(stat, timeoutMs, done, failure, mark);
        } finally {
            mark.close();
        }
    }

    private static boolean awaitPanelResult(Statistics stat, long timeoutMs,
        java.util.concurrent.CountDownLatch done, java.util.concurrent.atomic.AtomicReference<Throwable> failure,
        PanelRunMark mark) throws InterruptedException {
        boolean stopped = false;
        long wait = timeoutMs > 0 ? timeoutMs : STATISTIC_WAIT_CAP_MS;
        PanelWait outcome = awaitPanelRun(done, wait, mark);
        if (outcome == PanelWait.LOST) {
            throw new IllegalStateException(stat.getClass().getSimpleName() + " ended in Gephi's Statistics"
                + " panel without reporting back, so its result cannot be confirmed; run it again.");
        }
        if (outcome == PanelWait.TIMED_OUT) {
            if (timeoutMs <= 0 || !(stat instanceof org.gephi.utils.longtask.spi.LongTask)) {
                throw new IllegalStateException("The statistic is still running in Gephi after "
                    + (wait / 60000) + " minutes; stop it from Gephi's Statistics panel.");
            }
            ((org.gephi.utils.longtask.spi.LongTask) stat).cancel();
            stopped = true;
            if (!done.await(STATISTIC_STOP_GRACE_MS, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("The statistic was stopped at its deadline but has"
                    + " not finished; restart Gephi if it keeps running.");
            }
        }
        if (failure.get() != null) {
            throw new RuntimeException(failure.get());
        }
        return stopped;
    }

    /**
     * Start a layout through Gephi's LayoutController, so the Layout panel shows the algorithm,
     * the settings used and the running state. Selecting a layout reloads the settings Gephi
     * saved for it, so the settings go on after selection, starting from the layout's defaults
     * as a directly run layout does; selecting it again makes the panel show them. Returns the
     * setting names that matched no property.
     *
     * <p>Selection and settings are two event-thread tasks. In a workspace with no saved settings
     * for the layout, Gephi's Layout panel answers the selection by posting a load of the default
     * preset to the event thread, and with no preset saved that load resets the layout to its
     * defaults. Posted inside one task, the reset ran after the settings and the layout ran on
     * defaults; the second task is queued behind it, so the settings go on last.
     */
    static java.util.List<String> startLayoutThroughController(org.gephi.layout.api.LayoutController lc,
        Layout layout, Map<String, Object> properties,
        int iterations,
        java.util.function.Consumer<Runnable> onEdt) {
        java.util.List<String> unapplied = new java.util.ArrayList<>();
        onEdt.accept(() -> lc.setLayout(layout));
        onEdt.accept(() -> {
            try {
                layout.resetPropertiesValues();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "resetPropertiesValues failed for " + layout, e);
            }
            unapplied.addAll(applyLayoutProperties(layout, properties));
            lc.setLayout(layout);
            lc.executeLayout(iterations);
        });
        return unapplied;
    }

    private void onEdt(Runnable r) {
        runOnEDT(() -> {
            r.run();
            return null;
        });
    }

    private static org.gephi.layout.api.LayoutController layoutController() {
        org.gephi.layout.api.LayoutController lc =
            Lookup.getDefault().lookup(org.gephi.layout.api.LayoutController.class);
        return lc != null && lc.getModel() != null ? lc : null;
    }

    private JsonObject runStatistic(String builderName, Map<String, Object> params) {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            final GraphModel gm = currentGraphModel();

            // Find statistics builder by name
            StatisticsBuilder matchedBuilder = null;
            for (StatisticsBuilder sb : Lookup.getDefault().lookupAll(StatisticsBuilder.class)) {
                String name = sb.getName();
                LOGGER.fine("MCP: Found StatisticsBuilder: " + name + " (" + sb.getClass().getName() + ")");
                if (name.equalsIgnoreCase(builderName)
                    || sb.getClass().getSimpleName().toLowerCase().contains(builderName.toLowerCase())) {
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
                    } catch (Exception e) { /* skip */
                    }
                }
            }
            if (matchedBuilder == null) {
                return error("Statistics not found: " + builderName);
            }

            Statistics stat = matchedBuilder.getStatistics();

            // Set parameters via reflection; collect the ones that did not land so a
            // mistyped name is reported instead of silently ignored (a typo used to be
            // indistinguishable from a correctly-parameterised run).
            java.util.List<String> unappliedParams = new java.util.ArrayList<>();
            long timeoutMs = 0;
            if (params != null && params.get("timeout_ms") instanceof Number) {
                params = new java.util.HashMap<>(params);
                timeoutMs = ((Number) params.remove("timeout_ms")).longValue();
            }
            if (params != null) {
                for (Map.Entry<String, Object> e : params.entrySet()) {
                    if (!setViaReflection(stat, e.getKey(), e.getValue())) {
                        unappliedParams.add(e.getKey());
                    }
                }
            }

            // Plugin statistics are often LongTasks that assume the UI gave them a
            // progress ticket and call it without null checks (e.g. CWTS Leiden).
            // Provide a no-op ticket so they run outside the statistics dialog.
            if (stat instanceof org.gephi.utils.longtask.spi.LongTask) {
                ((org.gephi.utils.longtask.spi.LongTask) stat).setProgressTicket(NOOP_TICKET);
            }

            // Execute, stopping it at the deadline when one was given
            String dynamicProblem = dynamicStatisticProblem(stat, gm);
            if (dynamicProblem != null) {
                return error(dynamicProblem);
            }
            boolean stopped;
            boolean stoppedOnRequest;
            String busy = claimStatistic(stat, matchedBuilder.getName());
            if (busy != null) {
                return error(busy);
            }
            try {
                stopped = executeStatistic(stat, gm, timeoutMs,
                    statisticsPanelFor(ws,
                        Lookup.getDefault().lookup(org.gephi.desktop.statistics.api.StatisticsControllerUI.class)),
                    this::onEdt,
                    GephiControlService::watchPanel);
            } finally {
                RUNNING_STATISTICS.remove(stat);
                stoppedOnRequest = STOP_REQUESTED.remove(stat);
            }
            if (stoppedOnRequest) {
                JsonObject r = error(matchedBuilder.getName() + " was stopped before it finished;"
                    + " its column was not updated.");
                r.addProperty("stopped", true);
                return r;
            }
            if (stopped) {
                // A cancelled run writes nothing, so the column still holds the previous run.
                JsonObject r = error(matchedBuilder.getName() + " did not finish within "
                    + (timeoutMs / 1000) + " s and was stopped; its column was not updated.");
                r.addProperty("stopped", true);
                return r;
            }

            // Build result
            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.addProperty("statistic", matchedBuilder.getName());
            if (!unappliedParams.isEmpty()) {
                JsonArray ua = new JsonArray();
                for (String k : unappliedParams) {
                    ua.add(k);
                }
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
            // The line Gephi's Statistics panel shows for this run. Display text, possibly
            // rounded; its use is a headline for statistics with no getter above, such as
            // those from other Gephi plugins.
            String shown = panelResult(stat, Lookup.getDefault().lookupAll(StatisticsUI.class));
            if (shown != null) {
                r.addProperty("panel_result", shown);
            }

            // Get the report
            try {
                String report = stat.getReport();
                if (report != null) {
                    r.addProperty("report_available", true);
                    r.addProperty("report_html", report);
                }
            } catch (Exception e) { /* no report */
            }

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
                    if (converted == null) {
                        return false;  // name matched, value did not convert
                    }
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
                        if (converted == null) {
                            return false;
                        }
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
                if (ec.toString().equalsIgnoreCase(want)) {
                    return ec;
                }
            }
            return null;
        }
        return convertLayoutProperty(val, type);
    }

    /**
     * The result line the statistic's own UI gives for this run (what Gephi's Statistics panel
     * shows), or null when no UI is registered for its class, it shows nothing, or it fails.
     * Matched by exact class, as Gephi's Statistics panel matches them.
     */
    static String panelResult(Statistics stat, java.util.Collection<? extends StatisticsUI> uis) {
        for (StatisticsUI ui : uis) {
            if (!stat.getClass().equals(ui.getStatisticsClass())) {
                continue;
            }
            try {
                String shown = ui.getValue(stat);
                return shown == null || shown.isBlank() ? null : shown.trim();
            } catch (RuntimeException e) {
                LOGGER.log(Level.FINE, "Statistic UI gave no result for " + stat.getClass().getName(), e);
                return null;
            }
        }
        return null;
    }

    private void tryAddResult(JsonObject r, Object obj, String getter, String jsonKey) {
        try {
            java.lang.reflect.Method m = obj.getClass().getMethod(getter);
            Object val = m.invoke(obj);
            if (val instanceof Number) {
                r.addProperty(jsonKey, (Number) val);
            } else if (val instanceof Boolean) {
                r.addProperty(jsonKey, (Boolean) val);
            } else if (val != null) {
                r.addProperty(jsonKey, val.toString());
            }
        } catch (NoSuchMethodException e) { /* method not available for this statistic */
        } catch (Exception e) {
            LOGGER.fine("Could not get " + getter + ": " + e.getMessage());
        }
    }

    public JsonObject computeModularity(double resolution) {
        return computeModularity(resolution, 0);
    }

    public JsonObject computeModularity(double resolution, long timeoutMs) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("resolution", resolution);
        if (timeoutMs > 0) {
            params.put("timeout_ms", timeoutMs);
        }
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

    /** Nodes whose degree falls outside [min, max]; a max of 0 or less means no upper bound. */
    static java.util.List<Node> nodesOutsideDegreeRange(Graph g, int minDegree, int maxDegree) {
        java.util.List<Node> out = new java.util.ArrayList<>();
        for (Node n : g.getNodes().toArray()) {
            int deg = g.getDegree(n);
            if (deg < minDegree || (maxDegree > 0 && deg > maxDegree)) {
                out.add(n);
            }
        }
        return out;
    }

    /**
     * Removes the nodes outside the degree range. They are chosen after the write lock is taken,
     * so another write landing first cannot leave the list describing a graph that has changed.
     */
    static int removeNodesOutsideDegreeRange(Graph g, int minDegree, int maxDegree) {
        lockWrite(g);
        try {
            java.util.List<Node> toRemove = nodesOutsideDegreeRange(g, minDegree, maxDegree);
            for (Node n : toRemove) {
                g.removeNode(n);
            }
            return toRemove.size();
        } finally {
            unlockWrite(g);
        }
    }

    /** Edges whose weight falls outside [min, max]; a max of 0 or less means no upper bound. */
    static java.util.List<Edge> edgesOutsideWeightRange(Graph g, double minWeight, double maxWeight) {
        java.util.List<Edge> out = new java.util.ArrayList<>();
        for (Edge e : g.getEdges().toArray()) {
            double w = e.getWeight();
            if (w < minWeight || (maxWeight > 0 && w > maxWeight)) {
                out.add(e);
            }
        }
        return out;
    }

    /** Removes the edges outside the weight range, chosen under the write lock. */
    static int removeEdgesOutsideWeightRange(Graph g, double minWeight, double maxWeight) {
        lockWrite(g);
        try {
            java.util.List<Edge> toRemove = edgesOutsideWeightRange(g, minWeight, maxWeight);
            for (Edge e : toRemove) {
                g.removeEdge(e);
            }
            return toRemove.size();
        } finally {
            unlockWrite(g);
        }
    }

    public JsonObject filterByDegreeRange(int minDegree, int maxDegree, boolean dryRun) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            Graph g = currentGraphModel().getGraph();
            if (dryRun) {
                java.util.List<Node> toRemove = nodesOutsideDegreeRange(g, minDegree, maxDegree);
                JsonObject r = success("Dry run: " + toRemove.size() + " nodes would be removed");
                r.addProperty("would_remove", toRemove.size());
                r.addProperty("would_remain", g.getNodeCount() - toRemove.size());
                r.addProperty("dry_run", true);
                return r;
            }
            int removed = removeNodesOutsideDegreeRange(g, minDegree, maxDegree);
            JsonObject r = success("Filtered by degree [" + minDegree + ", " + maxDegree + "]");
            r.addProperty("removed", removed);
            r.addProperty("remaining_nodes", g.getNodeCount());
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject filterByEdgeWeight(double minWeight, double maxWeight, boolean dryRun) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            Graph g = currentGraphModel().getGraph();
            if (dryRun) {
                java.util.List<Edge> toRemove = edgesOutsideWeightRange(g, minWeight, maxWeight);
                JsonObject r = success("Dry run: " + toRemove.size() + " edges would be removed");
                r.addProperty("would_remove", toRemove.size());
                r.addProperty("would_remain", g.getEdgeCount() - toRemove.size());
                r.addProperty("dry_run", true);
                return r;
            }
            int removed = removeEdgesOutsideWeightRange(g, minWeight, maxWeight);
            JsonObject r = success("Filtered edges by weight [" + minWeight + ", " + maxWeight + "]");
            r.addProperty("removed", removed);
            r.addProperty("remaining_edges", g.getEdgeCount());
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    // ─── Preview Settings ────────────────────────────────────────────

    public JsonObject getPreviewSettings() {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
            PreviewModel pm = pc.getModel(ws);
            if (pm == null) {
                return error("Preview model not available");
            }

            JsonObject settings = new JsonObject();
            // Get commonly used properties
            for (PreviewProperty prop : pm.getProperties().getProperties()) {
                String name = prop.getName();
                Object val = prop.getValue();
                if (val != null) {
                    if (val instanceof Color) {
                        Color c = (Color) val;
                        settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(),
                            c.getBlue()));
                    } else if (val instanceof Number) {
                        settings.addProperty(name, (Number) val);
                    } else if (val instanceof Boolean) {
                        settings.addProperty(name, (Boolean) val);
                    } else if (val instanceof java.awt.Font) {
                        java.awt.Font f = (java.awt.Font) val;
                        String style = f.isBold() && f.isItalic() ? "BoldItalic" : f.isBold() ? "Bold"
                            : f.isItalic() ? "Italic" : "Plain";
                        settings.addProperty(name, f.getFamily() + " " + f.getSize() + " " + style);
                    } else if (val instanceof EdgeColor) {
                        EdgeColor ec = (EdgeColor) val;
                        if (ec.getMode() == EdgeColor.Mode.ORIGINAL) {
                            settings.addProperty(name, "original");
                        } else if (ec.getMode() == EdgeColor.Mode.MIXED) {
                            settings.addProperty(name, "mixed");
                        } else if (ec.getCustomColor() != null) {
                            Color c = ec.getCustomColor();
                            settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(),
                                c.getBlue()));
                        } else {
                            settings.addProperty(name, ec.getMode().toString().toLowerCase());
                        }
                    } else if (val instanceof DependantColor) {
                        DependantColor dc = (DependantColor) val;
                        if (dc.getMode() == DependantColor.Mode.PARENT) {
                            settings.addProperty(name, "parent");
                        } else if (dc.getMode() == DependantColor.Mode.DARKER) {
                            settings.addProperty(name, "darker");
                        } else if (dc.getCustomColor() != null) {
                            Color c = dc.getCustomColor();
                            settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(),
                                c.getBlue()));
                        } else {
                            settings.addProperty(name, "parent");
                        }
                    } else if (val instanceof DependantOriginalColor) {
                        DependantOriginalColor doc = (DependantOriginalColor) val;
                        if (doc.getMode() == DependantOriginalColor.Mode.ORIGINAL) {
                            settings.addProperty(name, "original");
                        } else if (doc.getMode() == DependantOriginalColor.Mode.PARENT) {
                            settings.addProperty(name, "parent");
                        } else if (doc.getCustomColor() != null) {
                            Color c = doc.getCustomColor();
                            settings.addProperty(name, String.format("#%02x%02x%02x", c.getRed(), c.getGreen(),
                                c.getBlue()));
                        } else {
                            settings.addProperty(name, "original");
                        }
                    } else {
                        settings.addProperty(name, val.toString());
                    }
                }
            }

            // Include background color if not already captured by the main loop
            try {
                // Reported under the name gephi_set_preview_settings takes, so a value can be
                // read back by the key it was written with.
                Object bgVal = pm.getProperties().getValue(PreviewProperty.BACKGROUND_COLOR);
                if (bgVal instanceof Color) {
                    Color c = (Color) bgVal;
                    settings.addProperty("background.color", String.format("#%02x%02x%02x", c.getRed(),
                        c.getGreen(), c.getBlue()));
                }
            } catch (Exception ignored) {
                // No background colour in these settings: it is left out.
            }

            JsonObject r = new JsonObject();
            r.addProperty("success", true);
            r.add("settings", settings);
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setPreviewSettings(Map<String, Object> settings) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
            PreviewModel pm = pc.getModel(ws);
            if (pm == null) {
                return error("Preview model not available");
            }
            PreviewProperties props = pm.getProperties();
            // Gephi's renderers register every preview property when the model is made. A name
            // that is neither registered nor one of the model's plain values is a typo: storing
            // it would change nothing in an export while the reply counted it as set.
            boolean registryKnown = props.getProperties().length > 0;

            int set = 0;
            java.util.List<String> unknown = new java.util.ArrayList<>();
            java.util.List<String> unapplied = new java.util.ArrayList<>();
            for (Map.Entry<String, Object> e : settings.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                if (val == null) {
                    continue;  // Skip null values to avoid corrupting preview model
                }
                // Background color: set on the preview model under Gephi's canonical key
                // (PreviewProperty.BACKGROUND_COLOR) so the Preview panel, the renderers,
                // and exportPng's export-time read all share one source of truth. The old
                // cached exportBackgroundColor field was process-wide sticky state: once
                // set it tinted every later export in every workspace and project, even
                // after the user changed the background in Gephi's own Preview panel.
                if ("background.color".equalsIgnoreCase(key) || "backgroundColor".equalsIgnoreCase(key)
                    || PreviewProperty.BACKGROUND_COLOR.equalsIgnoreCase(key)) {
                    Color bgColor = parseHexColor(val.toString());
                    if (bgColor == null) {
                        LOGGER.warning("MCP: Invalid background color: " + val);
                        unapplied.add(key);
                        continue;
                    }
                    PreviewProperty bgProp = props.getProperty(PreviewProperty.BACKGROUND_COLOR);
                    if (bgProp != null) {
                        bgProp.setValue(bgColor);
                    } else {
                        props.putValue(PreviewProperty.BACKGROUND_COLOR, bgColor);
                    }
                    set++;
                    continue;
                }

                PreviewProperty prop = props.getProperty(key);
                if (prop == null) {
                    if (registryKnown && !props.hasProperty(key)) {
                        unknown.add(key);
                        continue;
                    }
                    // A plain model value (or a model whose registry is empty): putValue works
                    // regardless and renderers read it at export time. Non-scalar values are
                    // never valid preview properties; storing one corrupts the model.
                    if (val instanceof Map || val instanceof List) {
                        LOGGER.warning("MCP: Skipping non-scalar preview value for " + key);
                        unapplied.add(key);
                        continue;
                    }
                    props.putValue(key, coercePlainPreviewValue(val));
                    set++;
                    continue;
                }
                try {
                    if (applyPreviewValue(prop, val)) {
                        set++;
                    } else {
                        unapplied.add(key);
                    }
                } catch (RuntimeException ex) {
                    LOGGER.warning("MCP: Failed to set preview property " + key + ": " + ex.getMessage());
                    unapplied.add(key);
                }
            }
            JsonObject r = success("Set " + set + " preview properties");
            r.addProperty("properties_set", set);
            if (!unknown.isEmpty()) {
                JsonArray a = new JsonArray();
                unknown.forEach(a::add);
                r.add("unknown_properties", a);
            }
            if (!unapplied.isEmpty()) {
                JsonArray a = new JsonArray();
                unapplied.forEach(a::add);
                r.add("unapplied_properties", a);
            }
            if (!unknown.isEmpty() || !unapplied.isEmpty()) {
                String notProperties = unknown.isEmpty() ? "" : "Not preview properties (see "
                    + "gephi_get_preview_settings for the names): " + unknown + ".";
                String unread = unapplied.isEmpty() ? "" : "Values that could not be read for their "
                    + "setting, left unchanged: " + unapplied + ".";
                r.addProperty("warning", (notProperties + " " + unread).trim());
            }
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /** A colour from "#rrggbb" or "rrggbb", or null when the text is not one. */
    static Color parseHexColor(String text) {
        String hex = text.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6) {
            return null;
        }
        try {
            return new Color(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** A plain preview value: "true"/"false" as booleans, numbers as floats, other text as given. */
    static Object coercePlainPreviewValue(Object val) {
        if (val instanceof String) {
            String sv = ((String) val).trim();
            if (sv.equalsIgnoreCase("true") || sv.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(sv);
            }
            try {
                return Float.parseFloat(sv);
            } catch (NumberFormatException ignore) {
                return val;
            }
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        return val;
    }

    /** A true/false from a JSON boolean or the text "true"/"false", else null. */
    static Boolean previewBoolean(Object val) {
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        String s = val.toString().trim();
        if ("true".equalsIgnoreCase(s)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(s)) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * Sets one registered preview property from a JSON value, converting it to the property's
     * type. Returns false, changing nothing, when the value cannot be read as that type: a
     * reading that guessed (any unrecognised text as false, say) would report success for a
     * setting it never made.
     */
    private boolean applyPreviewValue(PreviewProperty prop, Object val) {
        Class<?> type = prop.getType();
        if (type == Color.class) {
            Color c = val instanceof String ? parseHexColor((String) val) : null;
            if (c == null) {
                return false;
            }
            prop.setValue(c);
            return true;
        }
        if (type == Boolean.class || type == boolean.class) {
            Boolean b = previewBoolean(val);
            if (b == null) {
                return false;
            }
            prop.setValue(b);
            return true;
        }
        if (type == Float.class || type == float.class) {
            try {
                prop.setValue(Float.parseFloat(val.toString().trim()));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (type == Integer.class || type == int.class) {
            // JSON numbers arrive as doubles, so 30 reads as "30.0": parse as a number, then
            // accept it only when it is whole.
            try {
                double d = Double.parseDouble(val.toString().trim());
                if (d != Math.rint(d) || Math.abs(d) > Integer.MAX_VALUE) {
                    return false;
                }
                prop.setValue((int) d);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (type == java.awt.Font.class) {
            if (!(val instanceof String)) {
                return false;
            }
            prop.setValue(parsePreviewFont((String) val));
            return true;
        }
        if (!(val instanceof String)) {
            return false;
        }
        if (type == String.class) {
            // Plugin renderers register text settings (a column name, a legend title); the
            // value is kept as written, since column ids and titles are case-sensitive.
            prop.setValue(val);
            return true;
        }
        String s = val.toString().trim().toLowerCase();
        if (type == DependantColor.class) {
            if ("parent".equals(s)) {
                prop.setValue(new DependantColor(DependantColor.Mode.PARENT));
            } else if ("darker".equals(s)) {
                prop.setValue(new DependantColor(DependantColor.Mode.DARKER));
            } else if (parseHexColor(s) != null && s.startsWith("#")) {
                prop.setValue(new DependantColor(parseHexColor(s)));
            } else {
                return false;
            }
            return true;
        }
        if (type == DependantOriginalColor.class) {
            if ("parent".equals(s)) {
                prop.setValue(new DependantOriginalColor(DependantOriginalColor.Mode.PARENT));
            } else if ("original".equals(s)) {
                prop.setValue(new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL));
            } else if (parseHexColor(s) != null && s.startsWith("#")) {
                prop.setValue(new DependantOriginalColor(parseHexColor(s)));
            } else {
                return false;
            }
            return true;
        }
        if (type == EdgeColor.class) {
            if ("source".equals(s) || "target".equals(s)) {
                // For "source"/"target": color edges individually instead of using
                // EdgeColor mode (which corrupts SVG rendering in Gephi 0.10)
                boolean useSource = "source".equals(s);
                Graph graph = currentGraphModel().getGraph();
                java.util.Map<Node, Color> nodeColors = new java.util.HashMap<>();
                for (Node n : graph.getNodes().toArray()) {
                    nodeColors.put(n, n.getColor());
                }
                for (Edge edge : graph.getEdges().toArray()) {
                    Color c = nodeColors.get(useSource ? edge.getSource() : edge.getTarget());
                    if (c != null) {
                        edge.setColor(c);
                    }
                }
                prop.setValue(new EdgeColor(EdgeColor.Mode.ORIGINAL));
            } else if ("mixed".equals(s)) {
                prop.setValue(new EdgeColor(EdgeColor.Mode.MIXED));
            } else if ("original".equals(s)) {
                prop.setValue(new EdgeColor(EdgeColor.Mode.ORIGINAL));
            } else if (parseHexColor(s) != null && s.startsWith("#")) {
                prop.setValue(new EdgeColor(parseHexColor(s)));
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * A font from text like "Courier New 12 Bold": everything before the first digit is the
     * name, the first number the size, and the words after it the style.
     */
    static java.awt.Font parsePreviewFont(String text) {
        String fontStr = text.trim();
        String name = "Arial";
        int fontSize = 12;
        int fontStyle = java.awt.Font.PLAIN;
        int numStart = -1;
        for (int ci = 0; ci < fontStr.length(); ci++) {
            if (Character.isDigit(fontStr.charAt(ci))) {
                numStart = ci;
                break;
            }
        }
        if (numStart > 0) {
            name = fontStr.substring(0, numStart).trim();
            String[] rest = fontStr.substring(numStart).trim().split("\\s+");
            try {
                fontSize = Integer.parseInt(rest[0]);
            } catch (NumberFormatException ignored) {
                // Not a number: the default font size stays.
            }
            for (int pi = 1; pi < rest.length; pi++) {
                if ("Bold".equalsIgnoreCase(rest[pi])) {
                    fontStyle |= java.awt.Font.BOLD;
                } else if ("Italic".equalsIgnoreCase(rest[pi])) {
                    fontStyle |= java.awt.Font.ITALIC;
                }
            }
        } else if (numStart < 0) {
            name = fontStr;
        }
        return new java.awt.Font(name, fontStyle, fontSize);
    }

    // ─── Export ───────────────────────────────────────────────────────

    /**
     * The file an export writes, with its folder created first, so saving into a new folder
     * works. If the folder cannot be made, the write that follows fails and reports it.
     */
    static File outputFile(String filePath) {
        File file = new File(filePath).getAbsoluteFile();
        File dir = file.getParentFile();
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            LOGGER.fine("Could not create " + dir);
        }
        return file;
    }

    public JsonObject exportGexf(String filePath) {
        return exportGexf(filePath, true);
    }

    /**
     * @param visible export the filtered visible graph (true — the historical behaviour)
     *        or the full graph. Either way the response self-declares which view was
     *        written via addViewInfo, so a filtered export is never silent about it.
     */
    public JsonObject exportGexf(String filePath, boolean visible) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter("gexf");
            if (exporter == null) {
                return error("GEXF exporter not available");
            }
            if (exporter instanceof GraphExporter) {
                ((GraphExporter) exporter).setExportVisible(visible);
                ((GraphExporter) exporter).setWorkspace(ws);
            }
            ec.exportFile(outputFile(filePath), exporter);
            JsonObject r = success("Exported to " + filePath);
            addViewInfo(r, currentGraphModel(), visible);
            return r;
        } catch (Exception e) {
            return failure("Export failed: ", e);
        }
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
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter("gexf");
            if (exporter == null) {
                return error("GEXF exporter not available");
            }
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
        } catch (Exception e) {
            return failure("Export failed: ", e);
        }
    }

    public JsonObject exportPng(String filePath, int w, int h) {
        // Runs on the calling thread: rendering the export and compositing the
        // background below (ImageIO.read, a full BufferedImage copy, ImageIO.write —
        // at a default 1920x1080) needs nothing from the EDT. No explicit preview
        // refresh here either — PNGExporter.execute() calls PreviewController.refreshPreview()
        // itself before rendering, so doing it again here would just rebuild it twice.
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter("png");
            if (exporter == null) {
                return error("PNG exporter not available");
            }
            if (!(exporter instanceof PNGExporter)) {
                return error("Unexpected PNG exporter implementation: " + exporter.getClass().getName());
            }
            PNGExporter pngExporter = (PNGExporter) exporter;
            pngExporter.setWidth(w);
            pngExporter.setHeight(h);

            if (exporter instanceof GraphExporter) {
                ((GraphExporter) exporter).setWorkspace(ws);
            }

            ec.exportFile(outputFile(filePath), exporter);

            // Post-process: composite onto the preview model's background color.
            // Gephi's PNG exporter renders a transparent background; this fills it.
            // The color is read from the preview model AT EXPORT TIME, so a change in
            // Gephi's own Preview panel (or another workspace's settings) is honoured
            // rather than overridden by a stale process-wide copy.
            Color bgColor = previewBackgroundColor(ws);
            if (bgColor != null && !bgColor.equals(Color.WHITE)) {
                BufferedImage exported = ImageIO.read(new File(filePath));
                if (exported != null) {
                    BufferedImage result = new BufferedImage(exported.getWidth(), exported.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = result.createGraphics();
                    g2d.setColor(bgColor);
                    g2d.fillRect(0, 0, result.getWidth(), result.getHeight());
                    g2d.drawImage(exported, 0, 0, null);
                    g2d.dispose();
                    ImageIO.write(result, "PNG", new File(filePath));
                }
            }

            return success("Exported to " + filePath);
        } catch (Exception e) {
            return failure("Export failed: ", e);
        }
    }

    /** The workspace's preview background color, or null when none is available. */
    private static Color previewBackgroundColor(Workspace ws) {
        PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel pm = pc != null ? pc.getModel(ws) : null;
        if (pm == null) {
            return null;
        }
        Object bg = pm.getProperties().getValue(PreviewProperty.BACKGROUND_COLOR);
        // Legacy spelling: earlier plugin builds stored the color under "background.color".
        if (!(bg instanceof Color)) {
            bg = pm.getProperties().getValue("background.color");
        }
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
        if (ws == null) {
            return error("No project open");
        }

        File targetFile = outputFile(filePath);
        File targetDir = targetFile.getParentFile();
        if (targetDir != null && !targetDir.isDirectory()) {
            return error("Could not create the folder " + targetDir + " for the screenshot");
        }

        // ScreenshotController is not independently registered in Lookup — it is only
        // reachable via VisualizationController.getScreenshotController() (the same
        // VisualizationController singleton getSelection/focusView already use).
        org.gephi.visualization.api.VisualizationController vc = Lookup.getDefault()
            .lookup(org.gephi.visualization.api.VisualizationController.class);
        if (vc == null) {
            return error("Visualization controller not available");
        }
        org.gephi.visualization.api.ScreenshotController sc = vc.getScreenshotController();
        if (sc == null) {
            return error("Screenshot controller not available");
        }
        // Until the graph canvas exists and draws, Gephi's screenshot fails with "No rendering
        // engine available" (seen on the first call after Gephi starts). Wait for it briefly.
        try {
            if (!awaitRendererReady(() -> vizEngine().map(e -> java.util.OptionalLong.of(e.getFrameCount()))
                .orElse(java.util.OptionalLong.empty()), RENDERER_READY_BUDGET_MS)) {
                return error("Gephi's graph view is not drawing yet, so there is nothing to capture: show the"
                    + " Overview tab, wait for the graph to appear, and try again");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return error("Interrupted while waiting for Gephi's graph view");
        }

        // This takeScreenshot writes straight to the file, whatever the toolbar's screenshot
        // settings, never opens a dialog, and completes once the PNG is written. It is called
        // from the HTTP thread: the Future must not be waited on from the interface thread.
        // Gephi's first capture after it starts can fail (its image buffer is not ready yet),
        // so a failed capture is tried once more.
        String problem = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            java.util.concurrent.Future<File> future =
                sc.takeScreenshot(scaleFactor, transparentBackground, targetFile);
            try {
                future.get(15, java.util.concurrent.TimeUnit.SECONDS);
                JsonObject r = success("Exported to " + filePath);
                r.addProperty("scale_factor", scaleFactor);
                r.addProperty("selection_aware", true);
                return r;
            } catch (java.util.concurrent.TimeoutException e) {
                future.cancel(true);
                return error("Screenshot did not complete within 15s — the render engine may be busy, "
                    + "retry, or fully restart Gephi if this persists");
            } catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                problem = String.valueOf(cause.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return error("Interrupted while waiting for the screenshot");
            }
        }
        return error("Screenshot export failed: " + problem);
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
            if (last == expected) {
                return last;
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return last;
            }
        }
        return last;
    }

    /**
     * True when the visible drawing is wider than it is tall, so it prints landscape. PDFs are
     * always US Letter; only the orientation follows the layout.
     */
    static boolean landscapeFor(Graph g) {
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        lockRead(g);
        try {
            for (Node n : g.getNodes().toArray()) {
                minX = Math.min(minX, n.x());
                maxX = Math.max(maxX, n.x());
                minY = Math.min(minY, n.y());
                maxY = Math.max(maxY, n.y());
            }
        } finally {
            g.readUnlock();
        }
        return maxX - minX > maxY - minY;
    }

    public JsonObject exportPdf(String filePath) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            Graph g = currentGraphModel().getGraph();
            if (g.getNodeCount() == 0) {
                return error("Cannot export PDF: graph has no nodes");
            }
            // No explicit preview refresh here — PDFExporter.execute() calls
            // PreviewController.refreshPreview() itself before rendering.
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter("pdf");
            if (exporter == null) {
                return error("PDF exporter not available");
            }
            if (!(exporter instanceof PDFExporter)) {
                return error("Unexpected PDF exporter implementation: " + exporter.getClass().getName());
            }
            boolean landscape = landscapeFor(currentGraphModel().getGraphVisible());
            PDFExporter pdf = (PDFExporter) exporter;
            pdf.setPageSize(PDRectangle.LETTER);
            pdf.setLandscape(landscape);
            // Half-inch margins (in points), which any printer can reach.
            pdf.setMarginTop(36f);
            pdf.setMarginBottom(36f);
            pdf.setMarginLeft(36f);
            pdf.setMarginRight(36f);
            if (exporter instanceof GraphExporter) {
                exporter.setWorkspace(ws);
            }
            ec.exportFile(outputFile(filePath), exporter);
            JsonObject r = success("Exported to " + filePath);
            r.addProperty("page", landscape ? "US Letter, landscape (11 x 8.5 in)"
                : "US Letter, portrait (8.5 x 11 in)");
            return r;
        } catch (IllegalArgumentException e) {
            return error("Export failed: graph nodes may not be positioned — run a layout first");
        } catch (Exception e) {
            return failure("Export failed: ", e);
        }
    }

    public JsonObject exportSvg(String filePath) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            // No explicit preview refresh here — SVGExporter.execute() calls
            // PreviewController.refreshPreview() itself before rendering.
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter("svg");
            if (exporter == null) {
                return error("SVG exporter not available");
            }
            if (exporter instanceof GraphExporter) {
                exporter.setWorkspace(ws);
            }
            ec.exportFile(outputFile(filePath), exporter);
            return success("Exported to " + filePath);
        } catch (Exception e) {
            return failure("Export failed: ", e);
        }
    }

    public JsonObject exportGraphml(String filePath) {
        return exportGraphml(filePath, true);
    }

    /**
     * @param visible see exportGexf — same contract, response self-declares the view.
     */
    public JsonObject exportGraphml(String filePath, boolean visible) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter("graphml");
            if (exporter == null) {
                return error("GraphML exporter not available");
            }
            if (exporter instanceof GraphExporter) {
                ((GraphExporter) exporter).setExportVisible(visible);
                exporter.setWorkspace(ws);
            }
            ec.exportFile(outputFile(filePath), exporter);
            JsonObject r = success("Exported to " + filePath);
            addViewInfo(r, currentGraphModel(), visible);
            return r;
        } catch (Exception e) {
            return failure("Export failed: ", e);
        }
    }

    public JsonObject exportCsv(String filePath, String separator, String target) {
        // Runs on the calling thread: serialising the whole graph into a StringBuilder
        // and writing it to disk is bulk work with no Swing dependency — it has no
        // business on the EDT (see the threading note above setEdgeColor).
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        // Always use manual export — Gephi's built-in CSV exporter produces an adjacency matrix
        return exportCsvManual(filePath, separator, target);
    }

    private JsonObject exportCsvManual(String filePath, String separator, String target) {
        try {
            GraphModel gm = currentGraphModel();
            String csvText = buildCsv(gm, separator, target);
            try (java.io.Writer fw = new java.io.OutputStreamWriter(
                     new java.io.FileOutputStream(outputFile(filePath)), java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write(csvText);
            }
            JsonObject r = success("Exported to " + filePath);
            // CSV is built from the FULL graph (buildCsv walks gm.getGraph()) — declare
            // that, since the other exporters write the visible graph.
            addViewInfo(r, gm, false);
            return r;
        } catch (Exception e) {
            return failure("CSV export failed: ", e);
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
                    if (!col.isProperty()) {
                        sb.append(sep).append(csv(col.getTitle(), sep));
                    }
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
                } finally {
                    g.readUnlock();
                }
            }

            if ("edges".equalsIgnoreCase(target) || "both".equalsIgnoreCase(target)) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(csv("Source", sep)).append(sep).append(csv("Target", sep)).append(sep)
                    .append(csv("Weight", sep));
                for (Column col : gm.getEdgeTable()) {
                    if (!col.isProperty()) {
                        sb.append(sep).append(csv(col.getTitle(), sep));
                    }
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
                } finally {
                    g.readUnlock();
                }
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
        if (value == null) {
            value = "";
        }
        boolean needsQuote = value.contains(sep) || value.contains("\"")
            || value.contains("\n") || value.contains("\r");
        return needsQuote ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }

    // ─── Import ──────────────────────────────────────────────────────

    public JsonObject importFile(String filePath) {
        return importFile(filePath, null);
    }

    public JsonObject importFile(String filePath, Float maxNodeSize) {
        return importFile(filePath, maxNodeSize, null);
    }

    /**
     * Imports a file. By default ({@code mode} null or "new_workspace") the file gets its own
     * workspace, as in Gephi's own import dialog: a workspace's graph settings (time format, id
     * type, weight type) are fixed when it is created, so a new one is made to match the file.
     * An empty workspace that was open is then removed rather than left behind. "append" adds
     * the file to the current workspace instead, and fails with Gephi's report when the settings
     * do not match. The file's import warnings are returned as {@code import_issues}.
     * {@code maxNodeSize} caps imported node sizes when set; when null the file's own sizes are
     * preserved exactly, so an import followed by an export round-trips.
     */
    public JsonObject importFile(String filePath, Float maxNodeSize, String mode) {
        // Runs on the calling thread. Gephi's own import runs off the event dispatch thread,
        // and parsing a large file inside runOnEDT froze the UI and then blew its 15-second
        // budget, so the caller was told "Gephi's UI thread is unresponsive, fully quit and
        // reopen" while the import was in fact still running and went on to succeed.
        {
            File file = new File(filePath);
            if (!file.exists()) {
                return error("File not found: " + filePath);
            }
            try {
                ImportController ic = Lookup.getDefault().lookup(ImportController.class);
                Container c = ic.importFile(file);
                if (c == null) {
                    return error("Import failed - unsupported format or empty file");
                }

                return loadContainer(c, file.getName(), "Imported from " + file.getName(), maxNodeSize, mode);
            } catch (Exception e) {
                return failure("Import failed: ", e);
            }
        }
    }

    /** Largest graph the preferential-attachment generator is asked for. */
    static final int GENERATE_MAX_NODES = 100_000;
    /** The random generator tests every pair of nodes, so it is held to fewer. */
    static final int GENERATE_MAX_RANDOM_NODES = 20_000;
    /** Largest edge count a generated graph may have (exact for preferential attachment, expected for random). */
    static final long GENERATE_MAX_EXPECTED_EDGES = 2_000_000L;

    /**
     * Why a preferential-attachment graph of {@code nodes} nodes with {@code k} edges per node is
     * refused, or null when it may be made. Its edge count is exact: a star of {@code k} edges, then
     * {@code k} edges for each later node.
     */
    static String preferentialAttachmentRefusal(int nodes, int k) {
        if (nodes < 2 || nodes > GENERATE_MAX_NODES) {
            return "nodes must be between 2 and " + GENERATE_MAX_NODES;
        }
        if (k < 1 || k >= nodes) {
            return "edges_per_node must be at least 1 and less than nodes (" + nodes + ")";
        }
        long edges = k + (long) (nodes - k - 1) * k;
        if (edges > GENERATE_MAX_EXPECTED_EDGES) {
            return "A preferential-attachment graph of " + nodes + " nodes with " + k + " edges per node has "
                + edges + " edges, more than " + GENERATE_MAX_EXPECTED_EDGES + "; lower nodes or edges_per_node";
        }
        return null;
    }

    /**
     * Why a random graph of {@code nodes} nodes with wiring probability {@code p} is refused, or
     * null when it may be made. Its edge count is the expected one, {@code p} times the pairs.
     */
    static String randomGraphRefusal(int nodes, double p) {
        if (nodes < 2 || nodes > GENERATE_MAX_RANDOM_NODES) {
            return "nodes must be between 2 and " + GENERATE_MAX_RANDOM_NODES
                + " for the random model, which tests every pair of nodes";
        }
        if (!(p > 0 && p <= 1)) {
            return "wiring_probability must be greater than 0 and at most 1";
        }
        double expected = p * nodes * (nodes - 1.0) / 2;
        if (expected > GENERATE_MAX_EXPECTED_EDGES) {
            return "A random graph of " + nodes + " nodes with wiring_probability " + p + " expects about "
                + Math.round(expected) + " edges, more than " + GENERATE_MAX_EXPECTED_EDGES
                + "; lower nodes or wiring_probability";
        }
        return null;
    }

    /**
     * Makes a graph with one of Gephi's generators and loads it into a new workspace, as Gephi's
     * File > Generate does. "preferential_attachment" (Barabasi-Albert, Gephi 0.11.4) starts from a
     * star of {@code edgesPerNode + 1} nodes and attaches each later node to that many existing
     * nodes chosen in proportion to their degree; a seed is chosen and reported when none is
     * given, so the graph can be made again. "random" (Erdos-Renyi) links each pair of nodes with
     * {@code probability}; Gephi's random generator takes no seed, so it cannot be reproduced.
     */
    public JsonObject generateGraph(String model, int nodes, Integer edgesPerNode, Double probability, Long seed) {
        String m = model == null ? "" : model.trim().toLowerCase(java.util.Locale.ROOT);
        final org.gephi.io.generator.spi.Generator generator;
        final String name;
        JsonObject settings = new JsonObject();
        switch (m) {
            case "preferential_attachment":
            case "barabasi_albert": {
                int k = edgesPerNode == null ? 2 : edgesPerNode;
                String refusal = preferentialAttachmentRefusal(nodes, k);
                if (refusal != null) {
                    return error(refusal);
                }
                long s = seed != null ? seed : java.util.concurrent.ThreadLocalRandom.current().nextLong();
                org.gephi.io.generator.plugin.ScaleFreeGraph g = new org.gephi.io.generator.plugin.ScaleFreeGraph();
                g.setNumberOfNodes(nodes);
                g.setEdgesPerNode(k);
                g.setSeed(s);
                generator = g;
                name = "Preferential attachment (" + nodes + " nodes, " + k + " per node, seed " + s + ")";
                settings.addProperty("model", "preferential_attachment");
                settings.addProperty("edges_per_node", k);
                settings.addProperty("seed", s);
                settings.addProperty("reproducible", true);
                break;
            }
            case "random":
            case "erdos_renyi": {
                double p = probability == null ? 0.05 : probability;
                String refusal = randomGraphRefusal(nodes, p);
                if (refusal != null) {
                    return error(refusal);
                }
                org.gephi.io.generator.plugin.RandomGraph g = new org.gephi.io.generator.plugin.RandomGraph();
                g.setNumberOfNodes(nodes);
                g.setWiringProbability(p);
                generator = g;
                name = "Random graph (" + nodes + " nodes, p " + p + ")";
                settings.addProperty("model", "random");
                settings.addProperty("wiring_probability", p);
                settings.addProperty("reproducible", false);
                settings.addProperty("note", "Gephi's random graph generator takes no seed, so this graph cannot"
                    + " be made again; save or export it to keep it.");
                break;
            }
            default:
                return error("Unknown model '" + model + "': use \"preferential_attachment\" or \"random\"");
        }
        try {
            Container c = Lookup.getDefault().lookup(Container.Factory.class).newContainer();
            c.setSource(generator.getName());
            c.setReport(new org.gephi.io.importer.api.Report());
            // Both models are undirected. The generators set no edge type, so without this the
            // default of the container made every generated graph directed.
            c.getLoader().setEdgeDefault(org.gephi.io.importer.api.EdgeDirectionDefault.UNDIRECTED);
            generator.generate(c.getLoader());
            JsonObject r = loadContainer(c, name, "Generated " + name, null, null);
            if (r.get("success").getAsBoolean()) {
                settings.entrySet().forEach(e -> r.add(e.getKey(), e.getValue()));
                r.addProperty("directed", false);
                r.addProperty("workspace", name);
            }
            return r;
        } catch (Exception e) {
            return failure("Generating the graph failed: ", e);
        }
    }

    /**
     * Loads a filled import container the way {@link #importFile} loads a file: into a new
     * workspace named {@code name} by default (an empty workspace that was open is removed), or
     * appended to the current one with {@code mode} "append". Reports Gephi's import issues and
     * the resulting counts. Used for files and for generated graphs.
     */
    private JsonObject loadContainer(Container c, String name, String message, Float maxNodeSize, String mode)
        throws Exception {
        ImportController ic = Lookup.getDefault().lookup(ImportController.class);
        boolean append = "append".equalsIgnoreCase(mode);
        if (mode != null && !append && !"new_workspace".equalsIgnoreCase(mode)
            && !"new".equalsIgnoreCase(mode)) {
            return error("Unknown import mode '" + mode + "': use \"new_workspace\" (the default)"
                + " or \"append\"");
        }
        ProjectController pc = getProjectController();
        if (pc.getCurrentProject() == null) {
            onProjectThread(() -> {
                pc.newProject();
                return null;
            });
        }
        Workspace previous = currentWorkspace();
        boolean previousEmpty = false;
        if (previous != null) {
            Graph pg = getGraphController().getGraphModel(previous).getGraph();
            // Only an untouched workspace is clutter: empty and still under Gephi's default
            // name. One the person named is a decision, and stays.
            org.gephi.project.api.WorkspaceInformation info =
                previous.getLookup().lookup(org.gephi.project.api.WorkspaceInformation.class);
            String previousName = info == null ? "" : info.getName();
            previousEmpty = pg.getNodeCount() == 0 && pg.getEdgeCount() == 0
                && previousName != null && previousName.matches("Workspace \\d+");
        }
        if (append && previous == null) {
            return error("No workspace to append to");
        }

        Processor processor = findProcessor(append ? "AppendProcessor" : "DefaultProcessor");
        if (processor == null) {
            processor = Lookup.getDefault().lookup(Processor.class);
        }
        if (processor == null) {
            return error("No processor found");
        }

        // Gephi's import containers auto-scale by default: before processing, its
        // DefaultScaler recenters every node on the centroid, rescales sizes into
        // 4 to 100, and scales positions by the same ratio (clamped to +/-5000).
        // That would rewrite the viz:position and viz:size values the file carries,
        // so an export followed by an import would not round-trip. With auto-scale
        // off, files without positions still get spread out at random when the
        // container closes, and their sizes are kept as written.
        c.getLoader().setAutoScale(false);

        JsonArray issues = new JsonArray();
        addIssues(issues, c.getReport(), 20);
        Workspace importedWs;
        try {
            // A null workspace lets the processor create one whose settings match the file.
            importedWs = ic.process(c, processor, append ? previous : null);
        } catch (Exception e) {
            JsonObject err = error("Import failed: " + e.getMessage() + (append
                ? ". The file's time format or id type differs from this workspace's; import it"
                + " without mode \"append\" to open it in its own workspace." : ""));
            addIssues(issues, processor.getReport(), 20);
            if (issues.size() > 0) {
                err.add("import_issues", issues);
            }
            return err;
        }
        addIssues(issues, processor.getReport(), 20);
        Workspace ws = importedWs != null ? importedWs : previous;
        // Named after the file, as Gephi names a workspace opened from File > Open.
        final boolean tidy = !append && previousEmpty && previous != null && previous != ws;
        if (!append && ws != null && ws != previous) {
            onProjectThread(() -> {
                pc.renameWorkspace(ws, name);
                if (tidy) {
                    // The import already made its workspace current; switching to it
                    // again would close and reopen it for nothing.
                    if (pc.getCurrentWorkspace() != ws) {
                        pc.openWorkspace(ws);
                    }
                    pc.deleteWorkspace(previous);
                }
                return null;
            });
        }

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
        final Graph g = getGraphController().getGraphModel(effectiveWs).getGraph();
        JsonObject r = success(message);
        r.addProperty("import_mode", append ? "append" : "new_workspace");
        if (issues.size() > 0) {
            r.add("import_issues", issues);
        }
        if (capped > 0) {
            r.addProperty("nodes_size_capped", capped);
            r.addProperty("max_node_size", maxNodeSize);
        }
        r.addProperty("node_count", g.getNodeCount());
        r.addProperty("edge_count", g.getEdgeCount());
        return r;
    }

    /** Collect up to {@code limit} import issues (level and message) from a report. */
    static void addIssues(JsonArray out, org.gephi.io.importer.api.Report report, int limit) {
        if (report == null) {
            return;
        }
        for (org.gephi.io.importer.api.Issue issue : report.getIssuesList(limit)) {
            if (out.size() >= limit) {
                return;
            }
            JsonObject o = new JsonObject();
            o.addProperty("level", String.valueOf(issue.getLevel()));
            o.addProperty("message", issue.getMessage());
            out.add(o);
        }
    }

    private static Processor findProcessor(String simpleName) {
        for (Processor p : Lookup.getDefault().lookupAll(Processor.class)) {
            if (p.getClass().getSimpleName().equals(simpleName)) {
                return p;
            }
        }
        return null;
    }

    // ─── Graph Operations ────────────────────────────────────────────

    public JsonObject clearGraph() {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
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
            } finally {
                unlockWrite(g);
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    /** Nodes with no ties at all, which remove-isolates deletes. Reads only; call under a lock. */
    static java.util.List<Node> isolatedNodes(Graph g) {
        java.util.List<Node> isolates = new java.util.ArrayList<>();
        for (Node n : g.getNodes().toArray()) {
            if (g.getDegree(n) == 0) {
                isolates.add(n);
            }
        }
        return isolates;
    }

    public JsonObject removeIsolates() {
        return removeIsolates(false);
    }

    /** Remove every node with no ties, or with {@code dryRun} only count them and change nothing. */
    public JsonObject removeIsolates(boolean dryRun) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            Graph g = currentGraphModel().getGraph();
            if (dryRun) {
                lockRead(g);
                try {
                    int count = isolatedNodes(g).size();
                    JsonObject r = success("Would remove " + count + " isolated nodes");
                    r.addProperty("dry_run", true);
                    r.addProperty("would_remove", count);
                    r.addProperty("remaining_nodes", g.getNodeCount() - count);
                    return r;
                } finally {
                    g.readUnlock();
                }
            }
            java.util.List<Node> isolates;
            lockWrite(g);
            try {
                isolates = isolatedNodes(g);
                for (Node n : isolates) {
                    g.removeNode(n);
                }
            } finally {
                unlockWrite(g);
            }
            JsonObject r = success("Removed " + isolates.size() + " isolated nodes");
            r.addProperty("removed", isolates.size());
            r.addProperty("remaining_nodes", g.getNodeCount());
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject extractEgoNetwork(String nodeId, int depth) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            Graph g = currentGraphModel().getGraph();
            Node center = nodeById(g, nodeId);
            if (center == null) {
                return error("Node not found: " + nodeId);
            }

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
                if (dist >= depth) {
                    continue;
                }
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
                    if (!keep.contains(n)) {
                        toRemove.add(n);
                    }
                }
                for (Node n : toRemove) {
                    g.removeNode(n);
                }
            } finally {
                unlockWrite(g);
            }

            JsonObject r = success("Ego network extracted for " + nodeId);
            r.addProperty("kept_nodes", keep.size());
            r.addProperty("removed_nodes", toRemove.size());
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject extractGiantComponent() {
        // Statistics must run OFF the EDT (they dispatch UI work to EDT internally).
        // Node removal runs on the calling thread too — it doesn't need it either.
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = currentGraphModel();
            final Graph g = gm.getGraph();

            // Run connected components (on HTTP thread, not EDT)
            StatisticsBuilder ccBuilder = null;
            for (StatisticsBuilder sb : Lookup.getDefault().lookupAll(StatisticsBuilder.class)) {
                if (sb.getName().equalsIgnoreCase("ConnectedComponents") ||
                    sb.getClass().getSimpleName().toLowerCase().contains("connectedcomponents")) {
                    ccBuilder = sb;
                    break;
                }
            }
            if (ccBuilder == null) {
                return error("ConnectedComponents statistic not found");
            }

            Statistics stat = ccBuilder.getStatistics();
            stat.execute(gm);

            // Find the column
            Column ccCol = gm.getNodeTable().getColumn("componentnumber");
            if (ccCol == null) {
                // A copy: breaking out of a loop over the table itself would leave it locked.
                for (Column col : gm.getNodeTable().toArray()) {
                    if (col.getTitle().toLowerCase().contains("component")) {
                        ccCol = col;
                        break;
                    }
                }
            }
            if (ccCol == null) {
                return error("Component column not found after running statistics");
            }

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
            // write lock (see the threading note above setEdgeColor).
            java.util.List<Node> toRemove = new java.util.ArrayList<>();
            for (Node n : allNodes) {
                Object v = n.getAttribute(fccCol);
                int comp = v instanceof Number ? ((Number) v).intValue() : -1;
                if (comp != giantComp) {
                    toRemove.add(n);
                }
            }
            lockWrite(g);
            try {
                for (Node n : toRemove) {
                    g.removeNode(n);
                }
            } finally {
                unlockWrite(g);
            }
            JsonObject r = success("Giant component extracted");
            r.addProperty("kept_nodes", giantSize);
            r.addProperty("removed_nodes", toRemove.size());
            r.addProperty("component_count", componentSizes.size());
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    public JsonObject setEdgeThicknessByWeight(float minThickness, float maxThickness) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            PreviewController pc = Lookup.getDefault().lookup(PreviewController.class);
            PreviewModel pm = pc.getModel(ws);
            if (pm == null) {
                return error("Preview model not available");
            }

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
            return failure("Failed: ", e);
        }
    }

    public JsonObject resetFilters() {
        try {
            Workspace ws = currentWorkspace();
            if (ws == null) {
                return error("No project open");
            }
            GraphModel gm = currentGraphModel();
            Graph g = gm.getGraph();
            FilterController fc = Lookup.getDefault().lookup(FilterController.class);
            org.gephi.filters.api.FilterModel fm = fc == null ? null : fc.getModel();
            // A filter still being computed would apply its result after the reset and bring
            // itself back, so let it finish first.
            awaitFilterApplied(fm);
            // Turn the filter off through Gephi's filter controller, as the Filters panel's own
            // Filter button does, so the panel stops showing it as active and a refresh cannot
            // bring it back. Clearing takes Gephi's blocking write lock; holding the plugin's
            // deadlock-safe lock first lets those calls re-enter instead of queuing.
            lockWrite(g);
            try {
                if (fm != null && fm.isFiltering()) {
                    fc.filterVisible(null);
                }
                if (fm != null && fm.isSelecting()) {
                    fc.selectVisible(null);
                }
                if (fm == null) {
                    gm.setVisibleView(null);
                }
            } finally {
                unlockWrite(g);
            }
            awaitFilterApplied(fm);
            if (!gm.getVisibleView().isMainView()) {
                // A view set some other way than through the Filters panel.
                lockWrite(g);
                try {
                    gm.setVisibleView(null);
                } finally {
                    unlockWrite(g);
                }
            }
            return success("Filters reset - full graph view restored");
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
    }

    // ─── Shutdown ────────────────────────────────────────────────────

    /** Layouts run in Gephi's own LayoutController, which outlives the server, so stopping it leaves them alone. */
    public void shutdown() {
    }

    /** The running render engine, empty until the graph canvas has been created. */
    private static java.util.Optional<? extends org.gephi.viz.engine.VizEngine<?, ?>> vizEngine() {
        org.gephi.visualization.api.VisualizationController vc =
            Lookup.getDefault().lookup(org.gephi.visualization.api.VisualizationController.class);
        return vc instanceof org.gephi.visualization.VizController
            ? ((org.gephi.visualization.VizController) vc).getEngine() : java.util.Optional.empty();
    }

    /**
     * Reads the renderer's two counters (Gephi 0.11.4). The engine commits world updates many
     * times a second even when nothing changes, so frames that advance while world updates do
     * not mean the world updaters are blocked, usually on the graph lock: the screen keeps
     * showing the last picture while Gephi is hung. "not_drawing" means no frames at all.
     */
    static String rendererState(long framesAdvanced, long worldUpdatesAdvanced) {
        if (framesAdvanced <= 0) {
            return "not_drawing";
        }
        return worldUpdatesAdvanced > 0 ? "ok" : "stalled";
    }

    /**
     * Waits until the renderer exists and has drawn a frame, for at most {@code budgetMs}.
     * {@code frames} gives the frame count, or empty while there is no engine.
     */
    static boolean awaitRendererReady(java.util.function.Supplier<java.util.OptionalLong> frames, long budgetMs)
        throws InterruptedException {
        final long deadline = System.nanoTime() + budgetMs * 1_000_000L;
        long first = -1;
        while (true) {
            java.util.OptionalLong now = frames.get();
            if (now.isPresent()) {
                if (first < 0) {
                    first = now.getAsLong();
                } else if (now.getAsLong() > first) {
                    return true;
                }
            }
            if (System.nanoTime() > deadline) {
                return false;
            }
            Thread.sleep(25);
        }
    }

    /**
     * The renderer's counters, sampled twice, and the state they show: "not_started" until the
     * graph canvas exists, then "ok", "stalled" or "not_drawing" (see {@link #rendererState}).
     */
    public JsonObject rendererStats() {
        try {
            return rendererStats(() -> vizEngine().map(e -> new long[] {e.getFrameCount(), e.getWorldUpdateCount()}),
                RENDERER_SAMPLE_MS, RENDERER_STALL_CONFIRM_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JsonObject o = new JsonObject();
            o.addProperty("state", "unknown");
            return o;
        }
    }

    /**
     * {@link #rendererStats()} over any source of {frames, world updates}, empty while there is
     * no engine.
     */
    static JsonObject rendererStats(java.util.function.Supplier<java.util.Optional<long[]>> counters, long sampleMs,
        long confirmMs) throws InterruptedException {
        JsonObject o = new JsonObject();
        try {
            java.util.Optional<long[]> first = counters.get();
            if (first.isEmpty()) {
                o.addProperty("state", "not_started");
                return o;
            }
            Thread.sleep(sampleMs);
            long[] now = counters.get().orElse(first.get());
            String state = rendererState(now[0] - first.get()[0], now[1] - first.get()[1]);
            // A single slow world update (a very large graph, or a screenshot pausing updates)
            // also leaves frames drawn without new world updates. Only updates that stay frozen
            // for the confirmation time are a stall.
            long watched = sampleMs;
            while ("stalled".equals(state) && watched < confirmMs) {
                Thread.sleep(sampleMs);
                watched += sampleMs;
                now = counters.get().orElse(now);
                if (now[1] > first.get()[1]) {
                    state = "ok";
                }
            }
            o.addProperty("state", state);
            if ("stalled".equals(state)) {
                o.addProperty("stalled_for_ms", watched);
            }
            o.addProperty("frames", now[0]);
            o.addProperty("world_updates", now[1]);
        } catch (RuntimeException e) {
            o.addProperty("state", "unknown");
        }
        return o;
    }

    /** How long /health watches the renderer's counters. */
    static final long RENDERER_SAMPLE_MS = 250;
    /** How long world updates must stay frozen, with frames still drawn, before it is a stall. */
    static final long RENDERER_STALL_CONFIRM_MS = 2000;
    /** How long a screenshot waits for the graph view to start drawing. */
    static final long RENDERER_READY_BUDGET_MS = 10_000;

    /**
     * Cheap wedge detector for /health: try the graph read lock briefly.
     * "ok" = acquired instantly; "busy" = could not acquire (a writer is parked or
     * the renderer is saturating the lock — if persistent, Gephi needs a restart);
     * "none" = no workspace open.
     */
    public String graphLockProbe() {
        try {
            GraphModel gm = currentGraphModel();
            if (gm == null) {
                return "none";
            }
            Graph g = gm.getGraph();
            if (g.getLock().tryReadLock(150, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                g.readUnlock();
                return "ok";
            }
            return "busy";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "unknown";
        } catch (Throwable t) {
            return "unknown";
        }
    }

    /**
     * Live counters from GraphLock: active read holds,
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
            if (gm == null) {
                return o;
            }
            org.gephi.graph.api.GraphLock lock = gm.getGraph().getLock();
            if (lock == null) {
                return o;
            }
            o.addProperty("readers", lock.getReadLockCount());
            o.addProperty("write_locked", lock.isWriteLocked());
            o.addProperty("queued", lock.getQueueLength());
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
        if (clickListenerInstalled) {
            return;
        }
        org.gephi.visualization.api.VisualizationController vc =
            Lookup.getDefault().lookup(org.gephi.visualization.api.VisualizationController.class);
        if (vc == null) {
            return;
        }
        vc.addListener(new org.gephi.visualization.api.VisualizationEventListener() {
            @Override
            public boolean handleEvent(org.gephi.visualization.api.VisualizationEvent event) {
                try {
                    Object data = event.getData();
                    if (data instanceof Node[]) {
                        Node[] nodes = (Node[]) data;
                        if (nodes.length > 0) {
                            recordClick(nodes);
                        }
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
        if (rectangleAutoEnabled) {
            return;
        }
        try {
            org.gephi.visualization.api.VisualizationController vc = Lookup.getDefault()
                .lookup(org.gephi.visualization.api.VisualizationController.class);
            if (vc == null) {
                return;
            }
            org.gephi.visualization.api.VisualizationModel model = vc.getModel();
            if (model == null) {
                return;  // view not started; try again next call
            }
            if (!model.isRectangleSelection()) {
                vc.setRectangleSelection();
            }
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
            while (clickJournal.size() > CLICK_JOURNAL_MAX) {
                clickJournal.removeFirst();
            }
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
     * selected_now — the persistent selection (rectangle selection keeps it
     * after the mouse moves away; the primary channel), read from
     * VisualizationModel.getSelectedNodes(); clicks — the
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
                if (sel != null) {
                    for (Node n : sel) {
                        totalSelected++;
                        if (selected.size() < SELECTION_MAX_NODES) {
                            selected.add(nodeRef(n));
                        }
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
            for (JsonObject e : clickJournal) {
                clicks.add(e.deepCopy());
            }
            if (clear) {
                clickJournal.clear();
            }
        }
        r.add("clicks", clicks);
        r.addProperty("click_count", clicks.size());
        r.addProperty("listener_active", clickListenerInstalled);
        return r;
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
        if (vc == null) {
            return error("No visualization available (headless or view not started)");
        }
        GraphModel gm = currentGraphModel();
        if (gm == null) {
            return error("No workspace open");
        }
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
                    if (nodeId == null) {
                        return error("Missing 'id' for mode=node");
                    }
                    Node n = nodeById(g, nodeId);
                    if (n == null) {
                        return error("Node not found: " + nodeId);
                    }
                    vc.centerOnNode(n);
                    break;
                }
                case "edge": {
                    if (source == null || target == null) {
                        return error("Missing 'source'/'target' for mode=edge");
                    }
                    Node ns = nodeById(g, source);
                    Node nt = nodeById(g, target);
                    if (ns == null || nt == null) {
                        return error("Edge endpoints not found");
                    }
                    Edge e = g.getEdge(ns, nt, 1);  // directed
                    if (e == null) {
                        e = g.getEdge(ns, nt, 0);  // undirected
                    }
                    if (e == null) {
                        e = g.getEdge(ns, nt);  // default
                    }
                    if (e == null) {
                        e = g.getEdge(nt, ns, 1);
                    }
                    if (e == null) {
                        e = g.getEdge(nt, ns, 0);
                    }
                    if (e == null) {
                        e = g.getEdge(nt, ns);
                    }
                    if (e == null) {
                        return error("Edge not found: " + source + " -> " + target);
                    }
                    vc.centerOnEdge(e);
                    break;
                }
                case "region": {
                    if (x == null || y == null || w == null || h == null) {
                        return error("Missing x/y/w/h for mode=region");
                    }
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
                        Node n = nodeById(g, id);
                        if (n != null) {
                            nodes.add(n);
                        }
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
            if (zoom != null) {
                vc.setZoom(zoom.floatValue());
            }
            JsonObject r = success("View focused (" + m + ")");
            r.addProperty("mode", m);
            if (selectedCount != null) {
                r.addProperty("selected", selectedCount);
            }
            return r;
        } catch (Exception e) {
            return failure("Focus failed: ", e);
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
        if (vc == null) {
            return error("No visualization available (headless or view not started)");
        }
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
            return failure("Set selection mode failed: ", e);
        }
    }

    /** List the perspectives (Overview / Data Laboratory / Preview) and the active one. */
    public JsonObject getPerspective() {
        org.gephi.perspective.api.PerspectiveController pc =
            Lookup.getDefault().lookup(org.gephi.perspective.api.PerspectiveController.class);
        if (pc == null) {
            return error("No perspective controller (headless?)");
        }
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
            return failure("List perspectives failed: ", e);
        }
    }

    /** Switch the active perspective (tab) by name or display name (case-insensitive). */
    public JsonObject switchPerspective(String name) {
        org.gephi.perspective.api.PerspectiveController pc =
            Lookup.getDefault().lookup(org.gephi.perspective.api.PerspectiveController.class);
        if (pc == null) {
            return error("No perspective controller (headless?)");
        }
        if (name == null) {
            return error("Missing 'name'");
        }
        org.gephi.perspective.spi.Perspective match = null;
        for (org.gephi.perspective.spi.Perspective p : pc.getPerspectives()) {
            if (name.equalsIgnoreCase(p.getName()) || name.equalsIgnoreCase(p.getDisplayName())) {
                match = p;
                break;
            }
        }
        if (match == null) {
            return error("Perspective not found: " + name);
        }
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
                if (bs != null) {
                    java.util.Collections.addAll(out, bs);
                }
            } catch (Exception ignore) { /* some category builders need a specific state */
            }
        }
        return out;
    }

    /**
     * Every filter with a name that can be typed back. Gephi names the per-column filters with
     * HTML (the column in black, its type in grey), and gives the same name to that column's
     * Equal, Non-null and Partition Count filters, so a name alone reached only the first. Those
     * filters are named "Category: column type" instead, e.g. "Equal: group String (Node)".
     */
    private java.util.List<Map.Entry<String, FilterBuilder>> namedFilterBuilders(Workspace ws) {
        java.util.List<Map.Entry<String, FilterBuilder>> out = new java.util.ArrayList<>();
        for (FilterBuilder b : Lookup.getDefault().lookupAll(FilterBuilder.class)) {
            try {
                out.add(Map.entry(plainText(b.getName()), b));
            } catch (Exception ignore) {
                // A filter whose name cannot be read is left out of the list.
            }
        }
        for (CategoryBuilder cb : Lookup.getDefault().lookupAll(CategoryBuilder.class)) {
            try {
                FilterBuilder[] bs = cb.getBuilders(ws);
                if (bs == null) {
                    continue;
                }
                for (FilterBuilder b : bs) {
                    String category = b.getCategory() != null ? b.getCategory().getName() : null;
                    String name = plainText(b.getName());
                    out.add(Map.entry(category == null ? name : plainText(category) + ": " + name, b));
                }
            } catch (Exception ignore) { /* some category builders need a specific state */
            }
        }
        return out;
    }

    /** The text of a Gephi label that may carry HTML markup. */
    static String plainText(String label) {
        if (label == null) {
            return "";
        }
        return label.replaceAll("<[^>]*>", " ").replace("&amp;", "&").replace("&lt;", "<")
            .replace("&gt;", ">").replaceAll("\\s+", " ").trim();
    }

    /** The filter builder a name refers to: the plain name, or Gephi's own label. */
    private FilterBuilder findFilterBuilder(Workspace ws, String name) {
        if (name == null) {
            return null;
        }
        FilterBuilder byLabel = null;
        for (Map.Entry<String, FilterBuilder> e : namedFilterBuilders(ws)) {
            if (name.equalsIgnoreCase(e.getKey())) {
                return e.getValue();
            }
            try {
                if (byLabel == null && name.equalsIgnoreCase(e.getValue().getName())) {
                    byLabel = e.getValue();
                }
            } catch (Exception ignore) {
                // A filter whose name cannot be read cannot match by name.
            }
        }
        return byLabel;
    }

    /** Coerce a JSON value to a filter property's type; handles Range from a [lo, hi] pair. */
    static Object convertFilterProperty(Object val, Class<?> type) {
        if (val == null) {
            return null;
        }
        if (type == org.gephi.filters.api.Range.class) {
            java.util.List<?> pair = null;
            if (val instanceof java.util.List) {
                pair = (java.util.List<?>) val;
            } else if (val instanceof com.google.gson.JsonArray) {
                java.util.List<Object> l = new java.util.ArrayList<>();
                for (com.google.gson.JsonElement e : (com.google.gson.JsonArray) val) {
                    l.add(e.getAsDouble());
                }
                pair = l;
            }
            if (pair == null || pair.size() != 2) {
                return null;
            }
            double loD = pair.get(0) instanceof Number ? ((Number) pair.get(0)).doubleValue()
                : Double.parseDouble(pair.get(0).toString());
            double hiD = pair.get(1) instanceof Number ? ((Number) pair.get(1)).doubleValue()
                : Double.parseDouble(pair.get(1).toString());
            // Range requires both bounds to be the SAME Number class. Use Integer when
            // both are whole (degree/count filters), Double otherwise (continuous columns).
            boolean whole = loD == Math.floor(loD) && hiD == Math.floor(hiD)
                && !Double.isInfinite(loD) && !Double.isInfinite(hiD);
            if (whole) {
                return new org.gephi.filters.api.Range((int) loD, (int) hiD);
            }
            return new org.gephi.filters.api.Range(loD, hiD);
        }
        return convertLayoutProperty(val, type);
    }

    public JsonObject listFilters() {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No workspace open");
        }
        JsonArray arr = new JsonArray();
        for (Map.Entry<String, FilterBuilder> e : namedFilterBuilders(ws)) {
            FilterBuilder b = e.getValue();
            JsonObject o = new JsonObject();
            o.addProperty("name", e.getKey());
            try {
                o.addProperty("category", b.getCategory() == null ? null : b.getCategory().getName());
            } catch (Exception ignore) {
                // The category is optional.
            }
            try {
                o.addProperty("description", b.getDescription());
            } catch (Exception ignore) {
                // The description is optional.
            }
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
            } catch (Exception ignore) { /* introspection best-effort */
            }
            arr.add(o);
        }
        JsonObject r = success("Filters listed");
        r.add("filters", arr);
        return r;
    }

    public JsonObject applyFilter(String name, Map<String, Object> params, String action, String column) {
        FilterController fc = Lookup.getDefault().lookup(FilterController.class);
        if (fc == null) {
            return error("No filter controller available");
        }
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No workspace open");
        }
        GraphModel gm = currentGraphModel();
        if (gm == null) {
            return error("No workspace open");
        }
        if (name == null) {
            return error("Missing 'name'");
        }

        FilterBuilder builder = findFilterBuilder(ws, name);
        if (builder == null) {
            return error("Filter not found: " + name + " (gephi_list_filters lists them)");
        }

        Filter filter = builder.getFilter(ws);
        if (filter == null) {
            return error("Filter builder produced no filter: " + name);
        }

        // Set each named property; report the valid names if a param doesn't match.
        FilterProperty[] props = filter.getProperties();
        if (params != null && !params.isEmpty()) {
            java.util.List<String> propNames = new java.util.ArrayList<>();
            if (props != null) {
                for (FilterProperty p : props) {
                    propNames.add(p.getName());
                }
            }
            for (Map.Entry<String, Object> e : params.entrySet()) {
                FilterProperty match = null;
                if (props != null) {
                    for (FilterProperty p : props) {
                        if (e.getKey().equalsIgnoreCase(p.getName())) {
                            match = p;
                            break;
                        }
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
                try {
                    match.setValue(converted);
                } catch (Exception ex) {
                    return failure("Failed to set '" + e.getKey() + "': ", ex);
                }
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
                if (column == null) {
                    return error("action=column requires a 'column' name");
                }
                break;
            default:
                return error("Unknown action: " + action + " (use select|new_workspace|column)");
        }

        Query query = fc.createQuery(filter);

        // All three FilterController operations below end in Gephi's own BLOCKING
        // writeLock() (filterVisible via setVisibleView; the two exports process the
        // query through the same path). Hold our deadlock-safe lock first so those
        // calls re-enter instead of queuing behind the renderer — exactly the
        // mitigation resetFilters uses, and these run on an HTTP request thread too.
        Graph lockGraph = gm.getGraph();
        JsonObject r;
        switch (act) {
            case "select":
            case "visible":
                // Only a filter on the visible graph joins the Filters panel's queries. Adding
                // one makes it the current query, and Gephi's two exports read a current query
                // from its last result: with none yet, the column export wrote nothing and the
                // workspace export copied nothing, while both reported success.
                fc.add(query);
                lockWrite(lockGraph);
                try {
                    fc.filterVisible(query);
                } finally {
                    unlockWrite(lockGraph);
                }
                r = success("Filter applied to the visible graph");
                r.addProperty("nodes_before", nodesBefore);
                r.addProperty("edges_before", edgesBefore);
                // filterVisible computes its result in the background and returns before the
                // view is swapped, so reading the counts at once reported the pre-filter
                // numbers. Gephi's filter model says when the result has been applied.
                boolean settled = awaitFilterApplied(fc.getModel());
                r.addProperty("nodes_after", gm.getGraphVisible().getNodeCount());
                r.addProperty("edges_after", gm.getGraphVisible().getEdgeCount());
                if (!settled) {
                    r.addProperty("counts_settled", false);
                }
                break;
            case "new_workspace":
                // Materializes the filtered subgraph into a fresh workspace, so later
                // work runs on the subgraph alone and the original stays as it was.
                lockWrite(lockGraph);
                try {
                    fc.exportToNewWorkspace(query);
                } finally {
                    unlockWrite(lockGraph);
                }
                r = success("Filtered subgraph exported to a new workspace");
                break;
            case "column":
                if (column == null) {
                    return error("action=column requires a 'column' name");
                }
                lockWrite(lockGraph);
                try {
                    fc.exportToColumn(column, query);
                } finally {
                    unlockWrite(lockGraph);
                }
                r = success("Filter membership written to boolean column: " + column);
                r.addProperty("column", column);
                break;
            default:
                return error("Unknown action: " + action + " (use select|new_workspace|column)");
        }
        r.addProperty("filter", name);
        return r;
    }

    /** How long a filter's result may take to be applied before its counts are called provisional. */
    static final long FILTER_APPLY_BUDGET_MS = 30_000;

    /**
     * Waits until Gephi has applied the current filter's result. Since Gephi 0.11.4 the filter
     * model reports a result being computed or applied as running. Returns false if it was still
     * running when the budget ran out, so the caller can say the counts are provisional.
     */
    static boolean awaitFilterApplied(org.gephi.filters.api.FilterModel model) {
        return awaitFilterApplied(model, FILTER_APPLY_BUDGET_MS);
    }

    static boolean awaitFilterApplied(org.gephi.filters.api.FilterModel model, long budgetMs) {
        if (model == null) {
            return true;
        }
        final long deadline = System.nanoTime() + budgetMs * 1_000_000L;
        while (model.isRunning()) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    // ─── Combined filters ────────────────────────────────────────────

    /** A filter from its name and settings, or the reason it could not be made. */
    private Object buildFilter(Workspace ws, String name, Map<String, Object> params) {
        if (name == null) {
            return "Each filter needs a 'name'";
        }
        FilterBuilder builder = findFilterBuilder(ws, name);
        if (builder == null) {
            return "Filter not found: " + name + " (gephi_list_filters lists them)";
        }
        Filter filter = builder.getFilter(ws);
        if (filter == null) {
            return "Filter builder produced no filter: " + name;
        }
        FilterProperty[] props = filter.getProperties();
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                FilterProperty match = null;
                java.util.List<String> names = new java.util.ArrayList<>();
                if (props != null) {
                    for (FilterProperty p : props) {
                        names.add(p.getName());
                        if (match == null && e.getKey().equalsIgnoreCase(p.getName())) {
                            match = p;
                        }
                    }
                }
                if (match == null) {
                    return "Unknown filter property '" + e.getKey() + "' for " + name + " — valid properties: " + names;
                }
                Object converted = convertFilterProperty(e.getValue(), match.getValueType());
                if (converted == null) {
                    return "Could not coerce '" + e.getKey() + "' to " + match.getValueType().getSimpleName()
                        + " (Range wants a [lo, hi] pair)";
                }
                try {
                    match.setValue(converted);
                } catch (Exception ex) {
                    return "Failed to set '" + e.getKey() + "': " + ex.getMessage();
                }
            }
        }
        return filter;
    }

    private FilterBuilder operatorBuilder(Workspace ws, String simpleName) {
        for (FilterBuilder b : allFilterBuilders(ws)) {
            if (b.getClass().getSimpleName().equals(simpleName)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Applies several filters together: {@code combine} "all" keeps what every filter keeps,
     * "any" what at least one keeps. A filter marked {@code exclude} keeps the opposite of what
     * it would keep alone. {@code dry_run} counts what would remain without changing the view.
     * The combined query shows in Gephi's Filters panel as its operator with the filters under it.
     */
    @SuppressWarnings("unchecked")
    public JsonObject applyFilters(java.util.List<Map<String, Object>> specs, String combine, String action,
        String column, boolean dryRun) {
        FilterController fc = Lookup.getDefault().lookup(FilterController.class);
        if (fc == null) {
            return error("No filter controller available");
        }
        Workspace ws = currentWorkspace();
        GraphModel gm = currentGraphModel();
        if (ws == null || gm == null) {
            return error("No workspace open");
        }
        if (specs == null || specs.isEmpty()) {
            return error("Give at least one filter in 'filters'");
        }
        String mode = combine == null ? "all" : combine.toLowerCase(java.util.Locale.ROOT);
        if (!mode.equals("all") && !mode.equals("any")) {
            return error("combine must be \"all\" or \"any\"");
        }
        String act = action == null ? "select" : action.toLowerCase(java.util.Locale.ROOT);
        if (!dryRun && !act.equals("select") && !act.equals("new_workspace") && !act.equals("column")) {
            return error("Unknown action: " + action + " (use select|new_workspace|column)");
        }
        if (!dryRun && act.equals("column") && column == null) {
            return error("action=column requires a 'column' name");
        }

        java.util.List<Query> parts = new java.util.ArrayList<>();
        JsonArray applied = new JsonArray();
        for (Map<String, Object> spec : specs) {
            String name = spec.get("name") == null ? null : spec.get("name").toString();
            Object params = spec.get("params");
            Object built = buildFilter(ws, name, params instanceof Map ? (Map<String, Object>) params : null);
            if (built instanceof String) {
                return error((String) built);
            }
            Filter filter = (Filter) built;
            Query q = fc.createQuery(filter);
            boolean exclude = Boolean.TRUE.equals(spec.get("exclude"))
                || "true".equalsIgnoreCase(String.valueOf(spec.get("exclude")));
            if (exclude) {
                String not = filter instanceof org.gephi.filters.spi.EdgeFilter ? "NOTBuilderEdge" : "NOTBuilderNode";
                FilterBuilder nb = operatorBuilder(ws, not);
                if (nb == null) {
                    return error("Gephi's NOT operator is not available");
                }
                Query nq = fc.createQuery(nb.getFilter(ws));
                fc.setSubQuery(nq, q);
                q = nq;
            }
            parts.add(q);
            applied.add((exclude ? "NOT " : "") + name);
        }
        Query query;
        if (parts.size() == 1) {
            query = parts.get(0);
        } else {
            FilterBuilder ob = operatorBuilder(ws, mode.equals("all") ? "INTERSECTIONBuilder" : "UNIONBuilder");
            if (ob == null) {
                return error("Gephi's " + (mode.equals("all") ? "INTERSECTION" : "UNION")
                    + " operator is not available");
            }
            query = fc.createQuery(ob.getFilter(ws));
            for (Query part : parts) {
                fc.setSubQuery(query, part);
            }
        }

        int nodesBefore = gm.getGraph().getNodeCount();
        int edgesBefore = gm.getGraph().getEdgeCount();
        Graph lockGraph = gm.getGraph();
        JsonObject r;
        if (dryRun) {
            org.gephi.graph.api.GraphView view;
            lockWrite(lockGraph);
            try {
                view = fc.filter(query);
            } finally {
                unlockWrite(lockGraph);
            }
            Graph kept = gm.getGraph(view);
            r = success("Dry run: nothing was changed");
            r.addProperty("nodes_kept", kept.getNodeCount());
            r.addProperty("edges_kept", kept.getEdgeCount());
            r.addProperty("nodes_removed", nodesBefore - kept.getNodeCount());
            r.addProperty("edges_removed", edgesBefore - kept.getEdgeCount());
            if (!view.isMainView()) {
                gm.destroyView(view);
            }
        } else {
            if (act.equals("select")) {
                // Only here: see applyFilter for why the exports must not see it as current.
                fc.add(query);
            }
            lockWrite(lockGraph);
            try {
                switch (act) {
                    case "new_workspace":
                        fc.exportToNewWorkspace(query);
                        break;
                    case "column":
                        fc.exportToColumn(column, query);
                        break;
                    default:
                        fc.filterVisible(query);
                }
            } finally {
                unlockWrite(lockGraph);
            }
            if (act.equals("select")) {
                r = success("Filters applied to the visible graph");
                final boolean settled = awaitFilterApplied(fc.getModel());
                r.addProperty("nodes_before", nodesBefore);
                r.addProperty("edges_before", edgesBefore);
                r.addProperty("nodes_after", gm.getGraphVisible().getNodeCount());
                r.addProperty("edges_after", gm.getGraphVisible().getEdgeCount());
                if (!settled) {
                    r.addProperty("counts_settled", false);
                }
            } else if (act.equals("new_workspace")) {
                r = success("Filtered subgraph exported to a new workspace");
            } else {
                r = success("Filter membership written to boolean column: " + column);
                r.addProperty("column", column);
            }
        }
        r.addProperty("combine", mode);
        r.add("filters", applied);
        return r;
    }

    // ─── Time ────────────────────────────────────────────────────────

    /**
     * Gives nodes or edges their time from one or two columns: a start and an optional end,
     * as numbers (years, for instance) or dates. Afterwards the network has time data, so the
     * timeline, time slices and dynamic statistics work. Needs a workspace that stores time as
     * intervals, which is Gephi's default.
     */
    public JsonObject setTimeFromColumns(String target, String startName, String endName, String dateFormat) {
        return setTimeFromColumns(target, startName, endName, dateFormat, false);
    }

    /** {@code checkOnly}: report whether the change would be refused, without changing anything. */
    public JsonObject setTimeFromColumns(String target, String startName, String endName, String dateFormat,
        boolean checkOnly) {
        GraphModel gm = currentGraphModel();
        if (gm == null) {
            return error("No workspace open");
        }
        if (startName == null && endName == null) {
            return error("Name a 'start' column, an 'end' column, or both");
        }
        if (gm.getConfiguration().getTimeRepresentation() != org.gephi.graph.api.TimeRepresentation.INTERVAL) {
            return error("This workspace stores time as timestamps, so start and end columns cannot be"
                + " turned into intervals. Import the data into a new workspace first.");
        }
        Table table = tableFor(gm, target);
        Column start = startName == null ? null : findColumn(table, startName);
        Column end = endName == null ? null : findColumn(table, endName);
        if (startName != null && start == null) {
            return error("Column not found: " + startName);
        }
        if (endName != null && end == null) {
            return error("Column not found: " + endName);
        }
        org.gephi.datalab.api.AttributeColumnsMergeStrategiesController mc =
            Lookup.getDefault().lookup(org.gephi.datalab.api.AttributeColumnsMergeStrategiesController.class);
        if (mc == null) {
            return error("No datalab controller available");
        }
        boolean numeric = (start == null || isNumberColumn(start)) && (end == null || isNumberColumn(end));
        if (!numeric) {
            if (dateFormat == null) {
                return error("The columns hold text, so give 'date_format' as a Java date pattern,"
                    + " for example \"yyyy-MM-dd\" or \"dd/MM/yyyy\"");
            }
            try {
                new java.text.SimpleDateFormat(dateFormat);
            } catch (IllegalArgumentException e) {
                return error("Not a date pattern: " + dateFormat);
            }
        }
        if (checkOnly) {
            return success("Ready");
        }
        Graph g = gm.getGraph();
        int withTime = 0;
        lockWrite(g);
        try {
            if (numeric) {
                mc.mergeNumericColumnsToTimeInterval(table, start, end, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY);
            } else {
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(dateFormat);
                mc.mergeDateColumnsToTimeInterval(table, start, end, fmt, null, null);
            }
            for (org.gephi.graph.api.Element el : elementsFor(gm, target)) {
                if (el.getIntervals().length > 0) {
                    withTime++;
                }
            }
        } catch (Exception e) {
            return failure("Could not set time from the columns: ", e);
        } finally {
            unlockWrite(g);
        }
        JsonObject r = success("Time set on " + withTime + " " + ("edge".equalsIgnoreCase(target) ? "edges" : "nodes"));
        r.addProperty("with_time", withTime);
        org.gephi.graph.api.Interval b = gm.getTimeBounds();
        if (b != null && !Double.isInfinite(b.getLow())) {
            r.addProperty("time_min", b.getLow());
        }
        if (b != null && !Double.isInfinite(b.getHigh())) {
            r.addProperty("time_max", b.getHigh());
        }
        return r;
    }

    private static boolean isNumberColumn(Column c) {
        return Number.class.isAssignableFrom(c.getTypeClass())
            || (c.getTypeClass().isPrimitive() && c.getTypeClass() != boolean.class && c.getTypeClass() != char.class);
    }

    /**
     * True when an element is present at some moment in [low, high]. {@code rep} is how the
     * workspace stores time; an element with no time data counts as always present.
     */
    static boolean presentIn(org.gephi.graph.api.Element e, double low, double high,
        org.gephi.graph.api.TimeRepresentation rep) {
        if (rep == org.gephi.graph.api.TimeRepresentation.INTERVAL) {
            org.gephi.graph.api.Interval[] intervals = e.getIntervals();
            if (intervals.length == 0) {
                return true;
            }
            for (org.gephi.graph.api.Interval i : intervals) {
                if (i.getLow() <= high && i.getHigh() >= low) {
                    return true;
                }
            }
            return false;
        }
        double[] stamps = e.getTimestamps();
        if (stamps.length == 0) {
            return true;
        }
        for (double t : stamps) {
            if (t >= low && t <= high) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies what is present between {@code low} and {@code high} into a new workspace and
     * opens it: nodes present then, and edges present then between them. The network itself,
     * and Gephi's timeline, are left as they were.
     */
    public JsonObject timeSlice(double low, double high) {
        if (!(low <= high)) {
            return error("'start' must not be after 'end'");
        }
        final ProjectController pc = getProjectController();
        Workspace source = currentWorkspace();
        if (source == null) {
            return error("No workspace open");
        }
        GraphModel gm = getGraphController().getGraphModel(source);
        if (!gm.isDynamic()) {
            return error("This network has no time data. gephi_set_time_from_columns gives it time from"
                + " start and end columns.");
        }
        Graph g = gm.getGraph();
        java.util.List<Node> keep = new java.util.ArrayList<>();
        java.util.Set<Object> dropEdges = new java.util.HashSet<>();
        org.gephi.graph.api.TimeRepresentation rep = gm.getConfiguration().getTimeRepresentation();
        lockRead(g);
        try {
            for (Node n : g.getNodes().toArray()) {
                if (presentIn(n, low, high, rep)) {
                    keep.add(n);
                }
            }
            for (Edge e : g.getEdges().toArray()) {
                if (!presentIn(e, low, high, rep)) {
                    dropEdges.add(e.getId());
                }
            }
        } finally {
            g.readUnlock();
        }
        String name = workspaceName(source) + " " + fmtTime(low) + "–" + fmtTime(high);
        Workspace[] made = new Workspace[1];
        onProjectThread(() -> {
            // A workspace's settings (time format, id type) are fixed when it is made.
            made[0] = pc.newWorkspace(pc.getCurrentProject(), gm.getConfiguration());
            GraphModel target = getGraphController().getGraphModel(made[0]);
            target.bridge().copyNodes(keep.toArray(new Node[0]));
            Graph tg = target.getGraph();
            tg.writeLock();
            try {
                for (Object id : dropEdges) {
                    Edge e = tg.getEdge(id);
                    if (e != null) {
                        tg.removeEdge(e);
                    }
                }
            } finally {
                tg.writeUnlock();
            }
            pc.renameWorkspace(made[0], name);
            pc.openWorkspace(made[0]);
            return null;
        });
        Graph sliced = getGraphController().getGraphModel(made[0]).getGraph();
        JsonObject r = success("Opened a workspace with the network from " + fmtTime(low) + " to " + fmtTime(high));
        r.addProperty("workspace_id", made[0].getId());
        r.addProperty("workspace_name", name);
        r.addProperty("node_count", sliced.getNodeCount());
        r.addProperty("edge_count", sliced.getEdgeCount());
        r.addProperty("source_node_count", g.getNodeCount());
        r.addProperty("source_edge_count", g.getEdgeCount());
        return r;
    }

    private static String workspaceName(Workspace ws) {
        String n = ws.getName();
        return n == null || n.isBlank() ? "Workspace " + ws.getId() : n;
    }

    // ─── Shortest path ───────────────────────────────────────────────

    /** The result of a shortest-path search: the path, its length, and how many paths tie. */
    static final class PathResult {
        final java.util.List<Node> nodes;
        final java.util.List<Edge> edges;
        final double length;
        final long tiedPaths;

        PathResult(java.util.List<Node> nodes, java.util.List<Edge> edges, double length, long tiedPaths) {
            this.nodes = nodes;
            this.edges = edges;
            this.length = length;
            this.tiedPaths = tiedPaths;
        }
    }

    /**
     * Dijkstra's shortest path from {@code from} to {@code to}, or null when none exists.
     * {@code weighting}: "none" counts steps, "distance" reads an edge's weight as its length,
     * "strength" reads a heavier edge as a closer tie (length 1 / weight). Directed edges are
     * followed only forwards when {@code followDirection} is set. Also counts how many
     * different paths share the shortest length.
     */
    static PathResult shortestPath(Graph g, Node from, Node to, String weighting, boolean followDirection) {
        java.util.Map<Node, Double> dist = new java.util.HashMap<>();
        java.util.Map<Node, Long> ways = new java.util.HashMap<>();
        final java.util.Map<Node, Edge> via = new java.util.HashMap<>();
        java.util.PriorityQueue<Object[]> queue = new java.util.PriorityQueue<>(
            (a, b) -> Double.compare((Double) a[1], (Double) b[1]));
        dist.put(from, 0.0);
        ways.put(from, 1L);
        queue.add(new Object[] {from, 0.0});
        java.util.Set<Node> done = new java.util.HashSet<>();
        final double eps = 1e-9;
        while (!queue.isEmpty()) {
            Object[] head = queue.poll();
            Node n = (Node) head[0];
            if (!done.add(n)) {
                continue;
            }
            if (n == to) {
                break;
            }
            for (Edge e : g.getEdges(n).toArray()) {
                Node other = g.getOpposite(n, e);
                if (followDirection && e.isDirected() && e.getSource() != n) {
                    continue;
                }
                if (e.isSelfLoop() || done.contains(other)) {
                    continue;
                }
                double w = e.getWeight();
                double step = "distance".equals(weighting) ? w : "strength".equals(weighting) ? 1.0 / w : 1.0;
                if (!(step > 0) || Double.isInfinite(step)) {
                    continue;
                }
                double d = dist.get(n) + step;
                Double known = dist.get(other);
                if (known == null || d < known - eps) {
                    dist.put(other, d);
                    ways.put(other, ways.get(n));
                    via.put(other, e);
                    queue.add(new Object[] {other, d});
                } else if (Math.abs(d - known) <= eps) {
                    ways.merge(other, ways.get(n), Long::sum);
                }
            }
        }
        if (!dist.containsKey(to) || !done.contains(to)) {
            return null;
        }
        java.util.LinkedList<Node> nodes = new java.util.LinkedList<>();
        java.util.LinkedList<Edge> edges = new java.util.LinkedList<>();
        Node cur = to;
        nodes.addFirst(cur);
        while (cur != from) {
            Edge e = via.get(cur);
            edges.addFirst(e);
            cur = g.getOpposite(cur, e);
            nodes.addFirst(cur);
        }
        return new PathResult(nodes, edges, dist.get(to), ways.get(to));
    }

    public JsonObject findShortestPath(String fromId, String toId, String weighting, boolean followDirection,
        String markColumn) {
        GraphModel gm = currentGraphModel();
        if (gm == null) {
            return error("No workspace open");
        }
        String w = weighting == null ? "none" : weighting.toLowerCase(java.util.Locale.ROOT);
        if (!w.equals("none") && !w.equals("distance") && !w.equals("strength")) {
            return error("weighting must be \"none\", \"distance\" or \"strength\"");
        }
        Graph g = gm.getGraph();
        PathResult path;
        lockRead(g);
        try {
            Node from = nodeById(g, fromId);
            Node to = nodeById(g, toId);
            if (from == null) {
                return error("Node not found: " + fromId);
            }
            if (to == null) {
                return error("Node not found: " + toId);
            }
            path = from == to ? new PathResult(java.util.List.of(from), java.util.List.of(), 0, 1)
                : shortestPath(g, from, to, w, followDirection);
        } finally {
            g.readUnlock();
        }
        if (path == null) {
            JsonObject r = success("No path from " + fromId + " to " + toId
                + (followDirection && gm.isDirected() ? " following edge directions" : ""));
            r.addProperty("found", false);
            return r;
        }
        JsonArray nodes = new JsonArray();
        for (Node n : path.nodes) {
            JsonObject o = new JsonObject();
            o.addProperty("id", n.getId().toString());
            o.addProperty("label", n.getLabel());
            nodes.add(o);
        }
        JsonObject r = success("Path of " + path.edges.size() + " step(s)");
        r.addProperty("found", true);
        r.addProperty("steps", path.edges.size());
        if (!w.equals("none")) {
            r.addProperty("length", path.length);
        }
        r.addProperty("weighting", w);
        r.addProperty("equally_short_paths", path.tiedPaths);
        r.add("path", nodes);
        if (markColumn != null) {
            lockWrite(g);
            try {
                Column nc = findColumn(gm.getNodeTable(), markColumn);
                Column ec = findColumn(gm.getEdgeTable(), markColumn);
                // Checked before either column is made, so a refused mark adds nothing.
                if ((nc != null && nc.getTypeClass() != Boolean.class)
                    || (ec != null && ec.getTypeClass() != Boolean.class)) {
                    return error("Column '" + markColumn
                        + "' already exists and is not true/false; choose another name");
                }
                if (nc == null) {
                    nc = gm.getNodeTable().addColumn(markColumn, Boolean.class);
                }
                if (ec == null) {
                    ec = gm.getEdgeTable().addColumn(markColumn, Boolean.class);
                }
                for (Node n : g.getNodes().toArray()) {
                    n.setAttribute(nc, path.nodes.contains(n));
                }
                java.util.Set<Edge> onPath = new java.util.HashSet<>(path.edges);
                for (Edge e : g.getEdges().toArray()) {
                    e.setAttribute(ec, onPath.contains(e));
                }
            } finally {
                unlockWrite(g);
            }
            r.addProperty("mark_column", markColumn);
        }
        return r;
    }

    // ─── Column tidy-up ──────────────────────────────────────────────

    /**
     * Tidies one column. {@code action}: "delete" removes it; "rename" gives it {@code newName};
     * "convert" changes its type to {@code type}, reporting values that could not convert;
     * "fill_empty" writes {@code value} where the column is empty; "clear" empties it.
     */
    public JsonObject editColumn(String target, String columnName, String action, String value,
        String type, String newName) {
        return editColumn(target, columnName, action, value, type, newName, false);
    }

    /** {@code checkOnly}: report whether the edit would be refused, without changing anything. */
    public JsonObject editColumn(String target, String columnName, String action, String value,
        String type, String newName, boolean checkOnly) {
        GraphModel gm = currentGraphModel();
        if (gm == null) {
            return error("No workspace open");
        }
        if (columnName == null || action == null) {
            return error("Give 'column' and 'action'");
        }
        org.gephi.datalab.api.AttributeColumnsController acc =
            Lookup.getDefault().lookup(org.gephi.datalab.api.AttributeColumnsController.class);
        if (acc == null) {
            return error("No datalab controller available");
        }
        Table table = tableFor(gm, target);
        Column col = findColumn(table, columnName);
        if (col == null) {
            return error("Column not found: " + columnName);
        }
        String title = col.getTitle();
        Graph g = gm.getGraph();
        lockWrite(g);
        try {
            switch (action.toLowerCase(java.util.Locale.ROOT)) {
                case "delete": {
                    if (!acc.canDeleteColumn(col)) {
                        return error("Gephi keeps the '" + title + "' column; it cannot be deleted");
                    }
                    if (checkOnly) {
                        return success("Ready");
                    }
                    acc.deleteAttributeColumn(table, col);
                    return success("Deleted column " + title);
                }
                case "rename": {
                    if (newName == null || newName.isBlank()) {
                        return error("Give the new name in 'new_name'");
                    }
                    if (!acc.canDeleteColumn(col)) {
                        return error("Gephi's own '" + title + "' column cannot be renamed");
                    }
                    if (findColumn(table, newName) != null) {
                        return error("A column named " + newName + " already exists");
                    }
                    if (checkOnly) {
                        return success("Ready");
                    }
                    Column copy = acc.duplicateColumn(table, col, newName, col.getTypeClass());
                    acc.deleteAttributeColumn(table, col);
                    JsonObject r = success("Renamed " + title + " to " + copy.getTitle());
                    r.addProperty("column", copy.getId());
                    return r;
                }
                case "convert": {
                    Class<?> cls = typeStringToClass(type);
                    if (cls == null) {
                        return error("Give 'type': string, integer, long, float, double or boolean");
                    }
                    if (!acc.canDeleteColumn(col)) {
                        return error("Gephi's own '" + title + "' column cannot be converted");
                    }
                    if (checkOnly) {
                        return success("Ready");
                    }
                    final int before = countValues(gm, target, col);
                    Column tmp = acc.duplicateColumn(table, col, title + " (converting)", cls);
                    acc.deleteAttributeColumn(table, col);
                    Column converted = acc.duplicateColumn(table, tmp, title, cls);
                    acc.deleteAttributeColumn(table, tmp);
                    int after = countValues(gm, target, converted);
                    JsonObject r = success("Converted " + title + " to " + cls.getSimpleName());
                    r.addProperty("column", converted.getId());
                    r.addProperty("values_lost", before - after);
                    if (before > after) {
                        r.addProperty("warning", (before - after) + " value(s) could not be read as "
                            + cls.getSimpleName() + " and are now empty");
                    }
                    return r;
                }
                case "fill_empty": {
                    if (value == null) {
                        return error("Give the value to write in 'value'");
                    }
                    if (!acc.canChangeColumnData(col)) {
                        return error("The '" + title + "' column cannot be changed");
                    }
                    if (checkOnly) {
                        return success("Ready");
                    }
                    java.util.List<Node> nodes = new java.util.ArrayList<>();
                    java.util.List<Edge> edges = new java.util.ArrayList<>();
                    for (org.gephi.graph.api.Element el : elementsFor(gm, target)) {
                        if (el.getAttribute(col) != null) {
                            continue;
                        }
                        if (el instanceof Node) {
                            nodes.add((Node) el);
                        } else {
                            edges.add((Edge) el);
                        }
                    }
                    if (!nodes.isEmpty()) {
                        acc.fillNodesColumnWithValue(nodes.toArray(new Node[0]), col, value);
                    }
                    if (!edges.isEmpty()) {
                        acc.fillEdgesColumnWithValue(edges.toArray(new Edge[0]), col, value);
                    }
                    JsonObject r = success("Filled " + (nodes.size() + edges.size()) + " empty value(s) in " + title);
                    r.addProperty("filled", nodes.size() + edges.size());
                    return r;
                }
                case "clear": {
                    if (!acc.canClearColumnData(col)) {
                        return error("The '" + title + "' column cannot be cleared");
                    }
                    if (checkOnly) {
                        return success("Ready");
                    }
                    acc.clearColumnData(table, col);
                    return success("Cleared every value in " + title);
                }
                default:
                    return error("Unknown action: " + action + " (use delete|rename|convert|fill_empty|clear)");
            }
        } catch (Exception e) {
            return failure("Failed: ", e);
        } finally {
            unlockWrite(g);
        }
    }

    private static int countValues(GraphModel gm, String target, Column col) {
        int n = 0;
        for (org.gephi.graph.api.Element el : elementsFor(gm, target)) {
            if (el.getAttribute(col) != null) {
                n++;
            }
        }
        return n;
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
        Column col = findColumn(table, columnId);
        if (col == null) {
            return error("Column not found: " + columnId);
        }
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
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            f.addProperty(e.getKey(), e.getValue());
        }
        r.add("frequencies", f);
        return r;
    }

    /**
     * Groups of elements that share a value in one column (size >= 2). Pure
     * GraphModel logic, unit-testable. caseSensitive controls string matching.
     */
    static JsonObject detectDuplicatesCore(GraphModel gm, String target, String columnId, boolean caseSensitive) {
        Table table = tableFor(gm, target);
        Column col = findColumn(table, columnId);
        if (col == null) {
            return error("Column not found: " + columnId);
        }
        java.util.LinkedHashMap<String, java.util.List<String>> groups = new java.util.LinkedHashMap<>();
        for (org.gephi.graph.api.Element el : elementsFor(gm, target)) {
            Object v = el.getAttribute(col);
            if (v == null) {
                continue;
            }
            String key = v.toString();
            if (!caseSensitive) {
                key = key.toLowerCase();
            }
            groups.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(String.valueOf(el.getId()));
        }
        JsonArray dupes = new JsonArray();
        int groupCount = 0;
        for (java.util.List<String> ids : groups.values()) {
            if (ids.size() >= 2) {
                groupCount++;
                JsonArray a = new JsonArray();
                for (String id : ids) {
                    a.add(id);
                }
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
        if (gm == null) {
            return error("No workspace open");
        }
        if (columnId == null) {
            return error("Missing 'column'");
        }
        return columnValueFrequenciesCore(gm, target, columnId);
    }

    public JsonObject detectDuplicates(String target, String columnId, boolean caseSensitive) {
        GraphModel gm = currentGraphModel();
        if (gm == null) {
            return error("No workspace open");
        }
        if (columnId == null) {
            return error("Missing 'column'");
        }
        return detectDuplicatesCore(gm, target, columnId, caseSensitive);
    }

    /** Merge several nodes into one, reassigning edges; deletes the merged-away nodes. */
    public JsonObject mergeNodes(java.util.List<String> ids, String intoId) {
        GraphModel gm = currentGraphModel();
        if (gm == null) {
            return error("No workspace open");
        }
        if (ids == null || ids.isEmpty()) {
            return error("Missing 'ids'");
        }
        org.gephi.datalab.api.GraphElementsController gec =
            Lookup.getDefault().lookup(org.gephi.datalab.api.GraphElementsController.class);
        if (gec == null) {
            return error("No datalab controller available");
        }
        Graph g = gm.getGraph();
        java.util.List<Node> nodes = new java.util.ArrayList<>();
        for (String id : ids) {
            Node n = nodeById(g, id);
            if (n == null) {
                return error("Node not found: " + id);
            }
            nodes.add(n);
        }
        Node into = intoId != null ? nodeById(g, intoId) : nodes.get(0);
        if (into == null) {
            return error("Merge target node not found: " + intoId);
        }
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
            return failure("Merge failed: ", e);
        }
    }

    // ─── Edge appearance + generic export (Group E) ──────────────────

    /**
     * Color edges by an edge-column partition (relationship type, time period,
     * weight tier, …) — the edge twin of colorByPartition: per-value palette (supplied or
     * auto), applied through Gephi's Appearance API to the visible edges.
     */
    public JsonObject colorEdgesByPartition(String columnName, Map<String, int[]> colorMap) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        try {
            GraphModel gm = currentGraphModel();
            Graph graph = gm.getGraph();
            Column col = findColumn(gm.getEdgeTable(), columnName);
            if (col == null) {
                return error("Edge column not found: " + columnName);
            }

            java.util.Map<String, Color> palette = new java.util.LinkedHashMap<>();
            if (colorMap != null && !colorMap.isEmpty()) {
                for (Map.Entry<String, int[]> e : colorMap.entrySet()) {
                    int[] c = e.getValue();
                    palette.put(e.getKey(), new Color(c[0], c[1], c[2]));
                }
            } else {
                java.util.Map<String, Integer> counts = new java.util.HashMap<>();
                for (Edge ed : graph.getEdges().toArray()) {
                    Object v = ed.getAttribute(col);
                    if (v != null) {
                        counts.merge(v.toString(), 1, Integer::sum);
                    }
                }
                palette.putAll(partitionPalette(counts));
            }

            org.gephi.appearance.api.Function f = applyAppearance(ws, col, true,
                org.gephi.appearance.plugin.PartitionElementColorTransformer.class,
                fn -> applyPaletteToPartition(((org.gephi.appearance.api.PartitionFunction) fn).getPartition(),
                    fn.getGraph(), palette));
            if (f == null) {
                return noAppearanceFunction("edge partition colouring", col);
            }
            int colored = countVisible(gm, true, v -> v != null && palette.containsKey(v.toString()), col);
            JsonObject r = success("Colored " + colored + " edges by " + columnName);
            r.addProperty("partitions", palette.size());
            addPaletteNote(r, palette.size());
            addViewInfo(r, gm, true);
            return r;
        } catch (Exception e) {
            return failure("Failed: ", e);
        }
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

    /**
     * @param visible see exportGexf — same contract, response self-declares the view.
     */
    public JsonObject exportByFormat(String filePath, String format, boolean visible) {
        Workspace ws = currentWorkspace();
        if (ws == null) {
            return error("No project open");
        }
        if (filePath == null || format == null) {
            return error("Missing 'file' or 'format'");
        }
        try {
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            Exporter exporter = ec.getExporter(format);
            if (exporter == null && !format.startsWith(".")) {
                // Gephi matches an exporter by its name ("GML exporter") or by an extension
                // written with its dot (".gml"), so the bare extension finds nothing alone.
                exporter = ec.getExporter("." + format);
            }
            if (exporter == null) {
                return error("No exporter for format: " + format
                    + " (try vna, pajek, dl, spreadsheet, gdf, gml, json, gexf, graphml, csv)");
            }
            if (exporter instanceof GraphExporter) {
                ((GraphExporter) exporter).setExportVisible(visible);
                ((GraphExporter) exporter).setWorkspace(ws);
            }
            ec.exportFile(outputFile(filePath), exporter);
            JsonObject r = success("Exported to " + filePath);
            r.addProperty("format", format);
            addViewInfo(r, currentGraphModel(), visible);
            return r;
        } catch (Exception e) {
            return failure("Export failed: ", e);
        }
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
        if (gm == null) {
            return error("No workspace open");
        }
        JsonObject r = success("Timeline state");
        try {
            r.addProperty("graph_is_dynamic", gm.isDynamic());
            org.gephi.graph.api.Interval b = gm.getTimeBounds();
            if (b != null) {
                r.addProperty("time_min", b.getLow());
                r.addProperty("time_max", b.getHigh());
            }
            r.addProperty("time_format", String.valueOf(gm.getTimeFormat()));
        } catch (Exception e) {
            r.addProperty("bounds_error", e.getMessage());
        }
        org.gephi.timeline.api.TimelineController tc =
            Lookup.getDefault().lookup(org.gephi.timeline.api.TimelineController.class);
        if (tc != null) {
            try {
                JsonArray cols = new JsonArray();
                String[] dc = tc.getDynamicGraphColumns();
                if (dc != null) {
                    for (String c : dc) {
                        cols.add(c);
                    }
                }
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
            } catch (Exception e) {
                r.addProperty("timeline_error", e.getMessage());
            }
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
        if (gm == null) {
            return error("No workspace open");
        }
        if (columnId == null || regex == null || newColumnTitle == null) {
            return error("Missing 'column', 'regex', or 'new_column'");
        }
        org.gephi.datalab.api.AttributeColumnsController acc =
            Lookup.getDefault().lookup(org.gephi.datalab.api.AttributeColumnsController.class);
        if (acc == null) {
            return error("No datalab controller available");
        }
        Table table = tableFor(gm, target);
        Column col = findColumn(table, columnId);
        if (col == null) {
            return error("Column not found: " + columnId);
        }
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            Column created = acc.createBooleanMatchesColumn(table, col, newColumnTitle, pattern);
            JsonObject r = success("Created boolean match column: " + newColumnTitle);
            r.addProperty("column", created != null ? created.getId() : newColumnTitle);
            if (created != null) {
                // Gephi tests the whole value (Matcher.matches), so "^Dept-" marks nothing where
                // "Dept-.*" marks every label starting "Dept-". The count makes an all-false
                // column visible instead of looking like success.
                int matched = 0;
                org.gephi.graph.api.Element[] rows = table.isNodeTable()
                    ? gm.getGraph().getNodes().toArray() : gm.getGraph().getEdges().toArray();
                for (org.gephi.graph.api.Element row : rows) {
                    if (Boolean.TRUE.equals(row.getAttribute(created))) {
                        matched++;
                    }
                }
                r.addProperty("matched", matched);
                r.addProperty("rows", rows.length);
                if (matched == 0 && rows.length > 0) {
                    r.addProperty("warning", "No value matched. The pattern must match the whole value:"
                        + " use \"Dept-.*\" for values starting \"Dept-\", or \".*word.*\" for values"
                        + " containing it.");
                }
            }
            return r;
        } catch (java.util.regex.PatternSyntaxException e) {
            return error("Invalid regex: " + e.getMessage());
        } catch (Exception e) {
            return failure("Create match column failed: ", e);
        }
    }

}
