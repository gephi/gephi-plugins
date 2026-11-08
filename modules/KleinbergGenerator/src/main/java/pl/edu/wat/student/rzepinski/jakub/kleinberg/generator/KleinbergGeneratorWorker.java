package pl.edu.wat.student.rzepinski.jakub.kleinberg.generator;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import pl.edu.wat.student.rzepinski.jakub.util.IntPairs;
import pl.edu.wat.student.rzepinski.jakub.util.ManhattanDistanceCalculator;
import pl.edu.wat.student.rzepinski.jakub.util.Utils;

import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Math.pow;
import static java.lang.Math.random;
import static pl.edu.wat.student.rzepinski.jakub.util.Utils.getNodeId;

public class KleinbergGeneratorWorker implements Runnable {
    private final KleinbergGenerator generator;
    private final int clusteringCoefficient;
    private final int gridSize;
    private final boolean torusMode;
    private final ContainerLoader containerLoader;
    private final NodeDraft[][] nodes;
    private final ManhattanDistanceCalculator distanceCalculator;

    public KleinbergGeneratorWorker(KleinbergGenerator generator, ContainerLoader containerLoader) {
        this.generator = generator;
        this.clusteringCoefficient = generator.getClusteringCoefficient();
        this.gridSize = generator.getGridSize();
        this.torusMode = generator.isTorusMode();
        this.containerLoader = containerLoader;
        this.nodes = new NodeDraft[gridSize][gridSize];
        this.distanceCalculator = new ManhattanDistanceCalculator(gridSize, torusMode);
    }

    private void addEdge(NodeDraft nodeSource, NodeDraft nodeTarget) {
        EdgeDraft edge = containerLoader.factory().newEdgeDraft();
        edge.setDirection(EdgeDirection.UNDIRECTED);
        edge.setSource(nodeSource);
        edge.setTarget(nodeTarget);
        containerLoader.addEdge(edge);
    }

    private void addNode(Integer x, Integer y) {
        NodeDraft node = containerLoader.factory().newNodeDraft(getNodeId(x, y));
        Utils.setNodePosition(node,x,y);
        containerLoader.addNode(node);
        nodes[x][y] = node;
    }

    private double calculateNormalizationConstant(int x, int y) {
        return IntPairs.range(0, gridSize)
                .map((u, v) -> distanceCalculator.calculate(x, y, u, v))
                .filter(Utils::isPositive)
                .mapToDouble(t -> pow(t, -clusteringCoefficient))
                .sum();
    }

    private void createLongRangeEdges() {
        IntPairs.range(0, gridSize).forEach((x, y) -> {
            if (generator.isCancelled()) {
                return;
            }
            Progress.progress(generator.getProgressTicket());
            createLongRangeEdges(x, y);
        });
    }

    private void createLongRangeEdges(Integer x, Integer y) {
        double normalizationConstant = calculateNormalizationConstant(x, y);
        IntPairs.range(0, gridSize).forEach((u, v) -> {
            int distance = distanceCalculator.calculate(x, y, u, v);
            if (distance > 1) {
                double probability = pow(distance, -clusteringCoefficient) / normalizationConstant;
                boolean addEdge = random() <= probability;
                if (addEdge && !edgeExists(x, y, u, v)) {
                    addEdge(nodes[x][y], nodes[u][v]);
                }
            }
        });
    }

    private void createNodes() {
        IntPairs.range(0, gridSize)
                .forEach(this::addNode);
    }

    private void createShortRangeEdges() {
        IntPairs.range(0, gridSize).forEach((x, y) -> {
            Stream.of(getVerticalNeighbor(x, y), getHorizontalNeighbor(x, y))
                    .filter(Objects::nonNull)
                    .forEach(target -> addEdge(nodes[x][y], target));
        });
    }

    private boolean edgeExists(Integer x, Integer y, Integer u, Integer v) {
        return containerLoader.edgeExists(getNodeId(x, y), getNodeId(u, v));
    }

    private NodeDraft getHorizontalNeighbor(Integer x, Integer y) {
        if (y + 1 < gridSize) {
            return nodes[x][y + 1];
        } else if (torusMode) {
            return nodes[x][0];
        }
        return null;
    }

    private NodeDraft getVerticalNeighbor(Integer x, Integer y) {
        if (x + 1 < gridSize) {
            return nodes[x + 1][y];
        } else if (torusMode) {
            return nodes[0][y];
        }
        return null;
    }

    @Override
    public void run() {
        createNodes();
        createShortRangeEdges();
        createLongRangeEdges();
    }

}
