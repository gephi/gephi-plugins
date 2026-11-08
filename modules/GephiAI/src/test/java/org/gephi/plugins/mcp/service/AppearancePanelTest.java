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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.junit.jupiter.api.Test;

/**
 * Gephi AI colours and sizes nodes itself, then sets Gephi's Appearance panel to the same
 * choice, so the panel shows the column and the colours or sizes used and Apply there
 * reproduces the result. These pin that the panel's settings match what was applied.
 */
class AppearancePanelTest {

    /** A partition over fixed values, recording the colour set for each. */
    static class FakePartition implements Partition {
        final List<Object> values;
        final Map<Object, Color> colors = new HashMap<>();

        FakePartition(Object... values) {
            this.values = List.of(values);
        }

        @Override public Collection getValues(Graph g) {
            return values;
        }

        @Override public Collection getSortedValues(Graph g) {
            return values;
        }

        @Override public int getElementCount(Graph g) {
            return values.size();
        }

        @Override public int count(Object v, Graph g) {
            return 1;
        }

        @Override public Object getValue(Element e, Graph g) {
            return null;
        }

        @Override public Color getColor(Object v) {
            return colors.get(v);
        }

        @Override public void setColor(Object v, Color c) {
            colors.put(v, c);
        }

        @Override public void setColors(Graph g, Color[] c) {
        }

        @Override public float percentage(Object v, Graph g) {
            return 0;
        }

        @Override public int size(Graph g) {
            return values.size();
        }

        @Override public Column getColumn() {
            return null;
        }
    }

    @Test
    void thePartitionTakesTheExactColoursAppliedMatchedByValueText() {
        FakePartition partition = new FakePartition(0, 1, 2);
        Map<String, Color> palette = new LinkedHashMap<>();
        palette.put("0", new Color(42, 120, 214));
        palette.put("1", new Color(27, 175, 122));

        int set = GephiControlService.applyPaletteToPartition(partition, null, palette);

        assertEquals(2, set);
        assertEquals(new Color(42, 120, 214), partition.getColor(0));
        assertEquals(new Color(27, 175, 122), partition.getColor(1));
        assertNull(partition.getColor(2), "a value Gephi AI left alone keeps whatever the panel had");
    }

    @Test
    void theRankingColourRunsFromTheMinimumToTheMaximumColour() {
        RankingElementColorTransformer t = new RankingElementColorTransformer();

        GephiControlService.configureRankingColor(t, new Color(240, 240, 240), new Color(20, 40, 160));

        assertArrayEquals(new Color[]{new Color(240, 240, 240), new Color(20, 40, 160)}, t.getColors());
        assertArrayEquals(new float[]{0f, 1f}, t.getColorPositions());
    }

    @Test
    void theRankingSizeRunsFromTheMinimumToTheMaximumSize() {
        RankingNodeSizeTransformer t = new RankingNodeSizeTransformer();

        GephiControlService.configureRankingSize(t, 3f, 25f);

        assertEquals(3f, t.getMinSize());
        assertEquals(25f, t.getMaxSize());
    }

    @Test
    void columnIsFoundByItsIdOrByTheTitleUsersSee() {
        org.gephi.graph.api.GraphModel gm = org.gephi.graph.api.GraphModel.Factory.newInstance();
        org.gephi.graph.api.Table nodes = gm.getNodeTable();
        nodes.addColumn("0", "group", String.class, org.gephi.graph.api.Origin.DATA, null, true);

        assertEquals("0", GephiControlService.findColumn(nodes, "0").getId());
        assertEquals("0", GephiControlService.findColumn(nodes, "group").getId());
        assertEquals("0", GephiControlService.findColumn(nodes, "Group").getId());
        assertNull(GephiControlService.findColumn(nodes, "department"));
        assertNull(GephiControlService.findColumn(nodes, null));
    }

    @Test
    void findingAColumnByTitleLeavesTheTableUsableByOtherThreads() throws Exception {
        org.gephi.graph.api.GraphModel gm = org.gephi.graph.api.GraphModel.Factory.newInstance();
        org.gephi.graph.api.Table nodes = gm.getNodeTable();
        nodes.addColumn("0", "group", String.class, org.gephi.graph.api.Origin.DATA, null, true);
        nodes.addColumn("1", "later", String.class, org.gephi.graph.api.Origin.DATA, null, true);

        GephiControlService.findColumn(nodes, "group");

        // Iterating a table locks it until the loop ends. Returning from inside such a loop left
        // the lock held, and the next thread to list the columns (Gephi's interface) waited forever.
        java.util.concurrent.ExecutorService other = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            java.util.concurrent.Future<Integer> count = other.submit(() -> {
                int n = 0;
                for (org.gephi.graph.api.Column c : nodes) {
                    n++;
                }
                return n;
            });
            assertEquals(nodes.countColumns(), count.get(5, java.util.concurrent.TimeUnit.SECONDS));
        } finally {
            other.shutdownNow();
        }
    }
}
