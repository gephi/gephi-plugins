/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.testng.annotations.Test;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class RankCalculatorNGTest {

    private static final String PROMINENCE_ATTRIBUTE = "prominence";
    private static final double EPS = 0.001D;
    ProgressTicket progress;

    @BeforeMethod
    public void init() {
        progress = Mockito.mock(ProgressTicket.class);
    }

    @Test(timeOut = 2000)
    public void emptyGraph_AllZeroPrestige() {
        GraphModel g = GraphGeneratorUtil.generateUnconnectedDirectedGraph(0);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        assertEquals(0, map.size());
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
    }

    @Test(timeOut = 2000)
    public void unconnectedGraph_OneNode_ZeroPrestige() {
        GraphModel g = GraphGeneratorUtil.generateUnconnectedDirectedGraph(1);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(0D, map.keySet().iterator().next(), EPS);
        assertEquals(1, map.entrySet().iterator().next().getValue().intValue());
        Iterator<Node> it = g.getGraph().getNodes().iterator();
        // Has exactly one element
        if (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(0D, (Double) o, EPS);
        } else {
            fail("No nodes found");
        }
        assertFalse(it.hasNext());
    }

    @Test(timeOut = 2000)
    public void unconnectedGraph_FiveNodes_AllZeroPrestige() {
        GraphModel g = GraphGeneratorUtil.generateUnconnectedDirectedGraph(10);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(0D, map.keySet().iterator().next(), EPS);
        assertEquals(10, map.entrySet().iterator().next().getValue().intValue());
        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(0D, (Double) o, EPS);
        }
    }

    @Test(timeOut = 2000)
    public void circleGraph_FiveNodes_AllSamePrestige() {
        GraphModel g = GraphGeneratorUtil.generateCircleNetwork(5);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        double expectedPrestige = 2D;
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);
        }
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodes_AllSamePrestige() {
        GraphModel g = GraphGeneratorUtil.generateCompleteDirectedGraph(5);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        double expectedPrestige = 4 * 2D;
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);
        }
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodesAndParallelEdges_AllSamePrestige() {
        GraphModel g = GraphGeneratorUtil.generateCompleteDirectedGraphWithParallelEdges(5, 3);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        double expectedPrestige = 4 * 2D;
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);
        }
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodesAndSelfLoops_AllSamePrestige() {
        GraphModel g = GraphGeneratorUtil.generateCompelteDirectedGraphWithSelfLoops(5);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        double expectedPrestige = 4 * 2D;
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);
        }
    }

    @Test(timeOut = 5000)
    public void completeGraph_FiveNodes_MultipleExecutions_AllSamePrestige() {
        GraphModel g = GraphGeneratorUtil.generateCompelteDirectedGraphWithSelfLoops(5);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 3);
        double expectedPrestige = 4 * 3D;
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        int columnCountAfterFirstExecution = g.getNodeTable().countColumns();
        checkCompleteGraphWithFiveNodes(map, expectedPrestige, g);
        // Execute two additional times
        calc.calculate(g, progress);
        map = calc.calculate(g, progress);
        // Check that number of node attributes didn't increse
        assertEquals(columnCountAfterFirstExecution, g.getNodeTable().countColumns());
        checkCompleteGraphWithFiveNodes(map, expectedPrestige, g);
    }

    private void checkCompleteGraphWithFiveNodes(SortedMap<Double, Integer> map, double expectedPrestige, GraphModel g) {
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);
        }
    }

    @Test(timeOut = 2000)
    public void starGraph_FiveNodes_AllOuterZeroCenterOneLarger() {
        GraphModel g = GraphGeneratorUtil.generateDirectedStarGraphWith0InCenter(6);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, 2);
        double expectedCenterPrestige = 5 * 2D;
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
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
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            // Center node
            if (node.getId().equals("0")) {
                assertEquals(expectedCenterPrestige, (Double) o, EPS);
            } else {
                assertEquals(0D, (Double) o, EPS);
            }

        }
    }

    @Test(timeOut = 2000)
    public void starGraph_FiveNodesAndLogScaling_AllOuterZeroCenterOneLarger() {
        GraphModel g = GraphGeneratorUtil.generateDirectedStarGraphWith0InCenter(6);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, true, 0D, g);
        addNodeProminences(g, 2);
        double expectedCenterPrestige = Math.log(5 * 2D);
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
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
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            // Center node
            if (node.getId().equals("0")) {
                assertEquals(expectedCenterPrestige, (Double) o, EPS);
            } else {
                assertEquals(0D, (Double) o, EPS);
            }

        }
    }

    @Test(timeOut = 2000)
    public void starGraph_FiveNodesAndDefaultValue_AllOuterZeroCenterOneLarger() {
        GraphModel g = GraphGeneratorUtil.generateDirectedStarGraphWith0InCenter(6);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, true, 5D, g);
        double expectedCenterPrestige = Math.log(5 * 5D);
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
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
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            // Center node
            if (node.getId().equals("0")) {
                assertEquals(expectedCenterPrestige, (Double) o, EPS);
            } else {
                assertEquals(0D, (Double) o, EPS);
            }

        }
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodesAndNegValues_AllSamePrestige() {
        GraphModel g = GraphGeneratorUtil.generateCompelteDirectedGraphWithSelfLoops(5);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, false, 0D, g);
        addNodeProminences(g, -2);
        double expectedPrestige = 4 * -2D;
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);
        }
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodesAndNegValuesLogTransformed_AllSamePrestige() {
        GraphModel g = GraphGeneratorUtil.generateCompelteDirectedGraphWithSelfLoops(5);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, true, 0D, g);
        addNodeProminences(g, -2);
        double expectedPrestige = 0D; // Log normalization of neg values will result 0 (because of invalid operation)
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        SortedMap<Double, Integer> map = calc.calculate(g, progress);
        // Only one with all 10 nodes
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertEquals(1, map.size());
        assertEquals(expectedPrestige, map.keySet().iterator().next(), EPS);
        assertEquals(5, map.entrySet().iterator().next().getValue().intValue());

        Iterator<Node> it = g.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.RANK_KEY);
            assertNotNull(o);
            assertEquals(expectedPrestige, (Double) o, EPS);
        }
    }

    @Test
    public void starGraph_FiveNodesAndLogTransformed_MaxNormalizedValueEqOne() {
        GraphModel g = GraphGeneratorUtil.generateDirectedStarGraphWith0InCenter(5);
        RankCalculator calc = new RankCalculator(PROMINENCE_ATTRIBUTE, true, 0D, g);
        addNodeProminences(g, 5);
        assertFalse(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        calc.calculate(g, progress);
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.RANK_KEY));
        assertTrue(g.getNodeTable().hasColumn(RankCalculator.NORMALIZED_RANK_KEY));
        Iterator<Node> it = g.getGraph().getNodes().iterator();
        double max = Double.MIN_VALUE;
        while (it.hasNext()) {
            Node node = it.next();
            Object o = node.getAttribute(RankCalculator.NORMALIZED_RANK_KEY);
            assertNotNull(o);
            double val = (Double) o;
            if (val > max) {
                max = val;
            }
        }
        assertEquals(1D, max, EPS);
    }

    /*
     * Helper Methods
     */
    private void addNodeProminences(GraphModel graph, double prominence) {
        graph.getNodeTable().addColumn(PROMINENCE_ATTRIBUTE, PROMINENCE_ATTRIBUTE, Double.class, 0.0D);
        Iterator<Node> it = graph.getGraph().getNodes().iterator();
        while (it.hasNext()) {
            Node n = it.next();
            n.setAttribute(PROMINENCE_ATTRIBUTE, prominence);
        }
    }

}
