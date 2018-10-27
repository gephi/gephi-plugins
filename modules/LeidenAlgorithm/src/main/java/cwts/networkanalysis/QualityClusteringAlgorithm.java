package cwts.networkanalysis;

import org.gephi.graph.api.Graph;

/**
 * Interface for clustering algorithms that use a quality function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public interface QualityClusteringAlgorithm extends ClusteringAlgorithm
{
    /**
     * Calculates the quality of a clustering of the nodes in a graph.
     *
     * @param graph    Graph
     * @param clustering Clustering
     *
     * @return Quality of the clustering
     */
    public double calcQuality(NodeWeightGraph graph, Clustering clustering);
}
