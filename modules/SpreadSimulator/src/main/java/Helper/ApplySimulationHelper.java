package Helper;

import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.SimulationModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ApplySimulationHelper {
    public static void Apply(Graph graph, SimulationModel simulationModel){
        var nodes = List.of(graph.getNodes().toArray());

        if(simulationModel == null)
            return;

        var nodeRoles = simulationModel.getNodeRoles();
        SetupNodeRoles(nodes, nodeRoles);
        SetupNodeStates(nodes, nodeRoles);
    }

    public static void CrateModelColumns(Graph graph){

        var nodes = List.of(graph.getNodes().toArray());
        var table = nodes.get(0).getTable();
        CreateModelColumns(table);

    }

    public static void ClearModel(Graph graph){

        var nodes = List.of(graph.getNodes().toArray());
        var table = nodes.get(0).getTable();
        RemoveModel(table);

    }

    private static void RemoveModel(Table table) {
        if(table.hasColumn("NodeRole"))
            table.removeColumn("NodeRole");

        if(table.hasColumn("NodeState"))
            table.removeColumn("NodeState");

        if(table.hasColumn("TransitionMap"))
            table.removeColumn("TransitionMap");

    }


    private static void CreateModelColumns(Table table) {
        if(!table.hasColumn("NodeRole"))
            table.addColumn("NodeRole", String.class);

        if(!table.hasColumn("NodeState"))
            table.addColumn("NodeState", String.class);

        if(!table.hasColumn("TransitionMap"))
            table.addColumn("TransitionMap", String.class);
    }

    private static void SetupNodeRoles(List<Node> nodes, List<NodeRoleDecorator> nodeRoles) {
        var nodesCount = nodes.stream().count();
        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();

        nodeRoles.stream()
                .sorted(Comparator.comparingDouble(NodeRoleDecorator::getCoverage))
                .forEach(nodeRole -> {
                    var roleNodesNumber = nodesCount * nodeRole.getCoverage();
                    if(nodeRole.getMinCoverage() > roleNodesNumber){
                        roleNodesNumber = nodeRole.getMinCoverage();
                    }
                    var notAssignedToRoleNodes = nodes.stream().filter(x -> x.getAttribute("NodeRole") == null || x.getAttribute("NodeRole").toString().isEmpty()).collect(Collectors.toList());
                    Collections.shuffle(notAssignedToRoleNodes);
                    for (int i = 0; i < roleNodesNumber && i < notAssignedToRoleNodes.stream().count(); i++) {
                        var node = notAssignedToRoleNodes.get(i);
                        node.setAttribute("NodeRole", nodeRole.getNodeRole().getName());
                        try {
                            node.setAttribute("TransitionMap", mapper.writeValueAsString(nodeRole.getNodeRole().getTransitionMap()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private static void SetupNodeStates(List<Node> nodes, List<NodeRoleDecorator> nodeRoles) {
        nodeRoles.stream()
                .sorted(Comparator.comparingDouble(NodeRoleDecorator::getCoverage))
                .forEach(nodeRole -> {

                    var roleNodes = nodes.stream().filter(x -> x.getAttribute("NodeRole").toString().equals(nodeRole.getNodeRole().getName())).collect(Collectors.toList());

                    var nodeStates = nodeRole.getNodeStates();

                    nodeStates.stream().forEach(nodeState -> {
                        var nodesCount = roleNodes.size();
                        var roleStateNumber = nodesCount * nodeState.getCoverage();
                        if(nodeState.getMinCoverage() > roleStateNumber){
                            roleStateNumber = nodeState.getMinCoverage();
                        }
                        var notAssignedToRoleNodes = roleNodes.stream().filter(x -> x.getAttribute("NodeState") == null || x.getAttribute("NodeState").toString().isEmpty()).collect(Collectors.toList());
                        Collections.shuffle(notAssignedToRoleNodes);
                        for (int i = 0; i < roleStateNumber && i < notAssignedToRoleNodes.stream().count(); i++) {
                            var node = notAssignedToRoleNodes.get(i);
                            node.setAttribute("NodeState", nodeState.getNodeState().getName());
                        }
                    });
                });
    }
}
