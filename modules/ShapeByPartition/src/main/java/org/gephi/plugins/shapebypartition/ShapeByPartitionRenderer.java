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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.preview.api.CanvasSize;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.plugin.renderers.SVGUtils;
import org.gephi.preview.plugin.renderers.SizeUtils;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

/**
 * Draws each node as a shape chosen by the value of a categorical column, the
 * shape equivalent of colouring by partition.
 * <p>
 * The renderer extends the default node renderer and falls back to it for any
 * node it cannot shape, so colour, size, border and opacity behave exactly as
 * they do without the plugin. Only the outline changes.
 * <p>
 * Extending a built-in renderer is what registers this as a replacement rather
 * than an addition: {@code PreviewControllerImpl.getRegisteredRenderers()}
 * drops the built-in renderer whenever a plugin declares it as a superclass, so
 * this renderer is enabled on install and the two never draw the same node
 * twice. When another installed plugin also extends {@code NodeRenderer}, only
 * the one with the lowest service position is enabled to begin with, and the
 * other has to be chosen in Manage Renderers.
 *
 * @author Matt Artz
 */
@ServiceProvider(service = Renderer.class, position = 310)
public class ShapeByPartitionRenderer extends NodeRenderer {

    /**
     * Preview property holding the name of the column the partition is read
     * from.
     */
    public static final String COLUMN = "shapebypartition.column";

    /**
     * Preview property switching the shape mapping on and off without having to
     * change the managed renderer list.
     */
    public static final String ENABLED = "shapebypartition.enabled";

    /**
     * The node column read when no column has been named.
     * <p>
     * A Preview property can only be set by someone in front of the Preview
     * panel. Reading an agreed column as well lets the mapping travel with the
     * data instead: a GEXF that ships this column needs no configuration at
     * all, and any tool that can write a node attribute can drive the plugin
     * without knowing anything about Preview properties.
     */
    public static final String CONVENTIONAL_COLUMN = "shape_partition";

    private static final Logger LOG =
        Logger.getLogger(ShapeByPartitionRenderer.class.getName());

    /**
     * Beyond this many categories, shape stops discriminating for a reader even
     * though the vocabulary can still supply a distinct outline. Reported, not
     * enforced: the limit is perceptual, and where exactly it bites depends on
     * how large the nodes are drawn.
     */
    private static final int READABLE_CATEGORIES = 5;

    /**
     * Item data holding a node's shape. Kept on the item, in its own workspace's
     * preview model, rather than on this renderer: Gephi shares one renderer
     * across workspaces, so shapes held here were replaced whenever another
     * workspace refreshed.
     */
    static final String SHAPE = "shapebypartition.shape";

    /** What a notice is about. Each keeps its own memory, so one cannot reset another. */
    private enum Topic { SHAPE_COLUMN, READABILITY, COLOUR_KEY }

    /**
     * The last message reported on each topic, so that a condition holding across
     * many renders is logged once rather than on every refresh.
     */
    private final Map<Topic, String> lastNotice = new java.util.EnumMap<>(Topic.class);

