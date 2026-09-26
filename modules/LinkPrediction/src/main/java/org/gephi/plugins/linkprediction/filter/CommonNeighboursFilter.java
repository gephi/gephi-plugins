package org.gephi.plugins.linkprediction.filter;

import org.gephi.plugins.linkprediction.base.LinkPredictionFilter;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatisticsBuilder;

/**
 * Filter that limits the displayed edges to the number specified calculated
 * using the common neighbours algorithm.
 *
 * @author Marco Romanutti
 * @see CommonNeighboursFilterBuilder
 */
public class CommonNeighboursFilter extends LinkPredictionFilter {

    @Override public String getName() {
        return CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME;
    }

}
