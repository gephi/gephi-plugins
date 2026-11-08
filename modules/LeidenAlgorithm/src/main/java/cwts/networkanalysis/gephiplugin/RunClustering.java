package cwts.networkanalysis.gephiplugin;

import cwts.networkanalysis.Clustering;
import cwts.networkanalysis.IterativeCPMClusteringAlgorithm;
import cwts.networkanalysis.LeidenAlgorithm;
import cwts.networkanalysis.LouvainAlgorithm;
import cwts.networkanalysis.Network;
import cwts.util.DynamicDoubleArray;
import cwts.util.DynamicIntArray;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    // The same defaults as the user interface starts with, the statistics can be run without it
    protected boolean useEdgeWeights = true;
    protected boolean useRandomSeed = false;
    protected int randomSeed = 0;
    protected int nIterations = 10;
    protected int nRestarts = 1;
    protected double resolution = 0.01;
    protected RunClustering.Algorithm algorithm = RunClustering.Algorithm.LEIDEN;
    protected RunClustering.QualityFunction qualityFunction = RunClustering.QualityFunction.CPM;

    private Clustering clustering;
    private double quality;

    private boolean isCanceled = false;

    public double getQuality()
    {
        return quality;
    }

    @Override
    public String getReport()
    {
        return    "<table>"
                + "<tr><td colspan=\"2\"><b>Configuration</b></td></tr>"
                + "<tr><td>Algorithm</td><td>" + (algorithm == Algorithm.LEIDEN ? "Leiden" : "Louvain") + "</td></tr>"
                + "<tr><td>Quality Function</td><td>" + (qualityFunction == QualityFunction.CPM ? "Constant Potts Model (CPM)" : "Modularity") + "</td></tr>"
                + "<tr><td>Resolution</td><td>" + resolution + "</td></tr>"
                + "<tr><td>Number of iterations</td><td>" + nIterations + "</td></tr>"
                + "<tr><td>Number of restarts</td><td>" + nRestarts + "</td></tr>"
                + "<tr><td>Random seed</td><td>" + (useRandomSeed ? "random" : randomSeed) + "</td></tr>"
                + "<tr></tr>"
                + "<tr><td colspan=\"2\"><b>Results</b></td></tr>"
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

    @Override
    public void execute(GraphModel gm)
    {
        Graph graph = gm.getUndirectedGraphVisible();
        if (graph.getNodeCount() == 0)
        {
            // The algorithms can't work on a network without nodes
            clustering = new Clustering(0);
            quality = 0.0;
            return;
        }
        graph.readLock();
        DynamicIntArray[] edges = new DynamicIntArray[2];
        edges[0] = new DynamicIntArray(graph.getEdgeCount());
        edges[1] = new DynamicIntArray(graph.getEdgeCount());
        DynamicDoubleArray edgeWeights = new DynamicDoubleArray(graph.getEdgeCount());

        // The network needs consecutive node indices, which the store ids are not in a filtered view
        Map<Integer, Integer> nodeIndices = new HashMap<>();
        for (Node node : graph.getNodes())
        {
            nodeIndices.put(node.getStoreId(), nodeIndices.size());
        }

        for (Node node : graph.getNodes())
        {
            for (Node neighbor : new HashSet<>(graph.getNeighbors(node).toCollection()))
            {
                if (node.getStoreId() < neighbor.getStoreId())
                {
                    double w = 0.0;

                    // When not taking into account edge weights we do consider
                    // the possible multiplicity of edges.
                    for (Edge edge : graph.getEdges(node, neighbor))
                        w += useEdgeWeights ? edge.getWeight() : 1.0;

                    // The algorithms need positive weights, other edges are left out
                    if (w > 0.0)
                    {
                        edges[0].append(nodeIndices.get(node.getStoreId()));
                        edges[1].append(nodeIndices.get(neighbor.getStoreId()));
                        edgeWeights.append(w);
                    }
                }
            }
        }

        Network network = new Network(graph.getNodeCount(),
                                      (qualityFunction == QualityFunction.MODULARITY),
                                      new int[][] {edges[0].toArray(), edges[1].toArray()},
                                      edgeWeights.toArray(),
                                      false, true);
        graph.readUnlock();
        
        final Random random;
        if (!useRandomSeed)
            random = new Random(randomSeed);
        else
            random = new Random();

        IterativeCPMClusteringAlgorithm clusteringAlgorithm = null;
        switch (algorithm)
        {
            case LEIDEN:  clusteringAlgorithm = new LeidenAlgorithm(random);  break;
            case LOUVAIN: clusteringAlgorithm = new LouvainAlgorithm(random); break;
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
        clusteringAlgorithm.setNIterations(1);

        Clustering initialClustering = new Clustering(network.getNNodes());
        Clustering maxClustering = initialClustering.clone();

        Progress.start(progress, nRestarts*nIterations);
        double maxQuality = clusteringAlgorithm.calcQuality(network, maxClustering);
        for (int restart = 0; restart < nRestarts; restart++)
        {
            clustering = initialClustering.clone();
            for (int itr = 0; itr < nIterations; itr++)
            {
                clusteringAlgorithm.improveClustering(network, clustering);
                Progress.progress(progress, restart*nIterations + itr);
                if (isCanceled)
                    break;
            }

            double quality = clusteringAlgorithm.calcQuality(network, clustering);
            if (quality > maxQuality)
            {
                maxQuality = quality;
                maxClustering = clustering;
            }

            if (isCanceled)
                break;
        }
        Progress.finish(progress);

        clustering = maxClustering;
        clustering.orderClustersByNNodes();

        quality = clusteringAlgorithm.calcQuality(network, clustering);

        saveClustering(graph, nodeIndices);
    }

    private void saveClustering(Graph graph, Map<Integer, Integer> nodeIndices)
    {
        graph.readLock();
        Table nodeTable = graph.getModel().getNodeTable();
        Column modCol = nodeTable.getColumn("Cluster");
        if (modCol == null)
        {
            modCol = nodeTable.addColumn("Cluster", "Cluster", Integer.class, 0);
        }

        for (Node n : graph.getNodes())
        {
            n.setAttribute(modCol, clustering.getCluster(nodeIndices.get(n.getStoreId())));
        }
        graph.readUnlock();
    }
}
