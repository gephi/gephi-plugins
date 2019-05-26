package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Macro class that triggers the calculations for all selected algorithms.
 */
public class LinkPredictionMacro extends LinkPredictionStatistics {
    // Number of edge prediction iterations
    private int iterationLimit = ITERATION_LIMIT_DEFAULT;
    // List of link prediction statistics
    private List<LinkPredictionStatistics> statistics = new ArrayList<>();

    /**
     * Calcualtes link predictions on all statistics.
     *
     * @param graphModel Model to add statistics
     */
    public void execute(final GraphModel graphModel) {
        int i = 0;
        while (i < iterationLimit) {
            statistics.stream().forEach(statistic -> statistic.execute(graphModel));
            i++;
        }
    }

    /**
     * Add link prediction statistic class if no already exists in list.
     *
     * @param statistic Statistic to add
     */
    public void addStatistic(LinkPredictionStatistics statistic) {
        if (!statistics.contains(statistic)) {
            statistics.add(statistic);
        }
    }

    /**
     * Removes statistic from list.
     *
     * @param statistic Statistic to remove
     */
    public void removeStatistic(LinkPredictionStatistics statistic) {
        if (statistics.contains(statistic)) {
            statistics.remove(statistic);
        }
    }

    public int getIterationLimit() {
        return iterationLimit;
    }

    public void setIterationLimit(int iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    public List<LinkPredictionStatistics> getStatistics() {
        return statistics;
    }

    /**
     * Get specific link prediction algorithm from statistics list
     * @param statistic Class of searched statistic
     * @return LinkPredictionStatistic
     */
    public LinkPredictionStatistics getStatistic(Class statistic) {
        return statistics.stream().filter(s -> s.getClass().equals(statistic)).findFirst().orElse(null);
    }

    public void setStatistics(List<LinkPredictionStatistics> statistics) {
        this.statistics = statistics;
    }
}
