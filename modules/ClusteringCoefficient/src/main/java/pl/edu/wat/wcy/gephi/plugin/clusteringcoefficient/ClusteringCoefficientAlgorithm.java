package pl.edu.wat.wcy.gephi.plugin.clusteringcoefficient;

import org.gephi.graph.api.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Krystian on 28.04.2018.
 */
public class ClusteringCoefficientAlgorithm {

    private static final String COF_ATTR = "Clustering Coefficient";
    private final Graph graph;
    private final NodeIterable allNodes;
    private String report = "";

    public ClusteringCoefficientAlgorithm(GraphModel graphModel) {
        if (graphModel.getNodeTable().hasColumn(COF_ATTR)) {
            final Column column = graphModel.getNodeTable().getColumn(COF_ATTR);
            for (Node node : graphModel.getGraphVisible().getNodes()) {
                node.setAttribute(column, null);
            }
        } else {
            graphModel.getNodeTable().addColumn(COF_ATTR, Double.class);
        }
        this.graph = graphModel.getGraphVisible();
        // The neighbors are read in the visible view, which fails for the nodes that are not part of it
        allNodes = this.graph.getNodes();
    }

    public String calculate() {
        allNodes.forEach(node -> {
            if (ClusteringCoefficientStatistic.cancel) {
                return;
            }
            final List<Node> neighbors = (List<Node>) graph.getNeighbors(node).toCollection();
            neighbors.remove(node);
            final int neighborsCount = neighbors.size();
            final long connectionsBetweenNeighbors = getConnectionCountBetweenNeighbors(neighbors);
            final double c;
            if (neighborsCount <= 1) {
                c = 0f;
            } else {
                c = (2f * connectionsBetweenNeighbors) / (neighborsCount * (neighborsCount - 1));
            }
            node.setAttribute(COF_ATTR, c);
        });

        final double[] cSum = {0f};
        final AtomicLong size = new AtomicLong();
        allNodes.forEach(node -> {
            // Nodes left out when the calculation is cancelled have no value
            final Double value = (Double) node.getAttribute(COF_ATTR);
            double c = value == null ? 0 : value;
            cSum[0] += c;
            report += "Node " + node.getId() + ": C = " + String.valueOf(c) + "\n";
            size.getAndIncrement();
        });
        report += "General C = " + String.valueOf(cSum[0] / size.get());

        return report;
    }

    private long getConnectionCountBetweenNeighbors(List<Node> neighbours) {
        long counter = 0;
        for (int i = 0; i < neighbours.size(); i++) {
            for (int j = i + 1; j < neighbours.size(); j++) {
                if (graph.getEdge(neighbours.get(i), neighbours.get(j)) != null
                        || graph.getEdge(neighbours.get(j), neighbours.get(i)) != null) {
                    counter++;
                }
            }
        }
        return counter;
    }
}
