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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.jupiter.api.Test;

/**
 * The degree and edge-weight filters, checked on a hand-counted graph: which elements go, that
 * the bounds are inclusive, that a max of 0 means no upper bound, and that the write lock is
 * released afterwards.
 */
class RangeFilterTest {

    /** A hub with four leaves, and two leaves joined to each other: degrees hub 4, a 2, b 2, c 1, d 1. */
    private static Graph starWithOneExtraEdge() {
        GraphModel gm = GraphModel.Factory.newInstance();
        Graph g = gm.getUndirectedGraph();
        GraphFactory f = gm.factory();
        Node hub = f.newNode("hub");
        g.addNode(hub);
        String[] leaves = {"a", "b", "c", "d"};
        double w = 1;
        for (String id : leaves) {
            Node n = f.newNode(id);
            g.addNode(n);
            Edge e = f.newEdge(hub, n, 0, w++, false);
            g.addEdge(e);
        }
        g.addEdge(f.newEdge(g.getNode("a"), g.getNode("b"), 0, 5.0, false));
        return g;
    }

    @Test
    void removesNodesBelowTheMinimumWithNoUpperBound() {
        Graph g = starWithOneExtraEdge();
        assertEquals(2, GephiControlService.removeNodesOutsideDegreeRange(g, 2, 0));
        assertNull(g.getNode("c"));
        assertNull(g.getNode("d"));
        assertNotNull(g.getNode("hub"));
        assertNotNull(g.getNode("a"));
        assertEquals(0, g.getLock().getWriteHoldCount(), "write lock left held");
    }

    @Test
    void degreeBoundsAreInclusive() {
        Graph g = starWithOneExtraEdge();
        assertEquals(1, GephiControlService.removeNodesOutsideDegreeRange(g, 1, 2), "only the hub (4) is outside");
        assertNull(g.getNode("hub"));
        assertEquals(4, g.getNodeCount());
    }

    @Test
    void removesEdgesOutsideTheWeightRange() {
        Graph g = starWithOneExtraEdge();
        // Weights 1, 2, 3, 4 on the spokes and 5 on a-b; keep [2, 4].
        assertEquals(2, GephiControlService.removeEdgesOutsideWeightRange(g, 2.0, 4.0));
        assertEquals(3, g.getEdgeCount());
        for (Edge e : g.getEdges().toArray()) {
            assertTrue(e.getWeight() >= 2.0 && e.getWeight() <= 4.0, "kept weight " + e.getWeight());
        }
        assertEquals(0, g.getLock().getWriteHoldCount(), "write lock left held");
    }

    @Test
    void theDryRunListMatchesWhatIsRemoved() {
        Graph g = starWithOneExtraEdge();
        int planned = GephiControlService.nodesOutsideDegreeRange(g, 2, 0).size();
        assertEquals(5, g.getNodeCount(), "a dry run changes nothing");
        assertEquals(planned, GephiControlService.removeNodesOutsideDegreeRange(g, 2, 0));
    }
}
