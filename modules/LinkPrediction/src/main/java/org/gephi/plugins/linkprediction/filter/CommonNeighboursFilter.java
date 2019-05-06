package org.gephi.plugins.linkprediction.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilter;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatisticsBuilder;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.*;

/**
 * Filter that limits the displayed edges to the number specified calculated
 * using the common neighbours algorithm.
 *
 * @author Marco Romanutti
 * @see CommonNeighboursFilterBuilder
 */
public class CommonNeighboursFilter extends LinkPredictionFilter {

    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursFilter.class);

    @Override public Graph filter(Graph graph) {
        consoleLogger.debug("Apply new Common Neighbours Filter");

        //Graph graph = graphModel.getGraphVisible();
        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        Table edgeTable = graph.getModel().getEdgeTable();
        initializeColumns(edgeTable);

        // Get graph factory
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphFactory factory = gc.getGraphModel().factory();

        // Lock graph for writes
        graph.writeLock();

        // Get edges
        List<Edge> edges = new ArrayList<Edge>(Arrays.asList(graph.getEdges().toArray()));
        // Remove edges from other algorithms
        Predicate<Edge> algorithmPredicate = edge -> !edge.getAttribute(colLP)
                .equals(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        edges.removeIf(algorithmPredicate);
        // Limit edges to filter criteria
        edges.stream().sorted(Comparator.comparingLong(e -> (long) e.getAttribute(colAddinRun)))
                        .limit(edgesLimit);

        if (edges.size() > 0 ){
            graph.clearEdges();
            graph.addAllEdges(edges);
        } else {
            // TODO Throw Exception
        }

        // Unlock graph
        graph.writeUnlock();

        return graph;
    }

    @Override public String getName() {
        return CommonNeighboursFilterBuilder.COMMON_NEIGHBOURS_NAME;
    }

}
