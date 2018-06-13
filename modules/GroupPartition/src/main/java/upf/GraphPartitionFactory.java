/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

import org.gephi.graph.api.Graph;

/**
 *
 * @author puig
 */

//Generates the correct PartitionGraph
public class GraphPartitionFactory {
    IPartitionGraph algorithm;
    Graph graph;
    
    GraphPartitionFactory(Graph graph) {
        this.graph = graph;
    }
    
    IPartitionGraph getGraphPartition(){
        if(graph.isDirected()) algorithm = new DirectedGraphPartition(graph);
        else if(graph.isUndirected() || graph.isMixed()) algorithm = new UndirectedGraphPartition(graph); //Mixed Should be changed
        else throw new UnsupportedOperationException("Graph type is not supported yet."); //It should not ever get here
        return algorithm;
    }
}
