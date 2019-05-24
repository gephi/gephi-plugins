package org.gephi.plugins.linkprediction.statistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.util.GraphUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;

public class PreferentialAttachmentStatistics extends LinkPredictionStatistics {

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursStatistics.class);
    private Node neighbourA;
    private Node neighbourB;
    private Graph graph;

    @Override
    public void execute(GraphModel graphModel) {

        consoleLogger.debug("Execution of link prediction started");

        Table edgeTable = graphModel.getEdgeTable();

        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        initializeColumns(edgeTable);

        graph = graphModel.getGraph();
        GraphFactory factory = graphModel.factory();

        // Lock graph for writes
        graph.writeLock();

        // Clear predictions
        predictions.clear();

        //Iterate on all nodes
        ArrayList<Node> nodesA = new ArrayList<Node>( Arrays.asList(graph.getNodes().toArray()));
        ArrayList<Node> nodesB = new ArrayList<Node>( Arrays.asList(graph.getNodes().toArray()));

        // Initialize highest value
        int highestValue = Integer.MIN_VALUE;

        for (Node a : nodesA) {
            nodesB.remove(a);

            ArrayList<Node> aNeighbours = getRelevantNeighbours(a);
            for (Node b : nodesB) {
                int paValue = 0;

                ArrayList<Node> bNeighbours = getRelevantNeighbours(b);

                // Get prediction value
                paValue = aNeighbours.size() * bNeighbours.size();

                List<Edge> e = GraphUtils.getEdges(graph, a, b);
                Edge[] eArr = new Edge[e.size()];
                eArr = e.toArray(eArr);

                boolean lpEdgeExists = lpEdgeExists(eArr);

                if (!lpEdgeExists && paValue > highestValue) {
                    neighbourA = a;
                    neighbourB = b;
                    highestValue = paValue;
                }
            }

        }

        if (neighbourA != null) {
            Edge newEdge = factory.newEdge(neighbourA, neighbourB, false);
            graph.addEdge(newEdge);
            newEdge.setAttribute(colLastPrediction, PREFERENTIAL_ATTACHMENT_NAME);
            newEdge.setAttribute(colAddedInRun, getNextIteration(graph, PREFERENTIAL_ATTACHMENT_NAME));
            newEdge.setAttribute(colLastCalculatedValue, highestValue);
            predictions.put(newEdge, highestValue);
        }

        graph.writeUnlock();

    }

    private boolean lpEdgeExists(Edge[] eArr) {
        for(int i = 0; i < eArr.length; i++) {
            if ((eArr[i].getAttribute(colLastPrediction).equals(PREFERENTIAL_ATTACHMENT_NAME) && (Integer) eArr[0].getAttribute(
                    colAddedInRun) > 0) ||
            (eArr[i].getAttribute(colLastPrediction).equals("")))
            {
                    return true;
            }

        }
        return false;
    }

    private ArrayList<Node> getRelevantNeighbours(Node x) {

        ArrayList<Node> relevantNeighbours = new ArrayList<>();

        Node[] neighboursX = graph.getNeighbors(x).toArray();

        for (Node iN : neighboursX) {
            List<Edge> edges = GraphUtils.getEdges(graph, x, iN);
            Edge[] eList = new Edge[edges.size()];
            eList = edges.toArray(eList);

            boolean addedEdge = false;
            for (Edge e : eList) {
                 if ((e.getAttribute(colLastPrediction).equals(PREFERENTIAL_ATTACHMENT_NAME) || e.getAttribute(
                         colLastPrediction).equals("")) && !addedEdge) {
                     relevantNeighbours.add(iN);
                     addedEdge = true;
                 }
             }
        }
        
        return relevantNeighbours;
    }
}
