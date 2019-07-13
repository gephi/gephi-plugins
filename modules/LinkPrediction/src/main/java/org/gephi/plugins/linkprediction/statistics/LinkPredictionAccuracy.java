package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.project.api.Workspace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Calculates link prediction accuracy according to
 */
// TODO Add link
public class LinkPredictionAccuracy extends EvaluationMetric {

    public LinkPredictionAccuracy(LinkPredictionStatistics statistic, Graph initial, Graph validation, Workspace initialWS, Workspace validationWS) {
        super(statistic, initial, validation, initialWS, validationWS);
    }

    /**
     * Calculates accuracy as percentage of correct predicted edges compared to total predicted edges.
     *
     * @param addedEdges Number of edges to add
     * @param trained Graph on that links predictions are added
     * @param validation Validation graph
     * @param statistics Algorithm used
     * @return Accuracy in percent
     */
    @Override public double calculate(int addedEdges, Graph trained, Graph validation, LinkPredictionStatistics statistics) {
        Set<Edge> trainedEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));
        Set<Edge> validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));

        // Remove edges from other algorithms and
        // edges that initially existed
        trainedEdges.removeIf(e -> !e.getAttribute(LinkPredictionStatistics.LP_ALGORITHM).equals(statistics.getAlgorithmName()));

        // Get edges that are only in trained set
        Set<Edge> diff = trainedEdges;
        diff.removeIf(e -> !validation.isAdjacent(validation.getNode(e.getSource().getId()), validation.getNode(e.getTarget().getId())));

        double accuracy = ((double) diff.size() / (double) addedEdges) * 100;

        return accuracy;

    }
}
