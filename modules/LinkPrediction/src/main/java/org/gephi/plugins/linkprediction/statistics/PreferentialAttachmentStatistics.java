package org.gephi.plugins.linkprediction.statistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.util.GraphUtils;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;

public class PreferentialAttachmentStatistics extends LinkPredictionStatistics {

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursStatistics.class);
    private int highestValue;
    private Node neighbourA;
    private Node neighbourB;
    private Graph graph;

    @Override
    public void execute(GraphModel graphModel) {

        consoleLogger.debug("Execution of link prediction started");

        highestValue = 0;

        Table edgeTable = graphModel.getEdgeTable();

        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        initializeColumns(edgeTable);

        //GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        graph = graphModel.getGraph();
        GraphFactory factory = graphModel.factory();

        graph.writeLock();

        //Iterate on all nodes
        ArrayList<Node> nodesA = new ArrayList<Node>( Arrays.asList(graph.getNodes().toArray()));
        ArrayList<Node> nodesB = new ArrayList<Node>( Arrays.asList(graph.getNodes().toArray()));


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

                boolean lpEdgeExists = false;
                for(int i = 0; i < eArr.length; i++) {
                    if ((eArr[i].getAttribute(colLP).equals(PREFERENTIAL_ATTACHMENT_NAME) && (Integer) eArr[0].getAttribute(colAddinRun) > 0) ||
                    (eArr[i].getAttribute(colLP).equals("")))
                    {
                            lpEdgeExists = true;
                    }

                }

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
            newEdge.setAttribute(colLP, PREFERENTIAL_ATTACHMENT_NAME);
            newEdge.setAttribute(colAddinRun, getNextIteration(graph, PREFERENTIAL_ATTACHMENT_NAME));
            newEdge.setAttribute(colLastValue, highestValue);
            predictions.put(newEdge, highestValue);
        }

        graph.writeUnlock();

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
                 if ((e.getAttribute(colLP).equals(PREFERENTIAL_ATTACHMENT_NAME) || e.getAttribute(colLP).equals("")) && !addedEdge) {
                     relevantNeighbours.add(iN);
                     addedEdge = true;
                 }
             }
        }
        
        return relevantNeighbours;
    }
}
