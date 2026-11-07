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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Tests resolving a typed-in column name against a real table.
 *
 * @author Matt Artz
 */
public class ColumnLookupTest {

    private static Table nodeTable() {
        return GraphModel.Factory.newInstance().getNodeTable();
    }

    @Test
    public void findsAColumnByItsId() {
        Table table = nodeTable();
        table.addColumn("team", String.class);

        Column found = ColumnLookup.resolve(table, "team");

        assertNotNull(found);
        assertEquals("team", found.getId());
    }

    @Test
    public void findsAColumnByItsTitleWhenNoIdMatches() {
        // This is the GEXF import case: <attribute id="0" title="team"/> gives
        // a column the user sees as "team" and the platform indexes as "0".
        Table table = nodeTable();
        table.addColumn("0", "team", String.class, null);

        Column found = ColumnLookup.resolve(table, "team");

        assertNotNull("a title match must be found when no id matches", found);
        assertEquals("0", found.getId());
        assertEquals("team", found.getTitle());
    }

    @Test
    public void prefersTheIdMatchWhenBothExist() {
        Table table = nodeTable();
        table.addColumn("0", "team", String.class, null);
        table.addColumn("team", "Department", String.class, null);

        Column found = ColumnLookup.resolve(table, "team");

        assertEquals("an exact id match is unambiguous and must win",
            "team", found.getId());
    }

    @Test
    public void titleMatchingIgnoresCase() {
        Table table = nodeTable();
        table.addColumn("0", "Department", String.class, null);

        assertEquals("0", ColumnLookup.resolve(table, "department").getId());
        assertEquals("0", ColumnLookup.resolve(table, "DEPARTMENT").getId());
    }

    @Test
    public void surroundingSpaceIsIgnored() {
        Table table = nodeTable();
        table.addColumn("team", String.class);

        assertNotNull(ColumnLookup.resolve(table, "  team  "));
    }

    @Test
    public void anUnknownNameResolvesToNothingRatherThanThrowing() {
        Table table = nodeTable();
        table.addColumn("team", String.class);

        assertNull(ColumnLookup.resolve(table, "nosuchcolumn"));
        assertNull(ColumnLookup.resolve(table, ""));
        assertNull(ColumnLookup.resolve(table, "   "));
        assertNull(ColumnLookup.resolve(table, null));
        assertNull(ColumnLookup.resolve(null, "team"));
    }

    @Test
    public void aTitleMatchReleasesTheTableLock() throws Exception {
        // Iterating a Table locks it until the loop runs to the end. Returning from
        // inside the loop left it locked, and Gephi's UI froze the next time it read
        // the columns (a GEXF column, found by title, in the Preview panel).
        Table table = nodeTable();
        table.addColumn("0", "year", Integer.class, null);
        table.addColumn("1", "field", String.class, null);
        table.addColumn("2", "venue", String.class, null);

        assertEquals("1", ColumnLookup.resolve(table, "field").getId());

        ExecutorService other = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> read = other.submit(() -> {
                int n = 0;
                for (Column c : table) {
                    n++;
                }
                return n;
            });
            read.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("the table is still locked after a lookup by title");
        } finally {
            other.shutdownNow();
        }
    }
}
