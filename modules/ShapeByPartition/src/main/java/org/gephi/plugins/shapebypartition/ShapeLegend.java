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
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * The key to a shape mapping: which shape stands for which category.
 * <p>
 * Built once per refresh from the same assignment the nodes are drawn with, so
 * the key cannot disagree with the map. It is placed beside the graph, to the
 * right and level with its top, so it never covers a node, and it is sized
 * from the nodes themselves so it reads at the same scale as the marks it
 * explains.
 * <p>
 * Free of render targets, so layout can be tested without a display.
 *
 * @author Matt Artz
 */
final class ShapeLegend {

    /** Past this many rows the key stops being read; the rest are counted instead. */
    static final int MAX_ROWS = 16;

    /** How much wider than measured a label may render in a viewer's substitute font. */
    private static final double FALLBACK_WIDTH = 1.15;

    /** Row label for nodes with no value in the column; drawn on the figure, so translatable. */
    static final String NO_VALUE = NbBundle.getMessage(ShapeLegend.class, "ShapeLegend.noValue");

    /** Symbol fill when colour carries something other than the shapes' column. */
    static final Color NEUTRAL = new Color(0x6B, 0x72, 0x80);

    // Proportions, in units of the symbol size.
    private static final double TITLE_SIZE = 0.95;
    private static final double LABEL_SIZE = 0.8;
    private static final double ROW_PITCH = 1.55;
    private static final double LABEL_GAP = 0.6;
    private static final double TITLE_GAP = 0.75;
    private static final double GRAPH_GAP = 2.5;
    private static final double SECTION_GAP = 1.4;

    // Bounds on the symbol size, as a share of the graph's larger side, so the
    // key stays legible on a graph of tiny nodes and modest beside huge ones.
    private static final double MIN_SHARE = 0.018;
    private static final double MAX_SHARE = 0.05;

    private static final FontRenderContext MEASURE = new FontRenderContext(null, true, true);

    /** One row of the key. */
    static final class Entry {

        final String label;
        final NodeShape shape;
        final Color fill;

        Entry(String label, NodeShape shape, Color fill) {
            this.label = label;
            this.shape = shape;
            this.fill = fill;
        }
    }

    /** A positioned run of text. */
    static final class Text {

        final String text;
        final double x;
        final double baseline;
        final float size;
        final boolean bold;

        Text(String text, double x, double baseline, float size, boolean bold) {
            this.text = text;
            this.x = x;
            this.baseline = baseline;
            this.size = size;
            this.bold = bold;
        }
    }

    /** A positioned symbol. */
    static final class Symbol {

        final NodeShape shape;
        final Color fill;
        final double x;
        final double y;

        Symbol(NodeShape shape, Color fill, double x, double y) {
            this.shape = shape;
            this.fill = fill;
            this.x = x;
            this.y = y;
        }
    }

    final String title;
    final List<Entry> entries;
    final int hidden;
    final double symbol;
    final double left;
    final double top;
    /** The colour section drawn below the shapes, or null when there is none. */
    final ColourKey colourKey;

    private ShapeLegend(String title, List<Entry> entries, int hidden, double symbol,
                        double left, double top, ColourKey colourKey) {
        this.title = title;
        this.entries = Collections.unmodifiableList(entries);
        this.hidden = hidden;
        this.symbol = symbol;
        this.left = left;
        this.top = top;
        this.colourKey = colourKey;
    }

