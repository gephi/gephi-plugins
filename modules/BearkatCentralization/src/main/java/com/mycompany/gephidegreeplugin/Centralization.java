package com.mycompany.gephidegreeplugin;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.spi.Statistics;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Centralization implements Statistics {

    private double degreeCentralization;
    private double weightedDegreeCentralization;
    private double inDegreeCentralization;
    private double outDegreeCentralization;
    private double betweennessCentralization;
    private double eigenvectorCentralization;
    private double closenessCentralization;
    private double harmonicClosenessCentralization;
    private boolean directed;

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph = graphModel.getGraph();
        directed = graphModel.isDirected();

        calculateDegreeCentralization(graphModel, graph);
        calculateWeightedDegreeCentralization(graphModel, graph);

        if (directed) {
            calculateInDegreeCentralization(graphModel);
            calculateOutDegreeCentralization(graphModel);
        }

        calculateBetweennessCentralization(graphModel, graph);
        calculateEigenvectorCentralization(graphModel, graph);
        calculateClosenessCentralization(graphModel, graph);
        calculateHarmonicClosenessCentralization(graphModel, graph);
    }

    private void calculateDegreeCentralization(GraphModel graphModel, Graph graph) {
        int n = graph.getNodeCount();

        Table nodeTable = graphModel.getNodeTable();
        Column col = nodeTable.getColumn("degree_centrality");

        if (col == null) {
            col = nodeTable.addColumn("degree_centrality", Double.class);
        }

        int maxDegree = 0;

        for (Node node : graph.getNodes()) {
            int degree = graph.getDegree(node);
            maxDegree = Math.max(maxDegree, degree);
            node.setAttribute(col, (double) degree);
        }

        double numerator = 0;

        for (Node node : graph.getNodes()) {
            int degree = graph.getDegree(node);
            numerator += maxDegree - degree;
        }

        double denominator = (n - 1.0) * (n - 2.0);
        degreeCentralization = denominator > 0 ? numerator / denominator : 0;
    }

    private void calculateWeightedDegreeCentralization(GraphModel graphModel, Graph graph) {
        int n = graph.getNodeCount();

        Table nodeTable = graphModel.getNodeTable();
        Column col = nodeTable.getColumn("weighted_degree_centrality");

        if (col == null) {
            col = nodeTable.addColumn("weighted_degree_centrality", Double.class);
        }

        double maxWeightedDegree = 0;

        for (Node node : graph.getNodes()) {
            double weightedDegree = 0;

            for (Edge edge : graph.getEdges(node)) {
                double weight = edge.getWeight();

                if (weight <= 0) {
                    weight = 1.0;
                }

                weightedDegree += weight;
            }

            node.setAttribute(col, weightedDegree);
            maxWeightedDegree = Math.max(maxWeightedDegree, weightedDegree);
        }

        double numerator = 0;

        for (Node node : graph.getNodes()) {
            Double weightedDegree = (Double) node.getAttribute(col);

            if (weightedDegree == null) {
                weightedDegree = 0.0;
            }

            numerator += maxWeightedDegree - weightedDegree;
        }

        double denominator = maxWeightedDegree * (n - 1.0);
        weightedDegreeCentralization = denominator > 0 ? numerator / denominator : 0;
    }

    private void calculateInDegreeCentralization(GraphModel graphModel) {
        DirectedGraph graph = graphModel.getDirectedGraph();
        int n = graph.getNodeCount();

        Table nodeTable = graphModel.getNodeTable();
        Column col = nodeTable.getColumn("in_degree_centrality");

        if (col == null) {
            col = nodeTable.addColumn("in_degree_centrality", Double.class);
        }

        int maxInDegree = 0;

        for (Node node : graph.getNodes()) {
            int inDegree = graph.getInDegree(node);
            maxInDegree = Math.max(maxInDegree, inDegree);
            node.setAttribute(col, (double) inDegree);
        }

        double numerator = 0;

        for (Node node : graph.getNodes()) {
            int inDegree = graph.getInDegree(node);
            numerator += maxInDegree - inDegree;
        }

        double denominator = (n - 1.0) * (n - 1.0);
        inDegreeCentralization = denominator > 0 ? numerator / denominator : 0;
    }

    private void calculateOutDegreeCentralization(GraphModel graphModel) {
        DirectedGraph graph = graphModel.getDirectedGraph();
        int n = graph.getNodeCount();

        Table nodeTable = graphModel.getNodeTable();
        Column col = nodeTable.getColumn("out_degree_centrality");

        if (col == null) {
            col = nodeTable.addColumn("out_degree_centrality", Double.class);
        }

        int maxOutDegree = 0;

        for (Node node : graph.getNodes()) {
            int outDegree = graph.getOutDegree(node);
            maxOutDegree = Math.max(maxOutDegree, outDegree);
            node.setAttribute(col, (double) outDegree);
        }

        double numerator = 0;

        for (Node node : graph.getNodes()) {
            int outDegree = graph.getOutDegree(node);
            numerator += maxOutDegree - outDegree;
        }

        double denominator = (n - 1.0) * (n - 1.0);
        outDegreeCentralization = denominator > 0 ? numerator / denominator : 0;
    }

    private void calculateBetweennessCentralization(GraphModel graphModel, Graph graph) {
        GraphDistance distance = new GraphDistance();
        distance.setDirected(graphModel.isDirected());
        distance.execute(graphModel);

        int n = graph.getNodeCount();

        Table nodeTable = graphModel.getNodeTable();
        Column betweenCol = nodeTable.getColumn(GraphDistance.BETWEENNESS);
        Column myCol = nodeTable.getColumn("betweenness_centrality");

        if (myCol == null) {
            myCol = nodeTable.addColumn("betweenness_centrality", Double.class);
        }

        double maxBetweenness = 0;

        for (Node node : graph.getNodes()) {
            Double value = (Double) node.getAttribute(betweenCol);

            if (value == null) {
                value = 0.0;
            }

            node.setAttribute(myCol, value);
            maxBetweenness = Math.max(maxBetweenness, value);
        }

        double numerator = 0;

        for (Node node : graph.getNodes()) {
            Double value = (Double) node.getAttribute(myCol);

            if (value == null) {
                value = 0.0;
            }

            numerator += maxBetweenness - value;
        }

        double denominator;

if (n > 2) {
    if (graphModel.isDirected()) {
        denominator = (n - 1.0) * (n - 1.0) * (n - 2.0);
    } else {
        denominator = ((n - 1.0) * (n - 1.0) * (n - 2.0)) / 2.0;
    }
} else {
    denominator = 1;
}
        betweennessCentralization = denominator > 0 ? numerator / denominator : 0;
    }

    private void calculateEigenvectorCentralization(GraphModel graphModel, Graph graph) {
        EigenvectorCentrality eigenvector = new EigenvectorCentrality();
        eigenvector.execute(graphModel);

        int n = graph.getNodeCount();

        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(EigenvectorCentrality.EIGENVECTOR);
        Column myCol = nodeTable.getColumn("eigenvector_centrality");

        if (myCol == null) {
            myCol = nodeTable.addColumn("eigenvector_centrality", Double.class);
        }

        double maxEigenvector = 0;

        for (Node node : graph.getNodes()) {
            Double value = (Double) node.getAttribute(eigenCol);

            if (value == null) {
                value = 0.0;
            }

            node.setAttribute(myCol, value);
            maxEigenvector = Math.max(maxEigenvector, value);
        }

        double numerator = 0;

        for (Node node : graph.getNodes()) {
            Double value = (Double) node.getAttribute(myCol);

            if (value == null) {
                value = 0.0;
            }

            numerator += maxEigenvector - value;
        }

        double denominator = n - 1.0;
        eigenvectorCentralization = denominator > 0 ? numerator / denominator : 0;
    }

    private void calculateClosenessCentralization(GraphModel graphModel, Graph graph) {
        int n = graph.getNodeCount();

        Table nodeTable = graphModel.getNodeTable();
        Column closenessCol = nodeTable.getColumn(GraphDistance.CLOSENESS);
        Column myCol = nodeTable.getColumn("closeness_centrality");

        if (myCol == null) {
            myCol = nodeTable.addColumn("closeness_centrality", Double.class);
        }

        double maxCloseness = 0;

        for (Node node : graph.getNodes()) {
            Double value = (Double) node.getAttribute(closenessCol);

            if (value == null) {
                value = 0.0;
            }

            node.setAttribute(myCol, value);
            maxCloseness = Math.max(maxCloseness, value);
        }

        double numerator = 0;

        for (Node node : graph.getNodes()) {
            Double value = (Double) node.getAttribute(myCol);

            if (value == null) {
                value = 0.0;
            }

            numerator += maxCloseness - value;
        }

        double denominator = n - 2.0;
        closenessCentralization = denominator > 0 ? numerator / denominator : 0;
    }
    
    private void calculateHarmonicClosenessCentralization(GraphModel graphModel, Graph graph) {
    int n = graph.getNodeCount();

    Table nodeTable = graphModel.getNodeTable();
    Column col = nodeTable.getColumn("harmonic_closeness_centrality");

    if (col == null) {
        col = nodeTable.addColumn("harmonic_closeness_centrality", Double.class);
    }

    double maxHarmonic = 0;

    for (Node node : graph.getNodes()) {
        Map<Node, Integer> distances = bfsDistances(graphModel, graph, node);
        double harmonic = 0;

        for (Node other : graph.getNodes()) {
            if (!node.equals(other)) {
                Integer distance = distances.get(other);

                if (distance != null && distance > 0) {
                    harmonic += 1.0 / distance;
                }
            }
        }

        node.setAttribute(col, harmonic);
        maxHarmonic = Math.max(maxHarmonic, harmonic);
    }

    double numerator = 0;

    for (Node node : graph.getNodes()) {
        Double harmonic = (Double) node.getAttribute(col);

        if (harmonic == null) {
            harmonic = 0.0;
        }

        numerator += maxHarmonic - harmonic;
    }

    double denominator;

    if (n > 2) {
        if (graphModel.isDirected()) {
            denominator = (n - 1.0) * (n - 1.0);
        } else {
            denominator = ((n - 1.0) * (n - 2.0)) / 2.0;
        }
    } else {
        denominator = 1;
    }

    harmonicClosenessCentralization = denominator > 0 ? numerator / denominator : 0;
}

