package components.simulation;

import components.simulationLogic.report.SimulationStepReport;
import configLoader.ConfigLoader;
import helper.ApplySimulationHelper;
import simulationModel.interaction.RelativeFreeNodesInteraction;
import simulationModel.SimulationModel;
import simulationModel.transition.Transition;
import simulationModel.transition.TransitionCondition;
import simulationModel.transition.TransitionNoCondition;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class SimulationRelativeFreeNodes extends Simulation {

    public SimulationRelativeFreeNodes(Graph graph, SimulationModel simulationModel) {
        super(graph, simulationModel);
    }

    @Override
    public void Step() {
        step += 1;
        var table = graph.getModel().getNodeTable();
        if (!table.hasColumn(ConfigLoader.colNameNewNodeState))
            table.addColumn(ConfigLoader.colNameNewNodeState, String.class);
        var nodes = graph.getNodes();

        var selectedNodes = new ArrayList<>(List.of(nodes.toArray()));

        var interaction = (RelativeFreeNodesInteraction) simulationModel.getInteraction();

        var changesNodes = new ArrayList<Node>();

        for (int i = 0; (double) i / selectedNodes.size() < interaction.getNumber(); i+=2) {
            var node = GetRandomNode(selectedNodes);
            var neighbour = GetRandomNeighbor(node);
            selectedNodes.remove(node);
            selectedNodes.remove(neighbour);
            changesNodes.add(node);
            changesNodes.add(neighbour);
            GetTransitionByNode(node, neighbour);
            GetTransitionByNode(neighbour, node);
        }

        for (Node n : changesNodes) {
            n.setAttribute(ConfigLoader.colNameNodeState, n.getAttribute(ConfigLoader.colNameNewNodeState).toString());
        }

        ApplySimulationHelper.PaintGraph(List.of(nodes.toArray()), nodeRoleDecoratorList);
        table.removeColumn(ConfigLoader.colNameNewNodeState);
        GenerateNodeDecoratorList();
        this.report.add(new SimulationStepReport(this.step, this.nodeRoleDecoratorList));
    }

    private void GetTransitionByNode(Node node, Node neighbour) {
        node.setAttribute(ConfigLoader.colNameNewNodeState, node.getAttribute(ConfigLoader.colNameNodeState).toString());
        List<Transition> transitions = simulationModel.getNodeRoles().stream().filter(role -> role.getNodeRole().getName().toString().equals(node.getAttribute(ConfigLoader.colNameNodeRole).toString())).findFirst().get().getNodeRole().getTransitionMap();
        var probabilityTransition = transitions.stream().filter(transition -> transition.getSourceState().getName().equals(node.getAttribute(ConfigLoader.colNameNodeState).toString())).collect(Collectors.toList());
        Collections.shuffle(probabilityTransition);
        for (Transition transition : probabilityTransition) {
            switch (transition.getTransitionType()) {
                case zeroProbability:
                    break;
                case noConditionProbability:
                    NoConditionProbabilityNode(node, transition);
                    break;
                case conditionProbability:
                    ConditionProbabilityNode(node, neighbour, transition);
                    break;
                default:
                    throw new NotImplementedException(ConfigLoader.messageErrorUnknowTransitionType);
            }
        }
    }

    private Node GetRandomNeighbor(Node node) {
        var nodes = graph.getNeighbors(node);
        var nodesList = new ArrayList<>(List.of(nodes.toArray()));
        Collections.shuffle(nodesList);
        var newNode = nodesList.get(0);
        return newNode;
    }

    private Node GetRandomNode(ArrayList<Node> selectedNodes) {
        Collections.shuffle(selectedNodes);
        var node = selectedNodes.get(0);
        return node;
    }

    private void NoConditionProbabilityNode(Node node, Transition transition) {
        Random rnd = new Random();
        var trn = (TransitionNoCondition) transition;
        var x = rnd.nextDouble();
        if (trn.getProbability() < x) {
            return;
        }
        ChangeState(node, trn.getDestinationState());
    }

    private void ConditionProbabilityNode(Node node, Node newNode, Transition transition) {
        Random rnd = new Random();
        var trn = (TransitionCondition) transition;
        if (!trn.getProvocativeNeighborName().contains(newNode.getAttribute(ConfigLoader.colNameNodeState).toString()))
            return;
        var x = rnd.nextDouble();
        if (trn.getProbability() < x) {
            return;
        }
        ChangeState(node, trn.getDestinationState());
    }
}
