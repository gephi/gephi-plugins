package org.gephi.plugins.linkprediction.filter;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilter;

/**
 * Filter that limits the displayed edges to the number specified calculated
 * using the common neighbours algorithm.
 *
 * @author Marco Romanutti
 * @see CommonNeighboursFilterBuilder
 */
public class CommonNeighboursFilter extends LinkPredictionFilter {

    @Override public Graph filter(Graph graph) {
        // TODO Implement filter
        return graph;
    }

    @Override public String getName() {
        return CommonNeighboursFilterBuilder.COMMON_NEIGHBOURS_NAME;
    }

}
