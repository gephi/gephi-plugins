package cwts.networkanalysis;

import org.gephi.graph.api.Graph;

/**
 * Interface for clustering algorithms that are able to improve an existing
 * clustering.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public interface IncrementalClusteringAlgorithm extends ClusteringAlgorithm
{
    /**
     * Improves a clustering of the nodes in a graph.
     *
     * @param graph    Graph
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    public boolean improveClustering(NodeWeightGraph nodeWeightGraph, Clustering clustering);
}
