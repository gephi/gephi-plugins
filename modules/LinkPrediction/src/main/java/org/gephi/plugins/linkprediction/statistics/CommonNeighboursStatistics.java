package org.gephi.plugins.linkprediction.statistics;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.util.Complexity;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to calculate link predictions based on common neighbour algorithm.
 */
public class CommonNeighboursStatistics extends LinkPredictionStatistics {

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursStatistics.class);

    static {
        complexity = Complexity.QUADRATIC;
    }

    /**
     * Gets the name of the respective algorithm.
     *
     * @return Algorithm name
     */
    @Override public String getAlgorithmName() {
        return CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME;
    }

    /**
     * Executes link prediction and adds edge with highest possibility.
     *
     * @param graphModel Graph on which edge will be added
     */
    @Override public void execute(GraphModel graphModel) {
        consoleLogger.debug("Execution of link prediction started");

        // Look if the result column already exist and create it if needed
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

        if (isInitialExecution()) {
            // Iterate on all nodes for first execution
            consoleLogger.debug("Initial calculation");
            List<Node> nodesA = new ArrayList<>(Arrays.asList(graph.getNodes().toArray()));
            List<Node> nodesB = new ArrayList<>(nodesA);

            for (Node a : nodesA) {
                consoleLogger.log(Level.DEBUG, () -> "Calculation for node " + a.getId());

                // Remove self from neighbours
                nodesB.remove(a);

                // Get neighbours of a
                List<Node> aNeighbours = getNeighbours(graph, a);

                // Calculate common neighbors
                for (Node b : nodesB) {
                    // Get neighbours of b
                    consoleLogger.log(Level.DEBUG, () -> "Calculation for node " + b.getId());
                    List<Node> bNeighbours = getNeighbours(graph, b);

                    // Count number of neighbours
                    int commonNeighboursCount = getCommonNeighboursCount(aNeighbours, bNeighbours);
                    consoleLogger.log(Level.DEBUG,
                            () -> "Number of neighbours for node " + b.getId() + ": " + commonNeighboursCount);

                    // Temporary save calculated
                    // value if edge does not exist
                    if (isNewEdge(graph, a, b, CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME)) {
                        saveCalculatedValue(factory, a, b, commonNeighboursCount);
                    }
                }
            }

        } else {
            recalculateAffectedNodes(graph, factory);
        }

        // Add highest predicted edge to graph
        addHighestPredictedEdgeToGraph(graph, factory, CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);

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
     * Retrieve neighbours for node a from graph
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

    /**
     * Recalculates the link prediction probability for neighbours of affected nodes.
     *
     * @param factory Factory to create new edges
     * @param graph   Graph to add predictions on
     * @param a       Center node
     */
    @Override protected void recalculateProbability(GraphFactory factory, Graph graph, Node a) {

        // Get neighbours of a
        List<Node> aNeighbours = getNeighbours(graph, a);

        // Get edges and remove
        // self from potential neighbours
        List<Node> nodesB = new ArrayList<>(Arrays.asList(graph.getNodes().toArray()));
        nodesB.remove(a);

        // Iterate over other nodes
        // that could become new neighbours
        for (Node b : nodesB) {
            // Update temporary saved values
            // if edge does not exist
            if (isNewEdge(graph, a, b, CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME)) {
                List<Node> bNeighbours = getNeighbours(graph, b);
                int commonNeighboursCount = getCommonNeighboursCount(aNeighbours, bNeighbours);

                // Update saved and calculated values
                updateCalculatedValue(factory, a, b, commonNeighboursCount);
            }
        }
    }
}
