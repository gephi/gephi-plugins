package cwts.networkanalysis;

import java.util.Random;
import org.gephi.graph.api.Graph;

/**
 * Louvain algorithm.
 *
 * <p>
 * The Louvain algorithm consists of two phases:
 * </p>
 *
 * <ol>
 * <li>local moving of nodes between clusters,</li>
 * <li>aggregation of the graph based on the clusters.</li>
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
     * Improves a clustering by performing one iteration of the Louvain
     * algorithm.
     *
     * <p>
     * The Louvain algorithm consists of two phases:
     * </p>
     *
     * <ol>
     * <li>local moving of nodes between clusters,</li>
     * <li>aggregation of the graph based on the clusters.</li>
     * </ol>
     *
     * <p>
     * These phases are repeated until no further improvements can be made.
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
        Clustering reducedClustering;
        NodeWeightGraph reducedGraph;

        Graph graph = nodeWeightGraph.getGraph();

        // Update the clustering by moving individual nodes between clusters.
        update = localMovingAlgorithm.improveClustering(nodeWeightGraph, clustering);

        /*
         * Terminate the algorithm if each node is assigned to its own cluster.
         * Otherwise create an aggregate graph and recursively apply the
         * algorithm to this graph.
         */
        if (clustering.nClusters < graph.getNodeCount())
        {
            /*
             * Create an aggregate graph based on the clustering of the
             * non-aggregate graph.
             */
            reducedGraph = nodeWeightGraph.createReducedGraph(clustering);

            /*
             * Recursively apply the algorithm to the aggregate graph,
             * starting from a singleton clustering.
             */
            reducedClustering = new Clustering(reducedGraph.getGraph().getNodeCount());
            update |= improveClusteringOneIteration(reducedGraph, reducedClustering);

            /*
             * Update the clustering of the non-aggregate graph so that it
             * coincides with the final clustering obtained for the aggregate
             * graph.
             */
            clustering.mergeClusters(reducedClustering);
        }

        return update;
    }
}
