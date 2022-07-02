package org.openuniversityofisrael.katzcentrality;

import org.apache.commons.math3.linear.*;
import org.gephi.graph.api.*;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

import java.util.*;

public class KatzCentrality implements Statistics {
    private double alpha = DEFAULT_ALPHA;
    private final HashMap<Node, Integer> nodesMap = new HashMap<Node, Integer>();
    private String error;
    private RealMatrix resultVector; // a column
    private static final int NO_EDGES = 0;
    private ProgressTicket progress;
    private boolean isDirected;
    public static final String NO_EDGES_MESSAGE = "The graph contains no edges! Exiting...";
    public static final String SINGULAR_MATRIX_ERROR_MESSAGE = "Katz Centrality measures do not exist for this alpha.";
    public static final String NON_SQUARE_MATRIX_ERROR_MESSAGE = "The resulting graph adjacency matrix is not square.";
    public static final String UNKNOWN_ERROR_MESSAGE = "Unknown error occurred: ";
    public static final String LABEL = "label";
    public static final String FAILURE_HEADER = "Error";
    public static final double DEFAULT_ALPHA = 0.5;
    private static final String KATZ_CENTRALITY = "KatzCentrality";
    private static final int IDENTITY_CELL = 1;
    private static final int FIRST_COLUMN = 0;
    private static final int ROWS_IN_REPORT_TABLE = 10;
    private static final String DEBUG_ENV_VAR_NAME = "DEBUG";
    private final boolean isDebugMode;


