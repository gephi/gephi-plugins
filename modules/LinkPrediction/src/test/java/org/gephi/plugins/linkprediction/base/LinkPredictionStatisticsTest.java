package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatistics;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatisticsBuilder;
import org.gephi.plugins.linkprediction.statistics.LinkPredictionColumn;
import org.gephi.project.api.ProjectController;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkPredictionStatisticsTest {
    GraphModel graphModel;

    @Before
    public void setUp() {
        //Init project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        //Get the default graph model
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        //Create nodes
        GraphFactory factory = graphModel.factory();
        Node a = factory.newNode("A");
        a.setLabel("Node A");
        Node b = factory.newNode("B");
        b.setLabel("Node B");
        Node c = factory.newNode("C");
        c.setLabel("Node C");
        Node d = factory.newNode("D");
        d.setLabel("Node D");
        Node e = factory.newNode("E");
        e.setLabel("Node E");
        Node f = factory.newNode("F");
        f.setLabel("Node F");
        Node g = factory.newNode("G");
        g.setLabel("Node G");
        Node h = factory.newNode("H");
        h.setLabel("Node H");
        Node i = factory.newNode("I");
        i.setLabel("Node I");

        //Create edges
        Edge e1 = factory.newEdge("E1", a, b, 1, 1, false);
        Edge e2 = factory.newEdge("E2", a, d, 1, 1, false);
        Edge e3 = factory.newEdge("E3", a, e, 1, 1, false);
        Edge e4 = factory.newEdge("E4", b, d, 1, 1, false);
        Edge e5 = factory.newEdge("E5", b, c, 1, 1, false);
        Edge e6 = factory.newEdge("E6", c, d, 1, 1, false);
        Edge e7 = factory.newEdge("E7", c, f, 1, 1, false);
        Edge e8 = factory.newEdge("E8", e, f, 1, 1, false);
        Edge e9 = factory.newEdge("E9", b, a, 1, 1, false);
        Edge e10 = factory.newEdge("E10", f, g, 1, 1, false);
        Edge e11 = factory.newEdge("E11", g, h, 1, 1, false);
        Edge e12 = factory.newEdge("E12", g, i, 1, 1, false);

        // Add nodes
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        undirectedGraph.addNode(a);
        undirectedGraph.addNode(b);
        undirectedGraph.addNode(c);
        undirectedGraph.addNode(d);
        undirectedGraph.addNode(e);
        undirectedGraph.addNode(f);
        undirectedGraph.addNode(g);
        undirectedGraph.addNode(h);
        undirectedGraph.addNode(i);
        undirectedGraph.addEdge(e1);
        undirectedGraph.addEdge(e2);
        undirectedGraph.addEdge(e3);
        undirectedGraph.addEdge(e4);
        undirectedGraph.addEdge(e5);
        undirectedGraph.addEdge(e6);
        undirectedGraph.addEdge(e7);
        undirectedGraph.addEdge(e8);
        undirectedGraph.addEdge(e9);
        undirectedGraph.addEdge(e10);
        undirectedGraph.addEdge(e11);
        undirectedGraph.addEdge(e12);
    }

    @Test
    public void testInitializeColumns_Successfully() {
        LinkPredictionStatistics statistic = new CommonNeighboursStatistics();

        Table edgeTable = graphModel.getEdgeTable();
        statistic.initializeColumns(edgeTable);

        Column colLP = edgeTable.getColumn(LinkPredictionColumn.LP_ALGORITHM.getName());
        assertTrue(colLP != null);

        Column colAddinRun = edgeTable.getColumn(LinkPredictionColumn.ADDED_IN_RUN.getName());
        assertTrue(colAddinRun != null);

        Column colLastValue = edgeTable.getColumn(LinkPredictionColumn.LAST_VALUE.getName());
        assertTrue(colLastValue != null);
    }

    @Test
    public void testGetMaxIteration() {
        CommonNeighboursStatistics cs = new CommonNeighboursStatistics();

        Table edgeTable = graphModel.getEdgeTable();
        cs.initializeColumns(edgeTable);

        assertEquals(0, CommonNeighboursStatistics.getMaxIteration(graphModel.getGraph(),
                CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME));

        cs.execute(graphModel);

        assertEquals(1, CommonNeighboursStatistics.getMaxIteration(graphModel.getGraph(),
                CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME));
    }

    @Test
    public void testGetNextIteration() {
        CommonNeighboursStatistics cs = new CommonNeighboursStatistics();

        Table edgeTable = graphModel.getEdgeTable();
        cs.initializeColumns(edgeTable);

        assertEquals(1, cs.getNextIteration(graphModel.getGraph(),
                CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME));

        cs.execute(graphModel);

        assertEquals(2, cs.getNextIteration(graphModel.getGraph(),
                CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME));
    }

    @Test
    public void testLongRuntimeExpected() {
        CommonNeighboursStatistics cs = new CommonNeighboursStatistics();

        int iterationLimit = 1;
        long nodeCount = (long) Math.ceil(Math.sqrt(LinkPredictionStatistics.RUNTIME_THRESHOLD));
        assertFalse(CommonNeighboursStatistics.getComplexity().longRuntimeExpected(iterationLimit, nodeCount));

        iterationLimit = 2;
        nodeCount = LinkPredictionStatistics.RUNTIME_THRESHOLD;
        assertTrue(CommonNeighboursStatistics.getComplexity().longRuntimeExpected(iterationLimit, nodeCount));
    }

}
