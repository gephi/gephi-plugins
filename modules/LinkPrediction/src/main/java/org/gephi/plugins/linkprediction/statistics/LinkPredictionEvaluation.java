package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Macro class that triggers the evaluation calculation for all selected algorithms.
 */
public class LinkPredictionEvaluation extends LinkPredictionStatistics {
    // Number of edge prediction iterations
    private int iterationLimit = ITERATION_LIMIT_DEFAULT;
    // List of link prediction evaluations
    private Map<LinkPredictionStatistics, EvaluationMetric> evaluations = new HashMap<>();

    /**
     * Calcualtes link predictions and metrics for all evaluations.
     *
     * @param graphModel Model to add evaluations
     */
    public void execute(final GraphModel graphModel) {
        int i = 0;
        while (i < iterationLimit) {
            evaluations.keySet().stream().forEach(statistic -> {
                statistic.execute(graphModel);
                evaluations.get(statistic).calculate(graphModel, statistic);});
            i++;
        }
    }

    /**
     * Add link prediction statistic class if no already exists in list.
     *
     * @param statistic Statistic to add
     */
    public void addStatistic(LinkPredictionStatistics statistic) {
        if (!evaluations.keySet().contains(statistic)) {
            evaluations.put(statistic, new EvaluationMetric() {
                @Override public double calculate(GraphModel graphModel, LinkPredictionStatistics statistics) {
                    return 0;
                }
            });
            // TODO: Create implementation for accuarcy and use object of it instead
        }
    }

    /**
     * Removes evaluation from list.
     *
     * @param evaluation Statistic to remove
     */
    public void removeStatistic(LinkPredictionStatistics evaluation) {
        if (evaluations.keySet().contains(evaluation)) {
            evaluations.remove(evaluation);
        }
    }

    public int getIterationLimit() {
        return iterationLimit;
    }

    public void setIterationLimit(int iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    public Map<LinkPredictionStatistics, EvaluationMetric> getEvaluations() {
        return evaluations;
    }

    /**
     * Get specific link prediction algorithm from evaluations list
     * @param statistic Class of searched statistic
     * @return LinkPredictionStatistic
     */
    public EvaluationMetric getEvaluation(Class statistic) {
        return evaluations.get(statistic);
    }

    public void setEvaluation(Map<LinkPredictionStatistics, EvaluationMetric> evaluations) {
        this.evaluations.putAll(evaluations);
    }
}
