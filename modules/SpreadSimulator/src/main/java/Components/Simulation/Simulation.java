package Components.Simulation;

import Components.Simulation.Report.SimulationStepReport;
import Helper.ApplySimulationHelper;
import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.Node.NodeState;
import SimulationModel.Node.NodeStateDecorator;
import SimulationModel.Transition.Transition;
import SimulationModel.Transition.TransitionCondition;
import SimulationModel.Transition.TransitionNoCondition;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.type.TypeReference;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.NotImplementedException;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Getter
@Setter
public class Simulation {
    private  Integer step;
    private Graph graph;
    List<NodeRoleDecorator> nodeRoleDecoratorList;
    List<SimulationStepReport> report;

    public Simulation(Graph graph) {
        this.graph = graph;
        GenerateNodeDecoratorList();
        report = new ArrayList<>();
        step = 0;
    }

    public void Step(){
        step += 1;
        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();
        var table = graph.getModel().getNodeTable();
        if(!table.hasColumn("NewNodeState"))
            table.addColumn("NewNodeState", String.class);
        var nodes = graph.getNodes();

        for (Node node : nodes) {
            node.setAttribute("NewNodeState", node.getAttribute("NodeState").toString());
            var content = node.getAttribute("TransitionMap").toString();
            try {
                List<Transition> transitions = mapper.readValue(content, new TypeReference<List<Transition>>() {});
                var probabilityTransition = transitions.stream().filter(transition -> transition.getSourceState().getName().contains(node.getAttribute("NodeState").toString())).collect(Collectors.toList());
                for (Transition transition : probabilityTransition) {
                    switch (transition.getTransitionType()){
                        case noConditionProbability:
                            NoConditionProbabilityNode(node, transition);
                            break;
                        case conditionProbability:
                            ConditionProbabilityNode(graph, node, transition);
                            break;
                        default:
                            throw new NotImplementedException("Unknow transitiontype");
                    }
                }
            } catch (IOException ex) {
                table.removeColumn("NewNodeState");
                throw new RuntimeException(ex);
            }
        }

        for (Node node : nodes) {
            node.setAttribute("NodeState", node.getAttribute("NewNodeState").toString());
        }

        ApplySimulationHelper.PaintGraph(List.of(nodes.toArray()), nodeRoleDecoratorList);
        table.removeColumn("NewNodeState");
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

    private void ConditionProbabilityNode(Graph graph, Node node,Transition transition) {
        Random rnd = new Random();
        var trn = (TransitionCondition) transition;

        var neighbours = List.of(graph.getNeighbors(node).toArray());

        var neighboursNames = neighbours.stream().map(n -> n.getAttribute("NodeState").toString()).distinct().collect(Collectors.toList());

        if(!IsInNeighbourhood(neighboursNames, trn))
            return;

        var x = rnd.nextDouble();
        if (trn.getProbability() < x) {
            return;
        }
        ChangeState(node, trn.getDestinationState());
    }

    private void ChangeState(Node node, NodeState trn) {
        node.setAttribute("NewNodeState", trn.getName());
    }

    private boolean IsInNeighbourhood(List<String> neighboursNames, TransitionCondition trn) {
        for (String neighbourTrn : trn.getProvocativeNeighborName()) {
            if(neighboursNames.contains(neighbourTrn))
                return true;
        }
        return false;
    }

    private void GenerateNodeDecoratorList() {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        var nodeRoles = nodes.stream()
                .map(node -> node.getAttribute("NodeRole").toString())
                .distinct()
                .collect(Collectors.toList());

        this.nodeRoleDecoratorList = nodeRoles.stream().map(nodeRole -> new NodeRoleDecorator(new NodeRole(nodeRole)))
                .peek(this::populateNodeRoleDecorator)
                .collect(Collectors.toList());
    }

    private void populateNodeRoleDecorator(NodeRoleDecorator nodeRoleDecorator) {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        var nodesCount = nodes.size();

        var nodeStates = nodes.stream()
                .filter(node -> node.getAttribute("NodeRole").equals(nodeRoleDecorator.getNodeRole().getName()))
                .map(node -> node.getAttribute("NodeState").toString())
                .distinct()
                .map(nodeState -> new NodeStateDecorator(new NodeState(nodeState)))
                .collect(Collectors.toList());

        nodeStates.forEach(state -> state.setColor(getStateColor(state)));

        nodeRoleDecorator.setNodeStates(nodeStates);
        var nodeRoleCount = nodes.stream()
                .filter(node -> node.getAttribute("NodeRole").equals(nodeRoleDecorator.getNodeRole().getName()))
                .count();

        nodeRoleDecorator.setMinCoverage((int) nodeRoleCount);
        nodeRoleDecorator.setCoverage((double)nodeRoleCount / nodesCount);

        nodeStates.forEach(nodeState -> {
            var nodeStateCount = nodes.stream()
                    .filter(node -> node.getAttribute("NodeRole").equals(nodeRoleDecorator.getNodeRole().getName()))
                    .filter(node -> node.getAttribute("NodeState").equals(nodeState.getNodeState().getName()))
                    .count();

            nodeState.setCoverage((double)nodeStateCount / nodeRoleCount);
            nodeState.setMinCoverage((int) nodeStateCount);
        });
    }

    private Color getStateColor(NodeStateDecorator state) {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        return nodes.stream().filter(node -> node.getAttribute("NodeState").equals(state.getNodeState().getName())).collect(Collectors.toList()).get(0).getColor();
    }
}
