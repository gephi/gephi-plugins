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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.force.yifanHu.YifanHu;
import org.gephi.layout.plugin.openord.OpenOrdLayoutBuilder;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutProperty;
import org.junit.jupiter.api.Test;

/**
 * Pins the premise behind the {@code findLayout()} reset.
 *
 * <p>A layout straight out of {@code buildLayout()} has its properties at Java zero-values;
 * Gephi's real defaults are installed by {@code resetPropertiesValues()}, which the Gephi UI
 * calls on selection. The MCP plugin never called it, so layouts ran on zeros: OpenOrd with
 * {@code Layout Size} 0 collapsed every node onto (0,0), and Yifan Hu with
 * {@code optimalDistance} 0 was a no-op that still reported success.
 *
 * <p>These tests construct the real Gephi layouts directly (no NetBeans platform) and assert
 * both halves: that the zeros are really there before the reset, and that the reset clears
 * them. If a future Gephi version starts self-initializing these layouts, the "before"
 * assertions fail loudly rather than the fix quietly becoming redundant.
 */
class LayoutDefaultsTest {

    private static double numericProperty(Layout layout, String displayName) throws Exception {
        for (LayoutProperty p : layout.getProperties()) {
            if (displayName.equals(p.getProperty().getDisplayName())) {
                Object v = p.getProperty().getValue();
                return v == null ? 0d : ((Number) v).doubleValue();
            }
        }
        throw new AssertionError("No such layout property: " + displayName);
    }

    /** OpenOrd: Layout Size 0 is what collapsed every node onto the origin. */
    @Test
    void openOrdStartsOnZerosAndResetFixesIt() throws Exception {
        Layout layout = new OpenOrdLayoutBuilder().buildLayout();
        assertNotNull(layout);
        layout.setGraphModel(GraphModel.Factory.newInstance());

        assertEquals(0d, numericProperty(layout, "Layout Size"), 0d,
                "expected an un-reset OpenOrd to report Layout Size 0");
        assertEquals(0d, numericProperty(layout, "Num Iterations"), 0d,
                "expected an un-reset OpenOrd to report Num Iterations 0");

        layout.resetPropertiesValues();

        assertTrue(numericProperty(layout, "Layout Size") > 0d,
                "reset must give OpenOrd a non-zero coordinate span, or the layout collapses");
        assertTrue(numericProperty(layout, "Num Iterations") > 0d,
                "reset must give OpenOrd a non-zero iteration count");
    }

    /** Yifan Hu: optimalDistance/stepRatio 0 made the algorithm a silent no-op. */
    @Test
    void yifanHuStartsOnZerosAndResetFixesIt() throws Exception {
        Layout layout = new YifanHu().buildLayout();
        assertNotNull(layout);
        layout.setGraphModel(GraphModel.Factory.newInstance());

        assertEquals(0d, numericProperty(layout, "Optimal Distance"), 0d,
                "expected an un-reset Yifan Hu to report Optimal Distance 0");
        assertEquals(0d, numericProperty(layout, "Step ratio"), 0d,
                "expected an un-reset Yifan Hu to report Step ratio 0");

        layout.resetPropertiesValues();

        assertTrue(numericProperty(layout, "Optimal Distance") > 0d,
                "reset must give Yifan Hu a non-zero optimal distance, or it does nothing");
        assertTrue(numericProperty(layout, "Step ratio") > 0d,
                "reset must give Yifan Hu a non-zero step ratio");
    }

    /**
     * ForceAtlas 2 was never affected — it is the control case, and the reason the bug went
     * unnoticed: the workhorse layout self-initializes, so only the others ran on zeros.
     */
    @Test
    void forceAtlas2SelfInitializesBeforeAnyReset() throws Exception {
        Layout layout = new ForceAtlas2Builder().buildLayout();
        assertNotNull(layout);
        layout.setGraphModel(GraphModel.Factory.newInstance());

        assertTrue(numericProperty(layout, "Scaling") > 0d,
                "ForceAtlas 2 is expected to arrive already initialized");
        assertTrue(numericProperty(layout, "Tolerance (speed)") > 0d,
                "ForceAtlas 2 is expected to arrive already initialized");
    }
}
