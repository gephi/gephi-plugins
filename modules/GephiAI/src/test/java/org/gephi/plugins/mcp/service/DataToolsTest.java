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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openide.util.Lookup;

/** Column tidy-up, time from columns, and combined filters on a small known network. */
class DataToolsTest {

    private final GephiControlService service = GephiControlService.getInstance();
    private GraphModel gm;

    /**
     * A path a-b-c-d plus e joined to a, b and c: degrees a2 b3 c3 d1 e3. Each node has a
     * group (x for a, b, e; y for c, d), a score as text, and a joining year.
     */
    @BeforeEach
    void network() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        gm.getNodeTable().addColumn("group", String.class);
        gm.getNodeTable().addColumn("score", String.class);
        gm.getNodeTable().addColumn("joined", Integer.class);
        Graph g = gm.getUndirectedGraph();
        Object[][] rows = {{"a", "x", "1.5", 1990}, {"b", "x", "oops", 1994}, {"c", "y", null, 1998},
            {"d", "y", "4", 1999}, {"e", "x", "2", 1991}};
        for (Object[] row : rows) {
            Node n = gm.factory().newNode(row[0]);
            n.setAttribute("group", row[1]);
            if (row[2] != null) {
                n.setAttribute("score", row[2]);
            }
            n.setAttribute("joined", row[3]);
            g.addNode(n);
        }
        String[][] edges = {{"a", "b"}, {"b", "c"}, {"c", "d"}, {"e", "a"}, {"e", "b"}, {"e", "c"}};
        for (String[] e : edges) {
            g.addEdge(gm.factory().newEdge(g.getNode(e[0]), g.getNode(e[1]), false));
        }
    }

    private Object value(String node, String column) {
        return gm.getGraph().getNode(node).getAttribute(GephiControlService.findColumn(gm.getNodeTable(), column));
    }

    @Test
    void convertingReportsTheValuesItCouldNotRead() {
        JsonObject r = service.editColumn("node", "score", "convert", null, "double", null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(1, r.get("values_lost").getAsInt(), "'oops' is not a number");
        assertEquals(1.5, value("a", "score"));
        assertNull(value("b", "score"));
    }

    @Test
    void fillingEmptyCellsLeavesFilledOnesAlone() {
        JsonObject r = service.editColumn("node", "score", "fill_empty", "0", null, null);

        assertEquals(1, r.get("filled").getAsInt(), r.toString());
        assertEquals("0", value("c", "score"));
        assertEquals("oops", value("b", "score"));
    }

    @Test
    void renamingKeepsTheValues() {
        service.editColumn("node", "group", "rename", null, null, "Team");

        assertNull(GephiControlService.findColumn(gm.getNodeTable(), "group"));
        assertEquals("y", value("d", "Team"));
    }

    @Test
    void gephisOwnColumnsAreRefused() {
        JsonObject r = service.editColumn("node", "Label", "delete", null, null, null);

        assertFalse(r.get("success").getAsBoolean());
        assertNotNull(GephiControlService.findColumn(gm.getNodeTable(), "Label"));
    }

    @Test
    void checkChangesNothing() {
        JsonObject ok = service.editColumn("node", "group", "delete", null, null, null, true);
        JsonObject refused = service.editColumn("node", "Label", "delete", null, null, null, true);

        assertTrue(ok.get("success").getAsBoolean());
        assertFalse(refused.get("success").getAsBoolean());
        assertNotNull(GephiControlService.findColumn(gm.getNodeTable(), "group"), "a check deleted the column");
    }

    @Test
    void timeFromAYearColumnGivesEveryNodeAnInterval() {
        JsonObject r = service.setTimeFromColumns("node", "joined", null, null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(5, r.get("with_time").getAsInt());
        assertEquals(1990.0, r.get("time_min").getAsDouble());
    }

    @Test
    void textDatesNeedAPatternAndAreCheckedBeforeAnythingChanges() {
        JsonObject r = service.setTimeFromColumns("node", "group", null, null, true);

        assertFalse(r.get("success").getAsBoolean());
        assertTrue(r.get("error").getAsString().contains("date_format"), r.toString());
    }

    private int kept(JsonObject r) {
        assertTrue(r.get("success").getAsBoolean(), r.toString());
        return r.get("nodes_kept").getAsInt();
    }

    @Test
    void combinedFiltersCountAndOrAndNot() {
        Map<String, Object> degree3 = Map.of("name", "Degree Range", "params", Map.of("range", List.of(3, 100)));
        Map<String, Object> groupX = Map.of("name", "Equal: group String (Node)", "params", Map.of("pattern", "x"));
        Map<String, Object> notX = Map.of("name", "Equal: group String (Node)", "params", Map.of("pattern", "x"),
            "exclude", true);

        // degree 3: b, c, e. group x: a, b, e.
        assertEquals(2, kept(service.applyFilters(List.of(degree3, groupX), "all", null, null, true)));
        assertEquals(4, kept(service.applyFilters(List.of(degree3, groupX), "any", null, null, true)));
        assertEquals(1, kept(service.applyFilters(List.of(degree3, notX), "all", null, null, true)));
        assertEquals(5, gm.getGraphVisible().getNodeCount(), "a dry run hid nodes");
    }

    @Test
    void anUnknownFilterIsNamed() {
        JsonObject r = service.applyFilters(List.of(Map.of("name", "No Such Filter")), "all", null, null, true);

        assertFalse(r.get("success").getAsBoolean());
        assertTrue(r.get("error").getAsString().contains("No Such Filter"));
    }

    /** A visible filter reports the counts Gephi applied, not the ones before it finished. */
    @Test
    void visibleFilterReportsTheCountsItApplied() {
        JsonObject r = service.applyFilter("Degree Range", Map.of("range", List.of(3, 100)), "select", null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(3, r.get("nodes_after").getAsInt(), r.toString());
        assertFalse(r.has("counts_settled"), r.toString());
        assertEquals(3, gm.getGraphVisible().getNodeCount());
    }

    /**
     * Gephi 0.11.4 says when a filter's result is applied. A filter that keeps every node used
     * to wait out the whole settling budget, because its count never moved.
     */
    @Test
    void filterThatKeepsEveryNodeAnswersAtOnce() {
        // Warm up first, so the timing below measures the wait and not class loading.
        service.applyFilter("Degree Range", Map.of("range", List.of(0, 100)), "select", null);
        service.resetFilters();
        long started = System.nanoTime();
        JsonObject r = service.applyFilter("Degree Range", Map.of("range", List.of(0, 100)), "select", null);
        long ms = (System.nanoTime() - started) / 1_000_000;

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(5, r.get("nodes_after").getAsInt(), r.toString());
        assertTrue(ms < 1000, "a filter that removes nothing took " + ms + " ms");
    }

    @Test
    void combinedFiltersOnTheVisibleGraphAnswerAtOnce() {
        Map<String, Object> degree3 = Map.of("name", "Degree Range", "params", Map.of("range", List.of(3, 100)));
        service.applyFilters(List.of(degree3), "all", "select", null, false);
        service.resetFilters();
        long started = System.nanoTime();
        JsonObject r = service.applyFilters(List.of(degree3), "all", "select", null, false);
        long ms = (System.nanoTime() - started) / 1_000_000;

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(3, r.get("nodes_after").getAsInt(), r.toString());
        assertTrue(ms < 1000, "combined filters took " + ms + " ms");
    }

    /** A filter model that reports running for a set number of checks. */
    private static org.gephi.filters.api.FilterModel runningFor(int checks) {
        java.util.concurrent.atomic.AtomicInteger left = new java.util.concurrent.atomic.AtomicInteger(checks);
        return (org.gephi.filters.api.FilterModel) java.lang.reflect.Proxy.newProxyInstance(
            org.gephi.filters.api.FilterModel.class.getClassLoader(),
            new Class<?>[] {org.gephi.filters.api.FilterModel.class},
            (proxy, method, args) -> "isRunning".equals(method.getName()) ? left.getAndDecrement() > 0 : null);
    }

    @Test
    void filterWaitEndsWhenGephiSaysTheResultIsApplied() {
        assertTrue(GephiControlService.awaitFilterApplied(runningFor(3), 5_000));
    }

    @Test
    void filterStillRunningAtTheBudgetIsReportedAsNotSettled() {
        assertFalse(GephiControlService.awaitFilterApplied(runningFor(Integer.MAX_VALUE), 100));
    }

    @Test
    void noFilterModelMeansNothingToWaitFor() {
        assertTrue(GephiControlService.awaitFilterApplied(null, 100));
    }

    /**
     * Resetting filters must leave Gephi's Filters panel showing no active filter, as its own
     * Filter button does when turned off, or the panel keeps claiming a filter the graph no
     * longer shows and a later refresh can bring it back.
     */
    @Test
    void resettingFiltersAlsoTurnsOffTheFiltersPanel() {
        service.applyFilter("Degree Range", Map.of("range", List.of(3, 100)), "select", null);
        org.gephi.filters.api.FilterModel fm =
            Lookup.getDefault().lookup(org.gephi.filters.api.FilterController.class).getModel();
        assertTrue(fm.isFiltering(), "precondition: the filter is active in the panel");

        service.resetFilters();
        GephiControlService.awaitFilterApplied(fm);

        assertEquals(5, gm.getGraphVisible().getNodeCount(), "the full graph is visible");
        assertFalse(fm.isFiltering(), "the Filters panel still shows a filter as active");
    }

    @Test
    void resettingFiltersAlsoTurnsOffASelectionFilter() {
        org.gephi.filters.api.FilterController fc =
            Lookup.getDefault().lookup(org.gephi.filters.api.FilterController.class);
        org.gephi.filters.spi.FilterBuilder degree = null;
        for (org.gephi.filters.spi.FilterBuilder b
            : Lookup.getDefault().lookupAll(org.gephi.filters.spi.FilterBuilder.class)) {
            if ("Degree Range".equals(b.getName())) {
                degree = b;
            }
        }
        assertTrue(degree != null, "Gephi's Degree Range filter is available");
        org.gephi.project.api.Workspace ws = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        org.gephi.filters.api.Query query = fc.createQuery(degree.getFilter(ws));
        fc.add(query);
        fc.selectVisible(query);
        GephiControlService.awaitFilterApplied(fc.getModel());
        assertTrue(fc.getModel().isSelecting(), "precondition: the selection filter is active");

        service.resetFilters();

        assertFalse(fc.getModel().isSelecting(), "the Filters panel still shows a selection filter");
    }

    /**
     * A filter still being computed when the reset arrives must not come back after it: Gephi's
     * filter thread can apply its result once the reset has released the graph.
     */
    @Test
    void filterStillComputingWhenResetIsCalledDoesNotComeBack() {
        org.gephi.filters.api.FilterController fc =
            Lookup.getDefault().lookup(org.gephi.filters.api.FilterController.class);
        org.gephi.filters.spi.FilterBuilder degree = null;
        for (org.gephi.filters.spi.FilterBuilder b
            : Lookup.getDefault().lookupAll(org.gephi.filters.spi.FilterBuilder.class)) {
            if ("Degree Range".equals(b.getName())) {
                degree = b;
            }
        }
        org.gephi.project.api.Workspace ws = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        for (int round = 0; round < 30; round++) {
            org.gephi.filters.api.Query query = fc.createQuery(degree.getFilter(ws));
            fc.add(query);
            fc.filterVisible(query);
            service.resetFilters();
            GephiControlService.awaitFilterApplied(fc.getModel());

            assertFalse(fc.getModel().isFiltering(), "round " + round + ": the filter came back after the reset");
            assertTrue(gm.getVisibleView().isMainView(), "round " + round + ": the graph is filtered after the reset");
        }
    }

    /** A screenshot into a folder that cannot be made says so at once, before asking Gephi. */
    @Test
    void screenshotIntoAFolderThatCannotBeMadeIsRefusedAtOnce(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tmp)
        throws Exception {
        java.nio.file.Path blocker = java.nio.file.Files.createFile(tmp.resolve("not-a-folder"));
        long started = System.nanoTime();

        JsonObject r = service.exportScreenshot(blocker.resolve("shot.png").toString(), 1, false);

        assertFalse(r.get("success").getAsBoolean(), r.toString());
        assertTrue(r.get("error").getAsString().contains("folder"), r.toString());
        assertTrue((System.nanoTime() - started) / 1_000_000 < 2_000, "it waited for Gephi first");
    }

    @Test
    void resetReportsSuccess() {
        service.applyFilter("Degree Range", Map.of("range", List.of(3, 100)), "select", null);

        JsonObject r = service.resetFilters();

        assertTrue(r.get("success").getAsBoolean(), r.toString());
    }

    /** A view set some other way than through the Filters panel is cleared too. */
    @Test
    void resetClearsAViewSetOutsideTheFiltersPanel() {
        org.gephi.graph.api.GraphView view = gm.createView();
        Graph sub = gm.getGraph(view);
        sub.addNode(gm.getGraph().getNode("a"));
        gm.setVisibleView(view);
        assertFalse(gm.getVisibleView().isMainView(), "precondition: a subgraph is visible");

        assertTrue(service.resetFilters().get("success").getAsBoolean());

        assertTrue(gm.getVisibleView().isMainView(), "the subgraph view is still visible");
        assertEquals(5, gm.getGraphVisible().getNodeCount());
        assertFalse(gm.getGraph().getLock().isWriteLocked(), "the reset left the graph write-locked");
    }

    /** An export into folders that do not exist yet creates them. */
    @Test
    void outputFileCreatesMissingFolders(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tmp) {
        java.io.File f = GephiControlService.outputFile(tmp.resolve("new/deeper/map.gexf").toString());

        assertTrue(f.getParentFile().isDirectory(), "the folders were not created");
        assertEquals(tmp.resolve("new/deeper/map.gexf").toFile().getAbsoluteFile(), f);
    }
}
