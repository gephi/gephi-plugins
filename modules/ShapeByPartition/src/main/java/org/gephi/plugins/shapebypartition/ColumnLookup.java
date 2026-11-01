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
import org.gephi.graph.api.Table;

/**
 * Resolves the column a user named to the column the graph actually holds.
 * <p>
 * A Gephi column has both an id and a title, and they are often different. The
 * Data Laboratory shows the title, so the title is what a user reads and types,
 * but {@code Element.getAttribute(String)} resolves by id and throws when no id
 * matches. An import makes the gap visible: a GEXF attribute declared as
 * {@code <attribute id="0" title="team">} produces a column whose title is
 * "team" and whose id is "0", and asking for "team" then misses it.
 * <p>
 * Id takes precedence, because an exact id match is unambiguous and is what the
 * platform itself would have done. The title is only consulted when no id
 * matches.
 *
 * @author Matt Artz
 */
final class ColumnLookup {

    private ColumnLookup() {
    }

    /**
     * Finds the column a user meant by the name they typed.
     *
     * @param table the table to search, may be null
     * @param name  the column id or title, as typed
     * @return the matching column, or null when the table holds neither an id
     * nor a title by that name
     */
    static Column resolve(Table table, String name) {
        if (table == null || name == null) {
            return null;
        }
        String wanted = name.trim();
        if (wanted.isEmpty()) {
            return null;
        }
        if (table.hasColumn(wanted)) {
            return table.getColumn(wanted);
        }
        // A snapshot, not the table's iterator: the iterator holds the table lock until it
        // runs to the end, so returning from inside it would leave the table locked.
        for (Column column : table.toArray()) {
            if (wanted.equalsIgnoreCase(column.getTitle())) {
                return column;
            }
        }
        return null;
    }
}
