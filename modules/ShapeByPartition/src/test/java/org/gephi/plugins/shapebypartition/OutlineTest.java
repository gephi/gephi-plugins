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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the rounded-polygon geometry shared by every render target.
 *
 * @author Matt Artz
 */
public class OutlineTest {

    private static final double[] SQUARE = {-1, -1, 1, -1, 1, 1, -1, 1};

    @Test
    public void noRoundingLeavesTheSharpPolygon() {
        Outline sharp = Outline.rounded(SQUARE, 0, 0);
        assertEquals(4, Math.abs(Outline.signedArea(sharp.flatten())), 1e-9);
    }

    @Test
    public void roundingOnlyEverRemovesArea() {
        double previous = 4;
        for (double rounding = 0.1; rounding <= 1.0; rounding += 0.1) {
            double area = Math.abs(Outline.signedArea(Outline.rounded(SQUARE, rounding, 0).flatten()));
            assertTrue("rounding " + rounding, area < previous);
            previous = area;
        }
    }

    @Test
    public void convexAndConcaveCornersAreRoundedSeparately() {
        // An L shape: five outward corners and one inward one. Rounding only
        // the inward corner adds a fillet, so the area grows.
        double[] l = {0, 0, 2, 0, 2, 1, 1, 1, 1, 2, 0, 2};
        double sharp = Math.abs(Outline.signedArea(Outline.rounded(l, 0, 0).flatten()));
        double filleted = Math.abs(Outline.signedArea(Outline.rounded(l, 0, 0.5).flatten()));
        assertEquals(3, sharp, 1e-9);
        assertTrue(filleted > sharp);
    }

    @Test
    public void normalisingCentresAndSizesTheOutline() {
        double[] offCentre = {10, 10, 14, 10, 14, 13, 10, 13};
        Outline unit = Outline.rounded(offCentre, 0.3, 0).normalisedTo(2);
        double[] flat = unit.flatten();
        double[] centre = Outline.centroid(flat);

        assertEquals(Math.PI, Math.abs(Outline.signedArea(flat)), 1e-9);
        assertEquals(0, centre[0], 1e-9);
        assertEquals(0, centre[1], 1e-9);
    }

    @Test
    public void reachIsTheDistanceToTheBoundaryAlongARay() {
        assertEquals(1, Outline.reach(SQUARE, 1, 0), 1e-12);
        assertEquals(1, Outline.reach(SQUARE, 0, -1), 1e-12);
        double diagonal = Math.sqrt(0.5);
        assertEquals(Math.sqrt(2), Outline.reach(SQUARE, diagonal, diagonal), 1e-12);
    }

    @Test
    public void tracingEmitsOneEdgeAndOneCurvePerCorner() {
        List<String> commands = new ArrayList<>();
        Outline.rounded(SQUARE, 0.5, 0).trace(new PathSink() {
            @Override
            public void moveTo(double x, double y) {
                commands.add("M");
            }

            @Override
            public void lineTo(double x, double y) {
                commands.add("L");
            }

            @Override
            public void quadTo(double cx, double cy, double x, double y) {
                commands.add("Q");
            }

            @Override
            public void close() {
                commands.add("Z");
            }
        });
        assertEquals("[M, L, Q, L, Q, L, Q, L, Q, Z]", commands.toString());
    }
}
