package cwts.networkanalysis;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Clustering of the nodes in a network.
 *
 * <p>
 * Each node belongs to exactly one cluster.
 * </p>
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @author Vincent Traag
 */
public class Clustering implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1;

    /**
     * Number of nodes.
     */
    protected int nNodes;

    /**
     * Number of clusters.
     */
    protected int nClusters;

    /**
     * Cluster of each node.
     */
    protected int[] clusters;

    /**
     * Loads a clustering from a file.
     *
     * @param filename File from which a clustering is loaded
     *
     * @return Loaded clustering
     *
     * @throws ClassNotFoundException Class not found
     * @throws IOException            Could not read the file
     *
     * @see #save(String filename)
     */
    public static Clustering load(String filename) throws ClassNotFoundException, IOException
    {
        Clustering clustering;
        ObjectInputStream objectInputStream;

        objectInputStream = new ObjectInputStream(new FileInputStream(filename));

        clustering = (Clustering)objectInputStream.readObject();

        objectInputStream.close();

        return clustering;
    }

    /**
     * Constructs a singleton clustering for a specified number of nodes.
     *
     * @param nNodes Number of nodes
     */
    public Clustering(int nNodes)
    {
        this.nNodes = nNodes;
        initSingletonClustersHelper();
    }

    /**
     * Constructs a clustering using a specified cluster for each node.
     *
     * @param clusters Cluster of each node
     */
    public Clustering(int[] clusters)
    {
        nNodes = clusters.length;
        this.clusters = clusters.clone();
        nClusters = cwts.util.Arrays.calcMaximum(clusters) + 1;
    }

    /**
     * Clones the clustering.
     *
     * @return Cloned clustering
     */
    public Clustering clone()
    {
        Clustering clonedClustering;

        try
        {
            clonedClustering = (Clustering)super.clone();
            clonedClustering.clusters = clusters.clone();
            return clonedClustering;
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    /**
     * Saves the clustering in a file.
     *
     * @param filename File in which the clustering is saved
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
     * Returns the number of clusters.
     *
     * @return Number of clusters
     */
    public int getNClusters()
    {
        return nClusters;
    }

    /**
     * Returns the cluster of each node.
     *
     * @return Cluster of each node
     */
    public int[] getClusters()
    {
        return clusters.clone();
    }

    /**
     * Returns the cluster of a node.
     *
     * @param node Node
     *
     * @return Cluster
     */
    public int getCluster(int node)
    {
        return clusters[node];
    }

    /**
     * Returns the number of nodes per cluster.
     *
     * @return Number of nodes per cluster
     */
    public int[] getNNodesPerCluster()
    {
        int i;
        int[] nNodesPerCluster;

        nNodesPerCluster = new int[nClusters];
        for (i = 0; i < nNodes; i++)
            nNodesPerCluster[clusters[i]]++;
        return nNodesPerCluster;
    }

    /**
     * Returns a list of nodes per cluster.
     *
     * @return List of nodes per cluster
     */
    public int[][] getNodesPerCluster()
    {
        int i;
        int[] nNodesPerCluster;
        int[][] nodesPerCluster;

        nodesPerCluster = new int[nClusters][];
        nNodesPerCluster = getNNodesPerCluster();
        for (i = 0; i < nClusters; i++)
        {
            nodesPerCluster[i] = new int[nNodesPerCluster[i]];
            nNodesPerCluster[i] = 0;
        }
        for (i = 0; i < nNodes; i++)
        {
            nodesPerCluster[clusters[i]][nNodesPerCluster[clusters[i]]] = i;
            nNodesPerCluster[clusters[i]]++;
        }
        return nodesPerCluster;
    }

    /**
     * Assigns a node to a cluster.
     *
     * @param node    Node
     * @param cluster Cluster
     */
    public void setCluster(int node, int cluster)
    {
        this.clusters[node] = cluster;
        nClusters = Math.max(nClusters, cluster + 1);
    }

    /**
     * Initializes a singleton clustering.
     *
     * <p>
     * Each node {@code i} is assigned to a cluster {@code i}.
     * </p>
     */
    public void initSingletonClusters()
    {
        initSingletonClustersHelper();
    }

    /**
     * Removes empty clusters.
     *
     * <p>
     * Clusters are relabeled to follow a strictly consecutive numbering {@code
     * 0, ..., nClusters - 1}.
     * </p>
     *
     */
    public void removeEmptyClusters()
    {
        boolean[] nonEmptyClusters;
        int i, j;
        int[] newClusters;

        nonEmptyClusters = new boolean[nClusters];
        newClusters = new int[nClusters];
        for (i = 0; i < nNodes; i++)
            nonEmptyClusters[clusters[i]] = true;
        i = 0;
        for (j = 0; j < nClusters; j++)
            if (nonEmptyClusters[j])
            {
                newClusters[j] = i;
                i++;
            }
        nClusters = i;
        for (i = 0; i < nNodes; i++)
            clusters[i] = newClusters[clusters[i]];
    }

    /**
     * Orders the clusters in decreasing order of their number of nodes.
     *
     * @see #orderClustersByWeight(double[] nodeWeights)
     */
    public void orderClustersByNNodes()
    {
        orderClustersByWeight(cwts.util.Arrays.createDoubleArrayOfOnes(nNodes));
    }

    /**
     * Orders the clusters in decreasing order of their total node weight.
     *
     * <p>
     * The total node weight of a cluster equals the sum of the weights of the
     * nodes belonging to the cluster.
     * </p>
     *
     * @param nodeWeights Node weights
     *
     * @see #orderClustersByNNodes()
     */
    public void orderClustersByWeight(double[] nodeWeights)
    {
        class Cluster implements Comparable<Cluster>
        {

            int cluster;
            double weight;

            Cluster(int cluster, double weight)
            {
                this.cluster = cluster;
                this.weight = weight;
            }

            public int compareTo(Cluster cluster)
            {
                return (cluster.weight > weight) ? 1 : ((cluster.weight < weight) ? -1 : 0);
            }
        }

        Cluster[] clusters;
        double[] clusterWeights;
        int i;
        int[] newClusters;

        clusterWeights = new double[nClusters];
        for (i = 0; i < nNodes; i++)
            clusterWeights[this.clusters[i]] += nodeWeights[i];
        clusters = new Cluster[nClusters];
        for (i = 0; i < nClusters; i++)
            clusters[i] = new Cluster(i, clusterWeights[i]);

        java.util.Arrays.sort(clusters);

        newClusters = new int[nClusters];
        i = 0;
        do
        {
            newClusters[clusters[i].cluster] = i;
            i++;
        } while ((i < nClusters) && (clusters[i].weight > 0));
        nClusters = i;
        for (i = 0; i < nNodes; i++)
            this.clusters[i] = newClusters[this.clusters[i]];
    }

    /**
     * Merges the clusters based on a clustering of the clusters.
     *
     * @param clustering Clustering of the clusters
     */
    public void mergeClusters(Clustering clustering)
    {
        int i;

        for (i = 0; i < nNodes; i++)
            clusters[i] = clustering.clusters[clusters[i]];
        nClusters = clustering.nClusters;
    }

    /**
     * Calculates the normalized mutual information relative to another
     * clustering.
     *
     * @param clustering Other clustering
     *
     * @return Normalized mutual information
     */
    public double calcNormalizedMutualInformation(Clustering clustering)
    {
        ArrayList<HashMap<Integer, Integer>> nNodesOverlap;
        double entropy1, entropy2, mutualInformation, p;
        HashMap<Integer, Integer> clusterOverlaps;
        int c1, c2, v;
        int[] nNodesPerCluster1, nNodesPerCluster2;
        Integer overlap;
        Iterator<Map.Entry<Integer, Integer>> it;
        Map.Entry<Integer, Integer> pair;

        nNodesPerCluster1 = getNNodesPerCluster();
        nNodesPerCluster2 = clustering.getNNodesPerCluster();

        nNodesOverlap = new ArrayList<HashMap<Integer, Integer>>();

        entropy1 = 0;
        entropy2 = 0;
        for (c1 = 0; c1 < nClusters; c1++)
        {
            nNodesOverlap.add(new HashMap<Integer, Integer>());
            if (nNodesPerCluster1[c1] > 0)
            {
                p = (double)nNodesPerCluster1[c1] / nNodes;
                entropy1 += -p * Math.log(p);
            }
        }
        for (c2 = 0; c2 < clustering.nClusters; c2++)
            if (nNodesPerCluster2[c2] > 0)
            {
                p = (double)nNodesPerCluster2[c2] / nNodes;
                entropy2 += -p * Math.log(p);
            }

        for (v = 0; v < nNodes; v++)
        {
            clusterOverlaps = nNodesOverlap.get(clusters[v]);
            overlap = clusterOverlaps.get(clustering.clusters[v]);
            if (overlap == null)
                nNodesOverlap.get(clusters[v]).put(clustering.clusters[v], 1);
            else
                nNodesOverlap.get(clusters[v]).put(clustering.clusters[v], overlap + 1);
        }

        mutualInformation = 0;
        for (c1 = 0; c1 < nClusters; c1++)
        {
            it = nNodesOverlap.get(c1).entrySet().iterator();
            while (it.hasNext())
            {
                pair = it.next();
                c2 = (int)pair.getKey();
                mutualInformation += ((double)pair.getValue() / nNodes) * Math.log(nNodes * (double)pair.getValue() / (nNodesPerCluster1[c1] * nNodesPerCluster2[c2]));
            }
        }

        return 2 * mutualInformation / (entropy1 + entropy2);
    }

    /**
     * Calculates the variation of information relative to another clustering.
     *
     * @param clustering Other clustering
     *
     * @return Variation of information
     */
    public double calcVariationOfInformation(Clustering clustering)
    {
        ArrayList<HashMap<Integer, Integer>> nNodesOverlap;
        double entropy1, entropy2, jointEntropy, p;
        HashMap<Integer, Integer> clusterOverlaps;
        int c1, c2, v;
        int[] nNodesPerCluster1, nNodesPerCluster2;
        Integer overlap;
        Iterator<Map.Entry<Integer, Integer>> it;
        Map.Entry<Integer, Integer> pair;

        nNodesPerCluster1 = getNNodesPerCluster();
        nNodesPerCluster2 = clustering.getNNodesPerCluster();

        nNodesOverlap = new ArrayList<HashMap<Integer, Integer>>();

        entropy1 = 0;
        entropy2 = 0;
        for (c1 = 0; c1 < nClusters; c1++)
        {
            nNodesOverlap.add(new HashMap<Integer, Integer>());
            if (nNodesPerCluster1[c1] > 0)
            {
                p = (double)nNodesPerCluster1[c1] / nNodes;
                entropy1 += -p * Math.log(p);
            }
        }
        for (c2 = 0; c2 < clustering.nClusters; c2++)
            if (nNodesPerCluster2[c2] > 0)
            {
                p = (double)nNodesPerCluster2[c2] / nNodes;
                entropy2 += -p * Math.log(p);
            }

        for (v = 0; v < nNodes; v++)
        {
            clusterOverlaps = nNodesOverlap.get(clusters[v]);
            overlap = clusterOverlaps.get(clustering.clusters[v]);
            if (overlap == null)
                nNodesOverlap.get(clusters[v]).put(clustering.clusters[v], 1);
            else
                nNodesOverlap.get(clusters[v]).put(clustering.clusters[v], overlap + 1);
        }

        jointEntropy = 0;
        for (c1 = 0; c1 < nClusters; c1++)
        {
            it = nNodesOverlap.get(c1).entrySet().iterator();
            while (it.hasNext())
            {
                pair = it.next();
                c2 = (int)pair.getKey();
                p = (double)pair.getValue() / nNodes;
                jointEntropy += -p * Math.log(p);
            }
        }

        return 2 * jointEntropy - entropy1 - entropy2;
    }

    private void initSingletonClustersHelper()
    {
        int i;

        clusters = new int[nNodes];
        for (i = 0; i < nNodes; i++)
            clusters[i] = i;
        nClusters = nNodes;
    }
}
