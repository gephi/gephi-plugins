package cwts.networkanalysis;

import cwts.util.Arrays;
import java.util.Random;

/**
 * Fast local moving algorithm.
 *
 * <p>
 * The fast local moving algorithm first adds all nodes in a network to a
 * queue. It then removes a node from the queue. The node is moved to the
 * cluster that results in the largest increase in the quality function. If the
 * current cluster assignment of the node is already optimal, the node is not
 * moved. If the node is moved to a different cluster, the neighbors of the
 * node that do not belong to the node's new cluster and that are not yet in
 * the queue are added to the queue. The algorithm continues removing nodes
 * from the queue until the queue is empty.
 * </p>
 *
 * <p>
 * The fast local moving algorithm provides a fast variant of the {@link
 * StandardLocalMovingAlgorithm}.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class FastLocalMovingAlgorithm extends IterativeCPMClusteringAlgorithm
{
    /**
     * Random number generator.
     */
    protected Random random;

    /**
     * Constructs a fast local moving algorithm.
     */
    public FastLocalMovingAlgorithm()
    {
        this(new Random());
    }

    /**
     * Constructs a fast local moving algorithm.
     *
     * @param random Random number generator
     */
    public FastLocalMovingAlgorithm(Random random)
    {
        this(DEFAULT_RESOLUTION, DEFAULT_N_ITERATIONS, random);
    }

    /**
     * Constructs a fast local moving algorithm for a specified resolution
     * parameter and number of iterations.
     *
     * @param resolution  Resolution parameter
     * @param nIterations Number of iterations
     * @param random      Random number generator
     */
    public FastLocalMovingAlgorithm(double resolution, int nIterations, Random random)
    {
        super(resolution, nIterations);

        this.random = random;
    }

    /**
     * Improves a clustering by performing one iteration of the fast local
     * moving algorithm.
     *
     * <p>
     * The fast local moving algorithm first adds all nodes in a network to a
     * queue. It then removes a node from the queue. The node is moved to the
     * cluster that results in the largest increase in the quality function. If
     * the current cluster assignment of the node is already optimal, the node
     * is not moved. If the node is moved to a different cluster, the neighbors
     * of the node that do not belong to the node's new cluster and that are
     * not yet in the queue are added to the queue. The algorithm continues
     * removing nodes from the queue until the queue is empty.
     * </p>
     *
     * @param network    Network
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    protected boolean improveClusteringOneIteration(Network network, Clustering clustering)
    {
        boolean update;
        boolean[] stableNodes;
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
         * of the array has been reached, start again from the beginning. The
         * queue of nodes that still need to be visited is given by
         * nodeOrder[i], ..., nodeOrder[i + nUnstableNodes - 1]. Continue
         * iterating until the queue is empty.
         */
        edgeWeightPerCluster = new double[network.nNodes];
        neighboringClusters = new int[network.nNodes];
        stableNodes = new boolean[network.nNodes];
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

            /*
             * Mark the currently selected node as stable and remove it from
             * the queue.
             */
            stableNodes[j] = true;
            nUnstableNodes--;

            /*
             * If the new cluster of the currently selected node is different
             * from the old cluster, some further updating of the clustering
             * statistics is performed. Also, the neighbors of the currently
             * selected node that do not belong to the new cluster are marked
             * as unstable and are added to the queue.
             */
            if (bestCluster != currentCluster)
            {
                clustering.clusters[j] = bestCluster;
                if (bestCluster >= clustering.nClusters)
                    clustering.nClusters = bestCluster + 1;

                for (k = network.firstNeighborIndices[j]; k < network.firstNeighborIndices[j + 1]; k++)
                    if (stableNodes[network.neighbors[k]] && (clustering.clusters[network.neighbors[k]] != bestCluster))
                    {
                        stableNodes[network.neighbors[k]] = false;
                        nUnstableNodes++;
                        nodeOrder[(i + nUnstableNodes < network.nNodes) ? (i + nUnstableNodes) : (i + nUnstableNodes - network.nNodes)] = network.neighbors[k];
                    }

                update = true;
            }

            i = (i < network.nNodes - 1) ? (i + 1) : 0;
        } while (nUnstableNodes > 0);

        if (update)
            clustering.removeEmptyClusters();

        return update;
    }
}
