package Components.Simulation;

import Components.Simulation.Report.SimulationStepReport;
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

@Getter
@Setter
public class Simulation {
    private  Integer step;
    private Graph graph;
    private SimulationModel simulationModel;
    List<NodeRoleDecorator> nodeRoleDecoratorList;
    List<SimulationStepReport> report;

    public Simulation(Graph graph, SimulationModel simulationModel) {
        this.graph = graph;
        GenerateNodeDecoratorList();
        report = new ArrayList<>();
        step = 0;
        this.simulationModel = simulationModel;
    }

    public void Step(){
        step += 1;
        var table = graph.getModel().getNodeTable();
        if(!table.hasColumn("NewNodeState"))
            table.addColumn("NewNodeState", String.class);
        var nodes = graph.getNodes();
        var edges = graph.getEdges();

        var selectedNodes = SelectNodes(nodes, edges);

        for (Node node : selectedNodes) {
            node.setAttribute("NewNodeState", node.getAttribute("NodeState").toString());
            List<Transition> transitions = simulationModel.getNodeRoles().stream().filter(role -> role.getNodeRole().getName().toString().equals(node.getAttribute("NodeRole").toString())).findFirst().get().getNodeRole().getTransitionMap();
            var probabilityTransition = transitions.stream().filter(transition -> transition.getSourceState().getName().equals(node.getAttribute("NodeState").toString())).collect(Collectors.toList());
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
                        throw new NotImplementedException("Unknow transitiontype");
                }
            }
        }

        for (Node node : selectedNodes) {
            node.setAttribute("NodeState", node.getAttribute("NewNodeState").toString());
        }

        ApplySimulationHelper.PaintGraph(List.of(nodes.toArray()), nodeRoleDecoratorList);
        table.removeColumn("NewNodeState");
        GenerateNodeDecoratorList();
        this.report.add(new SimulationStepReport(this.step, this.nodeRoleDecoratorList));
    }

    private List<Node> SelectNodes(NodeIterable nodes, EdgeIterable edges) {
        var interaction = simulationModel.getInteraction();
        var nodeList = new ArrayList<> (List.of(nodes.toArray()));
        var edgeList = new ArrayList<> (List.of(edges.toArray()));
        int N = 0;
        switch (interaction.getInteractionType()){
            case All:
                return nodeList;
            case RelativeNodes:
                Collections.shuffle(nodeList);
                N = (int) (((RelativeNodesInteraction) interaction).getPercentage() * nodes.toArray().length);
                return nodeList.subList(0, N);
            case RelativeFreeNodes:
                Collections.shuffle(nodeList);
                N = ((RelativeFreeNodesInteraction) interaction).getNumber();
                return nodeList.subList(0, N);
            case RelativeEdges:
                Collections.shuffle(edgeList);
                N = (int) (((RelativeEdgesInteraction) interaction).getPercentage() * nodes.toArray().length);
                var selectedEdgesR = edgeList.subList(0, N);
                var sourceNodesR = selectedEdgesR.stream().map(edge -> edge.getSource()).collect(Collectors.toList());
                var targetNodesR = selectedEdgesR.stream().map(edge -> edge.getTarget()).collect(Collectors.toList());
                sourceNodesR.addAll(targetNodesR);
                return sourceNodesR;
            case RelativeFreeEdges:
                Collections.shuffle(edgeList);
                N = ((RelativeFreeEdgesInteraction) interaction).getNumber();
                var selectedEdges = edgeList.subList(0, N);
                var sourceNodes = selectedEdges.stream().map(edge -> edge.getSource()).collect(Collectors.toList());
                var targetNodes = selectedEdges.stream().map(edge -> edge.getTarget()).collect(Collectors.toList());
                sourceNodes.addAll(targetNodes);
                return sourceNodes;
        }
        return null;
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

        var neighboursNames = selectedNeighbours.stream().map(n -> n.getAttribute("NodeState").toString()).distinct().collect(Collectors.toList());

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
            if(neighboursNames.contains(neighbourTrn.toString()))
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
