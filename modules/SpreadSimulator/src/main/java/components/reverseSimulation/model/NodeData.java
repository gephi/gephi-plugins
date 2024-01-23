package components.reverseSimulation.model;

import components.reverseSimulation.NodeHelper;
import configLoader.ConfigLoader;
import lombok.Getter;
import lombok.Setter;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class NodeData {
    int nodeStroeId;
    String nodeCurrnetState;
    String nodeRootState;
//    int distanceToClosestInitNode;
//    int distanceToFahrestInitNode;
//    double avgDistanceToNodes;


    public NodeData(Node node) {
//        var graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
//        var initialNodes = Arrays.stream(graph.getNodes().toArray())
//                .filter(initNode -> initNode.getAttribute(ConfigLoader.colNameRootState).equals(stateName))
//                .collect(Collectors.toList());
        this.nodeStroeId = node.getStoreId();
        this.nodeCurrnetState = node.getAttribute(ConfigLoader.colNameNodeState).toString();
        this.nodeRootState = node.getAttribute(ConfigLoader.colNameRootState).toString();
//        this.distanceToClosestInitNode = NodeHelper.getDistanceToClosestNode(node, initialNodes, graph);
//        this.distanceToFahrestInitNode = NodeHelper.getDistanceToFarthestNode(node, initialNodes, graph);
//        this.avgDistanceToNodes = NodeHelper.getAvgDistanceToNodes(node, initialNodes, graph);
    }
}
