/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cwts.networkanalysis;

import cwts.util.Arrays;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class NodeWeightGraph
{
    double[] nodeWeights;
    double totalNodeWeight;
    Graph graph;

    NodeWeightGraph(Graph graph)
    {
        this(graph, Arrays.createDoubleArrayOfOnes(graph.getNodeCount()));
    }

    NodeWeightGraph(Graph graph, double[] nodeWeights)
    {
        this.graph = graph;
        this.nodeWeights = nodeWeights;

        this.totalNodeWeight = Arrays.calcSum(nodeWeights);
    }

    public Graph getGraph() { return graph; }

    public double getNodeWeight(Node node)
    {
        return getNodeWeight(node.getStoreId());
    }

    public double getNodeWeight(int i)
    {
        return nodeWeights[i];
    }

    double getTotalNodeWeight()
    {
        return totalNodeWeight;
    }

    double[] getNodeWeights()
    {
        return nodeWeights.clone();
    }

    NodeWeightGraph createReducedGraph(Clustering clustering)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    NodeWeightGraph[] createSubgraphs(Clustering clustering)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
