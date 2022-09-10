package org.gephi.plugins.linkprediction.evaluation;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatistics;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatisticsBuilder;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;
import static org.junit.Assert.assertEquals;

public class LinkPredictionAccuracyTest {
    GraphModel initModel;
    GraphModel validationModel;

    Workspace initialWs;
    Workspace validationWs;

    Node a;
    Node c;
    Node f;
    Node h;

    @Before
    public void setUp() {
        //Init project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        //Get the default graph model
        initModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        //Create nodes
        GraphFactory factory = initModel.factory();
        a = factory.newNode("A");
        a.setLabel("Node A");
        Node b = factory.newNode("B");
        b.setLabel("Node B");
        c = factory.newNode("C");
        c.setLabel("Node C");
        Node d = factory.newNode("D");
        d.setLabel("Node D");
        Node e = factory.newNode("E");
        e.setLabel("Node E");
        f = factory.newNode("F");
        f.setLabel("Node F");
        Node g = factory.newNode("G");
        g.setLabel("Node G");
        h = factory.newNode("H");
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
        UndirectedGraph undirectedGraph = initModel.getUndirectedGraph();
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

        // Initialize columns
        Table edgeTable = initModel.getEdgeTable();
        CommonNeighboursStatistics.initializeColumns(edgeTable);

        // Create validation graph
        Project pr = pc.getCurrentProject();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        initialWs = pc.getCurrentWorkspace();
        validationWs = pc.newWorkspace(pr);

        // Copy nodes
        Graph initial = initModel.getGraph();
        validationModel = gc.getGraphModel(validationWs);
        validationModel.bridge().copyNodes(initial.getNodes().toArray());
    }

    @Test
    public void testExecute_Successfully() {

        LinkPredictionEvaluation evaluation = new LinkPredictionEvaluation();

        // Create objects used for accuracy
        CommonNeighboursStatistics cn = new CommonNeighboursStatistics();
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();
        Graph trained = initModel.getUndirectedGraph();

        // Add correct prediction to validation and trained graph
        Edge e13validation = validationModel.factory().newEdge("E13", a, c, 1, 1, false);
        validation.addEdge(e13validation);
        Edge e13train = initModel.factory().newEdge("E13", a, c, 1, 1, false);
        e13train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        trained.addEdge(e13train);

        // Calculate accuracy
        LinkPredictionAccuracy accuracy = new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);

        double result = accuracy.calculate(1, trained, validation, cn);

        assertEquals(100, result, 0.001);

        // Add incorrect prediction to validation and trained graph
        Edge e14validation = validationModel.factory().newEdge("E14", a, f, 1, 1, false);
        validation.addEdge(e14validation);
        Edge e14train = initModel.factory().newEdge("E14", a, h, 1, 1, false);
        e14train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        trained.addEdge(e14train);

        result = accuracy.calculate(2, trained, validation, cn);

        assertEquals(50, result, 0.001);

        // Add edge from other algorithm
        Edge e15train = initModel.factory().newEdge("E15", a, h, 1, 1, false);
        e15train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        trained.addEdge(e15train);

        result = accuracy.calculate(2, trained, validation, cn);

        assertEquals(50, result, 0.001);
    }

}
