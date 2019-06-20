package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.Graph;

/**
 * Calculates the metric to evaluate the quality of a link prediction algorithm.
 *
 * @author Marco Romanutti
 */
public abstract class EvaluationMetric {
    /** Train graph */
    protected Graph train;
    /** Test graph */
    protected Graph test;
    /** Algorithm to evaluate */
    protected LinkPredictionStatistics statistic;
    /** Calculated result */
    protected double result;

    public void run(Graph train, Graph test, LinkPredictionStatistics statistic){
        this.train = train;
        this.test = test;
        this.statistic = statistic;
        result = calculate(train, test, statistic);
    }
    /**
     * Calculates respective metric for link prediction algorithm.
     *
     * @return Metric value
     */
    public abstract double calculate(Graph train, Graph test, LinkPredictionStatistics statistics);
}
