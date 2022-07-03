package org.gephi.plugins.linkprediction.base;

import java.util.Collection;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.filter.CommonNeighboursFilter;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatistics;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatistics;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class LinkPredictionFilterTest {
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
    public void removeOtherEdges_Single() {
        CommonNeighboursFilter cn = new CommonNeighboursFilter();
        CommonNeighboursStatistics cs = new CommonNeighboursStatistics();

        cs.execute(graphModel);
        // Get edges from graph
        List<Edge> edges = new ArrayList<>();
        // Add all edges to list
        List<Edge> graphEdges = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        graphEdges.forEach(e -> edges.add(e));

        List<Edge> remainigEdges = cn.removeOtherEdges(edges);

        assertEquals(edges.size(), remainigEdges.size());
    }

    @Test
    public void removeOtherEdges_Multi() {
        CommonNeighboursFilter cn = new CommonNeighboursFilter();
        CommonNeighboursStatistics cs = new CommonNeighboursStatistics();
        PreferentialAttachmentStatistics ps = new PreferentialAttachmentStatistics();

        cs.execute(graphModel);
        ps.execute(graphModel);
        // Get edges from graph
        List<Edge> edges = new ArrayList<>();
        // Add all edges to list
        List<Edge> graphEdges = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        graphEdges.forEach(e -> edges.add(e));

        List<Edge> remainigEdges = cn.removeOtherEdges(edges);

        assertTrue(remainigEdges.size() == 1);
    }

    @Test
    public void getNodesToRemove_Full() {
        List<Edge> edges = new ArrayList<>();
        CommonNeighboursFilter cn = new CommonNeighboursFilter();

        Collection<Node> nodesToRemove = cn.getNodesToRemove(graphModel.getGraph(), edges);

        int nodeCount = graphModel.getGraph().getNodeCount();
        assertEquals(nodeCount, nodesToRemove.size());
    }

    @Test
    public void getNodesToRemove_Empty() {
        List<Edge> edges = new ArrayList<>();
        // Add all edges to list
        List<Edge> graphEdges = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        graphEdges.forEach(e -> edges.add(e));

        CommonNeighboursFilter cn = new CommonNeighboursFilter();

        Collection<Node> nodesToRemove = cn.getNodesToRemove(graphModel.getGraph(), edges);

        int nodeCount = 0;
        assertEquals(nodeCount, nodesToRemove.size());
    }

    @Test
    public void retainEdges_Full() {
        List<Edge> edges = new ArrayList<>();
        // Add all edges to list
        List<Edge> graphEdges = Arrays.asList(graphModel.getGraph().getEdges().toArray());
        graphEdges.forEach(e -> edges.add(e));
        CommonNeighboursFilter cn = new CommonNeighboursFilter();
        int nodeCountOld = graphModel.getGraph().getNodeCount();

        cn.retainEdges(graphModel.getGraph(), edges);

        int nodeCountNew = graphModel.getGraph().getNodeCount();
        assertEquals(nodeCountOld, nodeCountNew);
    }
}
