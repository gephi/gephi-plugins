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
package org.gephi.plugins.shapebypartition;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests which column the renderer reads: the one named in Preview, or the
 * conventional one that lets the mapping travel with the data.
 *
 * @author Matt Artz
 */
public class ColumnSelectionTest {

    private static Table nodeTable() {
        return GraphModel.Factory.newInstance().getNodeTable();
    }

    @Test
    public void aNamedColumnIsUsed() {
        Table table = nodeTable();
        table.addColumn("team", String.class);

        Column chosen = ShapeByPartitionRenderer.selectColumn(table, "team");

        assertNotNull(chosen);
        assertEquals("team", chosen.getId());
    }

    @Test
    public void theConventionalColumnIsUsedWhenNoNameIsGiven() {
        // This is what lets a GEXF carry the mapping, or any tool that can write
        // a node attribute drive the plugin without touching Preview properties.
        Table table = nodeTable();
        table.addColumn(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class);

        for (String nothing : new String[]{"", "   ", null}) {
            Column chosen = ShapeByPartitionRenderer.selectColumn(table, nothing);
            assertNotNull("expected the convention for " + nothing, chosen);
            assertEquals(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, chosen.getId());
        }
    }

    @Test
    public void aNamedColumnWinsOverTheConvention() {
        Table table = nodeTable();
        table.addColumn(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class);
        table.addColumn("team", String.class);

        assertEquals("team", ShapeByPartitionRenderer.selectColumn(table, "team").getId());
    }

    @Test
    public void aNameThatDoesNotResolveIsNotQuietlyReplacedByTheConvention() {
        // Someone who typed a column name wants that column. Shaping by a
        // different one instead would be worse than drawing circles, because
        // the map would look right and mean something else.
        Table table = nodeTable();
        table.addColumn(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class);

        assertNull(ShapeByPartitionRenderer.selectColumn(table, "nosuchcolumn"));
    }

    @Test
    public void nothingIsChosenWhenNeitherIsPresent() {
        Table table = nodeTable();
        table.addColumn("unrelated", String.class);

        assertNull(ShapeByPartitionRenderer.selectColumn(table, ""));
        assertNull(ShapeByPartitionRenderer.selectColumn(table, "unrelated_typo"));
    }

    @Test
    public void theConventionResolvesByTitleToo() {
        // An import can leave a column's id and title different; the convention
        // has to survive that the same way a named column does.
        Table table = nodeTable();
        table.addColumn("0", ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class, null);

        Column chosen = ShapeByPartitionRenderer.selectColumn(table, "");

        assertNotNull(chosen);
        assertEquals("0", chosen.getId());
    }
}
