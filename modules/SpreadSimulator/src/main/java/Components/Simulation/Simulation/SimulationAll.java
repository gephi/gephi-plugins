package Components.Simulation.Simulation;

import Components.Simulation.Report.SimulationStepReport;
import ConfigLoader.ConfigLoader;
import Helper.ApplySimulationHelper;
import SimulationModel.Interaction.RelativeEdgesInteraction;
import SimulationModel.Interaction.RelativeFreeEdgesInteraction;
import SimulationModel.Interaction.RelativeFreeNodesInteraction;
import SimulationModel.Interaction.RelativeNodesInteraction;
import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.Node.NodeState;
import SimulationModel.Node.NodeStateDecorator;
import SimulationModel.SimulationModel;
import SimulationModel.Transition.Transition;
import SimulationModel.Transition.TransitionCondition;
import SimulationModel.Transition.TransitionNoCondition;
import lombok.Getter;
import lombok.Setter;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.openide.util.NotImplementedException;

import java.awt.*;
import java.util.*;
import java.util.List;
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

        var selectedNodes = new ArrayList<> (List.of(nodes.toArray()));;

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
                        ConditionProbabilityNode(graph, node, selectedNodes, transition);
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

    private void ConditionProbabilityNode(Graph graph, Node node, List<Node> selectedNodes, Transition transition) {
        Random rnd = new Random();
        var trn = (TransitionCondition) transition;

        var neighbours = List.of(graph.getNeighbors(node).toArray());
        var selectedNeighbours = neighbours.stream().filter(neigh -> selectedNodes.contains(neigh)).collect(Collectors.toList());

        var neighboursNames = selectedNeighbours.stream().map(n -> n.getAttribute(ConfigLoader.colNameNodeState).toString()).distinct().collect(Collectors.toList());

        if(!IsInNeighbourhood(neighboursNames, trn))
            return;

        var x = rnd.nextDouble();
        if (trn.getProbability() < x) {
            return;
        }
        ChangeState(node, trn.getDestinationState());
    }

    private boolean IsInNeighbourhood(List<String> neighboursNames, TransitionCondition trn) {
        for (String neighbourTrn : trn.getProvocativeNeighborName()) {
            if(neighboursNames.contains(neighbourTrn.toString()))
                return true;
        }
        return false;
    }
}
