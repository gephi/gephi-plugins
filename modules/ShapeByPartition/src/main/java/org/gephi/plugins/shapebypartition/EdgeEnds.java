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

import java.util.function.Function;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.SizeUtils;

/**
 * Makes edges and arrowheads meet a shaped node's outline instead of the
 * circle the built-in renderers assume.
 * <p>
 * Gephi's edge renderer stores on each edge how far from each node centre the
 * line stops: {@code -(edge radius + node size / 2 [+ arrow length])}. The
 * {@code size / 2} term is the circle. This class swaps that term for the
 * shape's own distance from centre to outline along the edge, and records the
 * same distance for {@link ShapeAwareArrowRenderer}, which places arrowheads.
 * Gephi's own terms, the edge-radius setting and the arrow length, are left as
 * Gephi computed them.
 * <p>
 * It runs from the node renderer's {@code preProcess}, which Gephi calls after
 * the edge renderer's because the node renderer has the higher position.
 *
 * @author Matt Artz
 */
final class EdgeEnds {

    /** Edge data: distance from the target centre to its outline, set only for shaped targets. */
    static final String TARGET_REACH = "shapebypartition.target.reach";

    // Edge item data written by Gephi's EdgeRenderer.
    static final String TARGET_RADIUS = "edge.target.radius";
    static final String SOURCE_RADIUS = "edge.source.radius";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String SELF_LOOP = "self_loop";
    /** Set by Gephi's edge renderer; there is no public constant for it. */
    private static final String ARC_CURVENESS = "edge.arc-curveness";

    /** Fixed-point steps for curved edges; the reach settles within a few. */
    private static final int CURVE_ITERATIONS = 4;

    private EdgeEnds() {
    }

    /**
     * Refits every edge whose source or target has a shape other than the
     * circle.
     *
     * @param model   the preview model, with items built and edges preprocessed
     * @param shapeOf the shape resolved for a node item, or null
     */
    static void fit(PreviewModel model, Function<Item, NodeShape> shapeOf) {
        PreviewProperties properties = model.getProperties();
        for (Item edge : model.getItems(Item.EDGE)) {
            if (Boolean.TRUE.equals(edge.getData(SELF_LOOP))) {
                continue;
            }
            Object source = edge.getData(SOURCE);
            Object target = edge.getData(TARGET);
            if (!(source instanceof Item) || !(target instanceof Item)) {
                continue;
            }
            fitEnd(edge, (Item) target, (Item) source, shapeOf.apply((Item) target), properties, true);
            fitEnd(edge, (Item) source, (Item) target, shapeOf.apply((Item) source), properties, false);
        }
    }

    private static void fitEnd(Item edge, Item node, Item other, NodeShape shape,
                               PreviewProperties properties, boolean isTarget) {
        if (shape == null || shape == NodeShape.CIRCLE) {
            return;
        }
        String key = isTarget ? TARGET_RADIUS : SOURCE_RADIUS;
        Object stored = edge.getData(key);
        if (!(stored instanceof Float)) {
            return;
        }
        float size = SizeUtils.getNodeSize(node, properties);
        float border = Math.min(SizeUtils.getBorderWidth(properties, size), size / 2f);
        double nx = node.<Float>getData(NodeItem.X);
        double ny = node.<Float>getData(NodeItem.Y);
        double ox = other.<Float>getData(NodeItem.X);
        double oy = other.<Float>getData(NodeItem.Y);
        double length = Math.hypot(ox - nx, oy - ny);
        if (length == 0) {
            return;
        }
        double circle = size / 2d;
        // Everything Gephi added beyond the circle: edge radius, arrow length.
        double beyond = -(Float) stored - circle;

        double ux = (ox - nx) / length;
        double uy = (oy - ny) / length;
        double reach = boundary(shape, ux, uy, size, border);
        if (isTarget && properties.getBooleanValue(PreviewProperty.EDGE_CURVED)) {
            double curveness = properties.getDoubleValue(ARC_CURVENESS);
            for (int i = 0; i < CURVE_ITERATIONS; i++) {
                double[] towardsTip = curvedApproach(nx, ny, ox, oy, length, curveness, beyond + reach);
                if (towardsTip == null) {
                    break;
                }
                reach = boundary(shape, towardsTip[0], towardsTip[1], size, border);
            }
        }

        edge.setData(key, (float) -(beyond + reach));
        if (isTarget) {
            edge.setData(TARGET_REACH, (float) reach);
        }
    }

    /**
     * Distance from the centre to the drawn outline's outer edge: the shape is
     * drawn at {@code size - border} with the border centred on it, exactly as
     * the default renderer draws its circle, so half the border lies outside.
     */
    private static double boundary(NodeShape shape, double dx, double dy, float size, float border) {
        return shape.reach(dx, dy, size - border) + border / 2d;
    }

    /**
     * The direction from a target's centre to where a curved edge arrives,
     * reproducing the construction in Gephi's arrow renderer: the edge is an
     * arc of radius {@code length / curveness}, and the arrow points along the
     * line from the target to where that arc crosses a circle of radius
     * {@code radius} around it.
     *
     * @return a unit vector, or null where Gephi's construction has no answer
     */
    static double[] curvedApproach(double tx, double ty, double sx, double sy, double length,
                                   double curveness, double radius) {
        double arc = length / curveness;
        if (arc < radius / 2d || arc < length / 2d) {
            return null;
        }
        double angle = Math.atan2(ty - sy, tx - sx);
        double h = Math.sqrt(arc * arc - length * length / 4d);
        angle += Math.PI / 2d - Math.atan2(h, length / 2d);
        double k = Math.sqrt(arc * arc - radius * radius / 4d);
        angle -= Math.PI / 2d - Math.atan2(k, radius / 2d);
        // Gephi's vector points from source to target; the tip sits on the
        // other side of the target's centre.
        return new double[]{-Math.cos(angle), -Math.sin(angle)};
    }
}
