package cwts.networkanalysis;

import java.util.Arrays;
import java.util.Random;

/**
 * Leiden algorithm.
 *
 * <p>
 * The Leiden algorithm consists of three phases:
 * </p>
 *
 * <ol>
 * <li>local moving of nodes between clusters,</li>
 * <li>refinement of the clusters,</li>
 * <li>aggregation of the network based on the refined clusters, using the
 * non-refined clusters to create an initial clustering for the aggregate
 * network.</li>
 * </ol>
 *
 * <p>
 * These phases are repeated until no further improvements can be made. By
 * default, local moving of nodes is performed using the {@link
 * FastLocalMovingAlgorithm}.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class LeidenAlgorithm extends IterativeCPMClusteringAlgorithm
{
    /**
     * Default randomness parameter.
     */
    public static final double DEFAULT_RANDOMNESS = LocalMergingAlgorithm.DEFAULT_RANDOMNESS;

    /**
     * Randomness parameter.
     */
    protected double randomness;

    /**
     * Local moving algorithm.
     */
    protected IncrementalCPMClusteringAlgorithm localMovingAlgorithm;

    /**
     * Random number generator.
     */
    protected Random random;

    /**
     * Constructs a Leiden algorithm.
     */
    public LeidenAlgorithm()
    {
        this(new Random());
    }

    /**
     * Constructs a Leiden algorithm.
     *
     * @param random Random number generator
     */
    public LeidenAlgorithm(Random random)
    {
        this(DEFAULT_RESOLUTION, DEFAULT_N_ITERATIONS, DEFAULT_RANDOMNESS, random);
    }

    /**
     * Constructs a Leiden algorithm for a specified resolution parameter,
     * number of iterations, and randomness parameter.
     *
     * @param resolution  Resolution parameter
     * @param nIterations Number of iterations
     * @param randomness  Randomness parameter
     * @param random      Random number generator
     */
     public LeidenAlgorithm(double resolution, int nIterations, double randomness, Random random)
     {
         this(resolution, nIterations, randomness, new FastLocalMovingAlgorithm(random), random);
     }

     /**
     * Constructs a Leiden algorithm for a specified resolution parameter,
     * number of iterations, randomness parameter, and local moving algorithm.
     *
     * @param resolution           Resolution parameter
     * @param nIterations          Number of iterations
     * @param randomness           Randomness parameter
     * @param localMovingAlgorithm Local moving algorithm
     * @param random               Random number generator
     */
    public LeidenAlgorithm(double resolution, int nIterations, double randomness, IncrementalCPMClusteringAlgorithm localMovingAlgorithm, Random random)
    {
        super(resolution, nIterations);

        this.randomness = randomness;
        this.random = random;
        setLocalMovingAlgorithm(localMovingAlgorithm);
    }

    /**
     * Clones the algorithm.
     *
     * @return Cloned algorithm
     */
    public LeidenAlgorithm clone()
    {
        LeidenAlgorithm LeidenAlgorithm;

        LeidenAlgorithm = (LeidenAlgorithm)super.clone();
        LeidenAlgorithm.localMovingAlgorithm = (IncrementalCPMClusteringAlgorithm)localMovingAlgorithm.clone();
        return LeidenAlgorithm;
    }

    /**
     * Returns the randomness parameter.
     *
     * @return Randomness parameter
     */
    public double getRandomness()
    {
        return randomness;
    }

    /**
     * Returns the local moving algorithm.
     *
     * @return Local moving algorithm
     */
    public IncrementalCPMClusteringAlgorithm getLocalMovingAlgorithm()
    {
        return (IncrementalCPMClusteringAlgorithm)localMovingAlgorithm.clone();
    }

    /**
     * Sets the randomness parameter.
     *
     * @param randomness Randomness parameter
     */
    public void setRandomness(double randomness)
    {
        this.randomness = randomness;
    }

    /**
     * Sets the local moving algorithm.
     *
     * @param localMovingAlgorithm Local moving algorithm
     */
    public void setLocalMovingAlgorithm(IncrementalCPMClusteringAlgorithm localMovingAlgorithm)
    {
        this.localMovingAlgorithm = (IncrementalCPMClusteringAlgorithm)localMovingAlgorithm.clone();
        this.localMovingAlgorithm.resolution = resolution;
    }

    /**
     * Sets the resolution parameter.
     *
     * Also ensures the resolution parameter for the local moving algorithm
     * is updated.
     *
     * @param resolution Resolution parameter
     */
    @Override
    public void setResolution(double resolution)
    {
        super.setResolution(resolution);
        this.localMovingAlgorithm.resolution = resolution;
    }

    /**
     * Improves a clustering by performing one iteration of the Leiden
     * algorithm.
     *
     * <p>
     * The Leiden algorithm consists of three phases:
     * </p>
     *
     * <ol>
     * <li>local moving of nodes between clusters,</li>
     * <li>refinement of the clusters,</li>
     * <li>aggregation of the network based on the refined clusters, using the
     * non-refined clusters to create an initial clustering for the aggregate
     * network.</li>
     * </ol>
     *
     * <p>
     * These phases are repeated until no further improvements can be made.
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
        Clustering clusteringReducedNetwork, clusteringSubnetwork;
        int i, j;
        int[] clustersReducedNetwork, nNodesPerClusterReducedNetwork;
        int[][] nodesPerCluster;
        LocalMergingAlgorithm localMergingAlgorithm;
        Network reducedNetwork;
        Network[] subnetworks;

        // Update the clustering by moving individual nodes between clusters.
        update = localMovingAlgorithm.improveClustering(network, clustering);

        /*
         * Terminate the algorithm if each node is assigned to its own cluster.
         * Otherwise create an aggregate network and recursively apply the
         * algorithm to this network.
         */
        if (clustering.nClusters < network.nNodes)
        {
            /*
             * Refine the clustering by iterating over the clusters and by
             * trying to split up each cluster into multiple clusters.
             */
            localMergingAlgorithm = new LocalMergingAlgorithm(resolution, randomness, random);
            subnetworks = network.createSubnetworks(clustering);
            nodesPerCluster = clustering.getNodesPerCluster();
            clustering.nClusters = 0;
            nNodesPerClusterReducedNetwork = new int[subnetworks.length];
            for (i = 0; i < subnetworks.length; i++)
            {
                clusteringSubnetwork = localMergingAlgorithm.findClustering(subnetworks[i]);

                for (j = 0; j < subnetworks[i].nNodes; j++)
                    clustering.clusters[nodesPerCluster[i][j]] = clustering.nClusters + clusteringSubnetwork.clusters[j];
                clustering.nClusters += clusteringSubnetwork.nClusters;
                nNodesPerClusterReducedNetwork[i] = clusteringSubnetwork.nClusters;
            }

            /*
             * Create an aggregate network based on the refined clustering of
             * the non-aggregate network.
             */
            reducedNetwork = network.createReducedNetwork(clustering);

            /*
             * Create an initial clustering for the aggregate network based on
             * the non-refined clustering of the non-aggregate network.
             */
            clustersReducedNetwork = new int[clustering.nClusters];
            i = 0;
            for (j = 0; j < nNodesPerClusterReducedNetwork.length; j++)
            {
                Arrays.fill(clustersReducedNetwork, i, i + nNodesPerClusterReducedNetwork[j], j);
                i += nNodesPerClusterReducedNetwork[j];
            }
            clusteringReducedNetwork = new Clustering(clustersReducedNetwork);

            /*
             * Recursively apply the algorithm to the aggregate network,
             * starting from the initial clustering created for this network.
             */
            update |= improveClusteringOneIteration(reducedNetwork, clusteringReducedNetwork);

            /*
             * Update the clustering of the non-aggregate network so that it
             * coincides with the final clustering obtained for the aggregate
             * network.
             */
            clustering.mergeClusters(clusteringReducedNetwork);
        }

        return update;
    }
}
