package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.GraphModel;

/**
 * Calculates the metric to evaluate the quality of a link prediction algorithm.
 *
 * @author Marco Romanutti
 */
public abstract class EvaluationMetric {
    /** Calculated result */
    private double result;

    /**
     * Calculates respective metric for link prediction algorithm.
     *
     * @return Metric value
     */
    public abstract double calculate(GraphModel graphModel, LinkPredictionStatistics statistics);
}
