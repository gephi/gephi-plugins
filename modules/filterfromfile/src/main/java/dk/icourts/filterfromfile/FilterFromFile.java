package dk.icourts.filterfromfile;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * Filter that allows to filter the nodes based on a list of labels provided by the user, through Gephi's interface
 *
 * @author Yannis Panagis
 */
public class FilterFromFile implements NodeFilter {

    protected static final int NODES_TO_KEEP_IDX = 0;
    protected static final int NEIGHBORS_IDX = 1;
    private Set<String> nodesToKeep = new HashSet<String>();
    private boolean neighbors = false;

    @Override
    public boolean init(Graph graph) {
        //Just make sure that the filter operates on non-empty graph
        return graph.getNodeCount() > 0;
    }

    @Override
    public boolean evaluate(Graph graph, Node node) {
        //This is where the actual filtering takes place
        if (!nodesToKeep.isEmpty()
                && nodesToKeep.contains(node.getLabel())) //Keep node if node's label is in the list
        {
            return true;
        } else if (graph.getNodeCount() == 0) {
            return false;
        } else if (this.neighbors) {
            /* Get the neighbors of the current node
             * If one of the neighbours is contained in the list of labels
             * then keep the node
             */
            for (Node v : graph.getNeighbors(node).toArray()) {
                if (this.nodesToKeep.contains(v.getLabel())) {
                    return true;
                }
            }
            //If we have reached this point the node shall be filtered out
            return false;
        } else {
            return false;
        }

    }

    @Override
    public void finish() {

    }

    @Override
    public String getName() {
        return "Filter by list";
    }

    @Override
    public FilterProperty[] getProperties() {
        FilterProperty[] properties = new FilterProperty[2];
        FilterProperty keepList = null;
        FilterProperty neighborsProp = null;
        try {
            //Define the properties and their getters and setters
            keepList = FilterProperty.createProperty(this, Set.class,
                    "nodesToKeep", "getNodesToKeep", "setNodesToKeep");
            properties[NODES_TO_KEEP_IDX] = keepList;
            neighborsProp = FilterProperty.createProperty(this,
                    Boolean.class, "neighbors", "getNeighbors", "setNeighbors");
            properties[NEIGHBORS_IDX] = neighborsProp;
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(FilterFromFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        return properties;
    }

    public Set<String> getNodesToKeep() {
        return this.nodesToKeep;
    }

    public void setNodesToKeep(Set<String> ntk) {
        this.nodesToKeep = ntk;
    }

    public boolean getNeighbors() {
        return this.neighbors;
    }

    public void setNeighbors(Boolean neighbors) {
        this.neighbors = neighbors;
    }

}
