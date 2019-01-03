package KatzCentrality;

import org.gephi.graph.api.*;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

import java.util.HashMap;
import java.util.Map;

public class KatzCentrality implements Statistics {
    public static final double ALPHA = 1.0;
    public static final double BETA = 1.0;
    public static double EPSILON = 1e-6;

    private Map<Node, Double> katz = new HashMap<>();
    private Graph graph;
    private boolean isDirected = false;
    private int numRuns = 1;

    KatzCentrality() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    @Override
    public void execute(GraphModel graphModel) {
        Column katzColumn = initializeAttributeColumn(graphModel);
        if (isDirected){
            graph = graphModel.getDirectedGraph();
            System.out.println("directed");
        }
        else{
            System.out.println("undirected");
            graph = graphModel.getUndirectedGraph();
        }
        graph.readLock();
        try {
            for (Node node : graph.getNodes()) {
                katz.put(node, 1.0);
            }

            for (int i = 0; i < numRuns; i++) {
                for (Node node : graph.getNodes()) {
                    double sum = 1.0;
                    if(isDirected){

                        for (Node neighbor : graph.getNeighbors(node)) {
                            sum += katz.get(neighbor);
                        }
                    } else {
                        for (Node adjacent : graph.getNodes()) {
                            if(graph.isAdjacent(node, adjacent)){
                                sum += katz.get(adjacent);
                            }
                        }
                    }

                    double value = ALPHA * sum + BETA;
                    katz.put(node, value);
                }
                normalization();
            }

            for (Node node : graph.getNodes()) {
                node.setAttribute(katzColumn, katz.get(node));
            }
        } finally {
            graph.readUnlockAll();
        }

    }

    @Override
    public String getReport() {
        String report = "<HTML> <BODY> <h1>Katz Centrality Report </h1> "
                + "<hr>"
                + "<br> <h2> Results: </h2>"
                + printValues()
                + "<br />"
                + "</BODY></HTML>";
        return report;
    }

    private String printValues() {
        StringBuilder sb = new StringBuilder();
        katz.forEach((node, value) -> {
            sb.append(katz.get(node))
                    .append("<br/>");
        });
        return sb.toString();
    }

    private Column initializeAttributeColumn(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column katzCol = nodeTable.getColumn("katz");
        if (katzCol == null) {
            katzCol = nodeTable.addColumn("katz", "Katz centrality", Double.class, 0.0);
        }
        return katzCol;
    }

    public int getNumRuns() {
        return numRuns;
    }

    public void setNumRuns(int numRuns) {
        this.numRuns = numRuns;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    private void normalization() {
        double max = katz.values().stream().max(Double::compareTo).orElseGet(() -> {
            System.out.println("There is no max value");
            return 0.0;
        });
        for (Node node : katz.keySet()) {
            double value = katz.get(node);
            value = value / max;
            System.out.println(katz.get(node) + " - " + value + " - " + max);
            katz.put(node, value);
        }
    }
}
