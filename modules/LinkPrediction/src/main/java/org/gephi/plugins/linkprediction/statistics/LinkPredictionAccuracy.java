package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.Graph;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

/**
 * Calculates link prediction accuracy according to
 */
// TODO Add link
public class LinkPredictionAccuracy extends EvaluationMetric {

    public LinkPredictionAccuracy(LinkPredictionStatistics statistic, Graph train, Graph test) {
        super(statistic, train, test);
    }

    @Override public double calculate(Graph train, Graph test, LinkPredictionStatistics statistics) {
        // TODO Calculate accuracy of train and test graphs
        return 0;
    }
}
