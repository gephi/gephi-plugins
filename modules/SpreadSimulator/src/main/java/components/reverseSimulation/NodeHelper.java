package components.reverseSimulation;

import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeHelper {

    public static int getNodeDegree(Node node, Graph graph) {
        return graph.getDegree(node);
    }

    public static int getDistanceToClosestNode(Node source, List<Node> targets, Graph graph) {
        return targets.stream()
                .map(target -> getDistanceFromTargetToSource(source, target, graph))
                .sorted()
                .collect(Collectors.toList())
                .get(0);
    }

    public static int getDistanceToFarthestNode(Node source, List<Node> targets, Graph graph) {
        List<Integer> distanceList = targets.stream()
                .map(target -> getDistanceFromTargetToSource(source, target, graph))
                .sorted()
                .collect(Collectors.toList());
        return distanceList.get(distanceList.size() -1);
    }

    public static double getAvgDistanceToNodes(Node source, List<Node> targets, Graph graph) {
        return targets.stream()
                .map(target -> getDistanceFromTargetToSource(source, target, graph))
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    private static int getDistanceFromTargetToSource(Node source, Node target, Graph graph) {
        DijkstraShortestPathAlgorithm dijkstra = new DijkstraShortestPathAlgorithm(graph, source);
        dijkstra.compute();
        return dijkstra.getDistances()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getStoreId() == target.getStoreId())
                .findFirst().map(Map.Entry::getValue)
                .orElse((double) -1)
                .intValue();
    }

    private static boolean edgeExists(Node source, Node target, Graph graph) {
        return Arrays.stream(graph.getEdges().toArray())
                .anyMatch(edge -> edge.getSource().equals(source) && edge.getTarget().equals(target) ||
                        edge.getSource().equals(target) && edge.getTarget().equals(source));
    }

    public static Column getBetweeneesColumn(Graph graph) {
        Node[] nodes = graph.getNodes().toArray();
        GraphDistance distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graph);
        GraphModel attributeModel = graph.getModel();
        return attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
    }

    public static double getNodeCloseness(Node source, Graph graph) {
        DijkstraShortestPathAlgorithm dijkstra = new DijkstraShortestPathAlgorithm(graph, source);
        int count = (int) Arrays.stream(graph.getNodes().toArray()).count();
        dijkstra.compute();
        return 1.0 / (dijkstra.getDistances().values().stream().mapToDouble(Double::doubleValue).sum() * count);
    }
}