    public KatzCentrality() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            this.isDirected = graphController.getGraphModel().isDirected();
        }
        this.isDebugMode = System.getenv(DEBUG_ENV_VAR_NAME) != null;
    }

    public double getAlpha() {
        return this.alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public boolean isDirected() {
        return this.isDirected;
    }

    public void setDirected(boolean directed) {
        isDirected = directed;
    }

    public RealMatrix getResultVector() {
        return this.resultVector;
    }

    public String getError() {
        return this.error;
    }

    private double[][] getMatrix(Graph graph) {

        NodeIterable nodes = graph.getNodes();
        EdgeIterable edges = graph.getEdges();

        if (graph.getEdgeCount() == NO_EDGES) {
            if (this.isDebugMode) {
                System.out.println(NO_EDGES_MESSAGE);
            }
            this.error = NO_EDGES_MESSAGE;
            return null;
        }
        int nodesNum = graph.getNodeCount();
        double[][] matrix = new double[nodesNum][nodesNum];
        int index = 0;

        for (Node node : nodes) {
            this.nodesMap.put(node, index);
            index++;
        }

        for (Edge edge : edges) {
            int sourceIndex = this.nodesMap.get(edge.getSource());
            int targetIndex = this.nodesMap.get(edge.getTarget());

            // If weight is not set it's 1.0 by default
            matrix[sourceIndex][targetIndex] = edge.getWeight();

            if (!this.isDirected) {
                matrix[targetIndex][sourceIndex] = edge.getWeight();
            }
        }

        if (this.isDebugMode) {
            System.out.println("finished building matrix.");
            System.out.println(matrixToString(matrix));
        }

        return matrix;
    }

    private Column initKatzCentralityColumn(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column katzCentralityColumn = nodeTable.getColumn(KATZ_CENTRALITY);

        if (katzCentralityColumn == null) {
            katzCentralityColumn = nodeTable.addColumn(KATZ_CENTRALITY, KATZ_CENTRALITY, Double.class, new Double(0));
        }

        return katzCentralityColumn;
    }

    private void saveCalculations(Graph graph, Column katzCentralityColumn) {
        NodeIterable nodeIterable = graph.getNodes();
        for (Node node : nodeIterable) {
            Integer index = this.nodesMap.get(node);
            if (index != null) {
                double attributeValue = this.resultVector.getEntry(index, FIRST_COLUMN);
                if (attributeValue < 0) {
                    this.error = SINGULAR_MATRIX_ERROR_MESSAGE;
                    nodeIterable.doBreak();
                    break;
                }
                node.setAttribute(katzCentralityColumn,
                    this.resultVector.getEntry(index, FIRST_COLUMN));
            }
        }
    }

    public void execute(GraphModel graphModel) {
        Graph graph;
        if (this.isDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }

        Column katzCentralityColumn = this.initKatzCentralityColumn(graph.getModel());

        double[][] matrix;
        try {
            graph.readLock();
            matrix = this.getMatrix(graph);
        }  finally {
            graph.readUnlockAll();
        }

        if (matrix != null) {
            this.calculateKatzCentrality(matrix);

            if (this.error == null) {
                this.saveCalculations(graph, katzCentralityColumn);
            }
        }
    }

    private void calculateKatzCentrality(double[][] matrix) {
        Progress.start(this.progress);
        RealMatrix realMatrix = new Array2DRowRealMatrix(matrix);
        RealMatrix transpose = realMatrix.transpose();
        RealMatrix multipliedByAlpha = transpose.scalarMultiply(this.alpha);
        int matrixDimension = realMatrix.getRowDimension();
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(matrixDimension);
        RealMatrix subtracted = identity.subtract(multipliedByAlpha);
        RealMatrix inverse = null;

        try {
            inverse = MatrixUtils.inverse(subtracted);
        } catch (NonSquareMatrixException exception) {
            this.error = NON_SQUARE_MATRIX_ERROR_MESSAGE;
        } catch (SingularMatrixException exception) {
            this.error = SINGULAR_MATRIX_ERROR_MESSAGE;
        } catch (Exception exception) {
            this.error = UNKNOWN_ERROR_MESSAGE + exception.getMessage();
        } finally {
            if (inverse != null) {
                double[] identityVector = new double[matrixDimension];
                Arrays.fill(identityVector, IDENTITY_CELL);

                RealMatrix identityVectorMatrix = new Array2DRowRealMatrix(identityVector);
                this.resultVector = inverse.multiply(identityVectorMatrix);

                if (this.isDebugMode) {
                    System.out.println("resultVector: " + this.resultVector.toString());
                }
            }
        }
    }

    // helper to visualize matrix
    public static String matrixToString(double[][] matrix) {
        if (matrix == null) {
            return "Matrix has not been initialized yet.";
        }

        StringBuilder columnsHeader = new StringBuilder("columns" + '\t');
        for (int i = 0; i < matrix[0].length; i++) {
            columnsHeader.append(((Integer) i).toString() + '\t');
        }
        columnsHeader.append('\n');

        StringBuilder matrixRepresentation = new StringBuilder(columnsHeader.toString());
        for (int i = 0; i < matrix[0].length; i++) {
            StringBuilder row = new StringBuilder();
            for (double num : matrix[i]) {
                row.append(((Double) num).toString() + '\t');
            }
            matrixRepresentation.append("row " + i + ": " + '\t' + row);
            matrixRepresentation.append('\n');
        }
        System.out.println("finished matrixRepresentation.");
        return matrixRepresentation.toString();
    }

    private String getReportHtml(String header, String content) {
        return "<html> " +
            "<body><h1>Katz Centrality Report</h1> "
            + "<hr><br />"
            + "<h2>Parameters:</h2>"
            + "Directed = " + this.isDirected + "<br>"
            + "Alpha = " + this.alpha + "<br>"
            + String.format("<br><h2>%s:</h2>", header)
            + content
            + "<br /><br />"
            + "</body></html>";
    }

    public String getReport() {
        if (this.error != null) {
            return this.getReportHtml(FAILURE_HEADER, this.error);
        }
        // global inside <style> is not picked up by
        // the renderer (https://github.com/gephi/gephi/blob/c1485e9355f99200e3cac48be48e75fcab21157f/modules/UIComponents/src/main/java/org/gephi/ui/components/JHTMLEditorPane.java#L76)
        // therefore the styles are inline
        String borderStyle = "border: 1px solid black;border-collapse: collapse;";
        String textAlign = "text-align: left;";
        String headerStyle = borderStyle + textAlign;
        String headers = String.format("<tr>" +
                "<th style=\"%s\">Rank</th>" +
                "<th style=\"%s\">Node</th>" +
                "<th style=\"%s\">Katz Centrality</th></tr>\n",
            headerStyle, headerStyle, headerStyle);

        StringBuilder rows = new StringBuilder(headers);

        // get top rows by Katz Centrality value
        PriorityQueue<Node> pq = new PriorityQueue<>(this.nodesMap.keySet().size(), new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                Double katzCentrality1 = ((Double) n1.getAttribute(KATZ_CENTRALITY));
                Double katzCentrality2 = ((Double) n2.getAttribute(KATZ_CENTRALITY));
                return Double.compare(katzCentrality2, katzCentrality1);
            }
        });

        for (Node node : this.nodesMap.keySet()) {
            pq.add(node);
        }

        int index = 0;
        while (!pq.isEmpty() && index < ROWS_IN_REPORT_TABLE) {
            Node node = pq.poll();
            String label = node.getLabel() != null ? node.getLabel() : node.getId().toString();
            Double katzCentralityValue = ((Double) node.getAttribute(KATZ_CENTRALITY));
            rows.append(String.format("<tr>" +
                    "<td style=\"%s\">%d</td>" +
                    "<td style=\"%s\" data-type=\"%s\">%s</td>" +
                    "<td style=\"%s\">%.2f</td></tr>\n",
                borderStyle,
                ++index,
                borderStyle,
                LABEL,
                label,
                borderStyle,
                katzCentralityValue
            ));
        }

        String table = "<table style=\"width:100%;" + borderStyle + "\">\n" +
            rows.toString() +
            "</table>";

        String header = String.format("Results (Top %d)", index) ;
        String report = this.getReportHtml(header, table);

        if (this.isDebugMode) {
            System.out.println("\n" + report + "\n");
        }

        return report;
    }
}
