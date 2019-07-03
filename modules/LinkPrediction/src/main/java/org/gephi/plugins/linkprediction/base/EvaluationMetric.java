package org.gephi.plugins.linkprediction.base;

import com.google.common.collect.Sets;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

        final Workspace ws = pc.duplicateWorkspace(trainWS);
        pc.openWorkspace(ws);

        GraphModel currentGraphModel = gc.getGraphModel(trainWS);
        Graph graph = currentGraphModel.getGraph();

        GraphModel newGraphModel = gc.getGraphModel(ws);
        newGraphModel.bridge().copyNodes(graph.getNodes().toArray());

        String statName = statistic.toString();
        String wsName = null;
        if (statName.contains("CommonNeighbours")) {
            wsName = "CommonNeighbours";
        }
        else if (statName.contains("PreferentialAttachment")) {
            wsName = "PreferentialAttachment";
        }
        else {
            wsName = "Default";
        }

        pc.renameWorkspace(ws, wsName);

        /**
         * Determines how many edges have to be calculated and then uses the chosen algorithm
         */
        Set<Edge> trainEdges = new HashSet<>(Arrays.asList(train.getEdges().toArray()));
        Set<Edge> testEdges = new HashSet<>(Arrays.asList(test.getEdges().toArray()));

        Set<Edge> diff = Sets.difference(trainEdges, testEdges);
        int diffEdgeCount = diff.size();

        for (int i = 0; i < diffEdgeCount; i++) {
            statistic.execute(currentGraphModel);
        }

        /**
         * Calculate accuracy of algorithm
         */
        //result = calculate(newGraph, test, statistic);
    }

    /**
     * Calculates respective metric for link prediction algorithm.
     *
     * @return Metric value
     */
    public abstract double calculate(Graph train, Graph test, LinkPredictionStatistics statistics);

    /**
     * Get caluclated evaluation result.
     *
     * @return Calculated metric value.
     */
    public double getResult() {
        return result;
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
