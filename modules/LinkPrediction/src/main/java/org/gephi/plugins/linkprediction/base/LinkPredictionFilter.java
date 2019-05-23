package org.gephi.plugins.linkprediction.base;

import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.plugins.linkprediction.exceptions.IllegalEdgeNumberException;
import org.openide.util.Exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.ColAddedInRun;
import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.ColLastPrediction;

/**
 * Filter that removes all predicted edges that do not originate from the
 * corresponding algorithm.
 * <p>
 * This base class contains all filter-independent implementations. The
 * base class is extended by the implementations of the respective algorithms.
 *
 * @author Marco Romanutti
 * @see LinkPredictionFilterBuilder
 */
public abstract class LinkPredictionFilter implements ComplexFilter {
    /** Default number of displayed predicted edges */
    public static final int EDGES_LIMIT_DEFAULT = 1;

    /** Properties used in filter */
    protected static FilterProperty[] filterProperties;
    /** Number of displayed predicted edges */
    protected Integer edgesLimit = EDGES_LIMIT_DEFAULT;

    /**
     * Gets or creates singleton instance of properties.
     *
     * @return Filter properties
     */
    public FilterProperty[] getProperties() {
        if (filterProperties == null) {
            try {
                filterProperties = new FilterProperty[] { FilterProperty.createProperty(this, Integer.class, "edgesLimit") };
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return filterProperties;
    }

    /**
     * Removes edges, that were added using another algorithm.
     *
     * @param edges Edges list to check
     */
    public void removeOtherEdges(List<Edge> edges) {
        Predicate<Edge> algorithmPredicate = edge -> !edge.getAttribute(ColLastPrediction)
                .equals(getName());
        edges.removeIf(algorithmPredicate);
        // Limit edges to filter criteria
        edges = edges.stream().sorted(Comparator.comparingLong(e -> (int) e.getAttribute(ColAddedInRun)))
                .limit(edgesLimit).collect(Collectors.toList());
    }

    /**
     * Extracts nodes from graph, that are not in edges list.
     *
     * @param graph Originial graph
     * @param edges Edges list for new graph
     * @return Edges to remove from graph
     */
    public List<Node> getNodesToRemove(Graph graph, List<Edge> edges) {
        // Get nodes
        List<Node> sourceNodes = edges.stream().map(edge -> edge.getSource()).collect(Collectors.toList());
        List<Node> targetNodes = edges.stream().map(edge -> edge.getTarget()).collect(Collectors.toList());

        // Union nodes
        sourceNodes.addAll(targetNodes);
        List<Node> remainingNodes = sourceNodes;

        // Nodes to remove
        // Get nodes
        List<Node> nodesToRemove = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));
        // Remove all nodes, which are not referenced
        Predicate<Node> containsNotNodePredicate = node -> remainingNodes.contains(node);
        nodesToRemove.removeIf(containsNotNodePredicate);
        return nodesToRemove;
    }

    /**
     * Removes all nodes and edges from graph other than those in edges list.
     *
     * @param graph Graph on which removal will be applied
     * @param edges Retaining edges
     */
    public void retainEdges(Graph graph, List<Edge> edges) {
        if (!edges.isEmpty() && edges.size() > 0 ){
            // Remove nodes
            List<Node> nodesToRemove = getNodesToRemove(graph, edges);
            graph.removeAllNodes(nodesToRemove);

            // Remove edges
            List<Edge> remainingEdges = new ArrayList<Edge>(Arrays.asList(graph.getEdges().toArray()));
            remainingEdges.stream().filter(edge -> !edges.contains(edge)).forEach(edge -> graph.removeEdge(edge));
        } else {
            new IllegalEdgeNumberException();
        }
    }


    /**
     * Gets properties.
     *
     * @return Filter properties
     */
    public static FilterProperty[] getFilterProperties() {
        return filterProperties;
    }

    /**
     * Sets filter properties.
     *
     * @param filterProperties Filter properties to set
     */
    public static void setFilterProperties(FilterProperty[] filterProperties) {
        LinkPredictionFilter.filterProperties = filterProperties;
    }

    /**
     * Gets edges limit.
     *
     * @return Edges limit
     */
    public Integer getEdgesLimit() {
        return edgesLimit;
    }

    /**
     * Sets edges limit.
     *
     * @param edgesLimit Number of displayed predicted edges
     */
    public void setEdgesLimit(Integer edgesLimit) {
        this.edgesLimit = edgesLimit;
    }
}
