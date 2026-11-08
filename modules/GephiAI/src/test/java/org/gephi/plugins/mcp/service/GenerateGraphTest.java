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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openide.util.Lookup;

/** Graphs made by Gephi's own generators, loaded the way an imported file is. */
class GenerateGraphTest {

    private final GephiControlService service = GephiControlService.getInstance();

    @BeforeEach
    void freshProject() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
    }

    /**
     * The graph's edges as sorted pairs of node positions (the order the generator added them), so
     * two graphs compare edge for edge. Gephi numbers each generated graph's ids from a counter that
     * keeps rising, so raw ids differ between two identical graphs.
     */
    private static List<String> edgeSet() {
        Graph g = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        java.util.Map<Node, Integer> position = new java.util.HashMap<>();
        for (Node n : g.getNodes().toArray()) {
            position.put(n, position.size());
        }
        List<String> edges = new ArrayList<>();
        for (org.gephi.graph.api.Edge e : g.getEdges().toArray()) {
            int a = position.get(e.getSource());
            int b = position.get(e.getTarget());
            edges.add(Math.min(a, b) + "-" + Math.max(a, b));
        }
        Collections.sort(edges);
        return edges;
    }

    @Test
    void preferentialAttachmentHasTheModelsEdgeCount() {
        JsonObject r = service.generateGraph("preferential_attachment", 200, 2, null, 42L);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(200, r.get("node_count").getAsInt());
        // A star of m + 1 nodes, then m edges for each later node.
        assertEquals(2 + (200 - 2 - 1) * 2, r.get("edge_count").getAsInt());
        assertTrue(r.get("reproducible").getAsBoolean());
        assertEquals(42L, r.get("seed").getAsLong());
    }

    @Test
    void theSameSeedGivesTheSameGraph() {
        service.generateGraph("preferential_attachment", 300, 3, null, 7L);
        List<String> first = edgeSet();
        service.generateGraph("preferential_attachment", 300, 3, null, 7L);
        List<String> second = edgeSet();
        service.generateGraph("preferential_attachment", 300, 3, null, 8L);
        List<String> other = edgeSet();

        assertEquals(first, second);
        assertNotEquals(first, other);
    }

    private static org.gephi.graph.api.GraphModel currentModel() {
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }

    /**
     * Barabasi-Albert and Erdos-Renyi are undirected models. Loaded with Gephi's default edge type
     * the graph came out directed, which made in-degree, reciprocity and density read wrongly.
     */
    @Test
    void generatedGraphsAreUndirected() {
        JsonObject ba = service.generateGraph("preferential_attachment", 60, 2, null, 3L);
        assertTrue(ba.get("success").getAsBoolean(), ba.toString());
        assertFalse(ba.get("directed").getAsBoolean(), ba.toString());
        assertTrue(currentModel().isUndirected(), "preferential attachment graph is not undirected");
        assertEquals(2 + (60 - 2 - 1) * 2, ba.get("edge_count").getAsInt());

        JsonObject er = service.generateGraph("random", 60, null, 0.2, null);
        assertTrue(er.get("success").getAsBoolean(), er.toString());
        assertFalse(er.get("directed").getAsBoolean(), er.toString());
        assertTrue(currentModel().isUndirected(), "random graph is not undirected");
    }

    @Test
    void randomGraphSaysItCannotBeReproduced() {
        JsonObject r = service.generateGraph("random", 100, null, 0.1, null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(100, r.get("node_count").getAsInt());
        assertTrue(r.get("edge_count").getAsInt() > 0 && r.get("edge_count").getAsInt() < 100 * 99 / 2);
        assertFalse(r.get("reproducible").getAsBoolean());
    }

    @Test
    void eachGraphGetsItsOwnWorkspaceNamedAfterTheModel() {
        service.generateGraph("preferential_attachment", 50, 1, null, 1L);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        String name = pc.getCurrentWorkspace().getLookup()
            .lookup(org.gephi.project.api.WorkspaceInformation.class).getName();

        assertTrue(name.startsWith("Preferential attachment"), name);
        assertTrue(name.contains("seed 1"), name);
    }

    @Test
    void badSettingsAreRefusedWithTheReason() {
        assertTrue(service.generateGraph("small_world", 50, 2, null, null).get("error").getAsString()
            .contains("preferential_attachment"));
        assertTrue(service.generateGraph("preferential_attachment", 10, 10, null, null).get("error")
            .getAsString().contains("edges_per_node"));
        assertTrue(service.generateGraph("random", 10, null, 1.5, null).get("error").getAsString()
            .contains("wiring_probability"));
        assertTrue(service.generateGraph("random", 1, null, 0.5, null).get("error").getAsString()
            .contains("nodes"));
        assertTrue(service.generateGraph("random", GephiControlService.GENERATE_MAX_RANDOM_NODES + 1, null, 0.01,
            null).get("error").getAsString().contains("nodes"));
    }

    /** One request must not be able to run Gephi out of memory. */
    @Test
    void preferentialAttachmentRefusesAnEdgeCountOverTheCap() {
        JsonObject r = service.generateGraph("preferential_attachment", 100_000, 50_000, null, 1L);

        assertFalse(r.get("success").getAsBoolean());
        String error = r.get("error").getAsString();
        String cap = String.valueOf(GephiControlService.GENERATE_MAX_EXPECTED_EDGES);
        assertTrue(error.contains("edges") && error.contains(cap), error);
    }

    @Test
    void preferentialAttachmentRefusesTooManyNodes() {
        JsonObject r = service.generateGraph("preferential_attachment", GephiControlService.GENERATE_MAX_NODES + 1, 2,
            null, 1L);

        assertTrue(r.get("error").getAsString().contains("nodes"), r.toString());
    }

    @Test
    void randomGraphRefusesAnExpectedEdgeCountOverTheCap() {
        JsonObject r = service.generateGraph("random", 20_000, null, 0.5, null);

        assertTrue(r.get("error").getAsString().contains("expects about"), r.toString());
    }

    private static int workspaceCount() {
        return Lookup.getDefault().lookup(ProjectController.class).getCurrentProject().getWorkspaces().size();
    }

    /** An empty workspace the person named is theirs: generating a graph must not delete it. */
    @Test
    void anEmptyWorkspaceThePersonNamedIsKept() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.renameWorkspace(pc.getCurrentWorkspace(), "Fieldwork 2026");
        int before = workspaceCount();

        service.generateGraph("preferential_attachment", 50, 1, null, 1L);

        assertEquals(before + 1, workspaceCount(), "the named empty workspace was deleted");
    }

    /** An empty workspace with Gephi's default name is still tidied away, as before. */
    @Test
    void anEmptyDefaultWorkspaceIsStillRemoved() {
        int before = workspaceCount();

        service.generateGraph("preferential_attachment", 50, 1, null, 1L);

        assertEquals(before, workspaceCount(), "the empty default workspace was left behind");
    }

    @Test
    void preferentialAttachmentUsesTheEdgesPerNodeAsked() {
        JsonObject r = service.generateGraph("barabasi_albert", 100, 3, null, 5L);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(3 + (100 - 3 - 1) * 3, r.get("edge_count").getAsInt());
        assertEquals("preferential_attachment", r.get("model").getAsString());
        assertEquals(3, r.get("edges_per_node").getAsInt());
        assertTrue(r.get("workspace").getAsString().startsWith("Preferential attachment (100 nodes, 3 per node"),
            r.toString());
    }

    /** A wiring probability of 1 links every pair, so the probability asked for is the one used. */
    @Test
    void randomGraphUsesTheProbabilityAsked() {
        JsonObject r = service.generateGraph("erdos_renyi", 30, null, 1.0, null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(30 * 29 / 2, r.get("edge_count").getAsInt());
        assertEquals("random", r.get("model").getAsString());
        assertEquals(1.0, r.get("wiring_probability").getAsDouble());
        assertTrue(r.get("note").getAsString().contains("no seed"), r.toString());
        assertTrue(r.get("workspace").getAsString().startsWith("Random graph (30 nodes"), r.toString());
    }

    @Test
    void preferentialAttachmentLimitsAreInclusive() {
        assertNull(GephiControlService.preferentialAttachmentRefusal(2, 1), "the smallest graph");
        assertNull(GephiControlService.preferentialAttachmentRefusal(GephiControlService.GENERATE_MAX_NODES, 1),
            "exactly the node cap");
        assertTrue(GephiControlService.preferentialAttachmentRefusal(1, 1).contains("nodes"));
        assertNull(GephiControlService.preferentialAttachmentRefusal(10, 9), "edges_per_node one below nodes");
        assertTrue(GephiControlService.preferentialAttachmentRefusal(10, 0).contains("edges_per_node"));
    }

    /** 40 + (50,040 - 41) x 40 is exactly 2,000,000 edges: allowed; one node more is not. */
    @Test
    void preferentialAttachmentEdgeCapIsExact() {
        assertNull(GephiControlService.preferentialAttachmentRefusal(50_040, 40));
        String over = GephiControlService.preferentialAttachmentRefusal(50_041, 40);

        assertTrue(over != null && over.contains("has 2000040 edges"), over);
    }

    @Test
    void randomGraphLimitsAreInclusive() {
        assertNull(GephiControlService.randomGraphRefusal(2, 1.0), "two nodes, every pair linked");
        assertNull(GephiControlService.randomGraphRefusal(GephiControlService.GENERATE_MAX_RANDOM_NODES, 1e-6),
            "exactly the node cap");
        assertTrue(GephiControlService.randomGraphRefusal(2, 0.0).contains("wiring_probability"));
    }

    /** 2,001 nodes have 2,001,000 pairs; this probability expects exactly 2,000,000 edges. */
    @Test
    void randomGraphExpectedEdgeCapIsExact() {
        double atCap = 0.9995002498750625;

        assertNull(GephiControlService.randomGraphRefusal(2001, atCap));
        String over = GephiControlService.randomGraphRefusal(2001, Math.nextUp(atCap));
        assertTrue(over != null && over.contains("expects about 2000000 edges"), over);
    }
}
