/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

import java.awt.Color;
import java.util.ArrayList;
import org.gephi.graph.api.Node;

/**
 *
 * @author puig
 */


public interface NodeGroup extends Node {
    
    public int weight();
    
    public ArrayList<Node> nodeContainer();
    
    public ArrayList<NodeGroup> connected();
    
    public Color color();
    
}
