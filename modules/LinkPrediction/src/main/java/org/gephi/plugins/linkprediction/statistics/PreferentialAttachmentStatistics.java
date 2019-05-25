package org.gephi.plugins.linkprediction.statistics;

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

public class PreferentialAttachmentStatistics extends LinkPredictionStatistics {

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(PreferentialAttachmentStatistics.class);

    // Calculation internas
    private Node neighbourA;
    private Node neighbourB;
    private Graph graph;

    static {
        complexity = Complexity.EXPONENTIAL;
    }

    @Override public void execute(GraphModel graphModel) {

        consoleLogger.debug("Execution of link prediction started");
        Table edgeTable = graphModel.getEdgeTable();

        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        initializeColumns(edgeTable);

        // Get graph factory
        consoleLogger.debug("Get factory");
        graph = graphModel.getGraph();
        GraphFactory factory = graphModel.factory();

        // Lock graph for writes
        consoleLogger.debug("Lock graph");
        graph.writeLock();

        // Clear predictions
        consoleLogger.debug("Clear predictions");
        predictions.clear();

        //Iterate on all nodes
        ArrayList<Node> nodesA = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));
        ArrayList<Node> nodesB = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));

        // Initialize highest value
        int highestValue = Integer.MIN_VALUE;

        for (Node a : nodesA) {
            if (consoleLogger.isDebugEnabled()) {
                consoleLogger.debug("Calculation for node " + a.getId());
            }
            // Add only non existing edges
            nodesB.remove(a);

            ArrayList<Node> aNeighbours = getRelevantNeighbours(a);
            for (Node b : nodesB) {
                if (consoleLogger.isDebugEnabled()) {
                    consoleLogger.debug("Calculation for node " + b.getId());
                }

                int paValue = 0;
                ArrayList<Node> bNeighbours = getRelevantNeighbours(b);

                // Get prediction value
                paValue = aNeighbours.size() * bNeighbours.size();

                List<Edge> e = GraphUtils.getEdges(graph, a, b);
                Edge[] eArr = new Edge[e.size()];
                eArr = e.toArray(eArr);

                boolean lpEdgeExists = lastPredictedEdgeExists(eArr);
                if (consoleLogger.isDebugEnabled()) {
                    consoleLogger.debug("Last predicted edge exists? " + lpEdgeExists);
                }

                if (!lpEdgeExists && paValue > highestValue) {
                    neighbourA = a;
                    neighbourB = b;
                    highestValue = paValue;
                    if (consoleLogger.isDebugEnabled()) {
                        consoleLogger.debug("New edge will be added: " + a.getLabel() + ", " + b.getLabel() + ", "
                                + highestValue);
                    }
                }
            }
        }

        if (neighbourA != null) {
            consoleLogger.debug("Add highest edge to graph");
            Edge newEdge = factory.newEdge(neighbourA, neighbourB, false);
            graph.addEdge(newEdge);
            newEdge.setAttribute(colLastPrediction, PREFERENTIAL_ATTACHMENT_NAME);
            newEdge.setAttribute(colAddedInRun, getNextIteration(graph, PREFERENTIAL_ATTACHMENT_NAME));
            newEdge.setAttribute(colLastCalculatedValue, highestValue);
            predictions.put(newEdge, highestValue);
        }

        consoleLogger.debug("Unlock graph");
        graph.writeUnlock();

    }

    /**
     * Verify if last predicted edge exsits.
     *
     * @param edges Edges to apply verification on
     * @return Flag
     */
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
}
