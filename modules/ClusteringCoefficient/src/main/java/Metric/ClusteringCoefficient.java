package Metric;

import org.gephi.graph.api.*;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ClusteringCoefficient implements Statistics, LongTask {

    private static final String COF_ATTR = "C";

    private String report = "";
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private Graph graph;

    public void execute(GraphModel graphModel) {
        if (graphModel.getNodeTable().hasColumn(COF_ATTR)) {
            graphModel.getGraphVisible().getNodes().forEach(Element::clearAttributes);
        } else {
            graphModel.getNodeTable().addColumn(COF_ATTR, Double.class);
        }

        graph = graphModel.getGraphVisible();
        NodeIterable allNodes = graphModel.getGraph().getNodes();
        allNodes.forEach(node -> {
            if (cancel) {
                return;
            }
            List<Node> neighbors = getNeighbors(node);
            neighbors.remove(node);
            int neighborsCount = neighbors.size();
            long connectionsBetweenNeighbors = getConnectionCountBetweenNeighbors(neighbors);
            double c;
            if (neighborsCount == 1) {
                c = 1f;
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

    private List<Node> getNeighbors(Node node) {
        return StreamSupport.stream(graph.getEdges(node).spliterator(), false)
                .map(Edge::getTarget).distinct().collect(Collectors.toList());
    }

    public String getReport() {
        return report;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}