package uk.co.timsummertonbrier.multinodelineage;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;

public interface NodeChildSupplier {
    NodeIterable getChildren(DirectedGraph graph, Node parent);
}