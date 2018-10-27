package cwts.networkanalysis;

import org.gephi.graph.api.Graph;

/**
 * Abstract base class for iterative clustering algorithms that use the CPM
 * quality function.
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public abstract class IterativeCPMClusteringAlgorithm extends IncrementalCPMClusteringAlgorithm
{
    /**
     * Default number of iterations.
     */
    public static final int DEFAULT_N_ITERATIONS = 1;

    /**
     * Number of iterations.
     */
    protected int nIterations;

    /**
     * Constructs an iterative CPM clustering algorithm.
     */
    public IterativeCPMClusteringAlgorithm()
    {
        this(DEFAULT_RESOLUTION, DEFAULT_N_ITERATIONS);
    }

    /**
     * Constructs an iterative CPM clustering algorithm with a specified
     * resolution parameter and number of iterations.
     *
     * @param resolution  Resolution parameter
     * @param nIterations Number of iterations
     */
    public IterativeCPMClusteringAlgorithm(double resolution, int nIterations)
    {
        super(resolution);

        this.nIterations = nIterations;
    }

    /**
     * Returns the number of iterations.
     *
     * @return Number of iterations
     */
    public int getNIterations()
    {
        return nIterations;
    }

    /**
     * Sets the number of iterations.
     *
     * @param nIterations Number of iterations
     */
    public void setNIterations(int nIterations)
    {
        this.nIterations = nIterations;
    }

    /**
     * Improves a clustering of the nodes in a graph.
     *
     * <p>
     * If the number of iterations {@code nIterations} is positive, the
     * clustering is improved by making {@code nIterations} calls to {@link
     * #improveClusteringOneIteration(Graph graph, Clustering clustering)}.
     * If {@code nIterations} equals 0, calls to {@link
     * #improveClusteringOneIteration(Graph graph, Clustering clustering)}
     * continue to be made until there has been a call that did not result in
     * an improvement of the clustering.
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
        int i;

        update = false;
        if (nIterations > 0)
            for (i = 0; i < nIterations; i++)
                update |= improveClusteringOneIteration(nodeWeightGraph, clustering);
        else
            while (improveClusteringOneIteration(nodeWeightGraph, clustering))
                update = true;
        return update;
    }

    /**
     * Improves a clustering by performing one iteration of an iterative
     * clustering algorithm.
     *
     * @param graph    Graph
     * @param clustering Clustering
     *
     * @return Boolean indicating whether the clustering has been improved
     */
    protected abstract boolean improveClusteringOneIteration(NodeWeightGraph nodeWeightGraph, Clustering clustering);
}
