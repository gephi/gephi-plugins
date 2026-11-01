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

/**
 * The fixed shape vocabulary a partition is mapped onto.
 * <p>
 * Every shape is sized by area to match the circle the default node renderer
 * would have drawn, and is centred on its own centre of area. Readers judge
 * the size of a mark by its area, so matching area is what keeps node size
 * meaning the same thing across categories: a square and a circle of the same
 * node size look the same size, where a square inscribed in the circle would
 * look a third smaller and a star would shrink to a speck. Shapes with
 * corners are then trimmed slightly so they look as heavy as the circle
 * rather than heavier; see {@link #getAreaRatio()}.
 * <p>
 * Corners are softened. Sharp vertices are where small marks look jagged, and
 * where a thick border would otherwise draw a mitred spike past the node.
 * <p>
 * The order is the order categories receive shapes, so it puts the most
 * distinct outlines first. Circle, square, triangle, diamond, and star come
 * first because gephi/gephi#1291 asked for exactly those. The cross is next
 * because nothing else has its silhouette. The downward triangle and the
 * hexagon come last: one differs from an earlier shape only by orientation,
 * and the other is the closest to a circle.
 * <p>
 * This class is deliberately free of Gephi and Swing types. It computes
 * geometry only, so it can be unit tested without a graph or a display.
 *
 * @author Matt Artz
 */
public enum NodeShape {

    /**
     * A circle, drawn by the default node renderer.
     */
    CIRCLE(null, 1),
    /**
     * A square with softened corners.
     */
    SQUARE(Outline.rounded(regular(4, Math.PI / 4d), 0.28, 0), 0.94),
    /**
     * A triangle pointing up.
     */
    TRIANGLE(Outline.rounded(pointingUp(3), 0.30, 0), 0.84),
    /**
     * A square stood on its corner.
     */
    DIAMOND(Outline.rounded(pointingUp(4), 0.28, 0), 0.96),
    /**
     * A five-pointed star with full, rounded points.
     */
    STAR(Outline.rounded(star(), 0.36, 0.30), 0.92),
    /**
     * A plus sign with rounded arm ends.
     */
    CROSS(Outline.rounded(cross(), 0.55, 0.45), 0.94),
    /**
     * A triangle pointing down.
     */
    TRIANGLE_DOWN(Outline.rounded(pointingDown(3), 0.30, 0), 0.84),
    /**
     * A hexagon with a flat top.
     */
    HEXAGON(Outline.rounded(regular(6, 0d), 0.25, 0), 1);

    /**
     * Ratio of the star's inner radius to its outer radius. A geometric
     * pentagram uses 0.382, which leaves arms too thin to survive at the sizes
     * nodes are drawn; a fuller star keeps its silhouette when small.
     */
    private static final double STAR_INNER_RATIO = 0.5;

    /**
     * Half-width of a cross arm, as a fraction of the arm's reach. Wide enough
     * that the cross reads as a filled mark rather than as a thin glyph.
     */
    private static final double CROSS_ARM = 0.36;

    /**
     * Angle of a vertex at the top of the shape. Coordinates grow downwards,
     * as in Java2D and SVG, so straight up is a negative quarter turn.
     */
    private static final double UP = -Math.PI / 2d;

    /** The outline at diameter 1, centred on the origin. Null for the circle. */
    private final Outline unit;

    /** Share of the circle's area this shape covers; see {@link #getAreaRatio()}. */
    private final double areaRatio;

    /** The unit outline flattened once, for boundary measurements. */
    private final double[] unitPolygon;

    NodeShape(Outline raw, double areaRatio) {
        this.areaRatio = areaRatio;
        this.unit = raw == null ? null : raw.normalisedTo(Math.sqrt(areaRatio));
        this.unitPolygon = unit == null ? null : unit.flatten();
    }

