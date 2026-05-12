package uk.co.timsummertonbrier.multinodelineage;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;
import static uk.co.timsummertonbrier.multinodelineage.GetNodeById.getNodeById;

public class BreadthFirstSearch {
    
    public static Set<String> run(
        DirectedGraph graph, 
        String originId,
        NodeChildSupplier nodeChildSupplier
    ) {        
        Set<String> seenIds = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        
        Node originNode = getNodeById(graph, originId);
        queue.add(originNode);
        seenIds.add(originId);
        
        while (!queue.isEmpty()) {
            Node current = queue.remove();
            for (Node child: nodeChildSupplier.getChildren(graph, current)) {
                if (!seenIds.contains(child.getId().toString())) {
                    seenIds.add(child.getId().toString());
                    queue.add(child);
                }
            }
        }
        
        return seenIds;
    }
    
}