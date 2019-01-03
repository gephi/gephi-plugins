package pl.edu.wat.wcy.gephi.plugin.dbscan.core.metrics;

import org.gephi.graph.api.Node;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class SimpleDistanceMetric implements DistanceMetric {

    @Override
    public double getDistance(Node first, Node second) {
        return sqrt(pow((second.x() - first.x()), 2) + pow((second.y() - first.y()), 2) + pow((second.z() - first.z()), 2));
    }
}
