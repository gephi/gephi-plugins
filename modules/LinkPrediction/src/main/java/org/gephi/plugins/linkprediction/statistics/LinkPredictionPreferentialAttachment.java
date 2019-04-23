package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import org.gephi.graph.api.*;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;

public class LinkPredictionPreferentialAttachment extends LinkPredictionStatistics {

    private static final String ADDED_IN_RUN = "added_in_run";
    private static final String LAST_VALUE = "last_link_prediction_value";
    private static final String LP_ALGORITHM = "link_prediction_algorithm";
    private static final String ALGORITHM = "Preferential Attachment";

    private Column colLP;
    private Column colAddinRun;
    private Column colLastValue;

    private int highestValue;
    private Node neighbourA;
    private Node neighbourB;

    @Override
    public void execute(GraphModel graphModel) {

        highestValue = 0;

        //Graph graph = graphModel.getGraphVisible();

        //Look if the result column already exist and create it if needed
        Table edgeTable = graphModel.getEdgeTable();

        colLP = edgeTable.getColumn(LP_ALGORITHM);
        if (colLP == null) {
            colLP = edgeTable.addColumn(LP_ALGORITHM, "Chosen Link Prediction Alogirthm", Integer.class, 0);
        }

        colAddinRun = edgeTable.getColumn(ADDED_IN_RUN);
        if (colAddinRun == null) {
            colAddinRun = edgeTable.addColumn(ADDED_IN_RUN, "Added in Run", Integer.class, 0);
        }

        colLastValue = edgeTable.getColumn(LAST_VALUE);
        if (colLastValue == null) {
            colLastValue = edgeTable.addColumn(LAST_VALUE, "Last Link Prediction Value", Integer.class, 0);
        }

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
                    newEdge.setAttribute(colLP, ALGORITHM);
                } else {

                    boolean upd = false;
                    for(int i = 0; i < eArr.length; i++) {

                        if (eArr[i].getAttribute(colLP).equals(ALGORITHM) && (Integer) eArr[0].getAttribute(colAddinRun) == 0) {
                            eArr[i].setAttribute((colLastValue), paValue);
                            upd = true;
                        } else if (eArr[i].getAttribute(colLP).equals(ALGORITHM) && (Integer) eArr[0].getAttribute(colAddinRun) > 0) {
                            upd = true;
                            lpEdgeExists = true;
                        }

                    }

                    if (!upd) {
                        newEdge = factory.newEdge(a, b, false);
                        graph.addEdge(newEdge);
                        newEdge.setAttribute(colAddinRun, 0);
                        newEdge.setAttribute(colLastValue, paValue);
                        newEdge.setAttribute(colLP, ALGORITHM);
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
                if (e.getAttribute(colLP).equals(ALGORITHM)) {
                    e.setAttribute(colLastValue, highestValue);
                    e.setAttribute(colAddinRun, 1);
                }
            }

        }

        graph.writeUnlock();

    }

    private Node[] getRelevantNeighbours(Node[] initNeighbours) {

        ArrayList<Node> relevantNeighbours = new ArrayList<Node>();

        for (Node iN : initNeighbours) {
            if (iN.getAttribute(colLP).equals(ALGORITHM) || iN.getAttribute(colLP) == null) {
                relevantNeighbours.add(iN);
            }
        }

        return (Node[]) relevantNeighbours.toArray();
    }
}
