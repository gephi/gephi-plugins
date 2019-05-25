package org.gephi.plugins.linkprediction.statistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.util.Complexity;
import org.gephi.plugins.linkprediction.util.GraphUtils;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommonNeighboursStatistics extends LinkPredictionStatistics {

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursStatistics.class);

    static {
        complexity = Complexity.EXPONENTIAL;
    }

    @Override public void execute(GraphModel graphModel) {
        consoleLogger.debug("Execution of link prediction started");

        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        Table edgeTable = graphModel.getEdgeTable();
        initializeColumns(edgeTable);

        // Get graph factory
        consoleLogger.debug("Get factory");
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = gc.getGraphModel().getGraph();
        GraphFactory factory = gc.getGraphModel().factory();

        // Lock graph for writes
        consoleLogger.debug("Lock graph");
        graph.writeLock();

        // Clear predictions
        consoleLogger.debug("Clear predictions");
        predictions.clear();

        //Iterate on all nodes
        List<Node> nodesA = new ArrayList<>(Arrays.asList(graph.getNodes().toArray()));

        for (Node a : nodesA) {
            consoleLogger.debug("Calculation for node " + a.getId());
            // Remove self from neighbours
            List<Node> nodesB = new ArrayList<>(nodesA);
            nodesB.remove(a);

            // Get neighbours of a
            List<Node> aNeighbours = getNeighbours(graph, a);

            //Calculate common neighbors
            for (Node b : nodesB) {
                if (consoleLogger.isDebugEnabled()) {
                    consoleLogger.debug("Calculation for node " + b.getId());
                }
                int cnValue = 0;

                // Get neighbours of b
                List<Node> bNeighbours = getNeighbours(graph, b);

                // Count number of neighbours
                cnValue = getCommonNeighboursCount(aNeighbours, bNeighbours);
                if (consoleLogger.isDebugEnabled()) {
                    consoleLogger.debug("Number of neighbours for node " + b.getId() + ": " + cnValue);
                }

                // Check if edge exists already
                // FIXME graph.getEdges returns always null
                //List<Edge> existingEdges = Arrays.asList(graph.getEdges(a, b).toArray());
                List<Edge> existingEdges = GraphUtils.getEdges(graph, a, b);
                long numberOfExistingEdges = existingEdges.size();
                if (consoleLogger.isDebugEnabled()) {
                    consoleLogger.debug("Size of existing edges: " + numberOfExistingEdges);
                }

                if (numberOfExistingEdges == 0) {
                    // Add new edge to calculation map
                    Edge newEdge = factory.newEdge(a, b, false);
                    newEdge.setAttribute(colLastCalculatedValue, cnValue);
                    if (consoleLogger.isDebugEnabled()) {
                        consoleLogger.debug("Add new edge: " + a.getLabel() + ", " + b.getLabel() + ", " + cnValue);
                    }
                    predictions.put(newEdge, cnValue);
                }
            }
        }

        // Get highest predicted edge
        Edge max = getHighestPrediction();

        // Add edge to graph
        if (max != null) {
            int iteration = getNextIteration(graph, CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
            max.setAttribute(colAddedInRun, iteration);
            max.setAttribute(colLastPrediction, CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
            if (consoleLogger.isDebugEnabled()) {
                consoleLogger.debug("Add highest predicted edge: " + max);
            }
            graph.addEdge(max);
        }

        // Unlock graph
        consoleLogger.debug("Unlock graph");
        graph.writeUnlock();
    }

    /**
     * Counts number of common neighbours of two nodes
     *
     * @param aNeighbours Neighbours of a
     * @param bNeighbours Neighbours of b
     * @return Number of common neighbours
     */
    private int getCommonNeighboursCount(List<Node> aNeighbours, List<Node> bNeighbours) {
        consoleLogger.debug("Get common neighbours count");
        return aNeighbours.stream().filter(bNeighbours::contains).collect(Collectors.toList()).size();
    }

    /**
     * Retrieve neighbours for node ad from graph
     *
     * @param graph Graph in which neighours will be searched
     * @param n     Node for which neighbours will be searched
     * @return Neighbours of n
     */
    private ArrayList<Node> getNeighbours(Graph graph, Node n) {
        consoleLogger.debug("Get neighbours");
        return new ArrayList<>(
                Arrays.asList(graph.getNeighbors(n).toArray()).stream().distinct().collect(Collectors.toList()));
    }

}
