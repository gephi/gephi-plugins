package org.gephi.plugins.linkprediction.statistics;

import com.google.common.collect.Sets;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Calculates link prediction accuracy according to
 */
// TODO Add link
public class LinkPredictionAccuracy extends EvaluationMetric {

    public LinkPredictionAccuracy(LinkPredictionStatistics statistic, Graph train, Graph test) {
        super(statistic, train, test);
    }

    @Override public double calculate(Graph train, Graph test, LinkPredictionStatistics statistics) {
        // FIXME Sets.difference seems not to work correctly
        // TODO Calcualte for specific algorithm (statistic instance common neigbhour/preferential attachment)
        // TODO Other calcucations for accuracy
        // TODO Report
        Set<Edge> trainEdges = new HashSet<>(Arrays.asList(train.getEdges().toArray()));
        Set<Edge> testEdges = new HashSet<>(Arrays.asList(test.getEdges().toArray()));

        // Get edges that are only in train set
        Set<Edge> diff = Sets.difference(trainEdges, testEdges);

        double diffCount = diff.size();
        double testCount = testEdges.size();

        double accuray = diffCount / testCount;

        return accuray;
    }
}
