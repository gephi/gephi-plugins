package SimulationModelBuilder;


import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.SimulationModel;
import lombok.Setter;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class SimulationModelBuilder implements PluginGeneralActionsManipulator {

    @Setter
    private SimulationModel simulationModel;

    @Override
    public void execute() {
        DataTablesController dtc = Lookup.getDefault().lookup(DataTablesController.class);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        var nodes = List.of(graph.getNodes().toArray());
        var table = nodes.get(0).getTable();

        CreateModelColumns(table);

        if(simulationModel == null)
            return;


        var nodeRoles = simulationModel.getNodeRoles();
        SetupNodeRoles(nodes, nodeRoles);
        SetupNodeStates(nodes, nodeRoles);

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

    private void SetupNodeStates(List<Node> nodes, List<NodeRoleDecorator> nodeRoles) {
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

    private static void CreateModelColumns(Table table) {
        if(!table.hasColumn("NodeRole"))
            table.addColumn("NodeRole", String.class);

        if(!table.hasColumn("NodeState"))
            table.addColumn("NodeState", String.class);

        if(!table.hasColumn("TransitionMap"))
            table.addColumn("TransitionMap", String.class);
    }

    @Override
    public String getName() {
        return "Spread Simulation Builder";
    }

    @Override
    public String getDescription() {
        return "Simulation Model Builder creates empty data model required to conduct spread simulation";
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new SimulationModelBuilderUI(this.simulationModel);
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

}
