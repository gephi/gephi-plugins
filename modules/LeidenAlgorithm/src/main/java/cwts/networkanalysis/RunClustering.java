/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cwts.networkanalysis;

import java.util.Random;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

public class RunClustering implements Statistics, LongTask
{
    public enum Algorithm { LEIDEN, LOUVAIN };
    public enum QualityFunction { CPM, MODULARITY };

    private ProgressTicket progress;

    protected boolean useRandomSeed;
    protected int randomSeed;
    protected int nIterations;
    protected int nRestarts;
    protected double resolution;
    protected RunClustering.Algorithm algorithm;
    protected RunClustering.QualityFunction qualityFunction;

    private Clustering clustering;
    private double quality;

    private boolean isCanceled = false;

    public double getQuality()
    {
        return quality;
    }

    public String getReport()
    {
        return    "<b>Configuration</b>"
                + "<table>"
                + "<tr><td>Algorithm</td><td>" + (algorithm == Algorithm.LEIDEN ? "Leiden" : "Louvain") + "</td></tr>"
                + "<tr><td>Quality Function</td><td>" + (qualityFunction == QualityFunction.CPM ? "Constant Potts Model (CPM)" : "Modularity") + "</td></tr>"
                + "<tr><td>Resolution</td><td>" + resolution + "</td></tr>"
                + "<tr><td>Number of iterations</td><td>" + nIterations + "</td></tr>"
                + "<tr><td>Number of restarts</td><td>" + nRestarts + "</td></tr>"
                + "<tr><td>Random seed</td><td>" + (useRandomSeed ? "random" : randomSeed) + "</td></tr>"
                + "</table>"
                + "<b>Results</b>"
                + "<table>"
                + "<tr><td>Quality</td><td>" + quality + "</td></tr>"
                + "<tr><td>Number of clusters</td><td>" + clustering.getNClusters() + "</td></tr>"
                + "</table>";
    }


    @Override
    public boolean cancel()
    {
        this.isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt)
    {
        this.progress = pt;
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

        Network network = new Network(graph.getNodeCount(), (qualityFunction == QualityFunction.MODULARITY),
                                      edges, edgeWeights, false, true);

        Random random = null;
        if (!useRandomSeed)
            random = new Random(randomSeed);
        else
            random = new Random();

        IterativeCPMClusteringAlgorithm clusteringAlgorithm = null;
        switch (algorithm)
        {
            case LEIDEN:    clusteringAlgorithm = new LeidenAlgorithm();  break;
            case LOUVAIN:   clusteringAlgorithm = new LouvainAlgorithm(); break;
        }

        switch (qualityFunction)
        {
            case CPM:
                clusteringAlgorithm.setResolution(resolution);
                break;
            case MODULARITY:
                clusteringAlgorithm.setResolution(resolution/(2 * network.getTotalEdgeWeight() + network.getTotalEdgeWeightSelfLinks()));
                break;
        }

        Clustering initialClustering = new Clustering(network.getNNodes());
        Clustering maxClustering = initialClustering.clone();

        progress.start(nRestarts*nIterations);
        double maxQuality = clusteringAlgorithm.calcQuality(network, maxClustering);
        for (int restart = 0; restart < nRestarts; restart++)
        {
            clustering = initialClustering.clone();
            for (int itr = 0; itr < nIterations; itr++)
            {
                clusteringAlgorithm.improveClusteringOneIteration(network, clustering);
                progress.progress(restart*nIterations + itr);
                if (isCanceled)
                    return;
            }

            double quality = clusteringAlgorithm.calcQuality(network, clustering);
            if (quality > maxQuality)
            {
                maxQuality = quality;
                maxClustering = clustering;
            }
        }
        progress.finish();

        clustering = maxClustering;
        clustering.orderClustersByNNodes();

        quality = clusteringAlgorithm.calcQuality(network, clustering);
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
