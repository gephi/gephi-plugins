package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.Graph;

import java.util.Objects;

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
     * Algorithm to evaluate
     */
    protected final LinkPredictionStatistics statistic;
    /**
     * Calculated result
     */
    protected double result;

    public EvaluationMetric(LinkPredictionStatistics statistic, Graph train, Graph test){
        this.statistic = statistic;
        this.train = train;
        this.test = test;
    }

    public void run() {
        result = calculate(train, test, statistic);
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
