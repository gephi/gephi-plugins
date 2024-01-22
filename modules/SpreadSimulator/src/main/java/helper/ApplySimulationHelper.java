package helper;

import configLoader.ConfigLoader;
import simulationModel.node.NodeRoleDecorator;
import simulationModel.node.NodeStateDecorator;
import simulationModel.SimulationModel;
import org.codehaus.jackson.map.ObjectMapper;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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
        SetupNodeRoles(nodes, nodeRoles);
        SetupNodeStates(nodes, nodeRoles);
        SaveSimulationModelIntoSimulationTmpFile(simulationModel);
    }

    private static void SaveSimulationModelIntoSimulationTmpFile(SimulationModel simulationModel) {
        ObjectMapper objectMapper = ObjectMapperHelper.CustomObjectMapperCreator();
        try {
            File folder = new File(ConfigLoader.folderSimulationTmp);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    JOptionPane.showMessageDialog(null,"Cannot create folder " + ConfigLoader.folderSimulationTmp);
                    return;
                }
            }
            File jsonFile = new File(ConfigLoader.folderSimulationTmp + ConfigLoader.folderSimulationTmpFilename);
            objectMapper.writeValue(jsonFile, simulationModel);
        } catch (IOException exx) {
            exx.printStackTrace();
        }
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

        var table = graph.getModel().getNodeTable();
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

        if(table.hasColumn("RootState"))
            table.removeColumn("RootState");
    }


    private static void CreateModelColumns(Table table) {
        if(!table.hasColumn("NodeRole"))
            table.addColumn("NodeRole", String.class);

        if(!table.hasColumn("NodeState"))
            table.addColumn("NodeState", String.class);

    }

    private static boolean Validate(Table table) {
        if(!table.hasColumn("NodeRole"))
            return false;

        if(!table.hasColumn("NodeState"))
            return false;

        return true;
    }

    private static void SetupNodeRoles(List<Node> nodes, List<NodeRoleDecorator> nodeRoles) {
        var nodesCount = nodes.stream().count();

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

    public static void PaintGraph(List<Node> nodes, List<NodeRoleDecorator> nodeRoles) {
        nodeRoles.stream()
                .forEach(nodeRole -> {
                    var nodeStates = nodeRole.getNodeStates();
                    var nodeRoleNodes = nodes.stream().filter(node -> nodeRole.getNodeRole().getName().equals(node.getAttribute("NodeRole").toString())).collect(Collectors.toList());
                    nodeStates.stream().forEach(nodeState -> {
                        var nodeRoleStateNodes = nodeRoleNodes.stream().filter(node -> nodeState.getNodeState().getName().equals(node.getAttribute("NodeState").toString())).collect(Collectors.toList());
                        nodeRoleStateNodes.forEach(node -> node.setColor(nodeState.getColor()));
                    });
                });
    }

    public static String GenerateName(List<NodeRoleDecorator> nodeRoles){
        var name = "SIMULATION-";
        for (NodeRoleDecorator role: nodeRoles) {
            name += role.getNodeRole().getName();
            name += "-";
            name += role.getCoverage().toString();
            name += "-states-";
            for (NodeStateDecorator state: role.getNodeStates()) {
                name += state.getNodeState().getName();
                name += "-";
                name += state.getCoverage().toString();
            }
            name += "_";
        }
        return name;
    }
}
