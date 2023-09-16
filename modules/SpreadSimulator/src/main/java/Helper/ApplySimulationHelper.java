package Helper;

import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.Node.NodeStateDecorator;
import SimulationModel.SimulationModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ApplySimulationHelper {

    public static void Apply(Graph graph, SimulationModel simulationModel){
        var nodes = List.of(graph.getNodes().toArray());

        if(simulationModel == null)
            return;

        var nodeRoles = simulationModel.getNodeRoles();
        GenerateColorPaintings(nodeRoles);
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

    public static boolean ValidateGraph(Graph graph){

        var nodes = List.of(graph.getNodes().toArray());
        var table = nodes.get(0).getTable();
        return Validate(table);
    }

    public static void GenerateColorPaintings(List<NodeRoleDecorator> nodeRoles){
        var nodeStateList = new ArrayList<NodeStateDecorator>();
        for (NodeRoleDecorator nodeRole: nodeRoles) {
            nodeStateList.addAll(nodeRole.getNodeStates());
        }
        var nodeStatesCount = nodeStateList.stream().count();

        var colors = generateUniqueColors((int)nodeStatesCount);

        int i = 0;
        for (var node: nodeStateList) {
            node.setColor(colors.get(i));
            i++;
        }
    }

    public static List<Color> generateUniqueColors(int n) {
        List<Color> uniqueColors = new ArrayList<>();
        Random random = new Random();

        while (uniqueColors.size() < n) {
            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);

            Color newColor = new Color(red, green, blue);
            uniqueColors.add(newColor);
        }

        return uniqueColors;
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

    private static boolean Validate(Table table) {
        if(!table.hasColumn("NodeRole"))
            return false;

        if(!table.hasColumn("NodeState"))
            return false;

        if(!table.hasColumn("TransitionMap"))
            return false;

        return true;
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
                            node.setColor(nodeState.getColor());
                        }
                    });
                });
    }

    public static void PaintGraph(List<Node> nodes, List<NodeRoleDecorator> nodeRoles) {
        nodeRoles.stream()
                .sorted(Comparator.comparingDouble(NodeRoleDecorator::getCoverage))
                .forEach(nodeRole -> {
                    var nodeStates = nodeRole.getNodeStates();
                    nodeStates.stream().forEach(nodeState -> {
                        for (Node node : nodes) {
                            if(node.getAttribute("NodeState").toString().contains(nodeState.getNodeState().getName())){
                                node.setColor(nodeState.getColor());
                            }
                        }
                    });
                });
    }
}