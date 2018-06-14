/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

/**
 *
 * @author puig
 */

public class DirectedGraphPartition implements IPartitionGraph {
    
    Graph _old;

    public DirectedGraphPartition(Graph old) {
        this._old = old;
    }
    
    public Graph partition() {
        
        GraphModel model = GraphModel.Factory.newInstance();  
        Graph newGraph = model.getUndirectedGraph();
        //newGraph.
        GraphFactory fact = model.factory();
        
        Table nodeTable = model.getNodeTable();
        nodeTable.addColumn("Size", int.class);
        
        Table edgeTable = model.getEdgeTable();
        edgeTable.addColumn("Size", int.class);
        
        Map<Integer, Node> color_Nodes = new HashMap<Integer,Node>(); //First one is RGBA Color, second is number of nodes with that color
        
        Edge[] oldEdges = _old.getEdges().toArray();
        Node[] oldNodes = _old.getNodes().toArray();
        
        for(Node cNode : oldNodes){
            int nodeColor = cNode.getColor().getRGB();
            Node nNode = color_Nodes.get(nodeColor);
            if(nNode != null) {
                String newQty = nNode.getAttribute("Size").toString();//+1;
                nNode.setAttribute("Size", Integer.valueOf(newQty)+1);
            }
            else {
                Node node = fact.newNode();
                node.setLabel(String.valueOf(nodeColor));
                node.setAttribute("Size", 1);
                node.setColor(cNode.getColor());
                color_Nodes.put(nodeColor, node);
                newGraph.addNode(node);
            }
        }
        
        int count = 0;
        for (Edge cEdge: oldEdges){
            Node source = cEdge.getSource();
            Node target = cEdge.getTarget();
            Color sourceColor = source.getColor();
            Color targetColor = target.getColor();
            
            if(sourceColor.getRGB()!= targetColor.getRGB()){
                Node n1 = color_Nodes.get(sourceColor.getRGB());
                Node n2 = color_Nodes.get(targetColor.getRGB());
                Edge edge = newGraph.getEdge(n1, n2);
                if(edge != null){
                    edge.setAttribute("Size", (Integer.valueOf(edge.getAttribute("Size").toString())+1));
                    count++;
                } else {
                    edge = fact.newEdge(n1, n2, false);
                    edge.setAttribute("Size", 1);
                    newGraph.addEdge(edge);
                    count++;
                }
            }
        }
        
        System.out.println("Total count: " + count);
        
        return newGraph;

    }
    
}
