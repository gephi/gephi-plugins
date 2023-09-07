package ModelCreator;


import SimulationModel.Node.NodeRole;
import SimulationModel.SimulationModel;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.map.ObjectMapper;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class ModelCreator implements PluginGeneralActionsManipulator {

    @Setter @Getter
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
        SetupSimulationName(nodes, simulationModel);
        SetupNodeRoles(nodes, nodeRoles);
        SetupNodeStates(nodes, nodeRoles);

    }

    private void SetupSimulationName(List<Node> nodes, SimulationModel simulationModel) {
        nodes.stream().forEach(node -> node.setAttribute("SimulationName", simulationModel.getName()));
    }

    private static void SetupNodeRoles(List<Node> nodes, List<NodeRole> nodeRoles) {
        var nodesCount = nodes.stream().count();

        nodeRoles.stream()
                .sorted(Comparator.comparingDouble(NodeRole::getCoverage))
                .forEach(nodeRole -> {
            var roleNodesNumber = nodesCount * nodeRole.getCoverage();
            if(nodeRole.getMinCoverage() > roleNodesNumber){
                roleNodesNumber = nodeRole.getMinCoverage();
            }
            var notAssignedToRoleNodes = nodes.stream().filter(x -> x.getAttribute("NodeRole") == null || x.getAttribute("NodeRole").toString().isEmpty()).collect(Collectors.toList());
            Collections.shuffle(notAssignedToRoleNodes);
            for (int i = 0; i < roleNodesNumber && i < notAssignedToRoleNodes.stream().count(); i++) {
                var node = notAssignedToRoleNodes.get(i);
                node.setAttribute("NodeRole", nodeRole.getName());
            }
        });
    }

    private void SetupNodeStates(List<Node> nodes, List<NodeRole> nodeRoles) {
        var mapper = new ObjectMapper();
        nodeRoles.stream()
                .sorted(Comparator.comparingDouble(NodeRole::getCoverage))
                .forEach(nodeRole -> {

                    var roleNodes = nodes.stream().filter(x -> x.getAttribute("NodeRole").toString().equals(nodeRole.getName())).collect(Collectors.toList());

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
                            node.setAttribute("NodeState", nodeState.getName());
                            try {
                                node.setAttribute("TransitionMap", mapper.writeValueAsString(nodeState.getTransitionMap()));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                });
    }

    private static void CreateModelColumns(Table table) {
        if(!table.hasColumn("SimulationName"))
            table.addColumn("SimulationName", String.class);

        if(!table.hasColumn("NodeRole"))
            table.addColumn("NodeRole", String.class);

        if(!table.hasColumn("NodeState"))
            table.addColumn("NodeState", String.class);

        if(!table.hasColumn("TransitionMap"))
            table.addColumn("TransitionMap", String.class);
    }

    @Override
    public String getName() {
        return "Set fire strategy";
    }

    @Override
    public String getDescription() {
        return "Fire strategy provide initial setup simulation on loaded models.";
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new ModelCreatorUI(this.simulationModel);
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

}
