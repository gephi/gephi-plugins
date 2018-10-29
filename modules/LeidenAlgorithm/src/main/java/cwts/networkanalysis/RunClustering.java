/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cwts.networkanalysis;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;

public class RunClustering implements Statistics
{
    private Clustering clustering;
    private CPMClusteringAlgorithm algorithm;
    private double quality;

    public double getQuality()
    {
        return quality;
    }

    public String getReport()
    {
        return "Resolution:\t" + algorithm.getResolution() + "\n"
               + "Number of clusters:\t" + clustering.getNClusters() + "\n"
               + "Quality:\t" + quality;
    }

    public void execute(GraphModel gm)
    {
        Graph graph = gm.getUndirectedGraph();
        int[][] edges = new int[2][graph.getEdgeCount()];
        double[] edgeWeights = new double[graph.getEdgeCount()];
        int e = 0;
        for (Edge edge : graph.getEdges())
        {
            edges[0][e] = edge.getSource().getStoreId();
            edges[1][e] = edge.getTarget().getStoreId();
            edgeWeights[e] = edge.getWeight();
            e += 1;
        }
        Network network = new Network(graph.getNodeCount(), false, edges, edgeWeights, false, true);
        double resolution = (double)graph.getEdgeCount()/(graph.getNodeCount()*(graph.getNodeCount() - 1));
        algorithm = new LeidenAlgorithm();
        algorithm.setResolution(resolution);
        clustering = algorithm.findClustering(network);
        quality = algorithm.calcQuality(network, clustering);
        saveClustering(graph);
    }

    private void saveClustering(Graph graph)
    {
        Table nodeTable = graph.getModel().getNodeTable();
        Column modCol = nodeTable.getColumn("Cluster");
        if (modCol == null)
        {
            modCol = nodeTable.addColumn("Cluster", "Cluster", Integer.class, 0);
        }

        for (Node n : graph.getNodes())
        {
            n.setAttribute(modCol, clustering.getCluster(n.getStoreId()));
        }
    }
}
