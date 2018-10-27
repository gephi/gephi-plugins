package cwts.networkanalysis;

import cwts.util.Arrays;
import java.util.Random;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * Standard local moving algorithm.
 *
 * <p>
 * The standard local moving algorithm iterates over the nodes in a graph. A
 * node is moved to the cluster that results in the largest increase in the
 * quality function. If the current cluster assignment of the node is already
 * optimal, the node is not moved. The algorithm continues iterating over the
 * nodes in a graph until no more nodes can be moved.
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
     * Improves a clustering of the nodes in a graph using the standard local
     * moving algorithm.
     *
     * <p>
     * The standard local moving algorithm iterates over the nodes in a
     * graph. A node is moved to the cluster that results in the largest
     * increase in the quality function. If the current cluster assignment of
     * the node is already optimal, the node is not moved. The algorithm
     * continues iterating over the nodes in a graph until no more nodes can
     * be moved.
     * </p>
     *
     * @param graph    Graph
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    public boolean improveClustering(NodeWeightGraph nodeWeightGraph, Clustering clustering)
    {
        boolean update;
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
         * of the array has been reached, start again from the beginning.
         * Continue iterating until none of the last nNodes node visits has
         * resulted in a node movement.
         */
        edgeWeightPerCluster = new double[graph.getNodeCount()];
        neighboringClusters = new int[graph.getNodeCount()];
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
            nUnstableNodes--;

            /*
             * If the new cluster of the currently selected node is different
             * from the old cluster, some further updating of the clustering
             * statistics is performed.
             */
            if (bestCluster != currentCluster)
            {
                clustering.clusters[nodeId] = bestCluster;
                if (bestCluster >= clustering.nClusters)
                    clustering.nClusters = bestCluster + 1;

                nUnstableNodes = graph.getNodeCount();

                update = true;
            }

            i = (i < graph.getNodeCount() - 1) ? (i + 1) : 0;
        } while (nUnstableNodes > 0);

        if (update)
            clustering.removeEmptyClusters();

        return update;
    }
}
