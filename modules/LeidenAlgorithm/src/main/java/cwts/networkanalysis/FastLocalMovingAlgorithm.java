package cwts.networkanalysis;

import cwts.util.Arrays;
import java.util.Random;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * Fast local moving algorithm.
 *
 * <p>
 * The fast local moving algorithm first adds all nodes in a graph to a
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
     * The fast local moving algorithm first adds all nodes in a graph to a
     * queue. It then removes a node from the queue. The node is moved to the
     * cluster that results in the largest increase in the quality function. If
     * the current cluster assignment of the node is already optimal, the node
     * is not moved. If the node is moved to a different cluster, the neighbors
     * of the node that do not belong to the node's new cluster and that are
     * not yet in the queue are added to the queue. The algorithm continues
     * removing nodes from the queue until the queue is empty.
     * </p>
     *
     * @param graph    Graph
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    protected boolean improveClusteringOneIteration(NodeWeightGraph nodeWeightGraph, Clustering clustering)
    {
        boolean update;
        boolean[] stableNodes;
        double maxQualityValueIncrement, qualityValueIncrement;
        double[] clusterWeights, edgeWeightPerCluster;
        int bestCluster, currentCluster, i, nodeId, k, l, nNeighboringClusters, nUnstableNodes, nUnusedClusters;
        int[] neighboringClusters, nNodesPerCluster, nodeOrder, unusedClusters;

        Graph graph = nodeWeightGraph.getGraph();

        if (graph.getNodeCount() == 1)
            return false;

        update = false;

        clusterWeights = new double[graph.getNodeCount()];
        nNodesPerCluster = new int[graph.getNodeCount()];
        for (Node node : graph.getNodes())
        {
            clusterWeights[clustering.clusters[node.getStoreId()]] += nodeWeightGraph.getNodeWeight(node);
            nNodesPerCluster[clustering.clusters[node.getStoreId()]]++;
        }

        nUnusedClusters = 0;
        unusedClusters = new int[graph.getNodeCount() - 1];
        for (i = graph.getNodeCount() - 1; i >= 0; i--)
            if (nNodesPerCluster[i] == 0)
            {
                unusedClusters[nUnusedClusters] = i;
                nUnusedClusters++;
            }

        nodeOrder = Arrays.generateRandomPermutation(graph.getNodeCount(), random);

        /*
         * Iterate over the nodeOrder array in a cyclical manner. When the end
         * of the array has been reached, start again from the beginning. The
         * queue of nodes that still need to be visited is given by
         * nodeOrder[i], ..., nodeOrder[i + nUnstableNodes - 1]. Continue
         * iterating until the queue is empty.
         */
        edgeWeightPerCluster = new double[graph.getNodeCount()];
        neighboringClusters = new int[graph.getNodeCount()];
        stableNodes = new boolean[graph.getNodeCount()];
        nUnstableNodes = graph.getNodeCount();
        i = 0;
        do
        {
            nodeId = nodeOrder[i];

            Node node = graph.getNode(nodeId);

            currentCluster = clustering.clusters[nodeId];

            // Remove the currently selected node from its current cluster.
            clusterWeights[currentCluster] -= nodeWeightGraph.getNodeWeight(nodeId);
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

            for (Node neighbor : graph.getNeighbors(node))
            {
                l = clustering.clusters[neighbor.getStoreId()];
                if (edgeWeightPerCluster[l] == 0)
                {
                    neighboringClusters[nNeighboringClusters] = l;
                    nNeighboringClusters++;
                }
                edgeWeightPerCluster[l] += graph.getEdge(node, neighbor).getWeight();
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
            maxQualityValueIncrement = edgeWeightPerCluster[currentCluster] - nodeWeightGraph.getNodeWeight(nodeId) * clusterWeights[currentCluster] * resolution;
            for (k = 0; k < nNeighboringClusters; k++)
            {
                l = neighboringClusters[k];

                qualityValueIncrement = edgeWeightPerCluster[l] - nodeWeightGraph.getNodeWeight(nodeId) * clusterWeights[l] * resolution;
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
            clusterWeights[bestCluster] += nodeWeightGraph.getNodeWeight(nodeId);
            nNodesPerCluster[bestCluster]++;
            if (bestCluster == unusedClusters[nUnusedClusters - 1])
                nUnusedClusters--;

            /*
             * Mark the currently selected node as stable and remove it from
             * the queue.
             */
            stableNodes[nodeId] = true;
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
                clustering.clusters[nodeId] = bestCluster;
                if (bestCluster >= clustering.nClusters)
                    clustering.nClusters = bestCluster + 1;

                for (Node neighbor : graph.getNeighbors(node))
                {
                    if (stableNodes[neighbor.getStoreId()] && (clustering.clusters[neighbor.getStoreId()] != bestCluster))
                    {
                        stableNodes[neighbor.getStoreId()] = false;
                        nUnstableNodes++;
                        nodeOrder[(i + nUnstableNodes < graph.getNodeCount()) ? (i + nUnstableNodes) : (i + nUnstableNodes - graph.getNodeCount())] = neighbor.getStoreId();
                    }
                }
                update = true;
            }

            i = (i < graph.getNodeCount() - 1) ? (i + 1) : 0;
        } while (nUnstableNodes > 0);

        if (update)
            clustering.removeEmptyClusters();

        return update;
    }
}
