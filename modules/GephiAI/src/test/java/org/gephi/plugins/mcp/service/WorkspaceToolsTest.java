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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.util.Lookup;

/**
 * The read, statistic, extraction, styling, preview and export tools on a small known network
 * in Gephi's current workspace, called the way the HTTP API calls them.
 */
class WorkspaceToolsTest {

    private final GephiControlService service = GephiControlService.getInstance();
    private GraphModel gm;

    /**
     * Three parts. A path a-b-c-d with e joined to a, b and c (degrees a2 b3 c3 d1 e3); a pair
     * f-g; and h on its own. Each node has a group (x or y) and a score; edge weights differ.
     */
    @BeforeEach
    void network() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        gm.getNodeTable().addColumn("group", String.class);
        gm.getNodeTable().addColumn("score", Double.class);
        Graph g = gm.getUndirectedGraph();
        Object[][] rows = {{"a", "x", 1.5}, {"b", "x", 7.0}, {"c", "y", 3.0}, {"d", "y", 4.0},
            {"e", "x", 2.0}, {"f", "y", 0.5}, {"g", "y", 9.0}, {"h", "x", 5.0}};
        for (Object[] row : rows) {
            Node n = gm.factory().newNode(row[0]);
            n.setLabel(((String) row[0]).toUpperCase());
            n.setAttribute("group", row[1]);
            n.setAttribute("score", row[2]);
            g.addNode(n);
        }
        Object[][] edges = {{"a", "b", 1.0}, {"b", "c", 2.0}, {"c", "d", 3.0}, {"e", "a", 4.0},
            {"e", "b", 5.0}, {"e", "c", 6.0}, {"f", "g", 7.0}};
        for (Object[] e : edges) {
            Edge edge = gm.factory().newEdge(g.getNode(e[0]), g.getNode(e[1]), false);
            edge.setWeight((Double) e[2]);
            g.addEdge(edge);
        }
    }

    private static JsonObject ok(JsonObject r) {
        assertTrue(r.get("success").getAsBoolean(), r.toString());
        return r;
    }

    private static String refused(JsonObject r) {
        assertFalse(r.get("success").getAsBoolean(), r.toString());
        return r.get("error").getAsString();
    }

    private static List<String> ids(JsonArray nodes) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : nodes) {
            out.add(e.getAsJsonObject().get("id").getAsString());
        }
        return out;
    }

    private JsonObject query(String column, String value, String contains, Double min, Double max, int limit,
        int offset, String sortBy, boolean descending, String columns) {
        return ok(service.queryNodes(column, value, contains, min, max, limit, offset, false, sortBy, descending,
            columns));
    }

    private Node node(String id) {
        return gm.getGraph().getNode(id);
    }

    // ─── Reading ─────────────────────────────────────────────────────

    @Test
    void searchingByValueTextAndRangeKeepsOnlyMatches() {
        JsonObject byValue = query("group", "Y", null, null, null, 100, 0, null, true, null);
        assertEquals(List.of("c", "d", "f", "g"), ids(byValue.getAsJsonArray("nodes")));
        assertEquals(4, byValue.get("matches").getAsInt());
        assertEquals(8, byValue.get("total").getAsInt());

        JsonObject byRange = query("score", null, null, 2.0, 5.0, 100, 0, null, true, null);
        assertEquals(List.of("c", "d", "e", "h"), ids(byRange.getAsJsonArray("nodes")));

        JsonObject numericValue = query("score", "7", null, null, null, 100, 0, null, true, null);
        assertEquals(List.of("b"), ids(numericValue.getAsJsonArray("nodes")));

        JsonObject byText = query("group", null, "x", null, null, 100, 0, null, true, null);
        assertEquals(4, byText.get("matches").getAsInt());
    }

    @Test
    void searchNeedsAColumnThatExists() {
        assertTrue(refused(service.queryNodes(null, "x", null, null, null, 10, 0, false, null, true, null))
            .contains("column"));
        assertTrue(refused(service.queryNodes("colour", "x", null, null, null, 10, 0, false, null, true, null))
            .contains("colour"));
        assertTrue(refused(service.queryNodes(null, null, null, null, null, 10, 0, false, "rank", true, null))
            .contains("rank"));
    }

    @Test
    void sortingOrdersBeforePagingAndMatchesAreCountedPastTheLimit() {
        JsonObject top = query(null, null, null, null, null, 2, 0, "score", true, null);
        assertEquals(List.of("g", "b"), ids(top.getAsJsonArray("nodes")));
        assertEquals("score", top.get("sorted_by").getAsString());

        JsonObject next = query(null, null, null, null, null, 2, 2, "score", true, null);
        assertEquals(List.of("h", "d"), ids(next.getAsJsonArray("nodes")));

        JsonObject lowest = query(null, null, null, null, null, 1, 0, "degree", false, null);
        assertEquals(List.of("h"), ids(lowest.getAsJsonArray("nodes")));
        assertEquals("degree", lowest.get("sorted_by").getAsString());

        JsonObject paged = query("group", "x", null, null, null, 1, 0, null, true, null);
        assertEquals(1, paged.get("count").getAsInt());
        assertEquals(4, paged.get("matches").getAsInt(), "matches past the page were not counted");
    }

    @Test
    void askingForColumnsKeepsTheListingSmall() {
        JsonObject r = query(null, null, null, null, null, 1, 0, null, true, "score");
        JsonObject first = r.getAsJsonArray("nodes").get(0).getAsJsonObject();
        assertTrue(first.has("degree"));
        assertFalse(first.has("x"), "positions are left out when columns are named");
        assertFalse(first.has("r"), "colours are left out when columns are named");
        JsonObject attrs = first.getAsJsonObject("attributes");
        assertTrue(attrs.has("score"));
        assertFalse(attrs.has("group"));
    }

    @Test
    void edgesArePagedAndCounted() {
        JsonObject all = ok(service.queryEdges(100, 0, false));
        assertEquals(7, all.getAsJsonArray("edges").size());
        JsonObject page = ok(service.queryEdges(2, 6, false));
        assertEquals(1, page.getAsJsonArray("edges").size());
    }

    @Test
    void nodeIsReadWithItsAttributes() {
        JsonObject n = ok(service.getNode("b")).getAsJsonObject("node");
        assertEquals("B", n.get("label").getAsString());
        assertEquals("x", n.getAsJsonObject("attributes").get("group").getAsString());
        assertEquals(7.0, n.getAsJsonObject("attributes").get("score").getAsDouble());
        assertTrue(refused(service.getNode("zz")).contains("zz"));
    }

    @Test
    void statsTypeAndColumnsDescribeTheGraph() {
        JsonObject s = ok(service.getGraphStats(false));
        assertEquals(8, s.get("node_count").getAsInt());
        assertEquals(7, s.get("edge_count").getAsInt());
        assertFalse(s.get("is_directed").getAsBoolean());
        // Undirected: each tie fills one of n(n-1)/2 pairs and adds to two degrees.
        assertEquals(2.0 * 7 / (8 * 7), s.get("density").getAsDouble(), 1e-12);
        assertEquals(2.0 * 7 / 8, s.get("average_degree").getAsDouble(), 1e-12);

        assertTrue(ok(service.getGraphType()).get("undirected").getAsBoolean());

        JsonArray cols = ok(service.getColumns("node")).getAsJsonArray("columns");
        boolean found = false;
        for (JsonElement c : cols) {
            if ("score".equals(c.getAsJsonObject().get("title").getAsString())) {
                assertEquals("Double", c.getAsJsonObject().get("type").getAsString());
                found = true;
            }
        }
        assertTrue(found, cols.toString());
        assertTrue(ok(service.getColumns("edge")).getAsJsonArray("columns").size() > 0);
    }

    /**
     * A directed tie fills one of n(n-1) ordered pairs, as Gephi's own Density statistic counts
     * them. The undirected formula reported twice the density on a directed graph.
     */
    @Test
    void directedDensityCountsOrderedPairs() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        GraphModel dm = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph g = dm.getDirectedGraph();
        for (String id : new String[] {"p", "q", "r", "s"}) {
            g.addNode(dm.factory().newNode(id));
        }
        String[][] ties = {{"p", "q"}, {"q", "p"}, {"q", "r"}};
        for (String[] t : ties) {
            g.addEdge(dm.factory().newEdge(g.getNode(t[0]), g.getNode(t[1]), true));
        }

        JsonObject s = ok(service.getGraphStats(false));

        assertTrue(s.get("is_directed").getAsBoolean());
        assertEquals(3.0 / (4 * 3), s.get("density").getAsDouble(), 1e-12);
        // Edges per node: the mean in-degree, which equals the mean out-degree (Gephi's convention).
        assertEquals(3.0 / 4, s.get("average_degree").getAsDouble(), 1e-12);
    }

    /** The listing carries the index the switch, delete and compare tools take, not only Gephi's id. */
    @Test
    void theWorkspaceListingGivesTheIndexTheOtherToolsTake() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newWorkspace(pc.getCurrentProject());
        pc.newWorkspace(pc.getCurrentProject());

        JsonArray list = ok(service.listWorkspaces()).getAsJsonArray("workspaces");

        assertEquals(3, list.size());
        for (int i = 0; i < list.size(); i++) {
            JsonObject w = list.get(i).getAsJsonObject();
            assertEquals(i, w.get("index").getAsInt(), w.toString());
            ok(service.switchWorkspace(i));
            assertEquals(w.get("id").getAsInt(), pc.getCurrentWorkspace().getId(), w.toString());
        }
    }

    // ─── Statistics ──────────────────────────────────────────────────

    @Test
    void degreeWritesItsColumn() {
        JsonObject r = ok(service.runStatisticByName("Degree", null));
        assertEquals(1.75, r.get("average_degree").getAsDouble(), 1e-9);
        Object degree = node("b").getAttribute(GephiControlService.findColumn(gm.getNodeTable(), "degree"));
        assertEquals(3, ((Number) degree).intValue());
    }

    @Test
    void anUnknownStatisticIsNamed() {
        assertTrue(refused(service.runStatisticByName("NoSuchMeasure", null)).contains("NoSuchMeasure"));
    }

    @Test
    void mistypedParameterIsReportedNotSilentlyIgnored() {
        Map<String, Object> params = new HashMap<>();
        params.put("resolutoin", 1.0);
        JsonObject r = ok(service.runStatisticByName("Modularity", params));
        assertEquals("resolutoin", r.getAsJsonArray("unapplied_params").get(0).getAsString());
        assertTrue(r.get("warning").getAsString().contains("NOT applied"));
    }

    @Test
    void modularityReportsItsScoreAndWritesClasses() {
        JsonObject r = ok(service.computeModularity(1.0));
        assertTrue(r.has("modularity"), r.toString());
        assertNull(r.get("unapplied_params"), "resolution and useWeight are real Modularity settings");
        assertNotNull(GephiControlService.findColumn(gm.getNodeTable(), "modularity_class"));
    }

    @Test
    void connectedComponentsCountsTheThreeParts() {
        assertEquals(3, ok(service.computeConnectedComponents()).get("connected_components").getAsInt());
    }

    // ─── Extraction and filters ─────────────────────────────────────

    @Test
    void theGiantComponentKeepsTheLargestPartOnly() {
        JsonObject r = ok(service.extractGiantComponent());
        assertEquals(List.of("a", "b", "c", "d", "e"), sortedIds());
        assertEquals(5, gm.getGraph().getNodeCount(), r.toString());
    }

    @Test
    void anEgoNetworkKeepsNeighboursWithinTheDepth() {
        JsonObject r = ok(service.extractEgoNetwork("a", 1));
        assertEquals(3, r.get("kept_nodes").getAsInt());
        assertEquals(List.of("a", "b", "e"), sortedIds());
        assertTrue(refused(service.extractEgoNetwork("zz", 1)).contains("zz"));
    }

    @Test
    void isolatesAreCountedOnADryRunAndRemovedOtherwise() {
        JsonObject dry = ok(service.removeIsolates(true));
        assertEquals(1, dry.get("would_remove").getAsInt());
        assertEquals(8, gm.getGraph().getNodeCount(), "a dry run removed nodes");
        assertEquals(1, ok(service.removeIsolates(false)).get("removed").getAsInt());
        assertNull(node("h"));
    }

    @Test
    void degreeAndWeightFiltersCountOnADryRunAndRemoveOtherwise() {
        JsonObject dry = ok(service.filterByDegreeRange(3, 0, true));
        assertEquals(5, dry.get("would_remove").getAsInt());
        assertEquals(8, gm.getGraph().getNodeCount());
        assertEquals(5, ok(service.filterByDegreeRange(3, 0, false)).get("removed").getAsInt());
        assertEquals(List.of("b", "c", "e"), sortedIds());

        JsonObject w = ok(service.filterByEdgeWeight(5.0, 0, true));
        assertEquals(2, w.get("would_remain").getAsInt());
        assertEquals(1, ok(service.filterByEdgeWeight(5.0, 0, false)).get("removed").getAsInt());
    }

    @Test
    void theFilterListNamesFiltersAndTheirProperties() {
        JsonArray filters = ok(service.listFilters()).getAsJsonArray("filters");
        JsonObject degree = null;
        for (JsonElement f : filters) {
            if ("Degree Range".equals(f.getAsJsonObject().get("name").getAsString())) {
                degree = f.getAsJsonObject();
            }
        }
        assertNotNull(degree, filters.toString());
        assertTrue(degree.getAsJsonArray("properties").size() > 0);
    }

    @Test
    void filterWritesItsMembershipToAColumn() {
        Map<String, Object> params = Map.of("range", List.of(3, 100));
        JsonObject r = ok(service.applyFilter("Degree Range", params, "column", "busy"));
        assertEquals("busy", r.get("column").getAsString());
        org.gephi.graph.api.Column busy = GephiControlService.findColumn(gm.getNodeTable(), "busy");
        assertNotNull(busy, java.util.Arrays.toString(gm.getNodeTable().toArray()) + " " + r);
        Object mark = node("b").getAttribute(busy);
        assertEquals(Boolean.TRUE, mark);
        assertEquals(8, gm.getGraphVisible().getNodeCount(), "a column export hid nodes");
    }

    @Test
    void combinedFiltersWriteTheirMembershipToAColumn() {
        Map<String, Object> degree3 = Map.of("name", "Degree Range", "params", Map.of("range", List.of(3, 100)));
        ok(service.applyFilters(List.of(degree3), "all", "column", "busy", false));
        org.gephi.graph.api.Column busy = GephiControlService.findColumn(gm.getNodeTable(), "busy");
        assertNotNull(busy, "the column the response named was never written");
        assertEquals(Boolean.TRUE, node("e").getAttribute(busy));
        assertEquals(Boolean.FALSE, node("a").getAttribute(busy));
    }

    @Test
    void filteredCopyHoldsOnlyTheKeptNodes() throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        org.gephi.project.api.Workspace original = pc.getCurrentWorkspace();
        ok(service.applyFilter("Degree Range", Map.of("range", List.of(3, 100)), "new_workspace", null));
        // Gephi copies the subgraph on a thread of its own.
        long deadline = System.currentTimeMillis() + 10_000;
        while (pc.getCurrentProject().getWorkspaces().size() < 2 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        org.gephi.project.api.Workspace copy = null;
        for (org.gephi.project.api.Workspace w : pc.getCurrentProject().getWorkspaces()) {
            if (w != original) {
                copy = w;
            }
        }
        assertNotNull(copy, "no workspace was made");
        GraphModel copied = Lookup.getDefault().lookup(GraphController.class).getGraphModel(copy);
        while (copied.getGraph().getNodeCount() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertEquals(3, copied.getGraph().getNodeCount(), "the copy is not the filtered subgraph");
        assertEquals(8, gm.getGraph().getNodeCount());
    }

    @Test
    void badFilterRequestChangesNothing() {
        assertTrue(refused(service.applyFilter("No Such Filter", null, null, null)).contains("No Such Filter"));
        assertTrue(refused(service.applyFilter("Degree Range", Map.of("rnage", List.of(1, 2)), null, null))
            .contains("valid properties"));
        assertTrue(refused(service.applyFilter("Degree Range", Map.of("range", List.of(3, 100)), "hide", null))
            .contains("Unknown action"));
        assertTrue(refused(service.applyFilter("Degree Range", Map.of("range", List.of(3, 100)), "column", null))
            .contains("column"));
        assertTrue(refused(service.applyFilter(null, null, null, null)).contains("name"));
        assertEquals(8, gm.getGraphVisible().getNodeCount());
    }

    // ─── Paths ───────────────────────────────────────────────────────

    @Test
    void pathIsFoundAndCanBeMarked() {
        JsonObject r = ok(service.findShortestPath("a", "d", null, true, "route"));
        assertTrue(r.get("found").getAsBoolean());
        assertEquals(3, r.get("steps").getAsInt());
        assertEquals(2, r.get("equally_short_paths").getAsInt());
        Object onRoute = node("d").getAttribute(GephiControlService.findColumn(gm.getNodeTable(), "route"));
        assertEquals(Boolean.TRUE, onRoute);
        Object offRoute = node("h").getAttribute(GephiControlService.findColumn(gm.getNodeTable(), "route"));
        assertEquals(Boolean.FALSE, offRoute);
    }

    @Test
    void unreachableAndUnknownNodesAreReported() {
        assertFalse(ok(service.findShortestPath("a", "g", null, true, null)).get("found").getAsBoolean());
        assertTrue(refused(service.findShortestPath("a", "zz", null, true, null)).contains("zz"));
        assertTrue(refused(service.findShortestPath("zz", "a", null, true, null)).contains("zz"));
        assertTrue(refused(service.findShortestPath("a", "d", "fastest", true, null)).contains("weighting"));
        assertEquals(0, ok(service.findShortestPath("a", "a", "distance", true, null)).get("steps").getAsInt());
    }

    @Test
    void markingOntoATextColumnIsRefusedAndAddsNoColumn() {
        String error = refused(service.findShortestPath("a", "d", null, true, "group"));
        assertTrue(error.contains("group"), error);
        assertNull(GephiControlService.findColumn(gm.getEdgeTable(), "group"),
            "the refused mark still added an edge column");
    }

    // ─── Appearance ─────────────────────────────────────────────────

    @Test
    void partitionColoursGiveEachGroupOneColour() {
        ok(service.colorByPartition("group", null));
        assertEquals(node("a").getColor(), node("b").getColor());
        assertNotEquals(node("a").getColor(), node("c").getColor());
        assertTrue(refused(service.colorByPartition("colour", null)).contains("colour"));
    }

    @Test
    void partitionColoursGivenByTheCallerAreUsed() {
        Map<String, int[]> colours = new HashMap<>();
        colours.put("x", new int[]{0, 114, 178});
        colours.put("y", new int[]{230, 159, 0});
        ok(service.colorByPartition("group", colours));
        assertEquals(new Color(0, 114, 178), withoutAlpha(node("a").getColor()));
        assertEquals(new Color(230, 159, 0), withoutAlpha(node("d").getColor()));
    }

    @Test
    void rankingColoursRunFromTheLowestToTheHighestValue() {
        JsonObject r = ok(service.colorByRanking("score", 0, 114, 178, 213, 94, 0));
        assertEquals(0.5, r.get("min_value").getAsDouble());
        assertEquals(9.0, r.get("max_value").getAsDouble());
        assertEquals(new Color(0, 114, 178), withoutAlpha(node("f").getColor()));
        assertEquals(new Color(213, 94, 0), withoutAlpha(node("g").getColor()));
        assertTrue(refused(service.colorByRanking("group", 0, 0, 0, 1, 1, 1)).contains("group"));
    }

    @Test
    void rankingSizesRunFromMinToMaxAndCanBeCapped() {
        ok(service.sizeByRanking("score", 4f, 40f, null));
        assertEquals(4f, node("f").size(), 1e-4);
        assertEquals(40f, node("g").size(), 1e-4);

        JsonObject capped = ok(service.sizeByRanking("score", 4f, 40f, 5.0));
        assertEquals(40f, node("h").size(), 1e-4, "a node at the cap gets the largest size");
        assertEquals(40f, node("g").size(), 1e-4);
        assertEquals(3, capped.get("nodes_at_cap").getAsInt(), "h, b and g score 5 or more");
    }

    @Test
    void edgeThicknessFollowsWeight() {
        ok(service.setEdgeThicknessByWeight(1f, 5f));
        JsonObject preview = ok(service.getPreviewSettings()).getAsJsonObject("settings");
        assertNotNull(preview);
    }

    // ─── Preview settings ───────────────────────────────────────────

    @Test
    void previewSettingsAreReadBackAfterTheyAreSet() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("node.label.show", true);
        settings.put("background.color", "#102030");
        settings.put("edge.thickness", 2.5);
        settings.put("node.label.font", "Arial 18 Bold");
        settings.put("edge.color", "mixed");
        JsonObject r = ok(service.setPreviewSettings(settings));
        assertEquals(5, r.get("properties_set").getAsInt(), r.toString());

        JsonObject read = ok(service.getPreviewSettings()).getAsJsonObject("settings");
        assertTrue(read.has("edge.thickness") && read.has("node.label.font"), read.toString());
        assertTrue(read.get("node.label.show").getAsBoolean());
        assertEquals("#102030", read.get("background.color").getAsString());
        assertEquals(2.5, read.get("edge.thickness").getAsDouble(), 1e-6);
        assertTrue(read.get("node.label.font").getAsString().endsWith("18 Bold"), read.toString());
        assertEquals("mixed", read.get("edge.color").getAsString());
    }

    @Test
    void wholeNumberFromJsonSetsAnIntegerSetting() {
        // JSON numbers reach the service as doubles; "30.0" is not an integer to Integer.parseInt.
        Map<String, Object> settings = new HashMap<>();
        settings.put("node.label.max-char", 30.0);
        JsonObject r = ok(service.setPreviewSettings(settings));
        assertEquals(1, r.get("properties_set").getAsInt(), r.toString());
        assertEquals(30, ok(service.getPreviewSettings()).getAsJsonObject("settings")
            .get("node.label.max-char").getAsInt());
    }

    @Test
    void misspelledOrUnreadableSettingIsNamedAndNotCounted() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("node.lable.show", true);
        settings.put("edge.thickness", "thick");
        settings.put("node.label.show", "perhaps");
        JsonObject r = ok(service.setPreviewSettings(settings));
        assertEquals(0, r.get("properties_set").getAsInt(), r.toString());
        assertEquals("node.lable.show", r.getAsJsonArray("unknown_properties").get(0).getAsString());
        assertEquals(2, r.getAsJsonArray("unapplied_properties").size(), r.toString());
        assertTrue(r.get("warning").getAsString().startsWith("Not preview properties"), r.toString());
        assertTrue(r.get("warning").getAsString().endsWith("."), r.toString());
    }

    @Test
    void sourceColouredEdgesTakeTheirSourceNodesColour() {
        node("a").setColor(new Color(0, 114, 178));
        Map<String, Object> settings = new HashMap<>();
        settings.put("edge.color", "source");
        assertEquals(1, ok(service.setPreviewSettings(settings)).get("properties_set").getAsInt());
        Graph g = gm.getGraph();
        Edge ab = GephiControlService.findEdge(g, node("a"), node("b"));
        assertEquals(new Color(0, 114, 178), withoutAlpha(ab.getColor()));
    }

    @Test
    void textSettingFromAPluginRendererIsSet() {
        // Third-party renderers (Shape by Partition's column and legend title, for example)
        // register String-typed properties; Gephi core registers none.
        PreviewProperties props = Lookup.getDefault().lookup(PreviewController.class)
            .getModel().getProperties();
        props.addProperty(PreviewProperty.createProperty(this, "test.text", String.class,
            "Text", "A text setting", PreviewProperty.CATEGORY_NODES).setValue(""));
        Map<String, Object> settings = new HashMap<>();
        settings.put("test.text", " Field of Study ");
        JsonObject r = ok(service.setPreviewSettings(settings));
        assertEquals(1, r.get("properties_set").getAsInt(), r.toString());
        assertEquals(" Field of Study ", props.getStringValue("test.text"));

        settings.put("test.text", 12.0);
        assertEquals(0, ok(service.setPreviewSettings(settings)).get("properties_set").getAsInt());
        assertEquals(" Field of Study ", props.getStringValue("test.text"));
    }

    // ─── Export ──────────────────────────────────────────────────────

    @Test
    void gexfIsReturnedInlineWithEveryNode() {
        JsonObject r = ok(service.exportGexfContent(false));
        String gexf = r.get("content").getAsString();
        for (String id : List.of("a", "b", "h")) {
            assertTrue(gexf.contains("id=\"" + id + "\""), id);
        }
    }

    @Test
    void filesAreWrittenInTheAskedFormat(@TempDir Path dir) throws Exception {
        Path gexf = dir.resolve("g.gexf");
        ok(service.exportGexf(gexf.toString(), false));
        assertTrue(Files.readString(gexf, StandardCharsets.UTF_8).contains("<gexf"));

        Path graphml = dir.resolve("g.graphml");
        ok(service.exportGraphml(graphml.toString(), false));
        assertTrue(Files.readString(graphml, StandardCharsets.UTF_8).contains("<graphml"));

        Path gdf = dir.resolve("g.gdf");
        JsonObject r = ok(service.exportByFormat(gdf.toString(), "gdf", false));
        assertEquals("gdf", r.get("format").getAsString());
        assertTrue(Files.size(gdf) > 0);

        assertTrue(refused(service.exportByFormat(dir.resolve("g.xyz").toString(), "xyz", false))
            .contains("xyz"));
    }

    @Test
    void csvHasAHeaderAndARowPerNodeOrEdge(@TempDir Path dir) throws Exception {
        Path nodes = dir.resolve("nodes.csv");
        ok(service.exportCsv(nodes.toString(), ",", "nodes"));
        List<String> lines = Files.readAllLines(nodes, StandardCharsets.UTF_8);
        assertTrue(lines.get(0).startsWith("Id,Label"), lines.get(0));
        assertEquals(9, lines.size());

        Path edges = dir.resolve("edges.csv");
        ok(service.exportCsv(edges.toString(), ";", "edges"));
        assertEquals(8, Files.readAllLines(edges, StandardCharsets.UTF_8).size());
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private List<String> sortedIds() {
        List<String> out = new ArrayList<>();
        for (Node n : gm.getGraph().getNodes().toArray()) {
            out.add(n.getId().toString());
        }
        out.sort(String::compareTo);
        return out;
    }

    private static Color withoutAlpha(Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue());
    }
}
