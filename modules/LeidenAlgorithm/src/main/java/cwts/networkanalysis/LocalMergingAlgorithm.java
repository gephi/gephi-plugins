package cwts.networkanalysis;

import cwts.util.Arrays;
import cwts.util.FastMath;
import java.util.Random;

/**
 * Local merging algorithm.
 *
 * <p>
 * The local merging algorithm starts from a singleton partition. It performs a
 * single iteration over the nodes in a network. Each node belonging to a
 * singleton cluster is considered for merging with another cluster. This
 * cluster is chosen randomly from all clusters that do not result in a
 * decrease in the quality function. The larger the increase in the quality
 * function, the more likely a cluster is to be chosen. The strength of this
 * effect is determined by the randomness parameter. The higher the value of
 * the randomness parameter, the stronger the randomness in the choice of a
 * cluster. The lower the value of the randomness parameter, the more likely
 * the cluster resulting in the largest increase in the quality function is to
 * be chosen. A node is merged with a cluster only if both are sufficiently
 * well connected to the rest of the network.
 * </p>
 *
 * <p>
 * The local merging algorithm is used in the cluster refinement phase of the
 * {@link LeidenAlgorithm}.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class LocalMergingAlgorithm extends CPMClusteringAlgorithm
{
    /**
     * Default randomness parameter.
     */
    public static final double DEFAULT_RANDOMNESS = 1e-2;

    /**
     * Randomness parameter.
     */
    protected double randomness;

    /**
     * Random number generator.
     */
    protected Random random;

    /**
     * Constructs a local merging algorithm.
     */
    public LocalMergingAlgorithm()
    {
        this(new Random());
    }

    /**
     * Constructs a local merging algorithm.
     *
     * @param random Random number generator
     */
    public LocalMergingAlgorithm(Random random)
    {
        this(DEFAULT_RESOLUTION, DEFAULT_RANDOMNESS, random);
    }

    /**
     * Constructs a local merging algorithm for a specified resolution
     * parameter and randomness parameter.
     *
     * @param resolution Resolution parameter
     * @param randomness Randomness parameter
     * @param random     Random number generator
     */
    public LocalMergingAlgorithm(double resolution, double randomness, Random random)
    {
        super(resolution);
        this.randomness = randomness;
        this.random = random;
    }

    /**
     * Returns the randomness parameter.
     *
     * @return Randomness
     */
    public double getRandomness()
    {
        return randomness;
    }

    /**
     * Sets the randomness parameter.
     *
     * @param randomness Randomness
     */
    public void setRandomness(double randomness)
    {
        this.randomness = randomness;
    }

    /**
     * Finds a clustering of the nodes in a network using the local merging
     * algorithm.
     *
     * <p>
     * The local merging algorithm starts from a singleton partition. It
     * performs a single iteration over the nodes in a network. Each node
     * belonging to a singleton cluster is considered for merging with another
     * cluster. This cluster is chosen randomly from all clusters that do not
     * result in a decrease in the quality function. The larger the increase in
     * the quality function, the more likely a cluster is to be chosen. The
     * strength of this effect is determined by the randomness parameter. The
     * higher the value of the randomness parameter, the stronger the
     * randomness in the choice of a cluster. The lower the value of the
     * randomness parameter, the more likely the cluster resulting in the
     * largest increase in the quality function is to be chosen. A node is
     * merged with a cluster only if both are sufficiently well connected to
     * the rest of the network.
     * </p>
     *
     * @param network Network
     *
     * @return Clustering
     */
    public Clustering findClustering(Network network)
    {
        boolean update;
        boolean[] nonSingletonClusters;
        double maxQualityValueIncrement, qualityValueIncrement, r, totalNodeWeight, totalTransformedQualityValueIncrement;
        double[] clusterWeights, cumTransformedQualityValueIncrementPerCluster, edgeWeightPerCluster, externalEdgeWeightPerCluster;
        int bestCluster, chosenCluster, i, j, k, l, max_idx, mid_idx, min_idx, nNeighboringClusters;
        int[] neighboringClusters, nodeOrder;

        Clustering clustering = new Clustering(network.nNodes);

        if (network.nNodes == 1)
            return clustering;

        update = false;

        totalNodeWeight = network.getTotalNodeWeight();
        clusterWeights = network.getNodeWeights();
        nonSingletonClusters = new boolean[network.nNodes];
        externalEdgeWeightPerCluster = network.getTotalEdgeWeightPerNode();

        nodeOrder = Arrays.generateRandomPermutation(network.nNodes, random);

        edgeWeightPerCluster = new double[network.nNodes];
        neighboringClusters = new int[network.nNodes];
        cumTransformedQualityValueIncrementPerCluster = new double[network.nNodes];
        for (i = 0; i < network.nNodes; i++)
        {
            j = nodeOrder[i];

            /*
             * Only nodes belonging to singleton clusters can be moved to a
             * different cluster. This guarantees that clusters will never be
             * split up. Additionally, only nodes that are well connected with
             * the rest of the network are considered for moving.
             */
            if (!nonSingletonClusters[j] && (externalEdgeWeightPerCluster[j] >= clusterWeights[j] * (totalNodeWeight - clusterWeights[j]) * resolution))
            {
                /*
                 * Remove the currently selected node from its current cluster.
                 * This causes the cluster to be empty.
                 */
                clusterWeights[j] = 0;
                externalEdgeWeightPerCluster[j] = 0;

                /*
                 * Identify the neighboring clusters of the currently selected
                 * node, that is, the clusters with which the currently
                 * selected node is connected. The old cluster of the currently
                 * selected node is also included in the set of neighboring
                 * clusters. In this way, it is always possible that the
                 * currently selected node will be moved back to its old
                 * cluster.
                 */
                neighboringClusters[0] = j;
                nNeighboringClusters = 1;
                for (k = network.firstNeighborIndices[j]; k < network.firstNeighborIndices[j + 1]; k++)
                {
                    l = clustering.clusters[network.neighbors[k]];
                    if (edgeWeightPerCluster[l] == 0)
                    {
                        neighboringClusters[nNeighboringClusters] = l;
                        nNeighboringClusters++;
                    }
                    edgeWeightPerCluster[l] += network.edgeWeights[k];
                }

                /*
                 * For each neighboring cluster of the currently selected node,
                 * determine whether the neighboring cluster is well connected
                 * with the rest of the network. For each neighboring cluster
                 * that is well connected, calculate the increment of the
                 * quality function obtained by moving the currently selected
                 * node to the neighboring cluster. For each neighboring
                 * cluster for which the increment is non-negative, calculate a
                 * transformed increment that will determine the probability
                 * with which the currently selected node is moved to the
                 * neighboring cluster.
                 */
                bestCluster = j;
                maxQualityValueIncrement = 0;
                totalTransformedQualityValueIncrement = 0;
                for (k = 0; k < nNeighboringClusters; k++)
                {
                    l = neighboringClusters[k];

                    if (externalEdgeWeightPerCluster[l] >= clusterWeights[l] * (totalNodeWeight - clusterWeights[l]) * resolution)
                    {
                        qualityValueIncrement = edgeWeightPerCluster[l] - network.nodeWeights[j] * clusterWeights[l] * resolution;

                        if (qualityValueIncrement > maxQualityValueIncrement)
                        {
                            bestCluster = l;
                            maxQualityValueIncrement = qualityValueIncrement;
                        }

                        if (qualityValueIncrement >= 0)
                            totalTransformedQualityValueIncrement += FastMath.fastExp(qualityValueIncrement / randomness);
                    }

                    cumTransformedQualityValueIncrementPerCluster[k] = totalTransformedQualityValueIncrement;

                    edgeWeightPerCluster[l] = 0;
                }

                /*
                 * Determine the neighboring cluster to which the currently
                 * selected node will be moved.
                 */
                if (totalTransformedQualityValueIncrement < Double.POSITIVE_INFINITY)
                {
                    r = totalTransformedQualityValueIncrement * random.nextDouble();
                    min_idx = -1;
                    max_idx = nNeighboringClusters + 1;
                    while (min_idx < max_idx - 1)
                    {
                        mid_idx = (min_idx + max_idx) / 2;
                        if (cumTransformedQualityValueIncrementPerCluster[mid_idx] >= r)
                            max_idx = mid_idx;
                        else
                            min_idx = mid_idx;
                    }
                    chosenCluster = neighboringClusters[max_idx];
                }
                else
                    chosenCluster = bestCluster;

                /*
                 * Move the currently selected node to its new cluster and
                 * update the clustering statistics.
                 */
                clusterWeights[chosenCluster] += network.nodeWeights[j];

                for (k = network.firstNeighborIndices[j]; k < network.firstNeighborIndices[j + 1]; k++)
                    if (clustering.clusters[network.neighbors[k]] == chosenCluster)
                        externalEdgeWeightPerCluster[chosenCluster] -= network.edgeWeights[k];
                    else
                        externalEdgeWeightPerCluster[chosenCluster] += network.edgeWeights[k];

                if (chosenCluster != j)
                {
                    clustering.clusters[j] = chosenCluster;

                    nonSingletonClusters[chosenCluster] = true;
                    update = true;
                }
            }
        }

        if (update)
            clustering.removeEmptyClusters();

        return clustering;
    }
}
