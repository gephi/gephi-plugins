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

package org.gephi.plugins.mcp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.jupiter.api.Test;

/** PDFs are US Letter, turned to landscape when the drawing is wider than it is tall. */
class PdfPageTest {

    private static Graph graphAt(float[][] positions) {
        GraphModel gm = GraphModel.Factory.newInstance();
        Graph g = gm.getGraph();
        for (int i = 0; i < positions.length; i++) {
            Node n = gm.factory().newNode("n" + i);
            n.setX(positions[i][0]);
            n.setY(positions[i][1]);
            g.addNode(n);
        }
        return g;
    }

    @Test
    void wideDrawingPrintsLandscape() {
        assertTrue(GephiControlService.landscapeFor(graphAt(new float[][]{{-300, 0}, {300, 50}})));
    }

    @Test
    void tallDrawingPrintsPortrait() {
        assertFalse(GephiControlService.landscapeFor(graphAt(new float[][]{{0, -300}, {50, 300}})));
    }

    @Test
    void squareOrSingleNodeDrawingPrintsPortrait() {
        assertFalse(GephiControlService.landscapeFor(graphAt(new float[][]{{-10, -10}, {10, 10}})));
        assertFalse(GephiControlService.landscapeFor(graphAt(new float[][]{{5, 5}})));
    }
}
