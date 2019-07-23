package org.gephi.plugins.linkprediction.statistics;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.util.Complexity;
import org.gephi.plugins.linkprediction.util.GraphUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;

/**
 * Class to calculate link predictions based on preferential attachment algorithm.
 */
public class PreferentialAttachmentStatistics extends LinkPredictionStatistics {

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(PreferentialAttachmentStatistics.class);

    // Calculation internas
    private Node neighbourA;
    private Node neighbourB;
    private Graph graph;

    static {
        complexity = Complexity.QUADRATIC;
    }

    /**
     * Gets the name of the respective algorithm.
     *
     * @return Algorithm name
     */
    @Override public String getAlgorithmName() {
        return PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;
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
        graph = graphModel.getGraph();
        GraphFactory factory = graphModel.factory();

        // Lock graph for writes
        consoleLogger.debug("Lock graph");
        graph.writeLock();

        if (isInitialExecution()) {
            // Iterate on all nodes for first execution
            consoleLogger.debug("Initial calculation");
            ArrayList<Node> nodesA = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));
            ArrayList<Node> nodesB = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));

            for (Node a : nodesA) {
                consoleLogger.log(Level.DEBUG, () -> "Calculation for node " + a.getId());

                // Add only non existing edges
                nodesB.remove(a);

                // Get neighbours of a
                ArrayList<Node> aNeighbours = getRelevantNeighbours(a);

                // Calculate preferential attachment
                for (Node b : nodesB) {
                    // Get neighbours of b
                    consoleLogger.log(Level.DEBUG, () -> "Calculation for node " + b.getId());
                    ArrayList<Node> bNeighbours = getRelevantNeighbours(b);

                    // Calculate prediction value
                    int totalNeighboursCount = aNeighbours.size() * bNeighbours.size();
                    consoleLogger.log(Level.DEBUG, () -> "Total neighbours product: " + totalNeighboursCount);

                    // Temporary save calculated
                    // value if edge does not exist
                    if (isNewEdge(graph, a, b, PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME)) {
                        saveCalculatedValue(factory, a, b, totalNeighboursCount);
                    }
                }
            }

        } else {
            recalculateAffectedNodes(graph, factory);
        }

        // Add highest predicted edge to graph
        addHighestPredictedEdgeToGraph(graph, factory, PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);

        consoleLogger.debug("Unlock graph");
        graph.writeUnlock();

    }

    /**
     * Verify if last predicted edge exists.
     *
     * @param edges Edges to apply validation on
     * @return Flag
     */
    // TODO: Remove
    private boolean lastPredictedEdgeExists(Edge[] edges) {
        for (int i = 0; i < edges.length; i++) {
            if ((edges[i].getAttribute(colLastPrediction).equals(PREFERENTIAL_ATTACHMENT_NAME)
                    && (Integer) edges[0].getAttribute(colAddedInRun) > 0) || (edges[i].getAttribute(colLastPrediction)
                    .equals(""))) {
                return true;
            }

        }
        return false;
    }

    /**
     * Finds relevant neighbours for node n.
     *
     * @param node Node for that neighbours will be searched
     * @return Neighbours, that were added by preferential attachment or have already been there before
     */
    private ArrayList<Node> getRelevantNeighbours(Node node) {

        ArrayList<Node> relevantNeighbours = new ArrayList<>();

        Node[] neighboursX = graph.getNeighbors(node).toArray();

        for (Node iN : neighboursX) {
            List<Edge> edges = GraphUtils.getEdges(graph, node, iN);
            Edge[] eList = new Edge[edges.size()];
            eList = edges.toArray(eList);

            boolean addedEdge = false;
            for (Edge e : eList) {
                if ((e.getAttribute(colLastPrediction).equals(PREFERENTIAL_ATTACHMENT_NAME) || e
                        .getAttribute(colLastPrediction).equals("")) && !addedEdge) {
                    relevantNeighbours.add(iN);
                    addedEdge = true;
                }
            }
        }

        return relevantNeighbours;
    }

    /**
     * Recalculates the link prediction probability for neighbours of affected nodes.
     *
     * @param factory Factory to create new edges
     * @param graph Graph to add predictions on
     * @param a Center node
     */
    @Override
    protected void recalculateProbability(GraphFactory factory, Graph graph, Node a) {

        // Get neighbours of a
        List<Node> aNeighbours = getRelevantNeighbours(a);

        // Get edges and remove
        // self from potential neighbours
        List<Node> nodesB = new ArrayList<>(Arrays.asList(graph.getNodes().toArray()));
        nodesB.remove(a);

        // Iterate over other nodes
        // that could become new neighbours
        for (Node b : nodesB) {

            // Update temporary saved values
            // if edge does not exist
            if (isNewEdge(graph, a, b, PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME)) {
                List<Node> bNeighbours = getRelevantNeighbours(b);
                int totalNeighboursCount = aNeighbours.size() * bNeighbours.size(); // TODO: Only this line differs from common neighbours

                // Update saved and calculated values
                updateCalculatedValue(factory, a, b, totalNeighboursCount);
            }
        }
    }
}
