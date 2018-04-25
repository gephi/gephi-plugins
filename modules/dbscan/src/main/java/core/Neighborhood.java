package core;

import core.metrics.DistanceMetric;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;

import java.util.*;
import java.util.function.Consumer;

import static util.Utils.spliteratorToSet;

public class Neighborhood {

    private final DistanceMetric distanceMetric;
    private int radius = 0;

    public Neighborhood(DistanceMetric distanceMetric) {
        this.distanceMetric = distanceMetric;
    }

    public List<Node> getNeighborsList(Node node, NodeIterable allNodes) {
        return getNeighborsList(node, spliteratorToSet(allNodes.spliterator()));
    }

    public Set<Node> getNeighbors(Node node, Collection<Node> allNodes) {
        Set<Node> nodes = new HashSet<>();
        doForAllNeighbors(node, allNodes, nodes::add);
        return nodes;
    }

    public List<Node> getNeighborsList(Node node, Collection<Node> allNodes) {
        List<Node> nodes = new ArrayList<>();
        doForAllNeighbors(node, allNodes, nodes::add);
        return nodes;
    }

    public void doForAllNeighbors(Node node, Collection<Node> allNodes, Consumer<Node> consumer) {
        allNodes.stream().filter(n -> isNeighbor(node, n)).forEach(consumer);
    }

    public boolean isNeighbor(Node node, Node suspect) {
        return (!node.equals(suspect)) && (calculateDistance(node, suspect) <= radius);
    }

    public double calculateDistance(Node first, Node second) {
        return distanceMetric.getDistance(first, second);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
