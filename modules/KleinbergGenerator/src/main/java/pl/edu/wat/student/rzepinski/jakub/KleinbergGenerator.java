package pl.edu.wat.student.rzepinski.jakub;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.util.Objects;
import java.util.stream.Stream;

@ServiceProvider(service = Generator.class)
public class KleinbergGenerator implements Generator {

    public static final int DEFAULT_GRID_SIZE = 10;
    public static final int DEFAULT_CLUSTERING_COEFFICIENT = 2;
    public static final boolean DEFAULT_TORUS_MODE = false;

    private static final int LAYOUT_SPACING = 50;

    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private int clusteringCoefficient = DEFAULT_CLUSTERING_COEFFICIENT;
    private int gridSize = DEFAULT_GRID_SIZE;
    private boolean torusMode = DEFAULT_TORUS_MODE;

    @Override
    public boolean cancel() {
        this.cancelled = true;
        return true;
    }

    @Override
    public void generate(ContainerLoader containerLoader) {
        NodeDraft[][] nodes = createNodes(containerLoader);
        createBasicEdges(containerLoader, nodes);

        //TODO additional edges
    }

    private void createBasicEdges(ContainerLoader containerLoader, NodeDraft[][] nodes) {
        Utils.doubleIntStream(0, gridSize, (x, y) -> {
            Stream.of(getNeighborX(nodes, x, y), getNeighborY(nodes, x, y))
                    .filter(Objects::nonNull)
                    .forEach(target -> {
                        EdgeDraft edge = containerLoader.factory().newEdgeDraft();
                        edge.setDirection(EdgeDirection.UNDIRECTED);
                        edge.setSource(nodes[x][y]);
                        edge.setTarget(target);
                        containerLoader.addEdge(edge);
                    });
        });
    }

    private NodeDraft getNeighborY(NodeDraft[][] nodes, Integer x, Integer y) {
        if (y + 1 < gridSize) {
            return nodes[x][y + 1];
        } else if (torusMode) {
            return nodes[x][0];
        }
        return null;
    }

    private NodeDraft getNeighborX(NodeDraft[][] nodes, Integer x, Integer y) {
        if (x + 1 < gridSize) {
            return nodes[x + 1][y];
        } else if (torusMode) {
            return nodes[0][y];
        }
        return null;
    }

    private NodeDraft[][] createNodes(ContainerLoader containerLoader) {
        NodeDraft[][] nodes = new NodeDraft[gridSize][gridSize];
        Utils.doubleIntStream(0, gridSize, (x, y) -> {
                    NodeDraft node = containerLoader.factory().newNodeDraft(x + ", " + y);
                    node.setX(x * LAYOUT_SPACING);
                    node.setY(y * LAYOUT_SPACING);
                    nodes[x][y] = node;
                    containerLoader.addNode(node);
                }
        );
        return nodes;
    }

    public int getClusteringCoefficient() {
        return clusteringCoefficient;
    }

    public void setClusteringCoefficient(int clusteringCoefficient) {
        this.clusteringCoefficient = clusteringCoefficient;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    public String getName() {
        return "Kleinberg model";
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(KleinbergGeneratorUI.class);
    }

    public boolean isTorusMode() {
        return torusMode;
    }

    public void setTorusMode(boolean torusMode) {
        this.torusMode = torusMode;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
