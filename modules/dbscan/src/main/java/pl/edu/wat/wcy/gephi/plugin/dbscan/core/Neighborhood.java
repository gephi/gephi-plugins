package pl.edu.wat.wcy.gephi.plugin.dbscan.core;

import pl.edu.wat.wcy.gephi.plugin.dbscan.core.metrics.DistanceMetric;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.edu.wat.wcy.gephi.plugin.dbscan.util.Utils.spliteratorToSet;

public class Neighborhood {

    private final DistanceMetric distanceMetric;
    private int radius = 0;

    public Neighborhood(DistanceMetric distanceMetric) {
        this.distanceMetric = distanceMetric;
    }

    public List<Node> getNeighborsList(Node node, NodeIterable allNodes) {
        return getNeighborsList(node, spliteratorToSet(allNodes.spliterator()));
    }

    public Set<Node> getNeighborsSet(Node node, Collection<Node> allNodes) {
        return getNeighbors(node, allNodes).collect(Collectors.toSet());
    }

    public List<Node> getNeighborsList(Node node, Collection<Node> allNodes) {
        return getNeighbors(node, allNodes).collect(Collectors.toList());
    }

    public Stream<Node> getNeighbors(Node node, Collection<Node> allNodes) {
        return allNodes.stream().filter(n -> isNeighbor(node, n));
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
