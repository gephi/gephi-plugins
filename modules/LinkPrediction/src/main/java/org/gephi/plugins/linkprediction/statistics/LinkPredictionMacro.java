package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import java.util.List;

/**
 * Macro class that triggers the calculations for all selected algorithms.
 */

public class LinkPredictionMacro extends LinkPredictionStatistics {
    List<LinkPredictionStatistics> statistics;

    public void execute(final GraphModel graphModel) {
        statistics.stream().forEach(statistic -> statistic.execute(graphModel));
    }
}
