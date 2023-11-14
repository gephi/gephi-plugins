package cwts.networkanalysis;

/**
 * Abstract base class for clustering algorithms that use the CPM quality
 * function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public abstract class CPMClusteringAlgorithm implements Cloneable, QualityClusteringAlgorithm
{
    /**
     * Default resolution parameter.
     */
    public static final double DEFAULT_RESOLUTION = 1;

    /**
     * Resolution parameter.
     */
    protected double resolution;

    /**
     * Constructs a CPM clustering algorithm.
     */
    public CPMClusteringAlgorithm()
    {
        this(DEFAULT_RESOLUTION);
    }

    /**
     * Constructs a CPM clustering algorithm with a specified resolution
     * parameter.
     *
     * @param resolution Resolution parameter
     */
    public CPMClusteringAlgorithm(double resolution)
    {
        this.resolution = resolution;
    }

    /**
     * Clones the algorithm.
     *
     * @return Cloned algorithm
     */
    public CPMClusteringAlgorithm clone()
    {
        try
        {
            return (CPMClusteringAlgorithm)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Returns the resolution parameter.
     *
     * @return Resolution parameter
     */
    public double getResolution()
    {
        return resolution;
    }

    /**
     * Sets the resolution parameter.
     *
     * @param resolution Resolution parameter
     */
    public void setResolution(double resolution)
    {
        this.resolution = resolution;
    }

    /**
     * Calculates the quality of a clustering using the CPM quality function.
     *
     * <p>
     * The CPM quality function is given by
     * </p>
     *
     * <blockquote>
     * {@code 1 / (2 * m) * sum(d(c[i], c[j]) * (a[i][j] - resolution * n[i] *
     * n[j]))},
     * </blockquote>
     *
     * <p>
     * where {@code a[i][j]} is the weight of the edge between nodes {@code i}
     * and {@code j}, {@code n[i]} is the weight of node {@code i}, {@code m}
     * is the total edge weight, and {@code resolution} is the resolution
     * parameter. The function {@code d(c[i], c[j])} equals 1 if nodes {@code
     * i} and {@code j} belong to the same cluster and 0 otherwise. The sum is
     * taken over all pairs of nodes {@code i} and {@code j}.
     * </p>
     *
     * <p>
     * Modularity can be expressed in terms of CPM by setting {@code n[i]}
     * equal to the total weight of the edges between node {@code i} and its
     * neighbors and by rescaling the resolution parameter by {@code 2 * m}.
     * </p>
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Quality of the clustering
     */
    public double calcQuality(Network network, Clustering clustering)
    {
        double quality;
        double[] clusterWeights;
        int i, j, k;

        quality = 0;

        for (i = 0; i < network.nNodes; i++)
        {
            j = clustering.clusters[i];
            for (k = network.firstNeighborIndices[i]; k < network.firstNeighborIndices[i + 1]; k++)
                if (clustering.clusters[network.neighbors[k]] == j)
                    quality += network.edgeWeights[k];
        }
        quality += network.totalEdgeWeightSelfLinks;

        clusterWeights = new double[clustering.nClusters];
        for (i = 0; i < network.nNodes; i++)
            clusterWeights[clustering.clusters[i]] += network.nodeWeights[i];
        for (i = 0; i < clustering.nClusters; i++)
            quality -= clusterWeights[i] * clusterWeights[i] * resolution;

        quality /= 2 * network.getTotalEdgeWeight() + network.totalEdgeWeightSelfLinks;

        return quality;
    }
}
