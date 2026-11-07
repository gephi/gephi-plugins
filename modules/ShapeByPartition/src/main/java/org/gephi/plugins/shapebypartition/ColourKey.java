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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The colour half of the legend: which colour stands for which group of a
 * column the user names.
 * <p>
 * Gephi does not record which column the node colours came from, and several
 * columns can split the nodes identically (a detected community and the
 * grouping it recovers), so the plugin never guesses. The user names the
 * column, and the key is drawn only if that column fully explains the colours:
 * every group in one colour, and no colour shared by two groups. Otherwise
 * {@link #problem} says what failed and no key is drawn, because a key built on
 * a mismatch would be confidently wrong.
 * <p>
 * Free of Gephi types, so it can be tested without a display.
 *
 * @author Matt Artz
 */
final class ColourKey {

    /** Label of the row for nodes with no value in the column. */
    static final String NO_VALUE = ShapeLegend.NO_VALUE;

    /** One row of the key. */
    static final class Row {

        final String label;
        final Color fill;

        Row(String label, Color fill) {
            this.label = label;
            this.fill = fill;
        }
    }

    final String title;
    final List<Row> rows;
    final int hidden;
    /** Why no key is drawn, or null when it is. */
    final String problem;

    private ColourKey(String title, List<Row> rows, int hidden, String problem) {
        this.title = title;
        this.rows = Collections.unmodifiableList(rows);
        this.hidden = hidden;
        this.problem = problem;
    }

    /**
     * Reads the key for a column from the colours the nodes are drawn with.
     *
     * @param title  the column's title, used as the key's heading
     * @param values each node's value in the column, null where it has none
     * @param colors each node's colour, in the same order
     * @return the key, or one carrying a {@link #problem} and no rows
     */
    static ColourKey read(String title, List<Object> values, List<Color> colors) {
        Map<Object, Color> colourOf = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            Color color = colors.get(i);
            String name = value == null ? NO_VALUE : "'" + value + "'";
            if (color == null) {
                return refused(title, "a node in " + name + " has no colour");
            }
            Color opaque = new Color(color.getRGB() & 0xFFFFFF);
            Color first = colourOf.putIfAbsent(value, opaque);
            if (first != null && !first.equals(opaque)) {
                return refused(title, name + " has nodes of more than one colour, so colour does not encode "
                    + title + ". Colour the nodes by " + title + " in Appearance, then refresh.");
            }
        }

        List<Object> order = PartitionShapes.sortedCategories(values);
        if (order.isEmpty()) {
            return refused(title, title + " has no values");
        }
        if (colourOf.containsKey(null)) {
            order.add(null);
        }
        Map<Color, Object> owner = new HashMap<>();
        for (Object value : order) {
            Object other = owner.putIfAbsent(colourOf.get(value), value);
            if (other != null) {
                return refused(title, label(other) + " and " + label(value) + " share one colour, so the key"
                    + " could not tell them apart.");
            }
        }

        List<Row> rows = new ArrayList<>();
        int hidden = 0;
        for (Object value : order) {
            if (rows.size() == ShapeLegend.MAX_ROWS) {
                hidden++;
            } else {
                rows.add(new Row(value == null ? NO_VALUE : String.valueOf(value), colourOf.get(value)));
            }
        }
        return new ColourKey(title, rows, hidden, null);
    }

    /** This key under another heading, or itself when the heading is blank. */
    ColourKey retitled(String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return this;
        }
        return new ColourKey(newTitle.trim(), new ArrayList<>(rows), hidden, problem);
    }

    private static String label(Object value) {
        return value == null ? NO_VALUE : "'" + value + "'";
    }

    private static ColourKey refused(String title, String why) {
        return new ColourKey(title, new ArrayList<>(), 0, "Colour legend for '" + title + "' not drawn: " + why);
    }
}
