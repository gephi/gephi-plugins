package Components.Simulation;

import Helper.ApplySimulationHelper;
import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.Node.NodeState;
import SimulationModel.Transition.Transition;
import SimulationModel.Transition.TransitionCondition;
import SimulationModel.Transition.TransitionNoCondition;
import org.codehaus.jackson.type.TypeReference;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.NotImplementedException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StepButton extends JButton {
    private Graph graph;
    private List<NodeRoleDecorator> nodeRoleDecoratorList;

    public StepButton(Graph graph, List<NodeRoleDecorator> nodeRoleDecoratorList) {
        this.setText("Step Simulation");
        this.graph = graph;
        this.nodeRoleDecoratorList = nodeRoleDecoratorList;
        this.addActionListener(new StepButtonListener());
    }


    private class StepButtonListener implements ActionListener {

        private final Random rnd = new Random();

        @Override
        public void actionPerformed(ActionEvent e) {
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
                                ConditionProbabilityNode(node, transition);
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
        }

        private void NoConditionProbabilityNode(Node node, Transition transition) {
            var trn = (TransitionNoCondition) transition;
            var x = rnd.nextDouble();
            if (trn.getProbability() < x) {
                return;
            }
            ChangeState(node, trn.getDestinationState());
        }

        private void ConditionProbabilityNode(Node node, Transition transition) {
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
    }
}
