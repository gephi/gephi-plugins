package org.gephi.plugins.linkprediction.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Table;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilter;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.initializeColumns;

/**
 * Filter that limits the displayed edges to the number specified calculated
 * using the preferential attachment algorithm.
 *
 * @author Marco Romanutti
 * @see PreferentialAttachmentFilterBuilder
 */
public class PreferentialAttachmentFilter extends LinkPredictionFilter {

    private static Logger consoleLogger = LogManager.getLogger(PreferentialAttachmentFilter.class);

    @Override public Graph filter(Graph graph) {
        consoleLogger.debug("Apply new Preferential Attachment Filter");

        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        Table edgeTable = graph.getModel().getEdgeTable();
        initializeColumns(edgeTable);
        
        // Lock graph for writes
        graph.writeLock();

        // Get edges
        List<Edge> edges = new ArrayList<Edge>(Arrays.asList(graph.getEdges().toArray()));
        // Remove edges from other algorithms
        removeOtherEdges(edges);

        // Remove other nodes and edges
        retainEdges(graph, edges);

        // Unlock graph
        graph.writeUnlock();

        return graph;
    }

    @Override public String getName() {
        return PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;
    }

}
