package cwts.networkanalysis;

import org.gephi.graph.api.Graph;

/**
 * Abstract base class for incremental clustering algorithms that use the CPM
 * quality function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public abstract class IncrementalCPMClusteringAlgorithm extends CPMClusteringAlgorithm implements IncrementalClusteringAlgorithm
{
    /**
     * Constructs an incremental CPM clustering algorithm.
     */
    public IncrementalCPMClusteringAlgorithm()
    {
        this(DEFAULT_RESOLUTION);
    }

    /**
     * Constructs an incremental CPM clustering algorithm with a specified
     * resolution parameter.
     *
     * @param resolution Resolution parameter
     */
    public IncrementalCPMClusteringAlgorithm(double resolution)
    {
        super(resolution);
    }

    /**
     * Finds a clustering of the nodes in a graph.
     *
     * <p>
     * The clustering is obtained by calling {@link #improveClustering(Graph
     * graph, Clustering clustering)} and by providing a singleton clustering
     * as input to this method.
     * </p>
     *
     * @param graph Graph
     *
     * @return Clustering
     */
    public Clustering findClustering(NodeWeightGraph nodeWeightGraph)
    {
        Clustering clustering;

        clustering = new Clustering(nodeWeightGraph.getGraph().getNodeCount());
        improveClustering(nodeWeightGraph, clustering);
        return clustering;
    }
}
