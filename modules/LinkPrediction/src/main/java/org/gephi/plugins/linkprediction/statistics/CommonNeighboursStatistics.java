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

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(CommonNeighboursStatistics.class);

    @Override public void execute(GraphModel graphModel) {
        consoleLogger.debug("Execution of link prediction started");

        //Graph graph = graphModel.getGraphVisible();
        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        Table edgeTable = graphModel.getEdgeTable();
        initializeColumns(edgeTable);

        // Get graph factory
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = gc.getGraphModel().getGraph();
        GraphFactory factory = gc.getGraphModel().factory();

        // Lock graph for writes
        graph.writeLock();

        //Iterate on all nodes
        List<Node> nodesA = new ArrayList<>(Arrays.asList(graph.getNodes().toArray()));

        for (Node a : nodesA) {
            // Remove self from neighbours
            List<Node> nodesB = new ArrayList<>(nodesA);
            nodesB.remove(a);

            // Get neighbours of a
            List<Node> aNeighbours = getNeighbours(graph, a);

            //Calculate common neighbors
            for (Node b : nodesB) {
                int cnValue = 0;

                // Get neighbours of b
                List<Node> bNeighbours = getNeighbours(graph, b);

                // Count number of neighbours
                cnValue = getCommonNeighboursCount(aNeighbours, bNeighbours);

                // Check if edge exists already
                List<Edge> existingEdges = new ArrayList<>(Arrays.asList(graph.getEdges(a, b).toArray()));
                if (existingEdges.size() == 0) {
                    // Add new edge to calculation map
                    Edge newEdge = factory.newEdge(a, b, false);
                    newEdge.setAttribute(colLastValue, cnValue);
                    predictions.put(newEdge, cnValue);
                }
            }
        }

        // Get highest predicted edge
        Edge max = getHighestPrediction();

        // Add edge to graph
        if (max != null) {
            int iteration = getNextIteration(graph, CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
            max.setAttribute(colAddinRun, iteration);
            max.setAttribute(colLP, CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
            graph.addEdge(max);
        }

        // Unlock graph
        graph.writeUnlock();

    }

    private int getCommonNeighboursCount(List<Node> firstPeerNeighbours, List<Node> bNeighbours) {
        return firstPeerNeighbours.stream().filter(bNeighbours::contains).collect(Collectors.toList()).size();
    }

    private ArrayList<Node> getNeighbours(Graph graph, Node a) {
        return new ArrayList<>(
                Arrays.asList(graph.getNeighbors(a).toArray()).stream().distinct().collect(Collectors.toList()));
    }

}
