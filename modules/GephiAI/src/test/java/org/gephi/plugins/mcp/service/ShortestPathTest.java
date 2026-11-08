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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.stream.Collectors;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.jupiter.api.Test;

/** Shortest paths by steps, by edge length, and by tie strength. */
class ShortestPathTest {

    /** a-b-d is two steps; a-c-d is two steps too; a-d directly is one step but weight 10. */
    private static GraphModel diamond(boolean directed) {
        GraphModel gm = GraphModel.Factory.newInstance();
        Graph g = directed ? gm.getDirectedGraph() : gm.getUndirectedGraph();
        for (String id : new String[]{"a", "b", "c", "d"}) {
            g.addNode(gm.factory().newNode(id));
        }
        edge(gm, g, "a", "b", 1, directed);
        edge(gm, g, "b", "d", 1, directed);
        edge(gm, g, "a", "c", 3, directed);
        edge(gm, g, "c", "d", 3, directed);
        edge(gm, g, "a", "d", 10, directed);
        return gm;
    }

    private static void edge(GraphModel gm, Graph g, String s, String t, double w, boolean directed) {
        Edge e = gm.factory().newEdge(g.getNode(s), g.getNode(t), 0, w, directed);
        g.addEdge(e);
    }

    private static List<String> ids(GephiControlService.PathResult p) {
        return p.nodes.stream().map(n -> (String) n.getId()).collect(Collectors.toList());
    }

    private static GephiControlService.PathResult path(GraphModel gm, String from, String to,
        String weighting, boolean follow) {
        Graph g = gm.getGraph();
        return GephiControlService.shortestPath(g, g.getNode(from), g.getNode(to), weighting, follow);
    }

    @Test
    void countingStepsTakesTheDirectEdge() {
        GephiControlService.PathResult p = path(diamond(false), "a", "d", "none", true);

        assertEquals(List.of("a", "d"), ids(p));
        assertEquals(1, p.tiedPaths);
    }

    @Test
    void readingWeightAsLengthTakesTheShortRoute() {
        GephiControlService.PathResult p = path(diamond(false), "a", "d", "distance", true);

        assertEquals(List.of("a", "b", "d"), ids(p));
        assertEquals(2.0, p.length, 1e-9);
    }

    @Test
    void readingWeightAsStrengthTakesTheStrongTie() {
        GephiControlService.PathResult p = path(diamond(false), "a", "d", "strength", true);

        assertEquals(List.of("a", "d"), ids(p));
        assertEquals(0.1, p.length, 1e-9);
    }

    @Test
    void equallyShortPathsAreCounted() {
        GraphModel gm = diamond(false);
        Graph g = gm.getGraph();
        g.removeEdge(g.getEdge(g.getNode("a"), g.getNode("d")));

        GephiControlService.PathResult p = path(gm, "a", "d", "none", true);

        assertEquals(2, p.nodes.size() - 1);
        assertEquals(2, p.tiedPaths);
    }

    @Test
    void directedEdgesAreFollowedForwardsOnly() {
        GraphModel gm = diamond(true);

        assertNull(path(gm, "d", "a", "none", true));
        assertEquals(List.of("d", "a"), ids(path(gm, "d", "a", "none", false)));
    }
}
