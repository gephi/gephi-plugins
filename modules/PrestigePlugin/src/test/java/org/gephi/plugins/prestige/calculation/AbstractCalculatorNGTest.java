/*
 * Copyright (C) 2016 Michael Henninger <gephi@michihenninger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.plugins.prestige.calculation;

import org.gephi.plugins.prestige.util.GraphGeneratorUtil;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.ProgressTicket;
import org.mockito.Mockito;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public abstract class AbstractCalculatorNGTest {

    private static final double EPS = 0.001D;
    ProgressTicket progress;

    @BeforeMethod
    public void init() {
        progress = Mockito.mock(ProgressTicket.class);
    }

    /**
     * @return Attribut column key for prestige measurement
     */
    protected abstract String getPrestigeColumnKey();

    /**
     * @return Attribut column key for NORMALIZED prestige measurement (if
     * exists), otherwise <code>null</code>
     */
    protected abstract String getNormalizedPrestigeColumnKey();

    protected void emptyGraph_AllZeroPrestige(CancableCalculation calc) {
        GraphModel g = GraphGeneratorUtil.generateUnconnectedDirectedGraph(0);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        if (getNormalizedPrestigeColumnKey() != null) {
            assertFalse(g.getNodeTable().hasColumn(getNormalizedPrestigeColumnKey()));
        }
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        assertEquals(0, map.size());
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        if (getNormalizedPrestigeColumnKey() != null) {
            assertTrue(g.getNodeTable().hasColumn(getNormalizedPrestigeColumnKey()));
        }
    }

    protected void unconnectedGraph_OneNode_ZeroPrestige(CancableCalculation calc) {
        GraphModel g = GraphGeneratorUtil.generateUnconnectedDirectedGraph(1);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        assertEquals(1, map.size());
        assertEquals(0D, map.keySet().iterator().next(), EPS);
        assertEquals(1, map.entrySet().iterator().next().getValue().intValue());
        Iterator<Node> it = g.getGraph().getNodes().iterator();
        // Has exactly one element
        if (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            assertEquals(0D, (Double) o, EPS);
        } else {
            fail("No nodes found");
        }
        assertFalse(it.hasNext());
    }

    protected void unconnectedGraph_FiveNodes_AllZeroPrestige(CancableCalculation calc) {
        GraphModel g = GraphGeneratorUtil.generateUnconnectedDirectedGraph(5);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        assertEquals(1, map.size());
        assertEquals(0D, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());
        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            assertEquals(0D, (Double) o, EPS);

            if (getNormalizedPrestigeColumnKey() != null) {
                Object on = node.getAttribute(getNormalizedPrestigeColumnKey());
                assertNotNull(on);
                assertEquals(0D, (Double) on, EPS);
            }
        }
    }

    protected void circleGraph_FiveNodes_AllSamePrestige(CancableCalculation calc, double expectedPrestige, double expectedNormalizedPrestige) {
        GraphModel g = GraphGeneratorUtil.generateCircleNetwork(5);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);

            if (getNormalizedPrestigeColumnKey() != null) {
                Object on = node.getAttribute(getNormalizedPrestigeColumnKey());
                assertNotNull(on);
                assertEquals((double) (expectedNormalizedPrestige), (Double) on, EPS);
            }
        }
    }

    protected void completeGraph_FiveNodes_AllSamePrestige(CancableCalculation calc, double expectedPrestige, double expectedNormalizedPrestige) {
        GraphModel g = GraphGeneratorUtil.generateCompleteDirectedGraph(5);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);

            if (getNormalizedPrestigeColumnKey() != null) {
                Object on = node.getAttribute(getNormalizedPrestigeColumnKey());
                assertNotNull(on);
                assertEquals(expectedNormalizedPrestige, (Double) on, EPS);
            }
        }
    }

    protected void completeGraph_FiveNodesAndParallelEdges_AllSamePrestige(CancableCalculation calc, double expectedPrestige, double expectedNormalizedPrestige) {
        GraphModel g = GraphGeneratorUtil.generateCompleteDirectedGraphWithParallelEdges(5, 3);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);

            if (getNormalizedPrestigeColumnKey() != null) {
                Object on = node.getAttribute(getNormalizedPrestigeColumnKey());
                assertNotNull(on);
                assertEquals(expectedNormalizedPrestige, (Double) on, EPS);
            }
        }
    }

    protected void completeGraph_FiveNodesAndSelfLoops_AllSamePrestige(CancableCalculation calc, double expectedPrestige, double expectedNormalizedPrestige) {
        GraphModel g = GraphGeneratorUtil.generateCompelteDirectedGraphWithSelfLoops(5);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);

            if (getNormalizedPrestigeColumnKey() != null) {
                Object on = node.getAttribute(getNormalizedPrestigeColumnKey());
                assertNotNull(on);
                assertEquals(expectedNormalizedPrestige, (Double) on, EPS);
            }
        }
    }

    protected void completeGraph_FiveNodes_MultipleExecutions_AllSamePrestige(CancableCalculation calc, double expectedPrestige, double expectedNormalizedPrestige) {
        GraphModel g = GraphGeneratorUtil.generateCompelteDirectedGraphWithSelfLoops(5);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        int columnCountAfterFirstExecution = g.getNodeTable().countColumns();
        checkCompleteGraphWithFiveNodes(map, expectedPrestige, expectedNormalizedPrestige, g);
        // Execute two additional times
        calc.calculate(g, progress);
        map = calc.calculate(g, progress);
        // Check that number of node attributes didn't increse
        assertEquals(columnCountAfterFirstExecution, g.getNodeTable().countColumns());
        checkCompleteGraphWithFiveNodes(map, expectedPrestige, expectedNormalizedPrestige, g);
    }

    private void checkCompleteGraphWithFiveNodes(SortedMap<Double, Integer> map, double expectedPrestige, double expectedNormalizedPrestige, GraphModel g) {
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);

            if (getNormalizedPrestigeColumnKey() != null) {
                Object on = node.getAttribute(getNormalizedPrestigeColumnKey());
                assertNotNull(on);
                assertEquals(expectedNormalizedPrestige, (Double) on, EPS);
            }
        }
    }

    protected void starGraph_FiveNodes_AllOuterZeroCenterOneLarger(CancableCalculation calc, double expectedCenterPrestige, double expectedNormalizedCenterPrestige) {
        GraphModel g = GraphGeneratorUtil.generateDirectedStarGraphWith0InCenter(6);
        assertFalse(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(getPrestigeColumnKey()));
        assertEquals(2, map.size());
        for (Map.Entry<Double, Integer> entry : map.entrySet()) {
            if (Math.abs(entry.getKey()) < EPS) {
                assertEquals(5, entry.getValue().intValue());
            } else if (Math.abs(entry.getKey() - expectedCenterPrestige) < EPS) {
                assertEquals(1, entry.getValue().intValue());
            } else {
                fail("Unexpected Key: " + entry.getKey());
            }
        }

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(getPrestigeColumnKey());
            assertNotNull(o);
            // Center node
            if (node.getId().equals("0")) {
                assertEquals(expectedCenterPrestige, (Double) o, EPS);
            } else {
                assertEquals(0D, (Double) o, EPS);
            }

            if (getNormalizedPrestigeColumnKey() != null) {
                Object on = node.getAttribute(getNormalizedPrestigeColumnKey());
                assertNotNull(on);
                // Center node
                if (node.getId().equals("0")) {
                    assertEquals(expectedNormalizedCenterPrestige, (Double) on, EPS);
                } else {
                    assertEquals(0D, (Double) on, EPS);
                }
            }

        }
    }
}