private Map<Node, Integer> bfsDistances(GraphModel graphModel, Graph graph, Node start) {
    Map<Node, Integer> distances = new HashMap<>();
    Queue<Node> queue = new LinkedList<>();

    distances.put(start, 0);
    queue.add(start);

    while (!queue.isEmpty()) {
        Node current = queue.poll();
        int currentDistance = distances.get(current);

        for (Node neighbor : graph.getNeighbors(current)) {
            if (!distances.containsKey(neighbor)) {
                distances.put(neighbor, currentDistance + 1);
                queue.add(neighbor);
            }
        }
    }

    return distances;
}

    @Override
    public String getReport() {
        String directedText;

        if (directed) {
            directedText = "<p><b>In-Degree Centralization:</b> " + inDegreeCentralization + "</p>"
                    + "<p><b>Out-Degree Centralization:</b> " + outDegreeCentralization + "</p>";
        } else {
            directedText = "<p><b>In-Degree Centralization:</b> Not applicable for undirected graphs</p>"
                    + "<p><b>Out-Degree Centralization:</b> Not applicable for undirected graphs</p>";
        }

        return "<html><body>"
                + "<h1>Bearkat Centralization Report</h1>"
               + "<p><b>Degree Centralization:</b> " + degreeCentralization + "</p>"
+ "<p><b>Weighted Degree Centralization:</b> " + weightedDegreeCentralization + "</p>"
+ directedText
+ "<p><b>Betweenness Centralization:</b> " + betweennessCentralization + "</p>"
+ "<p><b>Eigenvector Centralization:</b> " + eigenvectorCentralization + "</p>"
+ "<p><b>Closeness Centralization:</b> " + closenessCentralization + "</p>"
+ "<p><b>Harmonic Closeness Centralization:</b> " + harmonicClosenessCentralization + "</p>"

+ "<hr>"
+ "<h2>Notes and Interpretation</h2>"

+ "<p><b>Closeness Centralization:</b> "
+ "Standard closeness centralization may be affected by disconnected graphs or isolates because shortest-path distances become undefined. "
+ "The closeness values reported by this plugin rely on Gephi's underlying implementation. "
+ "For disconnected networks, Harmonic Closeness Centralization is generally the preferred measure because unreachable nodes contribute 0 through reciprocal distances.</p>"

+ "<p><b>Eigenvector Centralization:</b> "
+ "Eigenvector centralization may vary across software packages because implementations can differ in eigenvector normalization methods and graph-level centralization normalization. "
+ "Results should therefore be interpreted with attention to the software and methodology used.</p>"

+ "<hr>"
+ "<p>Node-level values for every metric are available in <b>Data Laboratory → Nodes</b>.</p>"
                + "<hr>"
+ "<p><b>Citations:</b></p>"
+ "<p>Freeman, Linton C. “Centrality in Social Networks Conceptual Clarification.” "
+ "<i>Social Networks</i> 1, no. 3 (1978): 215–39.</p>"
+ "<p>Borgatti, Stephen P. “Identifying Sets of Key Players in a Social Network.” "
+ "<i>Computational & Mathematical Organization Theory</i> 12 (2006): 21–34.</p>"
+ "<p>Gil, J. and Schmidt, S. (1996). “The Origin of the Mexican Network of Power.” "
+ "<i>Proceedings of the International Social Network Conference</i>, Charleston, SC, 22–25.</p>"
+ "<p><b>Programmed by Braell Dotson and Dr. Nate Jones at Sam Houston State University.</b></p>"
                + "</body></html>";
    }
}