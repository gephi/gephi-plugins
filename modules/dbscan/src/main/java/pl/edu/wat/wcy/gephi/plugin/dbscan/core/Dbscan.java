package pl.edu.wat.wcy.gephi.plugin.dbscan.core;

import pl.edu.wat.wcy.gephi.plugin.dbscan.core.metrics.DistanceMetric;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

import java.text.SimpleDateFormat;
import java.util.*;
import org.gephi.graph.api.Column;

import static pl.edu.wat.wcy.gephi.plugin.dbscan.core.Labels.*;
import static pl.edu.wat.wcy.gephi.plugin.dbscan.util.HtmlUtils.*;

/**
 * http://wsinf.edu.pl/assets/img/pdf/Zeszyty%20naukowe/vol.13/art03%20(3).pdf
 */
public class Dbscan implements Statistics, LongTask {

    private static final String ATTRIBUTE_CLUSTER = "Cluster number";

    private String report = "";
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private Neighborhood neighborhood;

    private int numberOfNeighbours;

    private Set<Node> visited = new HashSet<>();
    private List<Set<Node>> clusters = new ArrayList<>();
    private Set<Node> clustered = new HashSet<>();
    private long executionTime = 0;

    public Dbscan(DistanceMetric distanceMetric) {
        super();
        this.neighborhood = new Neighborhood(distanceMetric);
    }

    @Override
    public void execute(GraphModel graphModel) {
        long startExecution = System.currentTimeMillis();
        prepareGraphModel(graphModel);
        NodeIterable allNodes = graphModel.getGraph().getNodes();
        graphModel.getGraphVisible().readLock();
        Progress.start(progressTicket, graphModel.getGraph().getNodeCount());
        clusterize(allNodes);
        executionTime = System.currentTimeMillis() - startExecution;
        createReport();
        graphModel.getGraphVisible().readUnlockAll();
    }

    private void clusterize(NodeIterable nodes) {
        nodes.forEach(node -> {
            if (!visited.contains(node)) {
                visited.add(node);
                List<Node> neighbors = neighborhood.getNeighborsList(node, nodes);
                if (neighbors.size() >= numberOfNeighbours) {
                    Set<Node> cluster = createCluster();
                    expandCluster(node, neighbors, cluster);
                }
                Progress.progress(progressTicket);
                if (cancel) {
                    return;
                }
            }
        });
    }

    private void prepareGraphModel(GraphModel graphModel) {
        if (graphModel.getNodeTable().hasColumn(ATTRIBUTE_CLUSTER)) {
            final Column column = graphModel.getNodeTable().getColumn(ATTRIBUTE_CLUSTER);
            for (Node node : graphModel.getGraphVisible().getNodes()) {
                node.setAttribute(column, null);
            }
        } else {
            graphModel.getNodeTable().addColumn(ATTRIBUTE_CLUSTER, Integer.class);
        }
        clusters.clear();
        visited.clear();
        clustered.clear();
    }

    private Set<Node> createCluster() {
        Set<Node> cluster = new HashSet<>();
        clusters.add(cluster);
        return cluster;
    }

    private void expandCluster(Node node, List<Node> neighbors, Set<Node> cluster) {
        addNodeToCluster(node, cluster);
        ListIterator<Node> listIterator = neighbors.listIterator();
        while (listIterator.hasNext()) {
            Node current = listIterator.next();
            visited.add(current);
            Set<Node> nodeNeighbors = neighborhood.getNeighborsSet(current, neighbors);
            if (nodeNeighbors.size() >= numberOfNeighbours) {
                nodeNeighbors.forEach(listIterator::add);
            }
            if (!clustered.contains(current)) {
                addNodeToCluster(current, cluster);
            }
        }
    }

    private void addNodeToCluster(Node node, Set<Node> cluster) {
        cluster.add(node);
        clustered.add(node);
        node.setAttribute(ATTRIBUTE_CLUSTER, clusters.indexOf(cluster));
    }

    private void createReport() {
        report = START
                + putInParagraph(2, putInTag(bold(DBSCAN_RESULT), "h2"))
                + NEW_LINE
                + bold(EXECUTION_TIME)
                + new SimpleDateFormat("mm:ss.SSS").format(executionTime)
                + NEW_LINE
                + bold(CLUSTERS)
                + clusters.size()
                + NEW_LINE
                + bold(CLUSTERED_NODES)
                + clustered.size()
                + NEW_LINE
                + bold(NOISES)
                + (visited.size() - clustered.size())
                + END;
    }

    @Override
    public String getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public void setRadius(int radius) {
        neighborhood.setRadius(radius);
    }

    public void setNumberOfNeighbours(int numberOfNeighbours) {
        this.numberOfNeighbours = numberOfNeighbours;
    }

    public int getRadius() {
        return neighborhood.getRadius();
    }

    public int getNumberOfNeighbours() {
        return numberOfNeighbours;
    }
}
