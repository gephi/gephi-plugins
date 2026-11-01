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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.NodeItem;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests the shape legend: what it lists, how it colours its symbols, and where
 * it sits.
 *
 * @author Matt Artz
 */
public class ShapeLegendTest {

    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final double[] BOUNDS = {0, 0, 1000, 800};
    private static final Color RED = new Color(0xE15759);
    private static final Color BLUE = new Color(0x4E79A7);

    @Test
    public void rowsFollowTheShapeAssignment() {
        List<Object> values = Arrays.asList("a", "a", "a", "b", "b", "c");
        ShapeLegend legend = build(values, colours(values.size(), RED));

        assertEquals(3, legend.entries.size());
        assertEquals("a", legend.entries.get(0).label);
        assertEquals(NodeShape.CIRCLE, legend.entries.get(0).shape);
        assertEquals(NodeShape.SQUARE, legend.entries.get(1).shape);
        assertEquals(NodeShape.TRIANGLE, legend.entries.get(2).shape);
    }

    @Test
    public void symbolsAreNeutralWhenColourEncodesSomethingElse() {
        // Every node red: colour does not distinguish the categories, so
        // colouring the key would imply a meaning it does not have.
        List<Object> values = Arrays.asList("a", "b");
        ShapeLegend legend = build(values, colours(2, RED));

        for (ShapeLegend.Entry entry : legend.entries) {
            assertEquals(ShapeLegend.NEUTRAL, entry.fill);
        }
    }

    @Test
    public void symbolsTakeTheCategoryColourWhenColourEncodesTheSameColumn() {
        List<Object> values = Arrays.asList("a", "a", "b");
        ShapeLegend legend = build(values, Arrays.asList(RED, RED, BLUE));

        assertEquals(RED, legend.entries.get(0).fill);
        assertEquals(BLUE, legend.entries.get(1).fill);
    }

    @Test
    public void aCategoryOfMixedColoursKeepsTheKeyNeutral() {
        List<Object> values = Arrays.asList("a", "a", "b");
        ShapeLegend legend = build(values, Arrays.asList(RED, BLUE, BLUE));

        assertEquals(ShapeLegend.NEUTRAL, legend.entries.get(0).fill);
    }

    @Test
    public void nodesWithNoValueAreListedBecauseTheyAreCirclesToo() {
        List<Object> values = Arrays.asList("a", "a", "b", null);
        ShapeLegend legend = build(values, colours(4, RED));

        ShapeLegend.Entry last = legend.entries.get(legend.entries.size() - 1);
        assertEquals("no value", last.label);
        assertEquals(NodeShape.CIRCLE, last.shape);
    }

    @Test
    public void longPartitionsAreCutAndCounted() {
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < ShapeLegend.MAX_ROWS + 5; i++) {
            values.add("c" + i);
        }
        ShapeLegend legend = build(values, colours(values.size(), RED));

