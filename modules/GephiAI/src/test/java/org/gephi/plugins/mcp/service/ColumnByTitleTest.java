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
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.junit.jupiter.api.Test;

/**
 * Node listings name columns by title, so a value written back under that title must land in
 * the same column rather than a new one beside it.
 */
class ColumnByTitleTest {

    private static GraphModel withTitledColumn() {
        GraphModel gm = GraphModel.Factory.newInstance();
        gm.getNodeTable().addColumn("modularity_class", "Modularity Class", Integer.class,
            org.gephi.graph.api.Origin.DATA, null, true);
        Node n = gm.factory().newNode("a");
        gm.getDirectedGraph().addNode(n);
        return gm;
    }

    @Test
    void valueWrittenUnderAColumnsTitleLandsInThatColumn() {
        GraphModel gm = withTitledColumn();
        Table table = gm.getNodeTable();
        Node n = gm.getGraph().getNode("a");
        int before = table.countColumns();

        GephiControlService.ensureColumnAndSet(table, n, "Modularity Class", 4);

        assertEquals(before, table.countColumns(), "a duplicate column was created");
        assertEquals(4, n.getAttribute(table.getColumn("modularity_class")));
    }

    @Test
    void anUnknownNameStillMakesANewColumn() {
        GraphModel gm = withTitledColumn();
        Table table = gm.getNodeTable();
        Node n = gm.getGraph().getNode("a");
        assertNull(GephiControlService.findColumn(table, "Country"));

        GephiControlService.ensureColumnAndSet(table, n, "Country", "Peru");

        Column c = GephiControlService.findColumn(table, "Country");
        assertEquals("Peru", n.getAttribute(c));
    }

    @Test
    void valueCountsAcceptAColumnsTitle() {
        GraphModel gm = withTitledColumn();
        gm.getGraph().getNode("a").setAttribute("modularity_class", 2);

        var r = GephiControlService.columnValueFrequenciesCore(gm, "nodes", "Modularity Class");

        assertEquals(true, r.get("success").getAsBoolean(), r.toString());
        assertEquals(List.of("2"), List.copyOf(r.getAsJsonObject("frequencies").keySet()));
    }
}
