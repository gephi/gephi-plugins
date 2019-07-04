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

    public LinkPredictionAccuracy(LinkPredictionStatistics statistic, Graph train, Graph test, Workspace trainWS, Workspace testWS) {
        super(statistic, train, test, trainWS, testWS);
    }

    @Override public double calculate(Graph train, Graph test, LinkPredictionStatistics statistics, Graph newGraph, String alg) {
        // TODO Other calcucations for accuracy
        // TODO Report


        Set<Edge> trainEdges = new HashSet<>(Arrays.asList(train.getEdges().toArray()));
        Set<Edge> testEdges = new HashSet<>(Arrays.asList(test.getEdges().toArray()));
        Set<Edge> lpEdges = new HashSet<>(Arrays.asList(newGraph.getEdges().toArray()));

        lpEdges.removeIf(e -> !e.getAttribute("link_prediction_algorithm").equals(alg));

        // Get edges that are only in train set
        //Set<Edge> diff = Sets.difference(trainEdges, testEdges);
        Set<Edge> diff = Sets.symmetricDifference(trainEdges, testEdges);

        double diffCountAll = diff.size();
        lpEdges.removeIf(e -> !diff.contains(e));

        double diffCountAccurate = lpEdges.size();

        double accuracy = diffCountAccurate / diffCountAll;

        return accuracy;

    }
}
