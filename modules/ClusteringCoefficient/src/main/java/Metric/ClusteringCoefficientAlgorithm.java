package Metric;

import org.gephi.graph.api.*;
import sun.rmi.runtime.Log;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by Krystian on 28.04.2018.
 */
public class ClusteringCoefficientAlgorithm {

    private static final String COF_ATTR = "Clustering Coefficient";
    private Graph graph;
    private NodeIterable allNodes;
    private String report = "";

    public ClusteringCoefficientAlgorithm(GraphModel graphModel) {
        if (graphModel.getNodeTable().hasColumn(COF_ATTR)) {
            graphModel.getGraphVisible().getNodes().forEach(Element::clearAttributes);
        } else {
            graphModel.getNodeTable().addColumn(COF_ATTR, Double.class);
        }
        allNodes = graphModel.getGraph().getNodes();
        this.graph = graphModel.getGraphVisible();
    }

    public String calculate(){

        allNodes.forEach(node -> {
            if (ClusteringCoefficientStatistic.cancel) {
                return;
            }
            List<Node> neighbors = (List<Node>) graph.getNeighbors(node).toCollection();
            neighbors.remove(node);
            int neighborsCount = neighbors.size();
            long connectionsBetweenNeighbors = getConnectionCountBetweenNeighbors(neighbors);
            double c;
            if (neighborsCount == 1) {
                c = 0f;
            } else if (neighborsCount == 0) {
                c = 0f;
            } else {
                c = (2f * connectionsBetweenNeighbors) / (neighborsCount * (neighborsCount - 1));
            }
            node.setAttribute(COF_ATTR, c);
        });

        final double[] cSum = {0f};
        AtomicLong size = new AtomicLong();
        allNodes.forEach(node -> {
            double c = (Double) node.getAttribute(COF_ATTR);
            cSum[0] += c;
            report += node.getId() + " C = " + String.valueOf(c) + "\n";
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