    /**
     * The conventional column last announced, so the announcement is made when
     * it starts being used rather than on every refresh.
     */
    private String announcedSource = "";

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ShapeByPartitionRenderer.class, "ShapeByPartitionRenderer.name");
    }

    /**
     * Reads the column once for the whole model and resolves every node's
     * shape, so that rendering never touches the graph.
     * <p>
     * The item sources are the nodes the item builder already collected, so
     * this walks the preview model rather than re-reading the graph.
     * <p>
     * The result is kept until the next refresh, and deliberately not cleared
     * in {@code postProcess}. Gephi calls this method only when the preview is
     * refreshed, but calls {@code render} and {@code postProcess} on every
     * redraw, including each pan and zoom, so clearing after a render would
     * turn every shape back into a circle the moment the view moved.
     */
    @Override
    public void preProcess(PreviewModel previewModel) {
        Item[] items = previewModel.getItems(Item.NODE);
        for (Item item : items) {
            item.setData(SHAPE, null);
        }

        PreviewProperties properties = previewModel.getProperties();
        if (!properties.getBooleanValue(ENABLED)) {
            return;
        }
        String requested = columnName(properties);

        Node first = null;
        for (Item item : items) {
            if (item.getSource() instanceof Node) {
                first = (Node) item.getSource();
                break;
            }
        }
        if (first == null) {
            return;
        }

        Column resolved = selectColumn(first.getTable(), requested);
        if (resolved == null) {
            if (!requested.isEmpty()) {
                // Drawing circles and saying nothing looks identical to a column
                // of one value, so say which name failed rather than leaving the
                // user to guess.
                notice(Topic.SHAPE_COLUMN, "No node column named '" + requested
                    + "'. Nodes are drawn as circles. Check the column name"
                    + " in the Data Laboratory; the id and the title can differ.");
            }
            // With no name given and no conventional column present, the plugin
            // is simply not configured. That is not worth a warning.
            return;
        }
        announceSource(requested, resolved);
        lastNotice.remove(Topic.SHAPE_COLUMN);

        List<Node> nodes = new ArrayList<>(items.length);
        List<Item> nodeItems = new ArrayList<>(items.length);
        List<Object> values = new ArrayList<>(items.length);
        List<Color> colors = new ArrayList<>(items.length);
        List<Float> sizes = new ArrayList<>(items.length);
        double[] bounds = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (Item item : items) {
            Object source = item.getSource();
            if (!(source instanceof Node)) {
                continue;
            }
            Node node = (Node) source;
            nodes.add(node);
            nodeItems.add(item);
            values.add(node.getAttribute(resolved));
            colors.add(item.getData(NodeItem.COLOR));
            measure(item, properties, sizes, bounds);
        }

        Map<Object, NodeShape> byCategory = PartitionShapes.assign(values);
        reportReadability(resolved.getTitle(), byCategory.size());

        for (int i = 0; i < nodeItems.size(); i++) {
            nodeItems.get(i).setData(SHAPE, byCategory.get(values.get(i)));
        }
        EdgeEnds.fit(previewModel, this::shapeFor);

        // The key is built from this very assignment, so it cannot disagree
        // with the map. Its item exists only while the legend is switched on.
        Item[] legendItems = previewModel.getItems(LegendItemBuilder.TYPE);
        if (legendItems != null && legendItems.length > 0 && !sizes.isEmpty()) {
            ShapeLegend legend = ShapeLegend.build(resolved.getTitle(), byCategory, values, colors, sizes, bounds);
            if (legend != null) {
                legend = legend.withColourKey(colourKey(properties, first, resolved, nodes, colors));
            }
            legendItems[0].setData(LegendItemBuilder.LEGEND, legend);
        }
    }

    /**
     * The colour key for the column named in the legend settings, or null when
     * none is named, the column is the shapes' own (whose symbols already carry
     * its colours), or it does not exist. A key the column does not explain is
     * returned with its problem, which is reported here and keeps it off the page.
     */
    private ColourKey colourKey(PreviewProperties properties, Node first, Column shapeColumn,
                                List<Node> nodes, List<Color> colors) {
        String requested = properties.getStringValue(ShapeLegendRenderer.COLOUR_COLUMN);
        if (requested == null || requested.trim().isEmpty()) {
            lastNotice.remove(Topic.COLOUR_KEY);
            return null;
        }
        Column column = ColumnLookup.resolve(first.getTable(), requested);
        if (column == null) {
            notice(Topic.COLOUR_KEY, "No node column named '" + requested.trim() + "' for the colour legend."
                + " Check the column name in the Data Laboratory; the id and the title can differ.");
            return null;
        }
        if (column == shapeColumn) {
            notice(Topic.COLOUR_KEY, "The colour legend column '" + column.getTitle() + "' is also the shape"
                + " column, so its colours appear on the shape symbols and no separate colour section is drawn.");
            return null;
        }
        List<Object> values = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            values.add(node.getAttribute(column));
        }
        ColourKey key = ColourKey.read(column.getTitle(), values, colors);
        if (key.problem != null) {
            notice(Topic.COLOUR_KEY, key.problem);
        } else {
            lastNotice.remove(Topic.COLOUR_KEY);
        }
        return key;
    }

    /**
     * Adds a node's drawn size and extent to the legend's inputs. Skips an
     * item without position or size rather than failing the whole refresh.
     */
    private static void measure(Item item, PreviewProperties properties, List<Float> sizes,
                                double[] bounds) {
        Object x = item.getData(NodeItem.X);
        Object y = item.getData(NodeItem.Y);
        if (!(x instanceof Float) || !(y instanceof Float) || !(item.getData(NodeItem.SIZE) instanceof Float)) {
            return;
        }
        float size = SizeUtils.getNodeSize(item, properties);
        sizes.add(size);
        bounds[0] = Math.min(bounds[0], (Float) x - size / 2d);
        bounds[1] = Math.min(bounds[1], (Float) y - size / 2d);
        bounds[2] = Math.max(bounds[2], (Float) x + size / 2d);
        bounds[3] = Math.max(bounds[3], (Float) y + size / 2d);
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        NodeShape shape = shapeFor(item);
        if (shape == null || shape == NodeShape.CIRCLE) {
            // A circle is what the default renderer already draws, and it draws
            // it better: it handles the border overlap cases the shape path
            // does not need.
            super.render(item, target, properties);
            return;
        }

        if (target instanceof G2DTarget) {
            renderShapeG2D(item, (G2DTarget) target, properties, shape);
        } else if (target instanceof SVGTarget) {
            renderShapeSVG(item, (SVGTarget) target, properties, shape);
        } else if (target instanceof PDFTarget) {
            renderShapePDF(item, (PDFTarget) target, properties, shape);
        }
    }

    /**
     * Reports the room a shaped node needs.
     * <p>
     * The default answer is the node's circle. A triangle or a star of the
     * same area reaches past that circle, so on the edge of the graph its tip
     * would be cut off by the export's page. The shape's own extent is added.
     */
    @Override
    public CanvasSize getCanvasSize(Item item, PreviewProperties properties) {
        CanvasSize circle = super.getCanvasSize(item, properties);
        NodeShape shape = shapeFor(item);
        if (shape == null || shape == NodeShape.CIRCLE) {
            return circle;
        }
        Mark mark = Mark.of(item, properties, shape);
        double[] box = mark.outline.bounds();
        double half = mark.borderWidth / 2d;
        float minX = (float) Math.min(circle.getX(), box[0] - half);
        float minY = (float) Math.min(circle.getY(), box[1] - half);
        float maxX = (float) Math.max(circle.getMaxX(), box[2] + half);
        float maxY = (float) Math.max(circle.getMaxY(), box[3] + half);
        return new CanvasSize(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Chooses the column to read: the one named, or the conventional one when
     * no name was given.
     * <p>
     * A name that was given and does not resolve is not quietly replaced by the
     * convention. Someone who typed a column name wants that column, and
     * silently shaping by a different one would be worse than drawing circles.
     *
     * @param table     the node table to search
     * @param requested the column name as typed, possibly empty
     * @return the column to read, or null when neither is present
     */
    static Column selectColumn(Table table, String requested) {
        if (requested != null && !requested.trim().isEmpty()) {
            return ColumnLookup.resolve(table, requested);
        }
        return ColumnLookup.resolve(table, CONVENTIONAL_COLUMN);
    }

    /**
     * Says once, at the point it starts happening, that the conventional column
     * is being used. Shapes appearing with nothing typed into Preview would
     * otherwise look like the plugin acting on its own.
     *
     * @param requested the column name as typed, possibly empty
     * @param resolved  the column actually chosen
     */
    private void announceSource(String requested, Column resolved) {
        if (!requested.isEmpty()) {
            announcedSource = "";
            return;
        }
        String source = resolved.getId();
        if (!source.equals(announcedSource)) {
            announcedSource = source;
            LOG.info("No shape partition column was named, so the conventional column '"
                + CONVENTIONAL_COLUMN + "' is being used. Name a column in the Preview"
                + " settings to shape by something else.");
        }
    }

    /**
     * Reports how well the partition suits a shape encoding at all.
     * <p>
     * Two categories that share an outline are indistinguishable, which is
     * worse than not encoding them: the reader sees a distinction that is not
     * there. That case is silent unless it is said out loud, because the map
     * still looks correct.
     *
     * @param column     the column the partition was read from
     * @param categories how many distinct values it held
     */
    private void reportReadability(String column, int categories) {
        String message = readabilityNotice(column, categories);
        if (message == null) {
            // Nothing to report. Forget the last message so that the same
            // problem is reported again if it comes back.
            lastNotice.remove(Topic.READABILITY);
        } else {
            notice(Topic.READABILITY, message);
        }
    }

    /**
     * Builds the readability message for a partition, or returns null when the
     * partition is small enough to encode as shape without qualification.
     *
     * @param column     the column the partition was read from
     * @param categories how many distinct values it held
     * @return the message, or null when there is nothing to say
     */
    static String readabilityNotice(String column, int categories) {
        if (PartitionShapes.isAmbiguous(categories)) {
            return "Column '" + column + "' has " + categories + " categories but"
                + " the shape vocabulary holds " + NodeShape.values().length
                + ". Shapes repeat, so different categories share an outline."
                + " Group the smaller categories before encoding them as shape.";
        }
        if (categories > READABLE_CATEGORIES) {
            return "Column '" + column + "' has " + categories + " categories."
                + " Shape separates about " + READABLE_CATEGORIES
                + " reliably; beyond that readers stop telling the outlines apart"
                + " even though every category still has its own.";
        }
        return null;
    }

    /**
     * Logs a message once, rather than on every refresh, while the condition
     * that produced it holds.
     *
     * @param message what to report
     */
    private void notice(Topic topic, String message) {
        if (!message.equals(lastNotice.get(topic))) {
            lastNotice.put(topic, message);
            LOG.warning(message);
        }
    }

    /**
     * Returns the shape resolved for an item, or null when the item is not a
     * node or has no value in the partition column.
     *
     * @param item the item being rendered
     * @return the shape, or null to fall back to the default renderer
     */
    NodeShape shapeFor(Item item) {
        Object shape = item.getData(SHAPE);
        return shape instanceof NodeShape ? (NodeShape) shape : null;
    }

    private void renderShapeG2D(Item item, G2DTarget target, PreviewProperties properties,
                                NodeShape shape) {
        Mark mark = Mark.of(item, properties, shape);
        ShapePainter.g2d(target.getGraphics(), mark.outline, mark.fill, mark.border,
            mark.borderWidth, mark.alpha);
    }

    private void renderShapeSVG(Item item, SVGTarget target, PreviewProperties properties,
                                NodeShape shape) {
        Mark mark = Mark.of(item, properties, shape);
        Element element = ShapePainter.svg(target, mark.outline, mark.fill, mark.border,
            mark.borderWidth, mark.alpha);
        element.setAttribute("class", SVGUtils.idAsClassAttribute(((Node) item.getSource()).getId()));
        target.getTopElement(SVGTarget.TOP_NODES).appendChild(element);
    }

    private void renderShapePDF(Item item, PDFTarget target, PreviewProperties properties,
                                NodeShape shape) {
        Mark mark = Mark.of(item, properties, shape);
        ShapePainter.pdf(target.getContentStream(), mark.outline, mark.fill, mark.border,
            mark.borderWidth, mark.alpha);
    }

    /**
     * Everything a target needs to draw one shaped node, resolved the way the
     * default renderer resolves it so shaped and circular nodes match.
     */
    private static final class Mark {

        final Outline outline;
        final Color fill;
        final Color border;
        final float borderWidth;
        final float alpha;

        private Mark(Outline outline, Color fill, Color border, float borderWidth, float alpha) {
            this.outline = outline;
            this.fill = fill;
            this.border = border;
            this.borderWidth = borderWidth;
            this.alpha = alpha;
        }

        static Mark of(Item item, PreviewProperties properties, NodeShape shape) {
            float x = item.<Float>getData(NodeItem.X);
            float y = item.<Float>getData(NodeItem.Y);
            float size = SizeUtils.getNodeSize(item, properties);
            Color fill = item.getData(NodeItem.COLOR);
            Color border = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR))
                .getColor(fill);
            float borderWidth = Math.min(SizeUtils.getBorderWidth(properties, size), size / 2f);
            float alpha = properties.getBooleanValue(PreviewProperty.NODE_PER_NODE_OPACITY)
                ? fill.getAlpha() / 255f
                : properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f;
            // The default renderer draws its circle at this diameter with the
            // border centred on the outline. Matching it keeps a shaped node
            // and a circular one of the same size the same size on the page.
            return new Mark(shape.outline(x, y, size - borderWidth), fill, border, borderWidth, alpha);
        }
    }

    private static String columnName(PreviewProperties properties) {
        String value = properties.getStringValue(COLUMN);
        return value == null ? "" : value.trim();
    }

    @Override
    public PreviewProperty[] getProperties() {
        PreviewProperty[] inherited = super.getProperties();
        PreviewProperty[] all = new PreviewProperty[inherited.length + 2];
        System.arraycopy(inherited, 0, all, 0, inherited.length);

        all[inherited.length] = PreviewProperty.createProperty(this, ENABLED, Boolean.class,
            NbBundle.getMessage(ShapeByPartitionRenderer.class, "ShapeByPartitionRenderer.enabled.name"),
            NbBundle.getMessage(ShapeByPartitionRenderer.class, "ShapeByPartitionRenderer.enabled.description"),
            PreviewProperty.CATEGORY_NODES).setValue(true);

        all[inherited.length + 1] = PreviewProperty.createProperty(this, COLUMN, String.class,
            NbBundle.getMessage(ShapeByPartitionRenderer.class, "ShapeByPartitionRenderer.column.name"),
            NbBundle.getMessage(ShapeByPartitionRenderer.class, "ShapeByPartitionRenderer.column.description"),
            PreviewProperty.CATEGORY_NODES).setValue("");

        return all;
    }
}
