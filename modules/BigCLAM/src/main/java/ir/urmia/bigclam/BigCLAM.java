
package ir.urmia.bigclam;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

public class BigCLAM implements org.gephi.statistics.spi.Statistics, LongTask {
    private StringBuilder reportSB = new StringBuilder();
    private boolean cancel = false;
    private int k = 3;
    private int iterations = 100;
    double[][] adjacencyMatrix;
    Node[] nodes = null;

    public void createAdjacencyMatrix(Graph g) {
        adjacencyMatrix = new double[nodes.length][nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            Node n1 = nodes[i];
            for (int j = 0; j < nodes.length; j++) {
                Node n2 = nodes[j];
                adjacencyMatrix[i][j] = (g.getEdge(n1, n2) != null || g.getEdge(n2, n1) != null) ?
                        1 : 0;
            }
        }

        reportSB.append("Adjacency Matrix:\n");

        for (double[] row : adjacencyMatrix) {
            reportSB.append("\n");
            for (double element : row) {
                reportSB.append(String.format("%d", (int) element));
                reportSB.append(" ");
            }
            reportSB.delete(reportSB.length() - 3, reportSB.length());

        }
    }

    @Override
    public void execute(GraphModel gm) {
        Graph g = gm.getGraphVisible();
        g.readLock();
        nodes = g.getNodes().toArray();

        createAdjacencyMatrix(g);
        int communitiesCount = k;
        int iterationsCount = iterations;
        double learningRate = 0.01;

        double[][] F = Utils.bigclam(adjacencyMatrix, communitiesCount, iterationsCount, learningRate);

        reportSB.append("\n\nF:\n\n");

        for (int i = 0; i < F.length; i++) {
            double[] row = F[i];
            reportSB.append(nodes[i].getLabel());
            reportSB.append("\t");

            for (double element : row) {
                reportSB.append(String.format("%2.2f", element));
                reportSB.append(" | ");
            }
            reportSB.delete(reportSB.length() - 3, reportSB.length());
            reportSB.append("\n");
        }
        g.readUnlock();
    }

    @Override
    public String getReport() {
        reportSB.insert(0, "<pre>");
        reportSB.append("</pre>");
        return reportSB.toString();
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
    }

    public int getK() {
        return k;
    }

    public int getIterations() {
        return iterations;
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

}
