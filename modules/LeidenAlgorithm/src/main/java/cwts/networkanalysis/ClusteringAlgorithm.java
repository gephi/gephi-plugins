package cwts.networkanalysis;

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
     * Finds a clustering of the nodes in a network.
     *
     * @param network Network
     *
     * @return Clustering
     */
    public Clustering findClustering(Network network);
}
