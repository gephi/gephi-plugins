package upf.edu.gephi.plugin.grouppartition.algorithm;

import java.awt.Color;
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
public class DirectedGraphPartitionAlgorithm implements IGraphPartitionAlgorithm {

    Graph _old;

    public DirectedGraphPartitionAlgorithm(Graph old) {
        this._old = old;
    }

    public Graph doPartition() {

        GraphModel model = GraphModel.Factory.newInstance();
        Graph newGraph = model.getDirectedGraph();
        GraphFactory fact = model.factory();

        Table nodeTable = model.getNodeTable();
        nodeTable.addColumn("Size", int.class);

        Table edgeTable = model.getEdgeTable();
        edgeTable.addColumn("Size", int.class);

        Map<Integer, Node> color_Nodes = new HashMap<Integer, Node>(); //First one is RGBA Color, second is number of nodes with that color
        Map<Integer, Node> maxOldNode = new HashMap<Integer, Node>(); //First one is color RGB, second is max Node to make a label.

        Edge[] oldEdges = _old.getEdges().toArray();
        Node[] oldNodes = _old.getNodes().toArray();

        for (Node cNode : oldNodes) {
            int nodeColor = cNode.getColor().getRGB();
            Node nNode = color_Nodes.get(nodeColor);
            if (nNode != null) {
                String newQty = nNode.getAttribute("Size").toString();//+1;
                nNode.setAttribute("Size", Integer.valueOf(newQty) + 1);
            } else {
                Node node = fact.newNode();
                node.setLabel("Group of " + cNode.getLabel());
                node.setAttribute("Size", 1);
                node.setColor(cNode.getColor());
                color_Nodes.put(nodeColor, node);
                maxOldNode.put(nodeColor, cNode);
                newGraph.addNode(node);
            }
            Integer NodeMax = (int) maxOldNode.get(nodeColor).size();
            if (NodeMax < (int) cNode.size()) {
                maxOldNode.put(nodeColor, cNode);
                nNode.setLabel("Group of " + cNode.getLabel());
            }
        }

        for (Edge cEdge : oldEdges) {
            Node source = cEdge.getSource();
            Node target = cEdge.getTarget();
            Color sourceColor = source.getColor();
            Color targetColor = target.getColor();

            Node n1 = color_Nodes.get(sourceColor.getRGB());
            Node n2 = color_Nodes.get(targetColor.getRGB());
            Edge edge = newGraph.getEdge(n1, n2);
            if (edge != null) {
                edge.setAttribute("Size", (Integer.valueOf(edge.getAttribute("Size").toString()) + 1));
                edge.setWeight(edge.getWeight() + cEdge.getWeight());
            } else {
                edge = fact.newEdge(n1, n2, true);
                edge.setAttribute("Size", 1);
                edge.setWeight(cEdge.getWeight());
                newGraph.addEdge(edge);
            }
        }

        return newGraph;
    }

}
