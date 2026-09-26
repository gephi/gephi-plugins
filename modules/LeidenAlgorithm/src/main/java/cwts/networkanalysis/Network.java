package cwts.networkanalysis;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Network.
 *
 * <p>
 * Weighted nodes and weighted edges are supported. Directed edges are not
 * supported.
 * </p>
 *
 * <p>
 * Network objects are immutable.
 * </p>
 *
 * <p>
 * The adjacency matrix of the network is stored in a sparse compressed format.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class Network implements Serializable
{
    private static final long serialVersionUID = 1;

    /**
     * Number of nodes.
     */
    protected int nNodes;

    /**
     * Number of edges.
     *
     * <p>
     * Each edge is counted twice, once in each direction.
     * </p>
     */
    protected int nEdges;

    /**
     * Node weights.
     */
    protected double[] nodeWeights;

    /**
     * Index of the first neighbor of each node in the (@code neighbors} array.
     *
     * <p>
     * The neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}.
     * </p>
     */
    protected int[] firstNeighborIndices;

    /**
     * Neighbors of each node.
     */
    protected int[] neighbors;

    /**
     * Edge weights.
     */
    protected double[] edgeWeights;

    /**
     * Total edge weight of self links.
     */
    protected double totalEdgeWeightSelfLinks;

    /**
     * Loads a network from a file.
     *
     * @param filename File from which a network is loaded
     *
     * @return Loaded network
     *
     * @throws ClassNotFoundException Class not found
     * @throws IOException            Could not read the file
     *
     * @see #save(String filename)
     */
    public static Network load(String filename) throws ClassNotFoundException, IOException
    {
        Network network;
        ObjectInputStream objectInputStream;

        objectInputStream = new ObjectInputStream(new FileInputStream(filename));

        network = (Network)objectInputStream.readObject();

        objectInputStream.close();

        return network;
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]}. Edges do not have weights. If {@code sortedEdges} is
     * false, the list of edges does not need to be sorted and each edge must
     * be included only once. If {@code sortedEdges}is true, the list of edges
     * must be sorted and each edge must be included twice, once in each
     * direction.
     * </p>
     *
     * @param nodeWeights    Node weights
     * @param edges          Edge list
     * @param sortedEdges    Indicates whether the edge list is sorted
     * @param checkIntegrity Indicates whether to check the integrity of the
     *                       network
     */
    public Network(double[] nodeWeights, int[][] edges, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, edges, null, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]} and has weight {@code edgeWeights[i]}. If {@code
     * sortedEdges} is false, the list of edges does not need to be sorted and
     * each edge must be included only once. If {@code sortedEdges} is true,
     * the list of edges must be sorted and each edge must be included twice,
     * once in each direction.
     * </p>
     *
     * @param nodeWeights    Node weights
     * @param edges          Edge list
     * @param edgeWeights    Edge weights
     * @param sortedEdges    Indicates whether the edge list is sorted
     * @param checkIntegrity Indicates whether to check the integrity of the
     *                       network
     */
    public Network(double[] nodeWeights, int[][] edges, double[] edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, edges, edgeWeights, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. Edges do not have weights.
     * </p>
     *
     * @param nodeWeights          Node weights
     * @param firstNeighborIndices Index of the first neighbor of each node
     * @param neighbors            Neighbor list
     * @param checkIntegrity       Indicates whether to check the integrity of
     *                             the network
     */
    public Network(double[] nodeWeights, int[] firstNeighborIndices, int[] neighbors, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, firstNeighborIndices, neighbors, null, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. For each neighbor in the array {@code neighbors}, the
     * corresponding edge weight is provided in the array {@code edgeWeights}.
     * </p>
     *
     * @param nodeWeights          Node weights
     * @param firstNeighborIndices Index of the first neighbor of each node
     * @param neighbors            Neighbor list
     * @param edgeWeights          Edge weights
     * @param checkIntegrity       Indicates whether to check the integrity of
     *                             the network
     */
    public Network(double[] nodeWeights, int[] firstNeighborIndices, int[] neighbors, double[] edgeWeights, boolean checkIntegrity)
    {
        this(nodeWeights.length, nodeWeights, false, firstNeighborIndices, neighbors, edgeWeights, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]}. Edges do not have weights. If {@code sortedEdges} is
     * false, the list of edges does not need to be sorted and each edge must
     * be included only once. If {@code sortedEdges}is true, the list of edges
     * must be sorted and each edge must be included twice, once in each
     * direction.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param edges                            Edge list
     * @param sortedEdges                      Indicates whether the edge list
     *                                         is sorted
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[][] edges, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, edges, null, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of edges.
     *
     * <p>
     * The list of edges is provided in the two-dimensional array {@code
     * edges}. Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]} and has weight {@code edgeWeights[i]}. If {@code
     * sortedEdges} is false, the list of edges does not need to be sorted and
     * each edge must be included only once. If {@code sortedEdges} is true,
     * the list of edges must be sorted and each edge must be included twice,
     * once in each direction.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param edges                            Edge list
     * @param edgeWeights                      Edge weights
     * @param sortedEdges                      Indicates whether the edge list
     *                                         is sorted
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[][] edges, double[] edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, edges, edgeWeights, sortedEdges, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. Edges do not have weights.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param firstNeighborIndices             Index of the first neighbor of
     *                                         each node
     * @param neighbors                        Neighbor list
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[] firstNeighborIndices, int[] neighbors, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, firstNeighborIndices, neighbors, null, checkIntegrity);
    }

    /**
     * Constructs a network based on a list of neighbors.
     *
     * <p>
     * The list of neighbors is provided in the array {@code neighbors}. The
     * neighbors of node {@code i} are given by {@code
     * neighbors[firstNeighborIndices[i]], ...,
     * neighbors[firstNeighborIndices[i + 1] - 1]}. The array {@code
     * firstNeighborIndices} must have a length of the number of nodes plus 1.
     * The neighbors of a node must be listed in increasing order in the array
     * {@code neighbors}. For each neighbor in the array {@code neighbors}, the
     * corresponding edge weight is provided in the array {@code edgeWeights}.
     * </p>
     *
     * <p>
     * If {@code setNodeWeightsToTotalEdgeWeights} is false, the weights of the
     * nodes are set to 1. If {@code setNodeWeightsToTotalEdgeWeights} is true,
     * the weight of a node is set equal to the total weight of the edges
     * between the node and its neighbors.
     * </p>
     *
     * @param nNodes                           Number of nodes
     * @param setNodeWeightsToTotalEdgeWeights Indicates whether to set node
     *                                         weights equal to total edge
     *                                         weights
     * @param firstNeighborIndices             Index of the first neighbor of
     *                                         each node
     * @param neighbors                        Neighbor list
     * @param edgeWeights                      Edge weights
     * @param checkIntegrity                   Indicates whether to check the
     *                                         integrity of the network
     */
    public Network(int nNodes, boolean setNodeWeightsToTotalEdgeWeights, int[] firstNeighborIndices, int[] neighbors, double[] edgeWeights, boolean checkIntegrity)
    {
        this(nNodes, null, setNodeWeightsToTotalEdgeWeights, firstNeighborIndices, neighbors, edgeWeights, checkIntegrity);
    }

    /**
     * Saves the network in a file.
     *
     * @param filename File in which the network is saved
     *
     * @throws IOException Could not write to the file
     *
     * @see #load(String filename)
     */
    public void save(String filename) throws IOException
    {
        ObjectOutputStream objectOutputStream;

        objectOutputStream = new ObjectOutputStream(new FileOutputStream(filename));

        objectOutputStream.writeObject(this);

        objectOutputStream.close();
    }

    /**
     * Returns the number of nodes.
     *
     * @return Number of nodes
     */
    public int getNNodes()
    {
        return nNodes;
    }

    /**
     * Returns the total node weight.
     *
     * @return Total node weight
     */
    public double getTotalNodeWeight()
    {
        return cwts.util.Arrays.calcSum(nodeWeights);
    }

    /**
     * Returns the weight of each node.
     *
     * @return Weight of each node
     */
    public double[] getNodeWeights()
    {
        return nodeWeights.clone();
    }

    /**
     * Returns the weight of a node.
     *
     * @param node Node
     *
     * @return Weight
     */
    public double getNodeWeight(int node)
    {
        return nodeWeights[node];
    }

    /**
     * Returns the number of edges.
     *
     * <p>
     * Each edge is counted only once, even though an edge runs in two
     * directions. This means that the number of edges returned by {@link
     * #getEdges()} equals twice the number of edges returned by {@link
     * #getNEdges()}.
     * </p>
     *
     * @return Number of edges
     */
    public int getNEdges()
    {
        return nEdges / 2;
    }

    /**
     * Returns the number of neighbors per node.
     *
     * @return Number of neighbors per node
     */
    public int[] getNNeighborsPerNode()
    {
        int i;
        int[] nNeighborsPerNode;

        nNeighborsPerNode = new int[nNodes];
        for (i = 0; i < nNodes; i++)
            nNeighborsPerNode[i] = firstNeighborIndices[i + 1] - firstNeighborIndices[i];
        return nNeighborsPerNode;
    }

    /**
     * Returns the number of neighbors of a node.
     *
     * @param node Node
     *
     * @return Number of neighbors
     */
    public int getNNeighbors(int node)
    {
        return firstNeighborIndices[node + 1] - firstNeighborIndices[node];
    }

    /**
     * Returns the list of edges.
     *
     * <p>
     * Each edge is included twice, once in each direction. This means that the
     * number of edges returned by {@link #getEdges()} equals twice the number
     * of edges returned by {@link #getNEdges()}.
     * </p>
     *
     * <p>
     * The list of edges is returned in a two-dimensional array {@code edges}.
     * Edge {@code i} connects nodes {@code edges[0][i]} and {@code
     * edges[1][i]}.
     * </p>
     *
     * @return List of edges
     */
    public int[][] getEdges()
    {
        int i;
        int[][] edges;

        edges = new int[2][];
        edges[0] = new int[nEdges];
        for (i = 0; i < nNodes; i++)
            Arrays.fill(edges[0], firstNeighborIndices[i], firstNeighborIndices[i + 1], i);
        edges[1] = neighbors.clone();
        return edges;
    }

    /**
     * Returns a list of neighbors per node.
     *
     * @return List of neighbors per node
     */
    public int[][] getNeighborsPerNode()
    {
        int i;
        int[][] neighborsPerNode;

        neighborsPerNode = new int[nNodes][];
        for (i = 0; i < nNodes; i++)
            neighborsPerNode[i] = Arrays.copyOfRange(neighbors, firstNeighborIndices[i], firstNeighborIndices[i + 1]);
        return neighborsPerNode;
    }

    /**
     * Returns the list of neighbors of a node.
     *
     * @param node Node
     *
     * @return List of neighbors
     */
    public int[] getNeighbors(int node)
    {
        return Arrays.copyOfRange(neighbors, firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns the total edge weight.
     *
     * <p>
     * Each edge is considered only once, even though an edge runs in two
     * directions. This means that the sum of the edge weights returned by
     * {@link #getEdgeWeights()} equals twice the total edge weight returned by
     * {@link #getTotalEdgeWeight()}.
     * </p>
     *
     * <p>
     * Edge weights of self links are not included.
     * </p>
     *
     * @return Total edge weight
     */
    public double getTotalEdgeWeight()
    {
        return cwts.util.Arrays.calcSum(edgeWeights) / 2;
    }

    /**
     * Returns the total edge weight per node. The total edge weight of a node
     * equals the sum of the weights of the edges between the node and its
     * neighbors.
     *
     * @return Total edge weight per node
     */
    public double[] getTotalEdgeWeightPerNode()
    {
        return getTotalEdgeWeightPerNodeHelper();
    }

    /**
     * Returns the total edge weight of a node. The total edge weight of a node
     * equals the sum of the weights of the edges between the node and its
     * neighbors.
     *
     * @param node Node
     *
     * @return Total edge weight
     */
    public double getTotalEdgeWeight(int node)
    {
        return cwts.util.Arrays.calcSum(edgeWeights, firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns the edge weights.
     *
     * <p>
     * Each edge is included twice, once in each direction. This means that the
     * sum of the edge weights returned by {@link #getEdgeWeights()} equals
     * twice the total edge weight returned by {@link #getTotalEdgeWeight()}.
     * </p>
     *
     * @return Edge weights
     */
    public double[] getEdgeWeights()
    {
        return edgeWeights.clone();
    }

    /**
     * Returns a list of edge weights per node. These are the weights of the
     * edges between a node and its neighbors.
     *
     * @return List of edge weights per node
     */
    public double[][] getEdgeWeightsPerNode()
    {
        double[][] edgeWeightsPerNode;
        int i;

        edgeWeightsPerNode = new double[nNodes][];
        for (i = 0; i < nNodes; i++)
            edgeWeightsPerNode[i] = Arrays.copyOfRange(edgeWeights, firstNeighborIndices[i], firstNeighborIndices[i + 1]);
        return edgeWeightsPerNode;
    }

    /**
     * Returns the list of edge weights of a node. These are the weights of the
     * edges between the node and its neighbors.
     *
     * @param node Node
     *
     * @return List of edge weights
     */
    public double[] getEdgeWeights(int node)
    {
        return Arrays.copyOfRange(edgeWeights, firstNeighborIndices[node], firstNeighborIndices[node + 1]);
    }

    /**
     * Returns the total edge weight of self links.
     *
     * @return Total edge weight of self links
     */
    public double getTotalEdgeWeightSelfLinks()
    {
        return totalEdgeWeightSelfLinks;
    }

    /**
     * Creates a copy of the network, but without node weights.
     *
     * <p>
     * Each node is assigned a weight of 1.
     * </p>
     *
     * @return Network without node weights
     */
    public Network createNetworkWithoutNodeWeights()
    {
        Network networkWithoutNodeWeights;

        networkWithoutNodeWeights = new Network();
        networkWithoutNodeWeights.nNodes = nNodes;
        networkWithoutNodeWeights.nEdges = nEdges;
        networkWithoutNodeWeights.nodeWeights = cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        networkWithoutNodeWeights.firstNeighborIndices = firstNeighborIndices;
        networkWithoutNodeWeights.neighbors = neighbors;
        networkWithoutNodeWeights.edgeWeights = edgeWeights;
        networkWithoutNodeWeights.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;
        return networkWithoutNodeWeights;
    }

    /**
     * Creates a copy of the network, but without edge weights.
     *
     * <p>
     * Each edge is assigned a weight of 1.
     * </p>
     *
     * @return Network without edge weights
     */
    public Network createNetworkWithoutEdgeWeights()
    {
        Network networkWithoutEdgeWeights;

        networkWithoutEdgeWeights = new Network();
        networkWithoutEdgeWeights.nNodes = nNodes;
        networkWithoutEdgeWeights.nEdges = nEdges;
        networkWithoutEdgeWeights.nodeWeights = nodeWeights;
        networkWithoutEdgeWeights.firstNeighborIndices = firstNeighborIndices;
        networkWithoutEdgeWeights.neighbors = neighbors;
        networkWithoutEdgeWeights.edgeWeights = cwts.util.Arrays.createDoubleArrayOfOnes(nEdges);
        networkWithoutEdgeWeights.totalEdgeWeightSelfLinks = 0;
        return networkWithoutEdgeWeights;
    }

    /**
     * Creates a copy of the network, but without node and edge weights.
     *
     * <p>
     * Each node is assigned a weight of 1, and each edge is assigned a weight
     * of 1.
     * </p>
     *
     * @return Network without node and edge weights
     */
    public Network createNetworkWithoutNodeAndEdgeWeights()
    {
        Network networkWithoutNodeAndEdgeWeights;

        networkWithoutNodeAndEdgeWeights = new Network();
        networkWithoutNodeAndEdgeWeights.nNodes = nNodes;
        networkWithoutNodeAndEdgeWeights.nEdges = nEdges;
        networkWithoutNodeAndEdgeWeights.nodeWeights = cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        networkWithoutNodeAndEdgeWeights.firstNeighborIndices = firstNeighborIndices;
        networkWithoutNodeAndEdgeWeights.neighbors = neighbors;
        networkWithoutNodeAndEdgeWeights.edgeWeights = cwts.util.Arrays.createDoubleArrayOfOnes(nEdges);
        networkWithoutNodeAndEdgeWeights.totalEdgeWeightSelfLinks = 0;
        return networkWithoutNodeAndEdgeWeights;
    }

    /**
     * Creates a copy of the network in which the edge weights have been
     * normalized using the association strength.
     *
     * <p>
     * The normalized weight {@code a'[i][j]} of the edge between nodes {@code
     * i} and {@code j} is given by
     * </p>
     *
     * <blockquote>
     * {@code a'[i][j] = a[i][j] / (n[i] * n[j] / (2 * m))},
     * </blockquote>
     *
     * <p>
     * where {@code a[i][j]} is the non-normalized weight of the edge between
     * nodes {@code i} and {@code j}, {@code n[i]} is the weight of node {@code
     * i}, and {@code m} is half the total node weight.
     * </p>
     *
     * <p>
     * If each node's weight equals the total weight of the edges between the
     * node and its neighbors, the edge weights are normalized by dividing them
     * by the expected edge weights in the random configuration model.
     * </p>
     *
     * <p>
     * The node weights are set to 1.
     * </p>
     *
     * @return Normalized network
     */
    public Network createNormalizedNetworkUsingAssociationStrength()
    {
        double totalNodeWeight;
        int i, j;
        Network normalizedNetwork;

        normalizedNetwork = new Network();

        normalizedNetwork.nNodes = nNodes;
        normalizedNetwork.nEdges = nEdges;
        normalizedNetwork.nodeWeights = cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        normalizedNetwork.firstNeighborIndices = firstNeighborIndices;
        normalizedNetwork.neighbors = neighbors;

        normalizedNetwork.edgeWeights = new double[nEdges];
        totalNodeWeight = getTotalNodeWeight();
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
                normalizedNetwork.edgeWeights[j] = edgeWeights[j] / ((nodeWeights[i] * nodeWeights[neighbors[j]]) / totalNodeWeight);

        normalizedNetwork.totalEdgeWeightSelfLinks = 0;

        return normalizedNetwork;
    }

    /**
     * Creates a copy of the network in which the edge weights have been
     * normalized using fractionalization.
     *
     * <p>
     * The normalized weight {@code a'[i][j]} of the edge between nodes {@code
     * i} and {@code j} is given by
     * </p>
     *
     * <blockquote>
     * {@code a'[i][j] = a[i][j] * (n / n[i] + n / n[j]) / 2},
     * </blockquote>
     *
     * <p>
     * where {@code a[i][j]} is the non-normalized weight of the edge between
     * nodes {@code i} and {@code j}, {@code n[i]} is the weight of node {@code
     * i}, and {@code n} is the number of nodes.
     * </p>
     *
     * <p>
     * The node weights are set to 1.
     * </p>
     *
     * @return Normalized network
     */
    public Network createNormalizedNetworkUsingFractionalization()
    {
        int i, j;
        Network normalizedNetwork;

        normalizedNetwork = new Network();

        normalizedNetwork.nNodes = nNodes;
        normalizedNetwork.nEdges = nEdges;
        normalizedNetwork.nodeWeights = cwts.util.Arrays.createDoubleArrayOfOnes(nNodes);
        normalizedNetwork.firstNeighborIndices = firstNeighborIndices;
        normalizedNetwork.neighbors = neighbors;

        normalizedNetwork.edgeWeights = new double[nEdges];
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
                normalizedNetwork.edgeWeights[j] = edgeWeights[j] / (2 / (nNodes / nodeWeights[i] + nNodes / nodeWeights[neighbors[j]]));

        normalizedNetwork.totalEdgeWeightSelfLinks = 0;

        return normalizedNetwork;
    }

    /**
     * Creates a copy of the network that has been pruned in order to have a
     * specified maximum number of edges.
     *
     * <p>
     * Only the edges with the highest weights are retained in the pruned
     * network. In case of ties, the edges to be retained are selected
     * randomly.
     * </p>
     *
     * @param maxNEdges Maximum number of edges
     *
     * @return Pruned network
     */
    public Network createPrunedNetwork(int maxNEdges)
    {
        return createPrunedNetwork(maxNEdges, new Random());
    }

    /**
     * Creates a copy of the network that has been pruned in order to have a
     * specified maximum number of edges.
     *
     * <p>
     * Only the edges with the highest weights are retained in the pruned
     * network. In case of ties, the edges to be retained are selected
     * randomly.
     * </p>
     *
     * @param maxNEdges Maximum number of edges
     * @param random    Random number generator
     *
     * @return Pruned network
     */
    public Network createPrunedNetwork(int maxNEdges, Random random)
    {
        double edgeWeightThreshold, randomNumberThreshold;
        double[] edgeWeights, randomNumbers, randomNumbersEdgesAtThreshold;
        int i, j, k, nEdgesAboveThreshold, nEdgesAtThreshold;
        Network prunedNetwork;

        maxNEdges *= 2;

        if (maxNEdges >= nEdges)
            return this;

        edgeWeights = new double[nEdges / 2];
        i = 0;
        for (j = 0; j < nNodes; j++)
        {
            k = firstNeighborIndices[j];
            while ((k < firstNeighborIndices[j + 1]) && (neighbors[k] < j))
            {
                edgeWeights[i] = this.edgeWeights[k];
                i++;
                k++;
            }
        }
        Arrays.sort(edgeWeights);
        edgeWeightThreshold = edgeWeights[(nEdges - maxNEdges) / 2];

        nEdgesAboveThreshold = 0;
        while (edgeWeights[nEdges / 2 - nEdgesAboveThreshold - 1] > edgeWeightThreshold)
            nEdgesAboveThreshold++;
        nEdgesAtThreshold = 0;
        while ((nEdgesAboveThreshold + nEdgesAtThreshold < nEdges / 2) && (edgeWeights[nEdges / 2 - nEdgesAboveThreshold - nEdgesAtThreshold - 1] == edgeWeightThreshold))
            nEdgesAtThreshold++;

        randomNumbers = cwts.util.Arrays.createDoubleArrayOfRandomNumbers(nNodes * nNodes, random);

        randomNumbersEdgesAtThreshold = new double[nEdgesAtThreshold];
        i = 0;
        for (j = 0; j < nNodes; j++)
        {
            k = firstNeighborIndices[j];
            while ((k < firstNeighborIndices[j + 1]) && (neighbors[k] < j))
            {
                if (this.edgeWeights[k] == edgeWeightThreshold)
                {
                    randomNumbersEdgesAtThreshold[i] = getRandomNumber(j, neighbors[k], randomNumbers);
                    i++;
                }
                k++;
            }
        }
        Arrays.sort(randomNumbersEdgesAtThreshold);
        randomNumberThreshold = randomNumbersEdgesAtThreshold[nEdgesAboveThreshold + nEdgesAtThreshold - maxNEdges / 2];

        prunedNetwork = new Network();

        prunedNetwork.nNodes = nNodes;
        prunedNetwork.nEdges = maxNEdges;
        prunedNetwork.nodeWeights = nodeWeights;

        prunedNetwork.firstNeighborIndices = new int[nNodes + 1];
        prunedNetwork.neighbors = new int[maxNEdges];
        prunedNetwork.edgeWeights = new double[maxNEdges];
        i = 0;
        for (j = 0; j < nNodes; j++)
        {
            for (k = firstNeighborIndices[j]; k < firstNeighborIndices[j + 1]; k++)
                if ((this.edgeWeights[k] > edgeWeightThreshold) || ((this.edgeWeights[k] == edgeWeightThreshold) && (getRandomNumber(j, neighbors[k], randomNumbers) >= randomNumberThreshold)))
                {
                    prunedNetwork.neighbors[i] = neighbors[k];
                    prunedNetwork.edgeWeights[i] = this.edgeWeights[k];
                    i++;
                }
            prunedNetwork.firstNeighborIndices[j + 1] = i;
        }

        prunedNetwork.totalEdgeWeightSelfLinks = 0;

        return prunedNetwork;
    }

    /**
     * Creates an induced subnetwork for specified nodes.
     *
     * @param nodes Nodes
     *
     * @return Subnetwork
     */
    public Network createSubnetwork(int[] nodes)
    {
        double[] subnetworkEdgeWeights;
        int i, j, k;
        int[] subnetworkNodes, subnetworkNeighbors;
        Network subnetwork;

        subnetwork = new Network();

        subnetwork.nNodes = nodes.length;

        if (subnetwork.nNodes == 1)
        {
            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[1];
            subnetwork.nodeWeights[0] = nodeWeights[nodes[0]];
            subnetwork.firstNeighborIndices = new int[2];
            subnetwork.neighbors = new int[0];
            subnetwork.edgeWeights = new double[0];
        }
        else
        {
            subnetworkNodes = new int[nNodes];
            Arrays.fill(subnetworkNodes, -1);
            for (i = 0; i < nodes.length; i++)
                subnetworkNodes[nodes[i]] = i;

            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[subnetwork.nNodes];
            subnetwork.firstNeighborIndices = new int[subnetwork.nNodes + 1];
            subnetworkNeighbors = new int[nEdges];
            subnetworkEdgeWeights = new double[nEdges];
            for (i = 0; i < subnetwork.nNodes; i++)
            {
                j = nodes[i];
                subnetwork.nodeWeights[i] = nodeWeights[j];
                for (k = firstNeighborIndices[j]; k < firstNeighborIndices[j + 1]; k++)
                    if (subnetworkNodes[neighbors[k]] >= 0)
                    {
                        subnetworkNeighbors[subnetwork.nEdges] = subnetworkNodes[neighbors[k]];
                        subnetworkEdgeWeights[subnetwork.nEdges] = edgeWeights[k];
                        subnetwork.nEdges++;
                    }
                subnetwork.firstNeighborIndices[i + 1] = subnetwork.nEdges;
            }
            subnetwork.neighbors = Arrays.copyOfRange(subnetworkNeighbors, 0, subnetwork.nEdges);
            subnetwork.edgeWeights = Arrays.copyOfRange(subnetworkEdgeWeights, 0, subnetwork.nEdges);
        }

        subnetwork.totalEdgeWeightSelfLinks = 0;

        return subnetwork;
    }

    /**
     * Creates an induced subnetwork for specified nodes.
     *
     * @param nodesInSubnetwork Indicates the nodes to be included in the
     *                          subnetwork.
     *
     * @return Subnetwork
     */
    public Network createSubnetwork(boolean[] nodesInSubnetwork)
    {
        int i, j;
        int[] nodes;

        i = 0;
        for (j = 0; j < nNodes; j++)
            if (nodesInSubnetwork[j])
                i++;
        nodes = new int[i];
        i = 0;
        for (j = 0; j < nNodes; j++)
            if (nodesInSubnetwork[j])
            {
                nodes[i] = j;
                i++;
            }
        return createSubnetwork(nodes);
    }

    /**
     * Creates an induced subnetwork for a specified cluster in a clustering.
     *
     * <p>
     * If subnetworks need to be created for all clusters in a clustering, it
     * is more efficient to use {@link #createSubnetworks(Clustering
     * clustering)}.
     * </p>
     *
     * @param clustering Clustering
     * @param cluster    Cluster
     *
     * @return Subnetwork
     */
    public Network createSubnetwork(Clustering clustering, int cluster)
    {
        double[] subnetworkEdgeWeights;
        int[] subnetworkNeighbors, subnetworkNodes;
        int[][] nodesPerCluster;

        nodesPerCluster = clustering.getNodesPerCluster();
        subnetworkNodes = new int[nNodes];
        subnetworkNeighbors = new int[nEdges];
        subnetworkEdgeWeights = new double[nEdges];
        return createSubnetwork(clustering, cluster, nodesPerCluster[cluster], subnetworkNodes, subnetworkNeighbors, subnetworkEdgeWeights);
    }

    /**
     * Creates induced subnetworks for the clusters in a clustering.
     *
     * @param clustering Clustering
     *
     * @return Subnetworks
     */
    public Network[] createSubnetworks(Clustering clustering)
    {
        double[] subnetworkEdgeWeights;
        int i;
        int[] subnetworkNeighbors, subnetworkNodes;
        int[][] nodesPerCluster;
        Network[] subnetworks;

        subnetworks = new Network[clustering.nClusters];
        nodesPerCluster = clustering.getNodesPerCluster();
        subnetworkNodes = new int[nNodes];
        subnetworkNeighbors = new int[nEdges];
        subnetworkEdgeWeights = new double[nEdges];
        for (i = 0; i < clustering.nClusters; i++)
            subnetworks[i] = createSubnetwork(clustering, i, nodesPerCluster[i], subnetworkNodes, subnetworkNeighbors, subnetworkEdgeWeights);
        return subnetworks;
    }

    /**
     * Creates an induced subnetwork of the largest connected component.
     *
     * @return Subnetwork
     */
    public Network createSubnetworkLargestComponent()
    {
        return createSubnetwork(identifyComponents(), 0);
    }

    /**
     * Creates a reduced (or aggregate) network based on a clustering.
     *
     * <p>
     * Each node in the reduced network corresponds to a cluster of nodes in
     * the original network. The weight of a node in the reduced network equals
     * the sum of the weights of the nodes in the corresponding cluster in the
     * original network. The weight of an edge between two nodes in the reduced
     * network equals the sum of the weights of the edges between the nodes in
     * the two corresponding clusters in the original network.
     * </p>
     *
     * @param clustering Clustering
     *
     * @return Reduced network
     */
    public Network createReducedNetwork(Clustering clustering)
    {
        double[] reducedNetworkEdgeWeights1, reducedNetworkEdgeWeights2;
        int i, j, k, l, m, n;
        int[] reducedNetworkNeighbors1, reducedNetworkNeighbors2;
        int[][] nodesPerCluster;
        Network reducedNetwork;

        reducedNetwork = new Network();

        reducedNetwork.nNodes = clustering.nClusters;

        reducedNetwork.nEdges = 0;
        reducedNetwork.nodeWeights = new double[clustering.nClusters];
        reducedNetwork.firstNeighborIndices = new int[clustering.nClusters + 1];
        reducedNetwork.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;
        reducedNetworkNeighbors1 = new int[nEdges];
        reducedNetworkEdgeWeights1 = new double[nEdges];
        reducedNetworkNeighbors2 = new int[clustering.nClusters - 1];
        reducedNetworkEdgeWeights2 = new double[clustering.nClusters];
        nodesPerCluster = clustering.getNodesPerCluster();
        for (i = 0; i < clustering.nClusters; i++)
        {
            j = 0;
            for (k = 0; k < nodesPerCluster[i].length; k++)
            {
                l = nodesPerCluster[i][k];

                reducedNetwork.nodeWeights[i] += nodeWeights[l];

                for (m = firstNeighborIndices[l]; m < firstNeighborIndices[l + 1]; m++)
                {
                    n = clustering.clusters[neighbors[m]];
                    if (n != i)
                    {
                        if (reducedNetworkEdgeWeights2[n] == 0)
                        {
                            reducedNetworkNeighbors2[j] = n;
                            j++;
                        }
                        reducedNetworkEdgeWeights2[n] += edgeWeights[m];
                    }
                    else
                        reducedNetwork.totalEdgeWeightSelfLinks += edgeWeights[m];
                }
            }

            for (k = 0; k < j; k++)
            {
                reducedNetworkNeighbors1[reducedNetwork.nEdges + k] = reducedNetworkNeighbors2[k];
                reducedNetworkEdgeWeights1[reducedNetwork.nEdges + k] = reducedNetworkEdgeWeights2[reducedNetworkNeighbors2[k]];
                reducedNetworkEdgeWeights2[reducedNetworkNeighbors2[k]] = 0;
            }
            reducedNetwork.nEdges += j;
            reducedNetwork.firstNeighborIndices[i + 1] = reducedNetwork.nEdges;
        }
        reducedNetwork.neighbors = Arrays.copyOfRange(reducedNetworkNeighbors1, 0, reducedNetwork.nEdges);
        reducedNetwork.edgeWeights = Arrays.copyOfRange(reducedNetworkEdgeWeights1, 0, reducedNetwork.nEdges);

        return reducedNetwork;
    }

    /**
     * Identifies the connected components of the network.
     *
     * @return Connected components
     */
    public Clustering identifyComponents()
    {
        ComponentsAlgorithm componentsAlgorithm;

        componentsAlgorithm = new ComponentsAlgorithm();
        return componentsAlgorithm.findClustering(this);
    }

    /**
     * Checks the integrity of the network.
     *
     * <p>
     * It is checked whether:
     * </p>
     *
     * <ul>
     * <li>variables have a correct value,</li>
     * <li>arrays have a correct length,</li>
     * <li>edges are sorted correctly,</li>
     * <li>edges are stored in both directions.</li>
     * </ul>
     *
     * <p>
     * An exception is thrown if the integrity of the network is violated.
     * </p>
     *
     * @throws IllegalArgumentException An illegal argument was provided in the
     *                                  construction of the network.
     */
    public void checkIntegrity() throws IllegalArgumentException
    {
        boolean[] checked;
        int i, j, k, l;

        // Check whether variables have a correct value and arrays have a
        // correct length.
        if (nNodes < 0)
            throw new IllegalArgumentException("nNodes must be non-negative.");

        if (nEdges < 0)
            throw new IllegalArgumentException("nEdges must be non-negative.");

        if (nEdges % 2 == 1)
            throw new IllegalArgumentException("nEdges must be even.");

        if (nodeWeights.length != nNodes)
            throw new IllegalArgumentException("Length of nodeWeight array must be equal to nNodes.");

        if (firstNeighborIndices.length != nNodes + 1)
            throw new IllegalArgumentException("Length of firstNeighborIndices array must be equal to nNodes + 1.");

        if (firstNeighborIndices[0] != 0)
            throw new IllegalArgumentException("First element of firstNeighborIndices array must be equal to 0.");

        if (firstNeighborIndices[nNodes] != nEdges)
            throw new IllegalArgumentException("Last element of firstNeighborIndices array must be equal to nEdges.");

        if (neighbors.length != nEdges)
            throw new IllegalArgumentException("Length of neighbors array must be equal to nEdges.");

        if (edgeWeights.length != nEdges)
            throw new IllegalArgumentException("Length of edgeWeights array must be equal to nEdges.");

        // Check whether edges are sorted correctly.
        for (i = 0; i < nNodes; i++)
        {
            if (firstNeighborIndices[i + 1] < firstNeighborIndices[i])
                throw new IllegalArgumentException("Elements of firstNeighborIndices array must be in non-decreasing order.");

            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
            {
                k = neighbors[j];

                if (k < 0)
                    throw new IllegalArgumentException("Elements of neighbors array must have non-negative values.");
                else if (k >= nNodes)
                    throw new IllegalArgumentException("Elements of neighbors array must have values less than nNodes.");

                if (j > firstNeighborIndices[i])
                {
                    l = neighbors[j - 1];
                    if (k < l)
                        throw new IllegalArgumentException("For each node, corresponding elements of neighbors array must be in increasing order.");
                    else if (k == l)
                        throw new IllegalArgumentException("For each node, corresponding elements of neighbors array must not include duplicate values.");
                }
            }
        }

        // Check whether edges are stored in both directions.
        checked = new boolean[nEdges];
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndices[i]; j < firstNeighborIndices[i + 1]; j++)
                if (!checked[j])
                {
                    k = neighbors[j];

                    l = Arrays.binarySearch(neighbors, firstNeighborIndices[k], firstNeighborIndices[k + 1], i);
                    if (l < 0)
                        throw new IllegalArgumentException("Edges must be stored in both directions.");
                    if (edgeWeights[j] != edgeWeights[l])
                        throw new IllegalArgumentException("Edge weights must be the same in both directions.");

                    checked[j] = true;
                    checked[l] = true;
                }
    }

    private static void sortEdges(int[][] edges, double[] edgeWeights)
    {
        class EdgeComparator implements Comparator<Integer>
        {
            int[][] edges;

            public EdgeComparator(int[][] edges)
            {
                this.edges = edges;
            }

            public int compare(Integer i, Integer j)
            {
                if (edges[0][i] > edges[0][j])
                    return 1;
                if (edges[0][i] < edges[0][j])
                    return -1;
                if (edges[1][i] > edges[1][j])
                    return 1;
                if (edges[1][i] < edges[1][j])
                    return -1;
                return 0;
            }
        }

        double[] edgeWeightsSorted;
        int i, nEdges;
        int[][] edgesSorted;
        Integer[] indices;

        nEdges = edges[0].length;

        // Determine sorting order.
        indices = new Integer[nEdges];
        for (i = 0; i < nEdges; i++)
            indices[i] = i;
        Arrays.parallelSort(indices, new EdgeComparator(edges));

        // Sort edges.
        edgesSorted = new int[2][nEdges];
        for (i = 0; i < nEdges; i++)
        {
            edgesSorted[0][i] = edges[0][indices[i]];
            edgesSorted[1][i] = edges[1][indices[i]];
        }
        edges[0] = edgesSorted[0];
        edges[1] = edgesSorted[1];

        // Sort edge weights.
        if (edgeWeights != null)
        {
            edgeWeightsSorted = new double[nEdges];
            for (i = 0; i < nEdges; i++)
                edgeWeightsSorted[i] = edgeWeights[indices[i]];
            System.arraycopy(edgeWeightsSorted, 0, edgeWeights, 0, nEdges);
        }
    }

    private Network()
    {
    }

    private Network(int nNodes, double[] nodeWeights, boolean setNodeWeightsToTotalEdgeWeights, int[][] edges, double[] edgeWeights, boolean sortedEdges, boolean checkIntegrity)
    {
        double[] edgeWeights2;
        int i, j;
        int[][] edges2;

        if (!sortedEdges)
        {
            edges2 = new int[2][2 * edges[0].length];
            edgeWeights2 = (edgeWeights != null) ? new double[2 * edges[0].length] : null;
            i = 0;
            for (j = 0; j < edges[0].length; j++)
            {
                edges2[0][i] = edges[0][j];
                edges2[1][i] = edges[1][j];
                if (edgeWeights != null)
                    edgeWeights2[i] = edgeWeights[j];
                i++;
                if (edges[0][j] != edges[1][j])
                {
                    edges2[0][i] = edges[1][j];
                    edges2[1][i] = edges[0][j];
                    if (edgeWeights != null)
                        edgeWeights2[i] = edgeWeights[j];
                    i++;
                }
            }
            edges[0] = Arrays.copyOfRange(edges2[0], 0, i);
            edges[1] = Arrays.copyOfRange(edges2[1], 0, i);
            if (edgeWeights != null)
                edgeWeights = Arrays.copyOfRange(edgeWeights2, 0, i);
            sortEdges(edges, edgeWeights);
        }

        this.nNodes = nNodes;
        nEdges = 0;
        firstNeighborIndices = new int[nNodes + 1];
        neighbors = new int[edges[0].length];
        this.edgeWeights = new double[edges[0].length];
        totalEdgeWeightSelfLinks = 0;
        i = 1;
        for (j = 0; j < edges[0].length; j++)
            if (edges[0][j] != edges[1][j])
            {
                for (; i <= edges[0][j]; i++)
                    firstNeighborIndices[i] = nEdges;
                neighbors[nEdges] = edges[1][j];
                this.edgeWeights[nEdges] = (edgeWeights != null) ? edgeWeights[j] : 1;
                nEdges++;
            }
            else
                totalEdgeWeightSelfLinks += (edgeWeights != null) ? edgeWeights[j] : 1;
        for (; i <= nNodes; i++)
            firstNeighborIndices[i] = nEdges;
        neighbors = Arrays.copyOfRange(neighbors, 0, nEdges);
        this.edgeWeights = Arrays.copyOfRange(this.edgeWeights, 0, nEdges);

        this.nodeWeights = (nodeWeights != null) ? nodeWeights.clone() : (setNodeWeightsToTotalEdgeWeights ? getTotalEdgeWeightPerNodeHelper() : cwts.util.Arrays.createDoubleArrayOfOnes(nNodes));

        if (checkIntegrity)
            checkIntegrity();
    }

    private Network(int nNodes, double[] nodeWeights, boolean setNodeWeightsToTotalEdgeWeights, int[] firstNeighborIndices, int[] neighbors, double[] edgeWeights, boolean checkIntegrity)
    {
        this.nNodes = nNodes;
        nEdges = neighbors.length;
        this.firstNeighborIndices = firstNeighborIndices.clone();
        this.neighbors = neighbors.clone();
        this.edgeWeights = (edgeWeights != null) ? edgeWeights.clone() : cwts.util.Arrays.createDoubleArrayOfOnes(nEdges);
        totalEdgeWeightSelfLinks = 0;

        this.nodeWeights = (nodeWeights != null) ? nodeWeights.clone() : (setNodeWeightsToTotalEdgeWeights ? getTotalEdgeWeightPerNodeHelper() : cwts.util.Arrays.createDoubleArrayOfOnes(nNodes));

        if (checkIntegrity)
            checkIntegrity();
    }

    private double[] getTotalEdgeWeightPerNodeHelper()
    {
        double[] totalEdgeWeightPerNode;
        int i;

        totalEdgeWeightPerNode = new double[nNodes];
        for (i = 0; i < nNodes; i++)
            totalEdgeWeightPerNode[i] = cwts.util.Arrays.calcSum(edgeWeights, firstNeighborIndices[i], firstNeighborIndices[i + 1]);
        return totalEdgeWeightPerNode;
    }

    private double getRandomNumber(int node1, int node2, double[] randomNumbers)
    {
        int i, j;

        if (node1 < node2)
        {
            i = node1;
            j = node2;
        }
        else
        {
            i = node2;
            j = node1;
        }
        return randomNumbers[i * nNodes + j];
    }

    private Network createSubnetwork(Clustering clustering, int cluster, int[] nodes, int[] subnetworkNodes, int[] subnetworkNeighbors, double[] subnetworkEdgeWeights)
    {
        int i, j, k;
        Network subnetwork;

        subnetwork = new Network();

        subnetwork.nNodes = nodes.length;

        if (subnetwork.nNodes == 1)
        {
            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[1];
            subnetwork.nodeWeights[0] = nodeWeights[nodes[0]];
            subnetwork.firstNeighborIndices = new int[2];
            subnetwork.neighbors = new int[0];
            subnetwork.edgeWeights = new double[0];
        }
        else
        {
            for (i = 0; i < nodes.length; i++)
                subnetworkNodes[nodes[i]] = i;

            subnetwork.nEdges = 0;
            subnetwork.nodeWeights = new double[subnetwork.nNodes];
            subnetwork.firstNeighborIndices = new int[subnetwork.nNodes + 1];
            for (i = 0; i < subnetwork.nNodes; i++)
            {
                j = nodes[i];
                subnetwork.nodeWeights[i] = nodeWeights[j];
                for (k = firstNeighborIndices[j]; k < firstNeighborIndices[j + 1]; k++)
                    if (clustering.clusters[neighbors[k]] == cluster)
                    {
                        subnetworkNeighbors[subnetwork.nEdges] = subnetworkNodes[neighbors[k]];
                        subnetworkEdgeWeights[subnetwork.nEdges] = edgeWeights[k];
                        subnetwork.nEdges++;
                    }
                subnetwork.firstNeighborIndices[i + 1] = subnetwork.nEdges;
            }
            subnetwork.neighbors = Arrays.copyOfRange(subnetworkNeighbors, 0, subnetwork.nEdges);
            subnetwork.edgeWeights = Arrays.copyOfRange(subnetworkEdgeWeights, 0, subnetwork.nEdges);
        }

        subnetwork.totalEdgeWeightSelfLinks = 0;

        return subnetwork;
    }
}
