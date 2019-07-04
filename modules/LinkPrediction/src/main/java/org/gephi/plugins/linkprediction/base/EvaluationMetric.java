package org.gephi.plugins.linkprediction.base;

import com.google.common.collect.Sets;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;

import java.util.*;

import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.spi.WorkspaceDuplicateProvider;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.openide.util.Lookup;
import org.gephi.project.api.ProjectController;
import org.gephi.graph.*;

/**
 * Calculates the metric to evaluate the quality of a link prediction algorithm.
 *
 * @author Marco Romanutti
 */
public abstract class EvaluationMetric {
    /**
     * Train graph
     */
    protected final Graph train;
    /**
     * Test graph
     */
    protected final Graph test;
    /**
     * Test Workspace
     */
    protected final Workspace testWS;
    /**
     * Train Workspace
     */
    protected final Workspace trainWS;

    /**
     * Algorithm to evaluate
     */
    protected final LinkPredictionStatistics statistic;
    /**
     * Calculated result
     */
    protected double result;
    protected String algorithmName;

    public EvaluationMetric(LinkPredictionStatistics statistic, Graph train, Graph test, Workspace trainWS, Workspace testWS){
        this.statistic = statistic;
        this.train = train;
        this.test = test;
        this.trainWS = trainWS;
        this.testWS = testWS;
    }

    public void run() {

        /**
         * Duplicate Graph for calculation of chosen algorithm
         */

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project pr = pc.getCurrentProject();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);

        String statName = statistic.toString();
        String wsName = null;
        String wsNameTable = null;

        if (statName.contains("CommonNeighbours")) {
            wsName = "CommonNeighbours";
            wsNameTable = "Common Neighbours";

        }
        else if (statName.contains("PreferentialAttachment")) {
            wsName = "PreferentialAttachment";
            wsNameTable = "PreferentialAttachment";
        }
        else {
            wsName = "Default";
        }

        /**
         * Determines how many edges have to be calculated and then uses the chosen algorithm
         */
        Set<Edge> trainEdges = new HashSet<>(Arrays.asList(train.getEdges().toArray()));
        Set<Edge> testEdges = new HashSet<>(Arrays.asList(test.getEdges().toArray()));
        Workspace ws = pc.newWorkspace(pr);

        int diffEdgeCount = 0;

        GraphModel currentGraphModel = gc.getGraphModel(trainWS);

        if (trainEdges.size() > testEdges.size()) {
            diffEdgeCount = trainEdges.size() - testEdges.size();
            currentGraphModel = gc.getGraphModel(testWS);

        }
        else if (trainEdges.size() < testEdges.size()) {
            diffEdgeCount = testEdges.size() - trainEdges.size();
            currentGraphModel = gc.getGraphModel(trainWS);
        }

        Graph graph = currentGraphModel.getGraph();
        GraphModel newGraphModel = gc.getGraphModel(ws);
        newGraphModel.bridge().copyNodes(graph.getNodes().toArray());
        pc.renameWorkspace(ws, wsName);
        pc.openWorkspace(ws);

        for (int i = 0; i < diffEdgeCount; i++) {
            statistic.execute(newGraphModel);
        }


        /**
         * Calculate accuracy of algorithm
         */
        Graph newGraph = newGraphModel.getGraph();
        if (trainEdges.size() > testEdges.size()) {
            result = calculate(test, train, statistic, newGraph, wsNameTable);

        }
        else if (trainEdges.size() < testEdges.size()) {
            result = calculate(train, test, statistic, newGraph, wsNameTable);
        }
        else {
            result = calculate (train, test, statistic, newGraph, wsNameTable);
        }

    }

    /**
     * Calculates respective metric for link prediction algorithm.
     *
     * @return Metric value
     */
    public abstract double calculate(Graph train, Graph test, LinkPredictionStatistics statistics, Graph newGraph, String alg);

    /**
     * Get caluclated evaluation result.
     *
     * @return Calculated metric value.
     */
    public double getResult() {
        return result;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Evlauates if evaluation metric has the same underlying statistic algorithm.
     *
     * @param o Object to compare
     * @return Equality of two evaluation metrics
     */
    @Override public boolean equals(Object o) {
        if (!(o instanceof EvaluationMetric))
            return false;
        if (((EvaluationMetric) o).statistic.getClass().equals(this.getClass()))
            return true;
        else
            return false;
    }

    /**
     * Hashes class from statistic algorithm.
     *
     * @return Hashed statistic class
     */
    @Override
    public int hashCode() {
        return Objects.hash(statistic.getClass());
    }
}
