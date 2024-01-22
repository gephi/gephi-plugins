package components.simulation;

import components.simulationLogic.report.SimulationStepReport;
import configLoader.ConfigLoader;
import helper.ApplySimulationHelper;
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


public class SimulationAll extends Simulation {

    public SimulationAll(Graph graph, SimulationModel simulationModel) {
        super(graph, simulationModel);
    }

    @Override
    public void Step(){
        step += 1;
        var table = graph.getModel().getNodeTable();
        if(!table.hasColumn(ConfigLoader.colNameNewNodeState))
            table.addColumn(ConfigLoader.colNameNewNodeState, String.class);
        var nodes = graph.getNodes();

        var selectedNodes = new ArrayList<> (List.of(nodes.toArray()));

        for (Node node : selectedNodes) {
            node.setAttribute(ConfigLoader.colNameNewNodeState, node.getAttribute(ConfigLoader.colNameNodeState).toString());
            List<Transition> transitions = simulationModel.getNodeRoles().stream().filter(role -> role.getNodeRole().getName().toString().equals(node.getAttribute(ConfigLoader.colNameNodeRole).toString())).findFirst().get().getNodeRole().getTransitionMap();
            var probabilityTransition = transitions.stream().filter(transition -> transition.getSourceState().getName().equals(node.getAttribute(ConfigLoader.colNameNodeState).toString())).collect(Collectors.toList());
            Collections.shuffle(probabilityTransition);
            for (Transition transition : probabilityTransition) {
                switch (transition.getTransitionType()){
                    case zeroProbability:
                        break;
                    case noConditionProbability:
                        NoConditionProbabilityNode(node, transition);
                        break;
                    case conditionProbability:
                        ConditionProbabilityNode(graph, node, transition);
                        break;
                    default:
                        throw new NotImplementedException(ConfigLoader.messageErrorUnknowTransitionType);
                }
            }
        }

        for (Node node : selectedNodes) {
            node.setAttribute(ConfigLoader.colNameNodeState, node.getAttribute(ConfigLoader.colNameNewNodeState).toString());
        }

        ApplySimulationHelper.PaintGraph(List.of(nodes.toArray()), nodeRoleDecoratorList);
        table.removeColumn(ConfigLoader.colNameNewNodeState);
        GenerateNodeDecoratorList();
        this.report.add(new SimulationStepReport(this.step, this.nodeRoleDecoratorList));
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

    private void ConditionProbabilityNode(Graph graph, Node node, Transition transition) {
        var trn = (TransitionCondition) transition;
        var neighbours = List.of(graph.getNeighbors(node).toArray());
        if(!IsInNeighbourhood(neighbours, trn))
            return;

        Random rnd = new Random();
        if (trn.getProbability() < rnd.nextDouble()) {
            return;
        }
        ChangeState(node, trn.getDestinationState());
    }

    private boolean IsInNeighbourhood(List<Node> neighbours, TransitionCondition trn) {
        var provListName = neighbours.stream().filter(n -> {
            var nodeStateName = n.getAttribute(ConfigLoader.colNameNodeState).toString();
            return trn.getProvocativeNeighborName().contains(nodeStateName);
        }).collect(Collectors.toList());
        return provListName.stream().count() > 0;
    }
}
