package cz.cvut.fit.gephi.multimode;

import java.util.ArrayList;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Kuchar
 */
public class GraphColoring {

    private Graph graph;
    private Node[] nodes;

    public GraphColoring() {
        // load all possible values of attribute
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graph = graphController.getGraphModel().getGraphVisible();
        nodes = graph.getNodes().toArray();
    }

    /**
     * graph coloring for bipartite networks Implementation inspired by:
     * http://pages.cs.wisc.edu/~elgar/cs577/Bipartite/Bipartite.java
     *
     * @return true if bipartite
     */
    public boolean bipartite() {
        String colorColumnName = "NodeColor-Multimode";
        Table nodeTable = graph.getModel().getNodeTable();
        Column colorColumn;
        
        if (!nodeTable.hasColumn(colorColumnName)) {
            colorColumn = nodeTable.addColumn(
                    colorColumnName,
                    "Node Color Multimode",
                    String.class,
                    Origin.DATA,
                    NodeColor.BLACK.getValue(),
                    true);
        } else {
            colorColumn = nodeTable.getColumn(colorColumnName);
        }

        // prepare
        Node current;
        NodeColor nextColor;

        // queue
        ArrayList<Node> queue = new ArrayList<Node>(graph.getNodeCount());
        // iterate all

        for (Node node : nodes) {
            // if node is black

            if (node.getAttribute(colorColumn).equals(NodeColor.BLACK.getValue())) {
                // set to red
                node.setAttribute(colorColumn, NodeColor.RED.getValue());
                // add to the queue
                queue.add(node);
                // iterate all nodes in queue
                while (!queue.isEmpty()) {
                    // current
                    current = queue.remove(0);
                    // if red - change to blue

                    if (current.getAttribute(colorColumn).equals(NodeColor.RED.getValue())) {
                        nextColor = NodeColor.BLUE;
                    } else {
                        nextColor = NodeColor.RED;
                    }
                    // get all neighbours
                    for (Node neighbor : graph.getNeighbors(current)) {
                        // if not yet processed

                        if (neighbor.getAttribute(colorColumn).equals(NodeColor.BLACK.getValue())) {
                            // set color
                            neighbor.setAttribute(colorColumn, nextColor.getValue());
                            // add to the queue
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        // check bipartite
        boolean bipartite = true;
        for (int i = 0; i < nodes.length && bipartite; i++) {
            for (Node n : graph.getNeighbors(nodes[i])) {
                // check colors of neighbours

                if (n.getAttribute(colorColumn).equals(nodes[i].getAttribute(colorColumn))) {
                    bipartite = false;
                }
            }
        }
        return bipartite;
    }

    /**
     * Node colors enum
     */
    protected enum NodeColor {

        BLACK("black"), BLUE("blue"), RED("red");
        private String value;

        NodeColor(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }
}
