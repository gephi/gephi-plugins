/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package multiviz;

/**
 *
 * @author J
 */
public class Readme {
    
    /**
     * not use distance between layers  , gravity can keep everything together
     * repel communities (no need to perfectly stack on top)
     * 
    */
    /**
     * Hooke’s law
     * repulsive forces between all nodes
     * attractive forces between nodes that are adjacent
     * 
     * forces between the nodes can be computed based on their graph theoretic distances, 
     * determined by the lengths of shortest paths between them.
     * 
     * Kamada and Kawai uses spring forces proportional to the graph theoretic distances
     * 
     * C60 bucky ball (60 vertices) one node connected to 3 others
     * 
     * Old basic force-directed limited to small graphs and results are poor for graphs with more than a few hundred vertices
     * 
     * fact that the physical model typically has many local minima
     * 
     * all the edge lengths ought to be the same, and the layout should display as much symmetry as possible
     * 
     * “even vertex distribution”
     * 
     * “atomic particles or celestial bodies, exerting attractive and repulsive forces from one another.”
     * fa(d) = d^2/k, fr(d) = −k^2/d
     * distance d between two vertices
     * optimal distance between vertices k 
     * k = C \sqrt( area / number of vertices )
     * O(|E|) attractive forces and O(|V|^2) repulsive forces
     * the temperature could start at an initial value (say one tenth the width of the frame) and decay to 0 in an inverse linear fashion.
     * temperature controls the displacement of vertices so that as the layout becomes better, the adjustments become smaller.
     * technique called simulated annealing
     * 
     * reduce the quadratic complexity of the repulsive forces, Fruchterman and Reingold suggest using a grid variant of their basic algorithm, where the repulsive forces between distant vertices are ignored.
     * For sparse graphs, and with uniform distribution of the vertices, this method allows a O(|V |) time approximation to the repulsive forces calculation
     * 
     * 
     */
            
    
    
}
