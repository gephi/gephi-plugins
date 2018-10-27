package cwts.networkanalysis;

import org.gephi.graph.api.Graph;

/**
 * Interface for clustering algorithms.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public interface ClusteringAlgorithm
{
    /**
     * Finds a clustering of the nodes in a graph.
     *
     * @param graph Graph
     *
     * @return Clustering
     */
    public Clustering findClustering(NodeWeightGraph nodeWeightGraph);
}