        assertEquals(ShapeLegend.MAX_ROWS, legend.entries.size());
        assertEquals(5, legend.hidden);
        List<ShapeLegend.Text> texts = legend.texts(FONT);
        assertEquals("+ 5 more", texts.get(texts.size() - 1).text);
    }

    @Test
    public void theKeySitsBesideTheGraphNeverOverIt() {
        ShapeLegend legend = build(Arrays.asList("a", "b"), colours(2, RED));
        double[] box = legend.bounds(FONT);

        assertTrue("right of the graph", box[0] > BOUNDS[2]);
        assertEquals("level with its top", BOUNDS[1], box[1], 1e-9);
        assertTrue(box[2] > box[0] && box[3] > box[1]);
        for (ShapeLegend.Text text : legend.texts(FONT)) {
            assertTrue(text.baseline <= box[3]);
        }
    }

    @Test
    public void theKeyLeavesRoomForAWiderFallbackFont() {
        // Gephi's SVG export adds no margin, and an SVG names its font: a viewer without
        // it substitutes one, and common fallbacks run up to about 15% wider. Bounds
        // that fit the measured text exactly clipped the longest label in that case.
        ShapeLegend legend = build(Arrays.asList("network science", "hci"), colours(2, RED));
        double[] box = legend.bounds(FONT);
        java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext(null, true, true);
        for (ShapeLegend.Text text : legend.texts(FONT)) {
            double width = FONT.deriveFont(text.bold ? Font.BOLD : Font.PLAIN, text.size)
                .getStringBounds(text.text, frc).getWidth();
            assertTrue("room for '" + text.text + "' 15% wider",
                box[2] >= text.x + width * 1.15 - 1e-9);
        }
    }

    @Test
    public void theSymbolSizeStaysLegibleAndModest() {
        List<Object> values = Arrays.asList("a", "b");
        ShapeLegend tiny = ShapeLegend.build("t", PartitionShapes.assign(values), values,
            colours(2, RED), Arrays.asList(0.1f, 0.1f), BOUNDS);
        ShapeLegend huge = ShapeLegend.build("t", PartitionShapes.assign(values), values,
            colours(2, RED), Arrays.asList(500f, 500f), BOUNDS);

        assertEquals(1000 * 0.018, tiny.symbol, 1e-9);
        assertEquals(1000 * 0.05, huge.symbol, 1e-9);
    }

    @Test
    public void aTitleCanBeGivenOrLeftToTheColumn() {
        ShapeLegend legend = build(Arrays.asList("a"), colours(1, RED));

        assertSame(legend, legend.retitled("  "));
        assertEquals("Role", legend.retitled(" Role ").title);
        assertEquals("Role", legend.retitled("Role").texts(FONT).get(0).text);
    }

    @Test
    public void textContrastsWithTheBackground() {
        PreviewProperties properties = new PreviewProperties();
        properties.putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
        Color onLight = ShapeLegendRenderer.textColor(properties);
        properties.putValue(PreviewProperty.BACKGROUND_COLOR, new Color(0x1E1E24));
        Color onDark = ShapeLegendRenderer.textColor(properties);

        assertTrue(brightness(onLight) < 0.3);
        assertTrue(brightness(onDark) > 0.7);
    }

    @Test
    public void svgNamesFontsTheWayBrowsersUnderstand() {
        assertEquals("sans-serif", ShapeLegendRenderer.cssFamily(new Font(Font.SANS_SERIF, Font.PLAIN, 1)));
        assertEquals("serif", ShapeLegendRenderer.cssFamily(new Font(Font.SERIF, Font.PLAIN, 1)));
    }

    @Test
    public void theNodeRendererFillsTheLegendItemFromItsOwnAssignment() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        graphModel.getNodeTable().addColumn(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class);
        String[] values = {"x", "x", "y"};
        Item[] nodes = new Item[values.length];
        for (int i = 0; i < values.length; i++) {
            Node node = graphModel.factory().newNode("n" + i);
            node.setAttribute(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, values[i]);
            graphModel.getGraph().addNode(node);
            NodeItem item = new NodeItem(node);
            item.setData(NodeItem.X, i * 100f);
            item.setData(NodeItem.Y, 0f);
            item.setData(NodeItem.SIZE, 10f);
            item.setData(NodeItem.COLOR, RED);
            nodes[i] = item;
        }
        Item legendItem = new LegendItemBuilder().getItems(null)[0];
        PreviewProperties properties = new PreviewProperties();
        properties.putValue(ShapeByPartitionRenderer.ENABLED, true);
        properties.putValue(ShapeByPartitionRenderer.COLUMN, "");
        properties.putValue(PreviewProperty.NODE_SCALE_FACTOR, 1f);
        PreviewModel model = (PreviewModel) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{PreviewModel.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getProperties":
                        return properties;
                    case "getItems":
                        if (Item.NODE.equals(args[0])) {
                            return nodes;
                        }
                        return LegendItemBuilder.TYPE.equals(args[0]) ? new Item[]{legendItem} : new Item[0];
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });

        new ShapeByPartitionRenderer().preProcess(model);

        ShapeLegend legend = legendItem.getData(LegendItemBuilder.LEGEND);
        assertNotNull(legend);
        assertEquals(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, legend.title);
        assertEquals("x", legend.entries.get(0).label);
        assertEquals(NodeShape.SQUARE, legend.entries.get(1).shape);
        assertTrue("beside the rightmost node", legend.left > 205);
    }

    @Test
    public void aColourKeySitsBelowTheShapesWithItsOwnSwatches() {
        List<Object> values = Arrays.asList("a", "a", "b");
        ShapeLegend shapesOnly = build(values, colours(3, RED));
        ColourKey key = ColourKey.read("project", Arrays.asList("p", "q", "q"), Arrays.asList(RED, BLUE, BLUE));
        ShapeLegend both = shapesOnly.withColourKey(key);

        List<ShapeLegend.Text> texts = both.texts(FONT);
        ShapeLegend.Text lastShapeLabel = texts.get(shapesOnly.texts(FONT).size() - 1);
        ShapeLegend.Text colourTitle = texts.get(shapesOnly.texts(FONT).size());
        assertEquals("project", colourTitle.text);
        assertTrue(colourTitle.bold);
        assertTrue("below the shape rows", colourTitle.baseline > lastShapeLabel.baseline);
        assertEquals("q", texts.get(texts.size() - 2).text);

        List<ShapeLegend.Symbol> symbols = both.symbols(FONT);
        assertEquals(shapesOnly.entries.size() + 2, symbols.size());
        ShapeLegend.Symbol firstSwatch = symbols.get(shapesOnly.entries.size());
        assertEquals(NodeShape.CIRCLE, firstSwatch.shape);
        assertEquals(BLUE, firstSwatch.fill);

        assertTrue("exports make room for the colour rows",
            both.bounds(FONT)[3] > shapesOnly.bounds(FONT)[3]);
    }

    @Test
    public void aRefusedColourKeyLeavesTheLegendAsItWas() {
        ShapeLegend legend = build(Arrays.asList("a", "b"), colours(2, RED));
        ColourKey refused = ColourKey.read("project", Arrays.asList("p", "p"), Arrays.asList(RED, BLUE));

        assertSame(legend, legend.withColourKey(refused));
        assertSame(legend, legend.withColourKey(null));
    }

    @Test
    public void aRetitledLegendKeepsItsColourKey() {
        ColourKey key = ColourKey.read("project", Arrays.asList("p", "q"), Arrays.asList(RED, BLUE));
        ShapeLegend legend = build(Arrays.asList("a", "b"), colours(2, RED)).withColourKey(key).retitled("Role");

        assertEquals("Role", legend.title);
        assertSame(key, legend.colourKey);
    }

    @Test
    public void theColourKeyCanBeGivenATitleOrLeftToTheColumn() {
        ColourKey key = ColourKey.read("project", Arrays.asList("p", "q"), Arrays.asList(RED, BLUE));
        ShapeLegend legend = build(Arrays.asList("a", "b"), colours(2, RED)).withColourKey(key);

        assertSame(legend, legend.withColourTitle("  "));
        assertSame(legend, legend.withColourTitle(null));
        ShapeLegend titled = legend.withColourTitle(" Project group ");
        assertEquals("Project group", titled.colourKey.title);
        assertEquals("the rows are unchanged", key.rows, titled.colourKey.rows);
        assertTrue(titled.texts(FONT).stream().anyMatch(t -> t.bold && t.text.equals("Project group")));
        assertNull("no key, nothing to title",
            build(Arrays.asList("a"), colours(1, RED)).withColourTitle("Project").colourKey);
    }

    @Test
    public void theNodeRendererAddsTheColourKeyForTheNamedColumn() {
        ShapeLegend legend = preProcessed(new String[]{"x", "x", "y"}, new String[]{"p", "q", "q"},
            new Color[]{RED, BLUE, BLUE}, "project");

        assertNotNull(legend.colourKey);
        assertEquals("project", legend.colourKey.title);
        assertEquals("q", legend.colourKey.rows.get(0).label);
        assertEquals(BLUE, legend.colourKey.rows.get(0).fill);
    }

    @Test
    public void theNodeRendererDrawsNoColourKeyWhenTheColumnDoesNotExplainTheColours() {
        ShapeLegend legend = preProcessed(new String[]{"x", "x", "y"}, new String[]{"p", "p", "q"},
            new Color[]{RED, BLUE, BLUE}, "project");

        assertNotNull("the shape legend still stands", legend);
        assertNull(legend.colourKey);
    }

    @Test
    public void theNodeRendererDrawsNoColourKeyForAnUnknownOrUnsetColumn() {
        assertNull(preProcessed(new String[]{"x", "y"}, new String[]{"p", "q"},
            new Color[]{RED, BLUE}, "nonexistent").colourKey);
        assertNull(preProcessed(new String[]{"x", "y"}, new String[]{"p", "q"},
            new Color[]{RED, BLUE}, "").colourKey);
    }

    @Test
    public void theNodeRendererDrawsNoSecondKeyForTheShapesOwnColumn() {
        ShapeLegend legend = preProcessed(new String[]{"x", "y"}, new String[]{"p", "q"},
            new Color[]{RED, BLUE}, ShapeByPartitionRenderer.CONVENTIONAL_COLUMN);

        assertNull(legend.colourKey);
        assertEquals("the shape symbols carry the colours already", RED, legend.entries.get(0).fill);
    }

    @Test
    public void aColourLegendProblemIsLoggedOnceNotOnEveryRefresh() {
        java.util.logging.Logger log = java.util.logging.Logger.getLogger(ShapeByPartitionRenderer.class.getName());
        List<String> logged = new ArrayList<>();
        java.util.logging.Handler capture = new java.util.logging.Handler() {
            @Override
            public void publish(java.util.logging.LogRecord record) {
                logged.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        log.addHandler(capture);
        try {
            ShapeByPartitionRenderer renderer = new ShapeByPartitionRenderer();
            String[] shapes = {"x", "x", "y"};
            String[] projects = {"p", "p", "q"};
            Color[] mixed = {RED, BLUE, BLUE};
            preProcessed(renderer, shapes, projects, mixed, "project");
            preProcessed(renderer, shapes, projects, mixed, "project");

            assertEquals(1, logged.stream().filter(m -> m.startsWith("Colour legend for 'project'")).count());
        } finally {
            log.removeHandler(capture);
        }
    }

    /** Runs the node renderer over nodes with a shape and a project value, returning the legend. */
    private ShapeLegend preProcessed(String[] shapeValues, String[] projects, Color[] colors, String colourColumn) {
        return preProcessed(new ShapeByPartitionRenderer(), shapeValues, projects, colors, colourColumn);
    }

    private ShapeLegend preProcessed(ShapeByPartitionRenderer renderer, String[] shapeValues, String[] projects,
                                     Color[] colors, String colourColumn) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        graphModel.getNodeTable().addColumn(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class);
        graphModel.getNodeTable().addColumn("project", String.class);
        Item[] nodes = new Item[shapeValues.length];
        for (int i = 0; i < shapeValues.length; i++) {
            Node node = graphModel.factory().newNode("n" + i);
            node.setAttribute(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, shapeValues[i]);
            node.setAttribute("project", projects[i]);
            graphModel.getGraph().addNode(node);
            NodeItem item = new NodeItem(node);
            item.setData(NodeItem.X, i * 100f);
            item.setData(NodeItem.Y, 0f);
            item.setData(NodeItem.SIZE, 10f);
            item.setData(NodeItem.COLOR, colors[i]);
            nodes[i] = item;
        }
        Item legendItem = new LegendItemBuilder().getItems(null)[0];
        PreviewProperties properties = new PreviewProperties();
        properties.putValue(ShapeByPartitionRenderer.ENABLED, true);
        properties.putValue(ShapeByPartitionRenderer.COLUMN, "");
        properties.putValue(ShapeLegendRenderer.COLOUR_COLUMN, colourColumn);
        properties.putValue(PreviewProperty.NODE_SCALE_FACTOR, 1f);
        PreviewModel model = (PreviewModel) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{PreviewModel.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getProperties":
                        return properties;
                    case "getItems":
                        if (Item.NODE.equals(args[0])) {
                            return nodes;
                        }
                        return LegendItemBuilder.TYPE.equals(args[0]) ? new Item[]{legendItem} : new Item[0];
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });
        renderer.preProcess(model);
        return legendItem.getData(LegendItemBuilder.LEGEND);
    }

    @Test
    public void thereIsNoLegendWithoutCategories() {
        assertNull(ShapeLegend.build("t", Collections.<Object, NodeShape>emptyMap(),
            Collections.emptyList(), Collections.<Color>emptyList(), Collections.<Float>emptyList(), BOUNDS));
    }

    private static ShapeLegend build(List<Object> values, List<Color> colors) {
        Map<Object, NodeShape> byCategory = PartitionShapes.assign(values);
        List<Float> sizes = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            sizes.add(20f);
        }
        return ShapeLegend.build("team", byCategory, values, colors, sizes, BOUNDS);
    }

    private static List<Color> colours(int n, Color color) {
        return new ArrayList<>(Collections.nCopies(n, color));
    }

    private static double brightness(Color color) {
        return (0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue()) / 255d;
    }
}
