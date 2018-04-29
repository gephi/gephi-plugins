package Metric;

import org.gephi.graph.api.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Krystian on 28.04.2018.
 */
public class TriangleClusteringCoefficientAlgorithm {

    private String report = "";
    private Graph graph;
    private NodeIterable allNodes;
    private EdgeIterable allEdges;
    private List<Triangle> triangles;


    public TriangleClusteringCoefficientAlgorithm(GraphModel graphModel) {
        this.allNodes = graphModel.getGraph().getNodes();
        this.allEdges = graphModel.getGraph().getEdges();
        this.graph = graphModel.getGraphVisible();
        triangles = new LinkedList<>();
    }

    public String calculate() {

        long triangleCount = getTriangleCountFromGraph();

        report += " Ilość trójkątów: " + triangleCount + "\n";

        long quantityPathsOfLenght2 = countPaths();
        report += "Ilość dróg o długości 2: " + quantityPathsOfLenght2 + "\n";

        double tcc = 3f * triangleCount / quantityPathsOfLenght2;

        report += "Wartość wsp klasteryzacji: " + tcc + "\n";

        return report;
    }


    public long getTriangleCountFromGraph() {

        allNodes.forEach(node -> {
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
        List<Triangle> triangleList = new LinkedList<>();

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
        allNodes.forEach(node -> {
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
