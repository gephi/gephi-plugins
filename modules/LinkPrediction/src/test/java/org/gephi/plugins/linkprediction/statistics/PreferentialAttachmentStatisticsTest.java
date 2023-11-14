package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.project.api.ProjectController;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PreferentialAttachmentStatisticsTest {
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
        Edge e9 = factory.newEdge("E9", e, g, 1, 1, false);
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
    public void testExecute_EdgeCount() {

        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();
        int edgesCountOriginal = graphModel.getGraph().getEdges().toArray().length;

        statistic.execute(graphModel);
        int edgesCountNew = graphModel.getGraph().getEdges().toArray().length;

        assertEquals(edgesCountOriginal + 1, edgesCountNew);
    }

    @Test
    public void testGetHighestPrediction_Successfully() {

        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();
        statistic.execute(graphModel);

        LinkPredictionStatistics.LinkPredictionProbability max = statistic.getHighestPrediction();
        Edge maxEdge = graphModel.getGraph().getEdge(max.getNodeSource(), max.getNodeTarget());


        assertTrue(maxEdge.getSource().getLabel().equals("Node A"));
        assertTrue(maxEdge.getTarget().getLabel().equals("Node G"));
        assertTrue(maxEdge.getAttribute(getColLastPrediction()).equals(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME));
        assertTrue((int) maxEdge.getAttribute(getColAddedInRun()) == 1);
        assertTrue((int) maxEdge.getAttribute(getColLastCalculatedValue()) == 12);
    }

    @Test
    public void testExecute_Successfully() {

        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();
        statistic.execute(graphModel);

        /*Edge max = statistic.getHighestPrediction();

        assertTrue(graphModel.getGraph().contains(max));*/

        LinkPredictionStatistics.LinkPredictionProbability max = statistic.getHighestPrediction();
        Edge maxEdge = graphModel.getGraph().getEdge(max.getNodeSource(), max.getNodeTarget());

        assertTrue(graphModel.getGraph().contains(maxEdge));
    }

    @Test
    public void testGetNextIteration_Successfully() {
        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();

        Table edgeTable = graphModel.getEdgeTable();
        statistic.initializeColumns(edgeTable);

        int firstNext = statistic.getNextIteration(graphModel.getGraph(), PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        assertEquals(1, firstNext);

        statistic.execute(graphModel);

        int secondNext = statistic.getNextIteration(graphModel.getGraph(), PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        assertEquals(2, secondNext);
    }

}
