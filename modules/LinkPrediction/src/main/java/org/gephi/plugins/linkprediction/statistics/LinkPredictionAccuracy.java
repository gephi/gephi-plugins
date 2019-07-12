package org.gephi.plugins.linkprediction.statistics;

import com.google.common.collect.Sets;
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

    @Override public double calculate(Graph initial, Graph trained, Graph validation, LinkPredictionStatistics statistics) {
        Set<Edge> trainedEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));
        Set<Edge> validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));

        // Remove edges from other algorithms and
        // edges that initially existed
        trainedEdges.removeIf(e -> !e.getAttribute("link_prediction_algorithm").equals(statistics.getAlgorithmName()));

        // Get edges that are only in trained set
        Set<Edge> diff = Sets.difference(trainedEdges, validationEdges);

        // Get number of added edges, compared to initial graph
        int addedEdges = validationEdges.size() - initial.getEdgeCount();

        double accuracy = ((double) diff.size() / (double) addedEdges) * 100;

        return accuracy;

    }
}
