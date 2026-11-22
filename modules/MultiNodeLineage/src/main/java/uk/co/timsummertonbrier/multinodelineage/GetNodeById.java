package uk.co.timsummertonbrier.multinodelineage;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;

public class GetNodeById {
    
    public static Node getNodeById(DirectedGraph graph, String id) {
        var nodeIdType = graph.getModel().getConfiguration().getNodeIdType();
        
        if (nodeIdType == Integer.class) {
            return graph.getNode(Integer.valueOf(id));
        } else if (nodeIdType == String.class) {
            return graph.getNode(id);
        } else {
            throw new RuntimeException("Unsupported node ID type: " + nodeIdType);
        }
    }
    
}