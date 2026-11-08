package org.gephi.plugins.linkprediction.evaluation;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatistics;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatistics;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class LinkPredictionEvaluationTest {
    GraphModel initModel;
    GraphModel validationModel;

    Workspace initialWs;
    Workspace validationWs;

    Node a;
    Node b;
    Node c;

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
        b = factory.newNode("B");
        b.setLabel("Node B");
        c = factory.newNode("C");
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
    public void testGetEvaluations() {

        LinkPredictionEvaluation evaluation = new LinkPredictionEvaluation();

        // Create metrics
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();

        LinkPredictionAccuracy accuracyCn = new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        LinkPredictionAccuracy accuracyPa = new LinkPredictionAccuracy(pa, init, validation, initialWs, validationWs);

        // Add metrics
        evaluation.addEvaluation(accuracyCn);
        evaluation.addEvaluation(accuracyPa);

        // Get metrics
        List<EvaluationMetric> evaluations = evaluation.getEvaluations();

        assertTrue(evaluations.size() == 2);
        assertTrue(evaluations.contains(accuracyCn));
        assertTrue(evaluations.contains(accuracyPa));
    }

    @Test
    public void testGetEvaluation() {

        LinkPredictionEvaluation evaluation = new LinkPredictionEvaluation();

        // Create metrics
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();

        LinkPredictionAccuracy accuracyCn = new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        LinkPredictionAccuracy accuracyPa = new LinkPredictionAccuracy(pa, init, validation, initialWs, validationWs);

        // Add metrics
        evaluation.addEvaluation(accuracyCn);
        evaluation.addEvaluation(accuracyPa);

        // Get metrics
        EvaluationMetric metric = evaluation.getEvaluation(accuracyCn);

        assertTrue(metric == accuracyCn);
    }

    @Test
    public void testAddEvaluation() {

        LinkPredictionEvaluation evaluation = new LinkPredictionEvaluation();

        // Create metrics
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();

        LinkPredictionAccuracy accuracyCn = new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        LinkPredictionAccuracy accuracyPa = new LinkPredictionAccuracy(pa, init, validation, initialWs, validationWs);

        // Add metrics
        evaluation.addEvaluation(accuracyCn);
        evaluation.addEvaluation(accuracyPa);

        // Get metrics
        List<EvaluationMetric> evaluations = evaluation.getEvaluations();

        assertTrue(evaluations.size() == 2);
        assertTrue(evaluations.contains(accuracyCn));
        assertTrue(evaluations.contains(accuracyPa));

        // Add metrics twice
        evaluation.addEvaluation(accuracyCn);

        // Get metrics
        evaluations = evaluation.getEvaluations();

        assertTrue(evaluations.size() == 2);
        assertTrue(evaluations.contains(accuracyCn));
        assertTrue(evaluations.contains(accuracyPa));
    }

    @Test
    public void testRemoveEvaluation() {

        LinkPredictionEvaluation evaluation = new LinkPredictionEvaluation();

        // Create metrics
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();

        LinkPredictionAccuracy accuracyCn = new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        LinkPredictionAccuracy accuracyPa = new LinkPredictionAccuracy(pa, init, validation, initialWs, validationWs);

        // Add metrics
        evaluation.addEvaluation(accuracyCn);
        evaluation.addEvaluation(accuracyPa);

        // Get metrics
        List<EvaluationMetric> evaluations = evaluation.getEvaluations();

        assertTrue(evaluations.size() == 2);
        assertTrue(evaluations.contains(accuracyCn));
        assertTrue(evaluations.contains(accuracyPa));

        // Remove metrics
        evaluation.removeEvaluation(accuracyCn);

        // Get metrics
        evaluations = evaluation.getEvaluations();

        assertTrue(evaluations.size() == 1);
        assertTrue(!evaluations.contains(accuracyCn));
        assertTrue(evaluations.contains(accuracyPa));

        // Remove metrics twice
        evaluation.removeEvaluation(accuracyCn);

        // Get metrics
        evaluations = evaluation.getEvaluations();

        assertTrue(evaluations.size() == 1);
        assertTrue(!evaluations.contains(accuracyCn));
        assertTrue(evaluations.contains(accuracyPa));
    }

    @Test
    public void testGetReport() {

        LinkPredictionEvaluation evaluation = new LinkPredictionEvaluation();

        // Create metrics
        Graph init = initModel.getUndirectedGraph();
        Graph validation = validationModel.getUndirectedGraph();

        LinkPredictionStatistics cn = new CommonNeighboursStatistics();
        LinkPredictionStatistics pa = new PreferentialAttachmentStatistics();

        LinkPredictionAccuracy accuracyCn = new LinkPredictionAccuracy(cn, init, validation, initialWs, validationWs);
        LinkPredictionAccuracy accuracyPa = new LinkPredictionAccuracy(pa, init, validation, initialWs, validationWs);

        // Add metrics
        evaluation.addEvaluation(accuracyCn);
        evaluation.addEvaluation(accuracyPa);

        // Get report
        String report = evaluation.getReport();

        assertTrue(report.contains("<HTML> <BODY> <h1>Evaluation of different prediction algorithms</h1>"));
        assertTrue(report.contains("<h2>Results:</h2>"));
        assertTrue(report.contains("<h2>Parameters:</h2>"));
        assertTrue(report.contains("<h2>Algorithms:</h2>"));
    }
}
