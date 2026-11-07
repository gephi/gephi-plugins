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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the shape geometry.
 *
 * @author Matt Artz
 */
public class NodeShapeTest {

    private static final double TOLERANCE = 1e-9;

    @Test
    public void theCircleIsLeftToTheDefaultRenderer() {
        assertNull(NodeShape.CIRCLE.outline(0, 0, 10));
    }

    @Test
    public void everyShapeHasTheCornersItsNameImplies() {
        assertEquals(4, NodeShape.SQUARE.outline(0, 0, 10).cornerCount());
        assertEquals(3, NodeShape.TRIANGLE.outline(0, 0, 10).cornerCount());
        assertEquals(4, NodeShape.DIAMOND.outline(0, 0, 10).cornerCount());
        assertEquals(10, NodeShape.STAR.outline(0, 0, 10).cornerCount());
        assertEquals(12, NodeShape.CROSS.outline(0, 0, 10).cornerCount());
        assertEquals(3, NodeShape.TRIANGLE_DOWN.outline(0, 0, 10).cornerCount());
        assertEquals(6, NodeShape.HEXAGON.outline(0, 0, 10).cornerCount());
    }

    @Test
    public void everyShapeCoversItsShareOfTheCirclesArea() {
        // Readers judge mark size by area, so this is what keeps node size
        // meaning the same thing whatever shape a node is drawn as.
        double diameter = 10;
        double circle = Math.PI * diameter * diameter / 4d;
        for (NodeShape shape : NodeShape.values()) {
            Outline outline = shape.outline(3, -8, diameter);
            if (outline == null) {
                continue;
            }
            double area = Math.abs(Outline.signedArea(outline.flatten()));
            assertEquals(shape.name(), shape.getAreaRatio(), area / circle, 1e-6);
        }
    }

    @Test
    public void opticalTrimsNeverStrayFarFromEqualArea() {
        // The trims make cornered shapes look as heavy as the circle, not
        // lighter. Past a sixth, size would stop meaning size.
        for (NodeShape shape : NodeShape.values()) {
            assertTrue(shape.name(), shape.getAreaRatio() <= 1d);
            assertTrue(shape.name(), shape.getAreaRatio() >= 5d / 6d);
        }
    }

    @Test
    public void everyShapeIsCentredOnItsCentreOfArea() {
        // Centring on the bounding box would make a triangle look as if it
        // sat above or below the node's position.
        for (NodeShape shape : NodeShape.values()) {
            Outline outline = shape.outline(17, -4, 10);
            if (outline == null) {
                continue;
            }
            double[] centre = Outline.centroid(outline.flatten());
            assertEquals(shape.name(), 17, centre[0], 1e-6);
            assertEquals(shape.name(), -4, centre[1], 1e-6);
        }
    }

    @Test
    public void shapesScaleAndTranslateWithTheNode() {
        double[] small = NodeShape.STAR.outline(0, 0, 10).flatten();
        double[] large = NodeShape.STAR.outline(100, 50, 20).flatten();

        for (int i = 0; i < small.length; i += 2) {
            assertEquals(small[i] * 2 + 100, large[i], TOLERANCE);
            assertEquals(small[i + 1] * 2 + 50, large[i + 1], TOLERANCE);
        }
    }

    @Test
    public void theTrianglesPointTheWayTheirNamesSay() {
        // Screen coordinates grow downwards, so "up" is the smallest y. The
        // apex is the extreme point that sits on the vertical axis.
        assertEquals(0, xAtExtremeY(NodeShape.TRIANGLE, false), 1e-6);
        assertEquals(0, xAtExtremeY(NodeShape.TRIANGLE_DOWN, true), 1e-6);
        assertTrue(Math.abs(xAtExtremeY(NodeShape.TRIANGLE, true)) > 1);
        assertTrue(Math.abs(xAtExtremeY(NodeShape.TRIANGLE_DOWN, false)) > 1);
    }

    @Test
    public void reachMeasuresEachShapeFromItsCentre() {
        assertEquals("the circle reaches its radius in every direction",
            5, NodeShape.CIRCLE.reach(0.6, 0.8, 10), 1e-12);
        // An upward triangle reaches further to its apex than to its base.
        assertTrue(NodeShape.TRIANGLE.reach(0, -1, 10) > NodeShape.TRIANGLE.reach(0, 1, 10));
        assertTrue(NodeShape.TRIANGLE_DOWN.reach(0, 1, 10) > NodeShape.TRIANGLE_DOWN.reach(0, -1, 10));
        for (NodeShape shape : NodeShape.values()) {
            for (int k = 0; k < 16; k++) {
                double angle = k * Math.PI / 8;
                double reach = shape.reach(Math.cos(angle), Math.sin(angle), 10);
                assertTrue(shape + " at " + k, reach > 1 && reach < 10);
            }
        }
    }

    @Test
    public void shapesCycleOnceThePaletteRunsOut() {
        int vocabulary = NodeShape.values().length;

        assertEquals(NodeShape.CIRCLE, NodeShape.forIndex(0));
        assertEquals(NodeShape.SQUARE, NodeShape.forIndex(1));
        assertEquals(NodeShape.CIRCLE, NodeShape.forIndex(vocabulary));
        assertEquals(NodeShape.SQUARE, NodeShape.forIndex(vocabulary + 1));
    }

    @Test
    public void theFirstFiveShapesAreTheOnesTheRequestAskedFor() {
        // gephi#1291 asks for circle, square, triangle, diamond, and star.
        // Anything added later must come after them, so an existing graph does
        // not change appearance when the vocabulary grows.
        assertEquals(NodeShape.CIRCLE, NodeShape.forIndex(0));
        assertEquals(NodeShape.SQUARE, NodeShape.forIndex(1));
        assertEquals(NodeShape.TRIANGLE, NodeShape.forIndex(2));
        assertEquals(NodeShape.DIAMOND, NodeShape.forIndex(3));
        assertEquals(NodeShape.STAR, NodeShape.forIndex(4));
    }

    @Test
    public void aNegativeIndexIsRejectedRatherThanWrappingToTheEnd() {
        try {
            NodeShape.forIndex(-1);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // The modulo of a negative number is negative in Java, so this
            // would otherwise throw ArrayIndexOutOfBounds from deep inside.
            assertTrue(expected.getMessage().contains("-1"));
        }
    }

    /** The x of the topmost (or bottommost) point of a shape centred on the origin. */
    private static double xAtExtremeY(NodeShape shape, boolean bottom) {
        double[] points = shape.outline(0, 0, 10).flatten();
        double bestX = 0;
        double bestY = bottom ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.length; i += 2) {
            double y = points[i + 1];
            if (bottom ? y > bestY + 1e-9 : y < bestY - 1e-9) {
                bestY = y;
                bestX = points[i];
            }
        }
        return bestX;
    }
}
