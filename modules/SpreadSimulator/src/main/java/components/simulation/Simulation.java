package components.simulation;

import components.simulationLogic.report.SimulationStepReport;
import configLoader.ConfigLoader;
import simulationModel.node.NodeRole;
import simulationModel.node.NodeRoleDecorator;
import simulationModel.node.NodeState;
import simulationModel.node.NodeStateDecorator;
import simulationModel.SimulationModel;
import lombok.Getter;
import lombok.Setter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Simulation implements Cloneable {
    protected Integer step;
    protected Graph graph;
    protected SimulationModel simulationModel;
    List<NodeRoleDecorator> nodeRoleDecoratorList;
    List<SimulationStepReport> report;

    public abstract void Step();

    public Simulation(Graph graph, SimulationModel simulationModel) {
        this.graph = graph;
        GenerateNodeDecoratorList();
        report = new ArrayList<>();
        step = 0;
        this.simulationModel = simulationModel;
    }

    protected void GenerateNodeDecoratorList() {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        var nodeRoles = nodes.stream()
                .map(node -> node.getAttribute(ConfigLoader.colNameNodeRole).toString())
                .distinct()
                .collect(Collectors.toList());

        this.nodeRoleDecoratorList = nodeRoles.stream().map(nodeRole -> new NodeRoleDecorator(new NodeRole(nodeRole)))
                .peek(this::PopulateNodeRoleDecorator)
                .collect(Collectors.toList());
    }

    private void PopulateNodeRoleDecorator(NodeRoleDecorator nodeRoleDecorator) {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        var nodesCount = nodes.size();

        var nodeStates = nodes.stream()
                .filter(node -> node.getAttribute(ConfigLoader.colNameNodeRole).equals(nodeRoleDecorator.getNodeRole().getName()))
                .map(node -> node.getAttribute(ConfigLoader.colNameNodeState).toString())
                .distinct()
                .map(nodeState -> new NodeStateDecorator(new NodeState(nodeState)))
                .collect(Collectors.toList());

        nodeStates.forEach(state -> state.setColor(GetStateColor(state)));

        nodeRoleDecorator.setNodeStates(nodeStates);
        var nodeRoleCount = nodes.stream()
                .filter(node -> node.getAttribute(ConfigLoader.colNameNodeRole).equals(nodeRoleDecorator.getNodeRole().getName()))
                .count();

        nodeRoleDecorator.setMinCoverage((int) nodeRoleCount);
        nodeRoleDecorator.setCoverage((double) nodeRoleCount / nodesCount);

        nodeStates.forEach(nodeState -> {
            var nodeStateCount = nodes.stream()
                    .filter(node -> node.getAttribute(ConfigLoader.colNameNodeRole).equals(nodeRoleDecorator.getNodeRole().getName()))
                    .filter(node -> node.getAttribute(ConfigLoader.colNameNodeState).equals(nodeState.getNodeState().getName()))
                    .count();

            nodeState.setCoverage((double) nodeStateCount / nodeRoleCount);
            nodeState.setMinCoverage((int) nodeStateCount);
        });
    }

    protected void ChangeState(Node node, NodeState trn) {
        node.setAttribute(ConfigLoader.colNameNewNodeState, trn.getName());
    }

    private Color GetStateColor(NodeStateDecorator state) {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        return nodes.stream().filter(node -> node.getAttribute(ConfigLoader.colNameNodeState).equals(state.getNodeState().getName())).collect(Collectors.toList()).get(0).getColor();
    }

    @Override
    public Simulation clone() {
        try {
            return (Simulation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
