package cwts.networkanalysis;

import cwts.util.Arrays;
import java.util.Random;

/**
 * Standard local moving algorithm.
 *
 * <p>
 * The standard local moving algorithm iterates over the nodes in a network. A
 * node is moved to the cluster that results in the largest increase in the
 * quality function. If the current cluster assignment of the node is already
 * optimal, the node is not moved. The algorithm continues iterating over the
 * nodes in a network until no more nodes can be moved.
 * </p>
 *
 * <p>
 * A fast variant of the standard local moving algorithm is provided by the
 * {@link FastLocalMovingAlgorithm}.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class StandardLocalMovingAlgorithm extends IncrementalCPMClusteringAlgorithm
{
    /**
     * Random number generator.
     */
    protected Random random;

    /**
     * Constructs a standard local moving algorithm.
     */
    public StandardLocalMovingAlgorithm()
    {
        this(new Random());
    }

    /**
     * Constructs a standard local moving algorithm.
     *
     * @param random Random number generator
     */
    public StandardLocalMovingAlgorithm(Random random)
    {
        this(DEFAULT_RESOLUTION, random);
    }

    /**
     * Constructs a standard local moving algorithm for a specified resolution
     * parameter.
     *
     * @param resolution Resolution parameter
     * @param random     Random number generator
     */
    public StandardLocalMovingAlgorithm(double resolution, Random random)
    {
        super(resolution);

        this.random = random;
    }

    /**
     * Improves a clustering of the nodes in a network using the standard local
     * moving algorithm.
     *
     * <p>
     * The standard local moving algorithm iterates over the nodes in a
     * network. A node is moved to the cluster that results in the largest
     * increase in the quality function. If the current cluster assignment of
     * the node is already optimal, the node is not moved. The algorithm
     * continues iterating over the nodes in a network until no more nodes can
     * be moved.
     * </p>
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    public boolean improveClustering(Network network, Clustering clustering)
    {
        boolean update;
        double maxQualityValueIncrement, qualityValueIncrement;
        double[] clusterWeights, edgeWeightPerCluster;
        int bestCluster, currentCluster, i, j, k, l, nNeighboringClusters, nUnstableNodes, nUnusedClusters;
        int[] neighboringClusters, nNodesPerCluster, nodeOrder, unusedClusters;

        if (network.nNodes == 1)
            return false;

        update = false;

        clusterWeights = new double[network.nNodes];
        nNodesPerCluster = new int[network.nNodes];
        for (i = 0; i < network.nNodes; i++)
        {
            clusterWeights[clustering.clusters[i]] += network.nodeWeights[i];
            nNodesPerCluster[clustering.clusters[i]]++;
        }

        nUnusedClusters = 0;
        unusedClusters = new int[network.nNodes - 1];
        for (i = network.nNodes - 1; i >= 0; i--)
            if (nNodesPerCluster[i] == 0)
            {
                unusedClusters[nUnusedClusters] = i;
                nUnusedClusters++;
            }

        nodeOrder = Arrays.generateRandomPermutation(network.nNodes, random);

        /*
         * Iterate over the nodeOrder array in a cyclical manner. When the end
         * of the array has been reached, start again from the beginning.
         * Continue iterating until none of the last nNodes node visits has
         * resulted in a node movement.
         */
        edgeWeightPerCluster = new double[network.nNodes];
        neighboringClusters = new int[network.nNodes];
        nUnstableNodes = network.nNodes;
        i = 0;
        do
        {
            j = nodeOrder[i];

            currentCluster = clustering.clusters[j];

            // Remove the currently selected node from its current cluster.
            clusterWeights[currentCluster] -= network.nodeWeights[j];
            nNodesPerCluster[currentCluster]--;
            if (nNodesPerCluster[currentCluster] == 0)
            {
                unusedClusters[nUnusedClusters] = currentCluster;
                nUnusedClusters++;
            }

            /*
             * Identify the neighboring clusters of the currently selected
             * node, that is, the clusters with which the currently selected
             * node is connected. An empty cluster is also included in the set
             * of neighboring clusters. In this way, it is always possible that
             * the currently selected node will be moved to an empty cluster.
             */
            neighboringClusters[0] = unusedClusters[nUnusedClusters - 1];
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
             * calculate the increment of the quality function obtained by
             * moving the currently selected node to the neighboring cluster.
             * Determine the neighboring cluster for which the increment of the
             * quality function is largest. The currently selected node will be
             * moved to this optimal cluster. In order to guarantee convergence
             * of the algorithm, if the old cluster of the currently selected
             * node is optimal but there are also other optimal clusters, the
             * currently selected node will be moved back to its old cluster.
             */
            bestCluster = currentCluster;
            maxQualityValueIncrement = edgeWeightPerCluster[currentCluster] - network.nodeWeights[j] * clusterWeights[currentCluster] * resolution;
            for (k = 0; k < nNeighboringClusters; k++)
            {
                l = neighboringClusters[k];

                qualityValueIncrement = edgeWeightPerCluster[l] - network.nodeWeights[j] * clusterWeights[l] * resolution;
                if (qualityValueIncrement > maxQualityValueIncrement)
                {
                    bestCluster = l;
                    maxQualityValueIncrement = qualityValueIncrement;
                }

                edgeWeightPerCluster[l] = 0;
            }

            /*
             * Move the currently selected node to its new cluster. Update the
             * clustering statistics.
             */
            clusterWeights[bestCluster] += network.nodeWeights[j];
            nNodesPerCluster[bestCluster]++;
            if (bestCluster == unusedClusters[nUnusedClusters - 1])
                nUnusedClusters--;
            nUnstableNodes--;

            /*
             * If the new cluster of the currently selected node is different
             * from the old cluster, some further updating of the clustering
             * statistics is performed.
             */
            if (bestCluster != currentCluster)
            {
                clustering.clusters[j] = bestCluster;
                if (bestCluster >= clustering.nClusters)
                    clustering.nClusters = bestCluster + 1;

                nUnstableNodes = network.nNodes;

                update = true;
            }

            i = (i < network.nNodes - 1) ? (i + 1) : 0;
        } while (nUnstableNodes > 0);

        if (update)
            clustering.removeEmptyClusters();

        return update;
    }
}
