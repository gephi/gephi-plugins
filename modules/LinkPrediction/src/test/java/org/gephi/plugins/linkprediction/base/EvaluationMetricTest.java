package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.evaluation.LinkPredictionAccuracy;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatistics;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatisticsBuilder;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatistics;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EvaluationMetricTest {
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
    public void testCalculate_Successfully() {

        // Create objects used for metric
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

        // Calculate metric
        EvaluationMetric metric = (EvaluationMetric) new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);

        double result = metric.calculate(1, trained, validation, cn);

        assertEquals(100, result, 0.001);

        // Add incorrect prediction to validation and trained graph
        Edge e14validation = validationModel.factory().newEdge("E14", a, f, 1, 1, false);
        validation.addEdge(e14validation);
        Edge e14train = initModel.factory().newEdge("E14", a, h, 1, 1, false);
        e14train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        trained.addEdge(e14train);

        result = metric.calculate(2, trained, validation, cn);

        assertEquals(50, result, 0.001);

        // Add edge from other algorithm
        Edge e15train = initModel.factory().newEdge("E15", a, h, 1, 1, false);
        e15train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        trained.addEdge(e15train);

        result = metric.calculate(2, trained, validation, cn);

        assertEquals(50, result, 0.001);
    }

    @Test
    public void testEquals() {

        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();

        // Create common neighbours metric
        CommonNeighboursStatistics cn = new CommonNeighboursStatistics();
        EvaluationMetric metricCn1 = (EvaluationMetric) new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);

        // Create preferential attachment metric
        PreferentialAttachmentStatistics pa = new PreferentialAttachmentStatistics();
        EvaluationMetric metricPa = (EvaluationMetric) new LinkPredictionAccuracy(pa, init, validation, initialWs, validationWs);

        assertTrue(!metricCn1.equals(metricPa));

        // Create second common neighbours metric
        Graph newInit = initModel.getUndirectedGraph();

        // Add edge to validation and newInit graph
        Edge e13validation = validationModel.factory().newEdge("E13", a, c, 1, 1, false);
        validation.addEdge(e13validation);
        Edge e13train = initModel.factory().newEdge("E13", a, c, 1, 1, false);
        e13train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        newInit.addEdge(e13train);

        EvaluationMetric metricCn2 = (EvaluationMetric) new LinkPredictionAccuracy(cn, newInit, validation, initialWs, validationWs);

        assertEquals(metricCn1, metricCn2);

        // Check completely different object
        assertFalse(metricCn1.equals(new Object()));
    }

    @Test
    public void testHashCode() {

        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();

        // Create common neighbours metric
        CommonNeighboursStatistics cn = new CommonNeighboursStatistics();
        EvaluationMetric metric = (EvaluationMetric) new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        assertEquals(Objects.hash(cn), metric.hashCode());
    }

    @Test
    public void testPredictLinks() {

        // Create objects used for metric
        CommonNeighboursStatistics cn = new CommonNeighboursStatistics();
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();
        Graph trained = initModel.getUndirectedGraph();

        // Add edge to validation graph
        Edge e13validation = validationModel.factory().newEdge("E13", a, c, 1, 1, false);
        validation.addEdge(e13validation);

        // Get initial sizes
        int trainedSize = trained.getEdges().toArray().length;
        int validationSize = validation.getEdges().toArray().length;

        assertTrue(trainedSize < validationSize);

        // Predict new links
        EvaluationMetric metric = (EvaluationMetric) new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        HashSet<Edge> initEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        HashSet<Edge> validationEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));

        metric.setDiffEdgeCount(1);
        metric.setTrained(trained);
        metric.predictLinks(initEdges, trained.getModel(), validationEdges);
        trainedSize = trained.getEdges().toArray().length;

        assertEquals(validationSize, trainedSize);

    }

    @Test
    public void testDeterminCurrentGrapModel() {

        // Create objects used for metric
        CommonNeighboursStatistics cn = new CommonNeighboursStatistics();
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();
        Graph trained = initModel.getUndirectedGraph();

        // Add edge to validation graph
        Edge e13validation = validationModel.factory().newEdge("E13", a, c, 1, 1, false);
        validation.addEdge(e13validation);

        // Determine current model
        EvaluationMetric metric = (EvaluationMetric) new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        HashSet<Edge> initEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        HashSet<Edge> validationEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel current = metric.determineCurrentGraphModel(gc, initEdges, validationEdges);

        assertEquals(validation.getModel(), current);

        // Add edge to init graph
        Edge e14validation = validationModel.factory().newEdge("E14", a, c, 1, 1, false);
        init.addEdge(e14validation);
        Edge e15validation = validationModel.factory().newEdge("E15", a, c, 1, 1, false);
        init.addEdge(e15validation);

        // Update edges lists
        initEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));

        current = metric.determineCurrentGraphModel(gc, initEdges, validationEdges);

        assertEquals(init.getModel(), current);
    }

    @Test
    public void testCalculateCurrentResult_Successful() {

        // Create objects used for metric
        CommonNeighboursStatistics cn = new CommonNeighboursStatistics();
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();
        Graph trained = initModel.getUndirectedGraph();

        // Save initial size of init graph
        int initSize = init.getEdgeCount();

        // Add correct prediction to validation and trained graph
        Edge e13validation = validationModel.factory().newEdge("E13", a, c, 1, 1, false);
        validation.addEdge(e13validation);
        Edge e13train = initModel.factory().newEdge("E13", a, c, 1, 1, false);
        e13train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        trained.addEdge(e13train);

        // Prepare calculation of final result
        EvaluationMetric metric = (EvaluationMetric) new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        HashSet<Edge> initEdges = new HashSet<>(Arrays.asList(init.getEdges().toArray()));
        HashSet<Edge> validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        metric.setTrained(trained);

        // Get final result
        double finalResult = metric.calculateCurrentResult(initSize, validationEdges.size());

        assertEquals(100, finalResult, 0.001);
    }

    @Test
    public void testCalculateCurrentResult_Failure() {

        // Create objects used for metric
        CommonNeighboursStatistics cn = new CommonNeighboursStatistics();
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();
        Graph trained = initModel.getUndirectedGraph();

        // Save initial size of init graph
        int initSize = init.getEdgeCount();

        // Add prediction to trained graph
        // to simulate wrong worksheet selection
        Edge e13train = initModel.factory().newEdge("E13", a, c, 1, 1, false);
        e13train.setAttribute(LinkPredictionStatistics.getColLastPrediction(), CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        trained.addEdge(e13train);

        // Prepare calculation of final result
        EvaluationMetric metric = (EvaluationMetric) new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        HashSet<Edge> initEdges = new HashSet<>(Arrays.asList(init.getEdges().toArray()));
        HashSet<Edge> validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        metric.setTrained(trained);

        // Get final result
        double finalResult = metric.calculateCurrentResult(trained.getEdgeCount(), validationEdges.size());

        assertEquals(0, finalResult, 0.001);
    }
}
