/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf.algorithm;

import org.gephi.graph.api.Graph;

/**
 *
 * @author puig
 */

//Generates the correct PartitionGraph
public class GraphPartitionAlgorithmFactory {
    IGraphPartitionAlgorithm algorithm;
    Graph graph;
    
    public GraphPartitionAlgorithmFactory(Graph graph) {
        this.graph = graph;
    }
    
    public IGraphPartitionAlgorithm getGraphPartition(){
        if(graph.isDirected()) algorithm = new DirectedGraphPartitionAlgorithm(graph);
        else if(graph.isUndirected()) algorithm = new UndirectedGraphPartitionAlgorithm(graph); 
        else throw new UnsupportedOperationException("Graph type is not supported yet."); //Other cases like mixed are not implemented yet.
        return algorithm;
    }
}