    /**
     * Distance from the centre to the edge of this shape in a direction, for
     * a shape drawn at the given diameter. This is where an edge has to stop
     * and an arrowhead has to point for it to meet the outline rather than a
     * circle the shape does not have.
     *
     * @param dx       x of the direction, unit length, screen orientation
     * @param dy       y of the direction, unit length, screen orientation
     * @param diameter diameter of the circle whose area the shape matches
     * @return the distance from the centre to the outline
     */
    double reach(double dx, double dy, double diameter) {
        if (unitPolygon == null) {
            return diameter / 2d;
        }
        return Outline.reach(unitPolygon, dx, dy) * diameter;
    }

    /**
     * Share of the circle's area this shape covers.
     * <p>
     * Equal area is the starting point, then trimmed by eye: a shape with
     * corners reaches further from its centre than a circle of the same area,
     * and reads as larger for it. A triangle at full area looks visibly
     * heavier than the circle beside it, so it gives up the most. No shape is
     * trimmed by more than a sixth, so size still means size.
     *
     * @return the area of this shape over the area of the node's circle
     */
    double getAreaRatio() {
        return areaRatio;
    }

    /**
     * Returns the outline of this shape centred on a point.
     * <p>
     * Coordinates increase downwards, which is the convention of both
     * {@code Graphics2D} and SVG. Callers drawing into a space where y
     * increases upwards, such as PDF, negate the centre and mirror the result.
     *
     * @param centreX  centre of the node on the x axis
     * @param centreY  centre of the node on the y axis
     * @param diameter diameter of the circle whose area the shape matches
     * @return the outline, or null for {@link #CIRCLE}, which is left to the
     * default renderer
     */
    Outline outline(double centreX, double centreY, double diameter) {
        return unit == null ? null : unit.placed(centreX, centreY, diameter);
    }

    /**
     * Returns an outline for any shape, the circle included, for drawing a
     * symbol outside the node renderer, as the legend does. The circle is a
     * fully rounded sixteen-gon, indistinguishable from a true circle at the
     * sizes a key is drawn.
     *
     * @param centreX  centre on the x axis
     * @param centreY  centre on the y axis
     * @param diameter diameter of the circle whose area the shape matches
     * @return the outline, never null
     */
    Outline symbol(double centreX, double centreY, double diameter) {
        Outline outline = outline(centreX, centreY, diameter);
        return outline != null ? outline : RoundSymbol.UNIT.placed(centreX, centreY, diameter);
    }

    /** Holds the circle's outline apart from the constants that cannot reach it. */
    private static final class RoundSymbol {

        static final Outline UNIT = Outline.rounded(regular(16, 0d), 1, 0).normalisedTo(1d);
    }

    /**
     * Returns the shape assigned to the given category index, cycling once the
     * index runs past the end of the vocabulary.
     *
     * @param index zero-based position of the category
     * @return the shape for that position
     * @throws IllegalArgumentException if the index is negative
     */
    public static NodeShape forIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Category index cannot be negative: " + index);
        }
        NodeShape[] all = values();
        return all[index % all.length];
    }

    /**
     * Vertices of a regular polygon on the unit circle.
     *
     * @param sides      number of sides
     * @param startAngle angle of the first vertex, in screen orientation
     * @return interleaved coordinates
     */
    private static double[] regular(int sides, double startAngle) {
        double[] points = new double[sides * 2];
        for (int i = 0; i < sides; i++) {
            double angle = startAngle + i * 2d * Math.PI / sides;
            points[i * 2] = Math.cos(angle);
            points[i * 2 + 1] = Math.sin(angle);
        }
        return points;
    }

    private static double[] pointingUp(int sides) {
        return regular(sides, UP);
    }

    private static double[] pointingDown(int sides) {
        return regular(sides, -UP);
    }

    private static double[] star() {
        double[] points = new double[20];
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? 1d : STAR_INNER_RATIO;
            double angle = UP + i * Math.PI / 5d;
            points[i * 2] = r * Math.cos(angle);
            points[i * 2 + 1] = r * Math.sin(angle);
        }
        return points;
    }

    private static double[] cross() {
        double a = CROSS_ARM;
        return new double[]{
            -a, -1, a, -1, a, -a, 1, -a,
            1, a, a, a, a, 1, -a, 1,
            -a, a, -1, a, -1, -a, -a, -a
        };
    }
}
