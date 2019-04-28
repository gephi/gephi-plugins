package org.gephi.plugins.linkprediction.statistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import org.gephi.graph.api.*;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;

import static org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;

public class PreferentialAttachmentStatistics extends LinkPredictionStatistics {

    private int highestValue;
    private Node neighbourA;
    private Node neighbourB;

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursStatistics.class);

    @Override
    public void execute(GraphModel graphModel) {

        consoleLogger.debug("Execution of link prediction started");

        highestValue = 0;

        //Graph graph = graphModel.getGraphVisible();

        //Look if the result column already exist and create it if needed
        Table edgeTable = graphModel.getEdgeTable();
        consoleLogger.debug("Initialize columns");
        initializeColumns(edgeTable);

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = gc.getGraphModel().getGraph();
        GraphFactory factory = gc.getGraphModel().factory();

        graph.writeLock();

        //Iterate on all nodes
        Node[] nodesA = graph.getNodes().toArray();
        ArrayList<Node> nodesB = new ArrayList<Node>( Arrays.asList(graph.getNodes().toArray()));


        for (Node a : nodesA) {
            nodesB.remove(a);

            Node[] aNeighbours = graph.getNeighbors(a).toArray();
            aNeighbours = getRelevantNeighbours(aNeighbours);
            //Calculate distance with neighbors
            for (Node b : nodesB) {
                int paValue = 0;

                Node[] bNeighbours = graph.getNeighbors(b).toArray();
                bNeighbours = getRelevantNeighbours(bNeighbours);

                paValue = aNeighbours.length * bNeighbours.length;

                EdgeIterable e = graph.getEdges(a, b);
                Edge newEdge;
                Edge[] eArr = e.toArray();

                boolean lpEdgeExists = false;
                if (eArr.length == 0) {
                    newEdge = factory.newEdge(a, b, false);
                    graph.addEdge(newEdge);
                    newEdge.setAttribute(colAddinRun, 0);
                    newEdge.setAttribute(colLastValue, paValue);
                    newEdge.setAttribute(colLP, PREFERENTIAL_ATTACHMENT_NAME);
                } else {

                    boolean upd = false;
                    for(int i = 0; i < eArr.length; i++) {

                        if (eArr[i].getAttribute(colLP).equals(PREFERENTIAL_ATTACHMENT_NAME) && (Integer) eArr[0].getAttribute(colAddinRun) == 0) {
                            eArr[i].setAttribute((colLastValue), paValue);
                            upd = true;
                        } else if (eArr[i].getAttribute(colLP).equals(PREFERENTIAL_ATTACHMENT_NAME) && (Integer) eArr[0].getAttribute(colAddinRun) > 0) {
                            upd = true;
                            lpEdgeExists = true;
                        }

                    }

                    if (!upd) {
                        newEdge = factory.newEdge(a, b, false);
                        graph.addEdge(newEdge);
                        newEdge.setAttribute(colAddinRun, 0);
                        newEdge.setAttribute(colLastValue, paValue);
                        newEdge.setAttribute(colLP, PREFERENTIAL_ATTACHMENT_NAME);
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
            Edge[] eI = graph.getEdges(neighbourA, neighbourB).toArray();
            for (Edge e : eI) {
                if (e.getAttribute(colLP).equals(PREFERENTIAL_ATTACHMENT_NAME)) {
                    e.setAttribute(colLastValue, highestValue);
                    e.setAttribute(colAddinRun, 1);
                }
            }

        }

        graph.writeUnlock();

    }

    private Node[] getRelevantNeighbours(Node[] initNeighbours) {


        //TODO Korrektur: Algorithmus auf Edge, nicht auf Node!!
        ArrayList<Node> relevantNeighbours = new ArrayList<>();

        for (Node iN : initNeighbours) {
            if (iN.getAttribute(colLP).equals(PREFERENTIAL_ATTACHMENT_NAME) || iN.getAttribute(colLP) == null) {
                relevantNeighbours.add(iN);
            }
        }
        
        return  relevantNeighbours.toArray(new Node[0]);
    }
}
