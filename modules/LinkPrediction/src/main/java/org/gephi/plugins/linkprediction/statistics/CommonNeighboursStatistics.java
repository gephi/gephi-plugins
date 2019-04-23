package org.gephi.plugins.linkprediction.statistics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommonNeighboursStatistics extends LinkPredictionStatistics {

    private static final String ALGORITHM = "Common Neighbours";

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursStatistics.class);

    private int highestValue;
    private Node neighbourA;
    private Node neighbourB;

    @Override public void execute(GraphModel graphModel) {
        consoleLogger.debug("Execution of link prediction started");

        highestValue = 0;

        //Graph graph = graphModel.getGraphVisible();

        //Look if the result column already exist and create it if needed
        Table edgeTable = graphModel.getEdgeTable();
        initializeColumns(edgeTable);

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = gc.getGraphModel().getGraph();
        GraphFactory factory = gc.getGraphModel().factory();

        graph.writeLock();

        //Iterate on all nodes
        List<Node> nodesA = new ArrayList<>(Arrays.asList(graph.getNodes().toArray()));
        //ArrayList<Node> nodesB = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));

        for (Node a : nodesA) {

            // Remove self from neighbours
            List<Node> nodesB = new ArrayList<>(nodesA);
            nodesB.remove(a);

            List<Node> aNeighbours = new ArrayList<>(Arrays.asList(graph.getNeighbors(a).toArray()));
            //Calculate distance with neighbors

            for (Node b : nodesB) {
                int cnValue = 0;

                List<Node> bNeighbours = new ArrayList<>(Arrays.asList(graph.getNeighbors(b).toArray()));

                cnValue = aNeighbours.stream().filter(bNeighbours::contains).collect(Collectors.toList()).size();

                EdgeIterable e = graph.getEdges(a, b);
                Edge newEdge;
                Edge[] eArr = e.toArray();

                boolean lpEdgeExists = false;
                if (eArr.length == 0) {
                    newEdge = factory.newEdge(a, b, false);
                    graph.addEdge(newEdge);
                    newEdge.setAttribute(colAddinRun, 0);
                    newEdge.setAttribute(colLastValue, cnValue);
                    newEdge.setAttribute(colLP, ALGORITHM);
                } else {

                    boolean upd = false;
                    for (int i = 0; i < eArr.length; i++) {

                        if (eArr[i].getAttribute(colLP).equals(ALGORITHM)
                                && (Integer) eArr[0].getAttribute(colAddinRun) == 0) {
                            eArr[i].setAttribute((colLastValue), cnValue);
                            upd = true;
                        } else if (eArr[i].getAttribute(colLP).equals(ALGORITHM)
                                && (Integer) eArr[0].getAttribute(colAddinRun) > 0) {
                            upd = true;
                            lpEdgeExists = true;
                        }

                    }

                    if (!upd) {
                        newEdge = factory.newEdge(a, b, false);
                        graph.addEdge(newEdge);
                        newEdge.setAttribute(colAddinRun, 0);
                        newEdge.setAttribute(colLastValue, cnValue);
                        newEdge.setAttribute(colLP, ALGORITHM);
                    }

                }

                if (!lpEdgeExists && cnValue > highestValue) {
                    neighbourA = a;
                    neighbourB = b;
                    highestValue = cnValue;
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