    /**
     * Builds the key for a mapping.
     *
     * @param title      what the shapes encode, normally the column's title
     * @param byCategory the shape for each category, in assignment order
     * @param values     each node's category, null where it has none
     * @param colors     each node's colour, in the same order
     * @param sizes      each node's drawn size, in the same order
     * @param bounds     the graph's extent as <code>{minX, minY, maxX, maxY}</code>
     * @return the key, or null when there is nothing to explain
     */
    static ShapeLegend build(String title, Map<Object, NodeShape> byCategory, List<Object> values,
                             List<Color> colors, List<Float> sizes, double[] bounds) {
        if (byCategory.isEmpty()) {
            return null;
        }
        Map<Object, Color> uniform = uniformColors(values, colors);
        boolean colourMeansCategory = uniform.keySet().containsAll(byCategory.keySet());

        List<Entry> entries = new ArrayList<>();
        int hidden = 0;
        for (Map.Entry<Object, NodeShape> category : byCategory.entrySet()) {
            if (entries.size() == MAX_ROWS) {
                hidden++;
                continue;
            }
            Color fill = colourMeansCategory ? uniform.get(category.getKey()) : NEUTRAL;
            entries.add(new Entry(String.valueOf(category.getKey()), category.getValue(), fill));
        }
        if (values.contains(null) && hidden == 0 && entries.size() < MAX_ROWS) {
            // Nodes with no value are drawn as circles too. Saying so is the
            // only way a reader can tell them from the first category.
            Color fill = colourMeansCategory && uniform.containsKey(null) ? uniform.get(null) : NEUTRAL;
            entries.add(new Entry(NO_VALUE, NodeShape.CIRCLE, fill));
        }

        double extent = Math.max(bounds[2] - bounds[0], bounds[3] - bounds[1]);
        double symbol = median(sizes);
        if (extent > 0) {
            symbol = Math.max(extent * MIN_SHARE, Math.min(extent * MAX_SHARE, symbol));
        }
        if (!(symbol > 0)) {
            symbol = 10;
        }
        return new ShapeLegend(title, entries, hidden, symbol, bounds[2] + GRAPH_GAP * symbol, bounds[1], null);
    }

    /**
     * Returns this legend with a colour section below the shapes, or itself
     * when the key is absent or was refused, so a mismatch never reaches the page.
     */
    ShapeLegend withColourKey(ColourKey key) {
        if (key == null || key.problem != null || key.rows.isEmpty()) {
            return this;
        }
        return new ShapeLegend(title, entries, hidden, symbol, left, top, key);
    }

