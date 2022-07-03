package org.gephi.plugins.linkprediction.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.project.api.ProjectController;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

import java.util.Arrays;
import java.util.List;

public class LinkPredictionMacroTest {
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
    public void testExecuteOnce_SingleSuccessfully() {

        List<Edge> edgesOld = Arrays.asList(graphModel.getGraph().getEdges().toArray());

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionMacro macro = new LinkPredictionMacro();
        macro.addStatistic(cn);

        macro.execute(graphModel);

        List<Edge> edgesNew = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        assertEquals(edgesOld.size() + 1, edgesNew.size());
        // Count number of added edges by common neighbour
        int cnCount = (int) edgesNew.stream()
                .filter(edge -> edge.getAttribute(LinkPredictionStatistics.getColLastPrediction())
                        .equals(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME)).count();
        assertEquals(1, cnCount);
    }

    @Test
    public void testExecuteTwice_SingleSuccessfully() {

        List<Edge> edgesOld = Arrays.asList(graphModel.getGraph().getEdges().toArray());

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionMacro macro = new LinkPredictionMacro();
        macro.addStatistic(cn);
        // Increase iteration limit
        macro.setIterationLimit(2);

        macro.execute(graphModel);

        List<Edge> edgesNew = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        assertEquals(edgesOld.size() + 2, edgesNew.size());
        // Count number of added edges by common neighbour
        int cnCount = (int) edgesNew.stream()
                .filter(edge -> edge.getAttribute(LinkPredictionStatistics.getColLastPrediction())
                        .equals(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME)).count();
        assertEquals(2, cnCount);
    }

    @Test
    public void testExecuteOnce_MultiSuccessfully() {

        List<Edge> edgesOld = Arrays.asList(graphModel.getGraph().getEdges().toArray());

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();
        LinkPredictionMacro macro = new LinkPredictionMacro();
        macro.addStatistic(cn);
        macro.addStatistic(pa);

        macro.execute(graphModel);

        List<Edge> edgesNew = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        assertEquals(edgesOld.size() + 2, edgesNew.size());
        // Count number of added edges by common neighbour
        int cnCount = (int) edgesNew.stream()
                .filter(edge -> edge.getAttribute(LinkPredictionStatistics.getColLastPrediction())
                        .equals(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME)).count();
        assertEquals(1, cnCount);
        // Count number of added edges by preferential attachment
        int panCount = (int) edgesNew.stream()
                .filter(edge -> edge.getAttribute(LinkPredictionStatistics.getColLastPrediction())
                        .equals(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME)).count();
        assertEquals(1, panCount);
    }

    @Test
    public void testExecuteTwice_MultiSuccessfully() {

        List<Edge> edgesOld = Arrays.asList(graphModel.getGraph().getEdges().toArray());

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();
        LinkPredictionMacro macro = new LinkPredictionMacro();
        macro.addStatistic(pa);
        macro.addStatistic(cn);
        // Increase iteration limit
        macro.setIterationLimit(2);

        macro.execute(graphModel);

        List<Edge> edgesNew = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        assertEquals(edgesOld.size() + 4, edgesNew.size());
        // Count number of added edges by common neighbour
        int cnCount = (int) edgesNew.stream()
                .filter(edge -> edge.getAttribute(LinkPredictionStatistics.getColLastPrediction())
                        .equals(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME)).count();
        assertEquals(2, cnCount);
        // Count number of added edges by preferential attachment
        int panCount = (int) edgesNew.stream()
                .filter(edge -> edge.getAttribute(LinkPredictionStatistics.getColLastPrediction())
                        .equals(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME)).count();
        assertEquals(2, panCount);
    }

    @Test
    public void addStatistic_Successfully() {
        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();
        LinkPredictionMacro macro = new LinkPredictionMacro();
        macro.addStatistic(cn);
        macro.addStatistic(pa);

        List<LinkPredictionStatistics> statistics = macro.getStatistics();
        assertEquals(2, statistics.size());
        assertTrue(statistics.contains(cn));
        assertTrue(statistics.contains(pa));
    }

    @Test
    public void removeStatistic_Successfully() {
        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();
        LinkPredictionMacro macro = new LinkPredictionMacro();
        macro.addStatistic(cn);
        macro.addStatistic(pa);

        List<LinkPredictionStatistics> statistics = macro.getStatistics();
        assertEquals(2, statistics.size());
        assertTrue(statistics.contains(cn));
        assertTrue(statistics.contains(pa));

        macro.removeStatistic(cn);
        assertEquals(1, statistics.size());
        assertTrue(statistics.contains(pa));

        macro.removeStatistic(pa);
        assertEquals(0, statistics.size());
    }
}
