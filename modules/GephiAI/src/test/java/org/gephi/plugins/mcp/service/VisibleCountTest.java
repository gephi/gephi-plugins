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

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.junit.jupiter.api.Test;

/**
 * Colouring through Gephi's Appearance API changes only what a filter leaves visible, so the
 * counts in the reply are taken over the visible graph, not the whole one.
 */
class VisibleCountTest {

    @Test
    void onlyVisibleElementsAreCountedWhenAFilterIsOn() {
        GraphModel gm = GraphModel.Factory.newInstance();
        Column grp = gm.getNodeTable().addColumn("grp", String.class);
        Column kind = gm.getEdgeTable().addColumn("kind", String.class);
        Graph g = gm.getGraph();
        Node[] n = new Node[4];
        for (int i = 0; i < 4; i++) {
            n[i] = gm.factory().newNode("n" + i);
            n[i].setAttribute(grp, i < 3 ? "x" : null);
            g.addNode(n[i]);
        }
        Edge e01 = gm.factory().newEdge(n[0], n[1], 0, false);
        Edge e23 = gm.factory().newEdge(n[2], n[3], 0, false);
        e01.setAttribute(kind, "k");
        e23.setAttribute(kind, "k");
        g.addEdge(e01);
        g.addEdge(e23);

        // No filter: every node with a value, every edge.
        assertEquals(3, GephiControlService.countVisible(gm, false, v -> v != null, grp));
        assertEquals(2, GephiControlService.countVisible(gm, true, v -> v != null, kind));

        // A filter that keeps n0, n1 and their edge.
        GraphView view = gm.createView();
        Subgraph sub = gm.getGraph(view);
        sub.addNode(n[0]);
        sub.addNode(n[1]);
        sub.addEdge(e01);
        gm.setVisibleView(view);

        assertEquals(2, GephiControlService.countVisible(gm, false, v -> v != null, grp));
        assertEquals(1, GephiControlService.countVisible(gm, true, v -> v != null, kind));
    }
}