    /**
     * Returns this legend under another title, or itself when the title is
     * blank, so a key can say "Role" where the column is called role_code.
     */
    ShapeLegend retitled(String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return this;
        }
        return new ShapeLegend(newTitle.trim(), entries, hidden, symbol, left, top, colourKey);
    }

    /**
     * Returns this legend with its colour section under another heading, or
     * itself when the heading is blank or there is no colour section.
     */
    ShapeLegend withColourTitle(String newTitle) {
        if (colourKey == null || newTitle == null || newTitle.trim().isEmpty()) {
            return this;
        }
        return new ShapeLegend(title, entries, hidden, symbol, left, top, colourKey.retitled(newTitle));
    }

    /** The title, row labels and overflow note, positioned. */
    List<Text> texts(Font font) {
        List<Text> out = new ArrayList<>();
        float titleSize = (float) (TITLE_SIZE * symbol);
        float labelSize = (float) (LABEL_SIZE * symbol);
        out.add(new Text(title, left, top + ascent(font, titleSize), titleSize, true));
        double labelX = left + symbol + LABEL_GAP * symbol;
        for (int i = 0; i < entries.size(); i++) {
            out.add(new Text(entries.get(i).label, labelX, baselineFor(rowCentre(font, i), font, labelSize),
                labelSize, false));
        }
        if (hidden > 0) {
            out.add(new Text(more(hidden), labelX,
                baselineFor(rowCentre(font, entries.size()), font, labelSize), labelSize, false));
        }
        if (colourKey != null) {
            out.add(new Text(colourKey.title, left, colourTop(font) + ascent(font, titleSize), titleSize, true));
            for (int i = 0; i < colourKey.rows.size(); i++) {
                out.add(new Text(colourKey.rows.get(i).label, labelX,
                    baselineFor(colourRowCentre(font, i), font, labelSize), labelSize, false));
            }
            if (colourKey.hidden > 0) {
                out.add(new Text(more(colourKey.hidden), labelX,
                    baselineFor(colourRowCentre(font, colourKey.rows.size()), font, labelSize), labelSize, false));
            }
        }
        return out;
    }

    /** One symbol per row, centred in the symbol column. */
    List<Symbol> symbols(Font font) {
        List<Symbol> out = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            out.add(new Symbol(entry.shape, entry.fill, left + symbol / 2d, rowCentre(font, i)));
        }
        if (colourKey != null) {
            // Colour swatches are circles: the shape a node takes when shape says nothing.
            for (int i = 0; i < colourKey.rows.size(); i++) {
                out.add(new Symbol(NodeShape.CIRCLE, colourKey.rows.get(i).fill, left + symbol / 2d,
                    colourRowCentre(font, i)));
            }
        }
        return out;
    }

    /**
     * The room the key occupies, so exports make space for it.
     *
     * @return <code>{minX, minY, maxX, maxY}</code>
     */
    double[] bounds(Font font) {
        double right = left + symbol;
        double bottom = top;
        for (Text text : texts(font)) {
            // Room for a fallback font up to 15% wider: an SVG names its font, a viewer
            // without it substitutes one, and Gephi's SVG export adds no margin.
            right = Math.max(right, text.x + width(font, text) * FALLBACK_WIDTH);
            bottom = Math.max(bottom, text.baseline + descent(font, text.size));
        }
        int rows = entries.size() + (hidden > 0 ? 1 : 0);
        if (rows > 0) {
            bottom = Math.max(bottom, rowCentre(font, rows - 1) + symbol * 0.75);
        }
        if (colourKey != null) {
            int colourRows = colourKey.rows.size() + (colourKey.hidden > 0 ? 1 : 0);
            bottom = Math.max(bottom, colourRowCentre(font, colourRows - 1) + symbol * 0.75);
        }
        return new double[]{left, top, right, bottom};
    }

    /** Where the colour section starts: below the shapes' last row, after a gap. */
    private double colourTop(Font font) {
        int rows = entries.size() + (hidden > 0 ? 1 : 0);
        return rowCentreFrom(font, top, rows - 1) + symbol / 2d + SECTION_GAP * symbol;
    }

    private double colourRowCentre(Font font, int row) {
        return rowCentreFrom(font, colourTop(font), row);
    }

    private double rowCentre(Font font, int row) {
        return rowCentreFrom(font, top, row);
    }

    /** The centre of a row in a section whose title starts at {@code sectionTop}. */
    private double rowCentreFrom(Font font, double sectionTop, int row) {
        float titleSize = (float) (TITLE_SIZE * symbol);
        double titleBottom = sectionTop + ascent(font, titleSize) + descent(font, titleSize);
        return titleBottom + TITLE_GAP * symbol + symbol / 2d + row * ROW_PITCH * symbol;
    }

    /** Puts the middle of the capital letters on the row's centre line. */
    private static double baselineFor(double centre, Font font, float size) {
        double capHeight = font.deriveFont(size).createGlyphVector(MEASURE, "H")
            .getVisualBounds().getHeight();
        return centre + capHeight / 2d;
    }

    /** The overflow note, "+ 4 more", drawn on the figure and so translatable. */
    static String more(int hidden) {
        return NbBundle.getMessage(ShapeLegend.class, "ShapeLegend.more", hidden);
    }

    private static double ascent(Font font, float size) {
        return font.deriveFont(size).getLineMetrics("Hg", MEASURE).getAscent();
    }

    private static double descent(Font font, float size) {
        return font.deriveFont(size).getLineMetrics("Hg", MEASURE).getDescent();
    }

    private static double width(Font font, Text text) {
        Font sized = font.deriveFont(text.bold ? Font.BOLD : Font.PLAIN, text.size);
        return sized.getStringBounds(text.text, MEASURE).getWidth();
    }

    /** Each category's colour if every node in it shares one; absent otherwise. */
    private static Map<Object, Color> uniformColors(List<Object> values, List<Color> colors) {
        Map<Object, Color> seen = new HashMap<>();
        Map<Object, Boolean> mixed = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            Color color = colors.get(i);
            Color first = seen.putIfAbsent(value, color);
            if (first != null && !sameRgb(first, color)) {
                mixed.put(value, true);
            }
        }
        Map<Object, Color> out = new HashMap<>();
        for (Map.Entry<Object, Color> entry : seen.entrySet()) {
            if (!mixed.containsKey(entry.getKey()) && entry.getValue() != null) {
                out.put(entry.getKey(), opaque(entry.getValue()));
            }
        }
        // Two categories sharing a colour means colour is not telling them
        // apart, so it does not encode the category. Nodes with no value are
        // not a category and do not count.
        Map<Object, Color> categories = new HashMap<>(out);
        categories.remove(null);
        if (new HashSet<>(categories.values()).size() < categories.size()) {
            out.clear();
        }
        return out;
    }

    private static boolean sameRgb(Color a, Color b) {
        return a != null && b != null && (a.getRGB() & 0xFFFFFF) == (b.getRGB() & 0xFFFFFF);
    }

    private static Color opaque(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    private static double median(List<Float> sizes) {
        if (sizes.isEmpty()) {
            return 0;
        }
        float[] sorted = new float[sizes.size()];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = sizes.get(i);
        }
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }
}
