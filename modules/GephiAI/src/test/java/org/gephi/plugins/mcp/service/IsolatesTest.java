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

import java.util.List;
import java.util.stream.Collectors;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.jupiter.api.Test;

/** A dry run counts exactly the nodes a real run would remove: those with no ties at all. */
class IsolatesTest {

    @Test
    void onlyNodesWithNoTiesAreIsolates() {
        GraphModel gm = GraphModel.Factory.newInstance();
        Graph g = gm.getGraph();
        Node a = gm.factory().newNode("a");
        Node b = gm.factory().newNode("b");
        Node alone = gm.factory().newNode("alone");
        Node loner = gm.factory().newNode("loner");
        g.addAllNodes(List.of(a, b, alone, loner));
        g.addEdge(gm.factory().newEdge(a, b, false));

        List<String> ids = GephiControlService.isolatedNodes(g).stream()
            .map(n -> n.getId().toString()).sorted().collect(Collectors.toList());

        assertEquals(List.of("alone", "loner"), ids);
        assertEquals(4, g.getNodeCount(), "finding isolates must not remove them");
    }
}
