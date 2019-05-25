package org.gephi.plugins.linkprediction.util;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Various utilities used for graph traversals.
 */
public final class GraphUtils {

    private GraphUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the edges adjacent to node1 and node2.
     * If there aren't any parallel edges only one edge will be returned.
     * Usead instead of buggy org.gephi.graph.api.Graph.getEdges(Node n1, Node n2).
     *
     * @param graph Graph to apply the function
     * @param node1 First node
     * @param node2 Second node
     * @return Adjacent edges
     */
    public static List<Edge> getEdges(Graph graph, Node node1, Node node2) {
        Predicate<Edge> containsEdgePredicate = edge ->
                (edge.getTarget().equals(node1) && edge.getSource().equals(node2)) || (edge.getTarget().equals(node2)
                        && edge.getSource().equals(node1));
        return Arrays.asList(graph.getEdges().toArray()).stream().filter(containsEdgePredicate)
                .collect(Collectors.toList());
    }
}
