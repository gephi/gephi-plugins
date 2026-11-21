package org.robertpalermo.distancebackbonetoolbox;

import org.gephi.graph.api.*;
import java.util.*;


/* 

Something important here to note is that the data that is being input into
Gephi (to have its backbone computed) must have the edge weight it is going to
use labeled in all lowercase letters as "weight".

*/


public class DistanceBackboneToolboxProcessor {
    private final Graph graph;

    public DistanceBackboneToolboxProcessor(GraphModel graphModel) {
        this.graph = graphModel.getUndirectedGraph();
    }
    
    public void computeUltrametricBackbone() {
        removeSelfLoops();
        List<Node> sortedNodes = getNodesSortedByDegree();

        for (Node u : sortedNodes) {
            Map<Node, Double> distances = ultrametricDijkstra(u);

            List<Edge> edges = new ArrayList<>();
            for (Edge edge : graph.getEdges(u)) {
                edges.add(edge);
            }

            for (Edge edge : edges) {
                Node v = edge.getSource().equals(u) ? edge.getTarget() : edge.getSource();
                double directWeight = edge.getWeight();

                if (distances.containsKey(v) && distances.get(v) < directWeight) {
                    graph.removeEdge(edge); 
                }
            }
        }
    }

    
    public void computeMetricBackbone() {
        removeSelfLoops();
        List<Node> sortedNodes = getNodesSortedByDegree();

        for (Node u : sortedNodes) {
            Map<Node, Double> distances = metricDijkstra(u);
            removeNonEssentialEdges(u, distances);
        }
    }


    private void removeSelfLoops() {
        List<Edge> toRemove = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.getSource().equals(edge.getTarget())) {
                toRemove.add(edge);
            }
        }
        for (Edge edge : toRemove) {
            graph.removeEdge(edge);
        }
    }


    private List<Node> getNodesSortedByDegree() {
        List<Node> nodes = new ArrayList<>();
        for (Node node : graph.getNodes()) {
            nodes.add(node);
        }
        nodes.sort(Comparator.comparingDouble(graph::getDegree));
        return nodes;
    }


    private void removeNonEssentialEdges(Node u, Map<Node, Double> distances) {
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : graph.getEdges(u)) {
            edges.add(edge);
        }

        for (Edge edge : edges) {
            Node v = edge.getSource().equals(u) ? edge.getTarget() : edge.getSource();
            double directWeight = edge.getWeight();

            if (distances.containsKey(v) && distances.get(v) < directWeight) {
                graph.removeEdge(edge);
            }
        }
    }

    
    // Only works for metric backbone.
    private Map<Node, Double> metricDijkstra(Node source) {
        Map<Node, Double> dist = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        dist.put(source, 0.0);
        queue.add(new NodeDistance(source, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            Node u = current.node;
            double currentDist = current.distance;

            if (currentDist > dist.get(u)) continue;

            for (Edge edge : graph.getEdges(u)) {
                Node v = edge.getSource().equals(u) ? edge.getTarget() : edge.getSource();
                double weight = edge.getWeight();
                double alt = dist.get(u) + weight;

                if (!dist.containsKey(v) || alt < dist.get(v)) {
                    dist.put(v, alt);
                    queue.add(new NodeDistance(v, alt));
                }
            }
        }

        return dist;
    }
    
    
    private Map<Node, Double> ultrametricDijkstra(Node source) {
        Map<Node, Double> dist = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        dist.put(source, 0.0);
        queue.add(new NodeDistance(source, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            Node u = current.node;
            double currentDist = current.distance;

            if (currentDist > dist.get(u)) continue;

            for (Edge edge : graph.getEdges(u)) {
                Node v = edge.getSource().equals(u) ? edge.getTarget() : edge.getSource();
                double weight = edge.getWeight();
                double alt = Math.max(dist.get(u), weight); // key difference for ultrametric

                if (!dist.containsKey(v) || alt < dist.get(v)) {
                    dist.put(v, alt);
                    queue.add(new NodeDistance(v, alt));
                }
            }
        }

        return dist;
    }

    
    private static class NodeDistance {
        Node node;
        double distance;

        NodeDistance(Node node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}

