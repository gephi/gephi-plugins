package upf.edu.gephi.plugin.grouppartition.algorithm;

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
        else if(graph.isMixed()) throw new UnsupportedOperationException("Mixed graph type is not supported yet.");
        else throw new UnsupportedOperationException("Graph type is not supported yet."); //Other cases like mixed are not implemented yet.
        return algorithm;
    }
}
