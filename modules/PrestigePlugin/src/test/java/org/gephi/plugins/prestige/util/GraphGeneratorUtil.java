/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.plugins.prestige.util;

import java.util.Iterator;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;

/**
 * Generates several networks for testing
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public final class GraphGeneratorUtil {

    private GraphGeneratorUtil() {
    }

    public static GraphModel generateUnconnectedDirectedGraph(int n) {
        GraphModel graphModel = createGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        for (int i = 0; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(Integer.toString(i));
            directedGraph.addNode(currentNode);
        }
        return graphModel;
    }

    public static GraphModel generateDirectedStarGraphWith0InCenter(int n) {
        GraphModel graphModel = createGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node centerNode = graphModel.factory().newNode("0");
        directedGraph.addNode(centerNode);
        for (int i = 1; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(Integer.toString(i));
            directedGraph.addNode(currentNode);
            Edge edge = graphModel.factory().newEdge(currentNode, centerNode, true);
            directedGraph.addEdge(edge);
        }
        return graphModel;
    }

    public static GraphModel generateCircleNetwork(int n) {
        GraphModel graphModel = createGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node first = graphModel.factory().newNode("0");
        directedGraph.addNode(first);
        Node before = first;
        for (int i = 1; i < n; i++) {
            Node currentNode = graphModel.factory().newNode(Integer.toString(i));
            directedGraph.addNode(currentNode);
            Edge edge = graphModel.factory().newEdge(currentNode, before, true);
            directedGraph.addEdge(edge);
            before = currentNode;
        }
        Edge edge = graphModel.factory().newEdge(first, before, true);
        directedGraph.addEdge(edge);
        return graphModel;
    }

    public static GraphModel generateCompelteDirectedGraphWithSelfLoops(int n) {
        GraphModel model = generateCompleteDirectedGraph(n);
        Graph graph = model.getGraph();
        graph.writeLock();
        Iterator<Node> it = graph.getNodes().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Edge edge = model.factory().newEdge(node, node, true);
            graph.addEdge(edge);
        }
        graph.writeUnlock();
        return model;
    }

    public static GraphModel generateCompleteDirectedGraph(int n) {
        return generateCompleteDirectedGraphWithParallelEdges(n, 0);
    }

    public static GraphModel generateCompleteDirectedGraphWithParallelEdges(int nNodes, int nAdditionalParallelEdges) {
        GraphModel graphModel = createGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        directedGraph.writeLock();
        Node[] nodes = new Node[nNodes];
        for (int i = 0; i < nNodes; i++) {
            Node currentNode = graphModel.factory().newNode(Integer.toString(i));
            nodes[i] = currentNode;
            directedGraph.addNode(currentNode);
        }
        for (int x = 0; x <= nAdditionalParallelEdges; x++) {
            for (int i = 0; i < nNodes - 1; i++) {
                for (int j = i + 1; j < nNodes; j++) {
                    Edge currentEdge = graphModel.factory().newEdge(nodes[i], nodes[j]);
                    directedGraph.addEdge(currentEdge);
                    currentEdge = graphModel.factory().newEdge(nodes[j], nodes[i]);
                    directedGraph.addEdge(currentEdge);
                }
            }
        }
        directedGraph.writeUnlock();
        return graphModel;
    }

    /*
    * Helper classes
     */
    private static GraphModel createGraphModel() {
        return GraphGenerator.build().getGraphModel();
    }
}
