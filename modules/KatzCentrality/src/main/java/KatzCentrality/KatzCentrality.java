package KatzCentrality;

import org.gephi.graph.api.*;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

import java.util.HashMap;
import java.util.Map;

public class KatzCentrality implements Statistics {
    public static final double DEFAULT_ALPHA = 1.0;
    public static final double DEFAULT_BETA = 1.0;

    private Map<Node, Double> katz = new HashMap<>();
    private Graph graph;
    private boolean directed = false;
    private boolean isDirected;

    KatzCentrality() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    @Override
    public void execute(GraphModel graphModel) {
        Column katzColumn = initializeAttributeColumn(graphModel);
        graph = graphModel.getGraph();
        int N = graph.getNodeCount();
        graph.readLock();
        try {
            for (Node node : graph.getNodes()) {
                katz.put(node, 0.0);
//                initializeShortestPathAlgorithm(graph, node);
            }
            for (Node node : graph.getNodes()) {
//                calculateShortestPaths(graph, node);
            }
//            handleUndirectedValues();
            for (Node node : graph.getNodes()) {
                node.setAttribute(katzColumn, katz.get(node));
            }
        } finally {
            graph.readUnlockAll();
        }

    }

    private Graph getGraph(GraphModel graphModel) {
        return graph;
    }


    @Override
    public String getReport() {
        String report = "<HTML> <BODY> <h1>Katz Centrality Report </h1> "
                + "<hr>"
                + "<br> <h2> Results: </h2>"
                + "<br />"
                + "</BODY></HTML>";
        return report;
    }

    private Column initializeAttributeColumn(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column katzCol = nodeTable.getColumn("katz");
        if (katzCol == null) {
            katzCol = nodeTable.addColumn("katz", "Katz centrality", Double.class, 0.0);
        }
        return katzCol;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }
}
