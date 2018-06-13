/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

import java.util.ArrayList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author puig
 */

public class DirectedGraphPartition implements IPartitionGraph {
    
    Graph _old;

    public DirectedGraphPartition(Graph old) {
        this._old = old;
    }
    
    // Creates a new graph from old one, dividing into groups depending on color
    public Graph partition() {
        ArrayList<NodeGroup> nodeGroups = null;
        Node[] oldNodes = _old.getNodes().toArray();
        
        // Iterate over every node
        for (Node oldNode : oldNodes) {
            // Check if node is in any nodeGroup by looking at its color
            // IF -> Add to group (nodeContainer)
            // IF NOT -> Create nodeGroup and add to it (nodeContainer)
        }
        
        // Iterate over every node Group
            // Iterate over every node 
            // Check if node is connected to any other node, if so check its group and add (connected)
            
        // Iterate over every nodeGroup connected and count as ints
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
