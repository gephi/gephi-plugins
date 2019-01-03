package jaknowakowski.gephi.plugin.dbscan.core.metrics;

import org.gephi.graph.api.Node;

public interface DistanceMetric {

    public double getDistance(Node first, Node second);
}
