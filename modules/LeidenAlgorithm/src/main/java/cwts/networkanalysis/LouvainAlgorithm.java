package cwts.networkanalysis;

import java.util.Random;

/**
 * Louvain algorithm.
 *
 * <p>
 * The Louvain algorithm consists of two phases:
 * </p>
 *
 * <ol>
 * <li>local moving of nodes between clusters,</li>
 * <li>aggregation of the network based on the clusters.</li>
 * </ol>
 *
 * <p>
 * These phases are repeated until no further improvements can be made. By
 * default, local moving of nodes is performed using the {@link
 * StandardLocalMovingAlgorithm}.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class LouvainAlgorithm extends IterativeCPMClusteringAlgorithm
{
    /**
     * Local moving algorithm.
     */
    protected IncrementalCPMClusteringAlgorithm localMovingAlgorithm;

    /**
     * Constructs a Louvain algorithm.
     */
    public LouvainAlgorithm()
    {
        this(new Random());
    }

    /**
     * Constructs a Louvain algorithm.
     *
     * @param random Random number generator
     */
    public LouvainAlgorithm(Random random)
    {
        this(DEFAULT_RESOLUTION, DEFAULT_N_ITERATIONS, random);
    }

    /**
     * Constructs a Louvain algorithm for a specified resolution parameter and
     * number of iterations.
     *
     * @param resolution  Resolution parameter
     * @param nIterations Number of iterations
     * @param random      Random number generator
     */
    public LouvainAlgorithm(double resolution, int nIterations, Random random)
    {
        this(resolution, nIterations, new StandardLocalMovingAlgorithm(random));
    }

    /**
     * Constructs a Louvain algorithm for a specified resolution parameter,
     * number of iterations, and local moving algorithm.
     *
     * @param resolution           Resolution parameter
     * @param nIterations          Number of iterations
     * @param localMovingAlgorithm Local moving algorithm
     */
    public LouvainAlgorithm(double resolution, int nIterations, IncrementalCPMClusteringAlgorithm localMovingAlgorithm)
    {
        super(resolution, nIterations);

        setLocalMovingAlgorithm(localMovingAlgorithm);
    }

    /**
     * Clones the algorithm.
     *
     * @return Cloned algorithm
     */
    public LouvainAlgorithm clone()
    {
        LouvainAlgorithm LouvainAlgorithm;

        LouvainAlgorithm = (LouvainAlgorithm)super.clone();
        LouvainAlgorithm.localMovingAlgorithm = (IncrementalCPMClusteringAlgorithm)localMovingAlgorithm.clone();
        return LouvainAlgorithm;
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
     * Improves a clustering by performing one iteration of the Louvain
     * algorithm.
     *
     * <p>
     * The Louvain algorithm consists of two phases:
     * </p>
     *
     * <ol>
     * <li>local moving of nodes between clusters,</li>
     * <li>aggregation of the network based on the clusters.</li>
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
        Clustering reducedClustering;
        Network reducedNetwork;

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
             * Create an aggregate network based on the clustering of the
             * non-aggregate network.
             */
            reducedNetwork = network.createReducedNetwork(clustering);

            /*
             * Recursively apply the algorithm to the aggregate network,
             * starting from a singleton clustering.
             */
            reducedClustering = new Clustering(reducedNetwork.getNNodes());
            update |= improveClusteringOneIteration(reducedNetwork, reducedClustering);

            /*
             * Update the clustering of the non-aggregate network so that it
             * coincides with the final clustering obtained for the aggregate
             * network.
             */
            clustering.mergeClusters(reducedClustering);
        }

        return update;
    }
}
