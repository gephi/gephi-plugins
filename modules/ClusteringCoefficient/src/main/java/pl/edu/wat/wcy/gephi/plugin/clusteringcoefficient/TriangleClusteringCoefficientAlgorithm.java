package pl.edu.wat.wcy.gephi.plugin.clusteringcoefficient;

import java.util.ArrayList;
import org.gephi.graph.api.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Krystian on 28.04.2018.
 */
public class TriangleClusteringCoefficientAlgorithm {

    private String report = "";
    private final Graph graph;
    private final List<Triangle> triangles;

    public TriangleClusteringCoefficientAlgorithm(GraphModel graphModel) {
        this.graph = graphModel.getGraphVisible();
        triangles = new ArrayList<>();
    }

    public String calculate() {
        final long triangleCount = getTriangleCountFromGraph();

        report += " Number of triangles: " + triangleCount + "\n";

        final long quantityPathsOfLenght2 = countPaths();
        report += "Number of paths (Length 2): " + quantityPathsOfLenght2 + "\n";

        final double tcc = 3f * triangleCount / quantityPathsOfLenght2;

        report += "Value of Clustering Coefficient: " + tcc + "\n";

        return report;
    }

    public long getTriangleCountFromGraph() {
        graph.getNodes().forEach(node -> {
            if (ClusteringCoefficientStatistic.cancel) {
                return;
            }
            List<Node> neighbors = (List<Node>) graph.getNeighbors(node).toCollection();
            neighbors.remove(node);
            addTrianglesFromNode(neighbors, node);
        });

        return triangles.size() / 3;
    }

    private List<Triangle> addTrianglesFromNode(List<Node> neighbours, Node baseNode) {
        final List<Triangle> triangleList = new ArrayList<>();

        for (int i = 0; i < neighbours.size(); i++) {
            for (int j = i + 1; j < neighbours.size(); j++) {
                if (graph.getEdge(neighbours.get(i), neighbours.get(j)) != null
                        || graph.getEdge(neighbours.get(j), neighbours.get(i)) != null) {
                    triangles.add(new Triangle(Arrays.asList(baseNode, neighbours.get(i), neighbours.get(j))));
                }
            }
        }
        return triangleList;
    }

    private long countPaths() {
        final long[] pathQuantity = {0};
        graph.getNodes().forEach(node -> {
            List<Node> neighbors = (List<Node>) graph.getNeighbors(node).toCollection();
            neighbors.remove(node);
            pathQuantity[0] += countPathsBetweenNeighbors(neighbors);
        });

        return pathQuantity[0];
    }

    private long countPathsBetweenNeighbors(List<Node> neighbors) {
        long n = neighbors.size();
        long pathsQuantity = n * (n - 1) / 2;
        return pathsQuantity;
    }

    public String getReport() {
        return report;
    }
}
