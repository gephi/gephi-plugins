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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.jupiter.api.Test;

/** Finding nodes by the value in one column. */
class NodeSearchTest {

    private static GraphModel people() {
        GraphModel gm = GraphModel.Factory.newInstance();
        gm.getNodeTable().addColumn("country", String.class);
        gm.getNodeTable().addColumn("score", Double.class);
        Object[][] rows = {{"a", "Peru", 0.5}, {"b", "peru", 2.0}, {"c", "Portugal", 7.5}, {"d", null, 1.0}};
        for (Object[] row : rows) {
            Node n = gm.factory().newNode((String) row[0]);
            if (row[1] != null) {
                n.setAttribute("country", row[1]);
            }
            n.setAttribute("score", row[2]);
            gm.getDirectedGraph().addNode(n);
        }
        return gm;
    }

    private static List<String> find(GraphModel gm, String column, String value, String contains,
        Double min, Double max) {
        Column col = GephiControlService.findColumn(gm.getNodeTable(), column);
        Predicate<Node> keep = GephiControlService.nodeMatcher(col, value, contains, min, max);
        return gm.getGraph().getNodes().toCollection().stream().filter(keep)
            .map(n -> (String) n.getId()).sorted().collect(Collectors.toList());
    }

    @Test
    void wholeValueMatchesIgnoringCase() {
        assertEquals(List.of("a", "b"), find(people(), "country", "PERU", null, null, null));
    }

    @Test
    void partOfTheTextMatches() {
        assertEquals(List.of("a", "b", "c"), find(people(), "country", null, "p", null, null));
    }

    @Test
    void numericRangeIsInclusive() {
        assertEquals(List.of("b", "d"), find(people(), "score", null, null, 1.0, 2.0));
    }

    @Test
    void numberMatchesByValueNotByText() {
        assertEquals(List.of("b"), find(people(), "score", "2", null, null, null));
    }

    @Test
    void noSearchMeansNoFilter() {
        assertNull(GephiControlService.nodeMatcher(null, null, null, null, null));
    }
}
