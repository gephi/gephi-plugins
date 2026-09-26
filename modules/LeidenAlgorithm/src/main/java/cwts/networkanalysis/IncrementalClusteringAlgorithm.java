package cwts.networkanalysis;

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
     * Improves a clustering of the nodes in a network.
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    public boolean improveClustering(Network network, Clustering clustering);
}
