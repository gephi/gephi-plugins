package modelBuilder.importer;


import configLoader.ConfigLoader;
import helper.ObjectMapperHelper;
import modelBuilder.stateBuilder.StateBuilder;
import modelBuilder.transitionBuilder.TransitionBuilder;
import simulationModel.node.NodeRole;
import simulationModel.node.NodeState;
import simulationModel.transition.Transition;
import simulationModel.transition.TransitionCondition;
import simulationModel.transition.TransitionNoCondition;
import lombok.Setter;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class ModelBuilderImporter implements PluginGeneralActionsManipulator {
    private Graph graph;

    @Setter
    private File path;
    private NodeRole nodeRole;

    @Override
    public void execute() {
        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        var nodeTable = graph.getModel().getNodeTable();
        var edgeTable = graph.getModel().getEdgeTable();

        StateBuilder.PrepareTable(nodeTable);
        TransitionBuilder.PrepareTable(edgeTable);

        var gec = Lookup.getDefault().lookup(GraphElementsController.class);

        nodeRole = new NodeRole();

        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();

        try {
            nodeRole = mapper.readValue(path, NodeRole.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var transitionMap = nodeRole.getTransitionMap();
        var sourceStates = transitionMap.stream().map(trn -> trn.getSourceState()).collect(Collectors.toList());
        var destinationStates = transitionMap.stream().map(trn -> trn.getDestinationState()).collect(Collectors.toList());

        var nodes = new ArrayList<Node>();

        CreateStateNodes(gec, sourceStates, nodes);
        CreateStateNodes(gec, destinationStates, nodes);

        nodes.forEach(n -> graph.addNode(n));

        transitionMap.forEach(t -> {
            switch (t.getTransitionType()){
                case noConditionProbability:
                    CreateNoConditionProbabilityEdge(gec, nodes,(TransitionNoCondition) t);
                    break;
                case conditionProbability:
                    try {
                        CreateConditionProbabilityEdge(gec, nodes,(TransitionCondition) t);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        });

    }

    private void CreateStateNodes(GraphElementsController gec, List<NodeState> states, ArrayList<Node> nodes) {
        states.forEach(x -> {
            if(nodes.stream().filter(n -> n.getAttribute(ConfigLoader.colNameModelBuilderNodeState).equals(x.getName())).count() == 0){
                var node = gec.createNode("State");
                node.setAttribute(ConfigLoader.colNameModelBuilderNodeState, x.getName());
                node.setAttribute(ConfigLoader.colNameModelBuilderDescription, x.getDescription());
                nodes.add(node);
            }
        });
    }

    private void CreateNoConditionProbabilityEdge(GraphElementsController gec, ArrayList<Node> nodes, TransitionNoCondition t) {
        var edge = createDefaultEdge(gec, nodes, t);
        edge.setAttribute(ConfigLoader.colNameModelBuilderProbability, t.getProbability());
        graph.addEdge(edge);
    }

    private void CreateConditionProbabilityEdge(GraphElementsController gec, ArrayList<Node> nodes, TransitionCondition t) throws IOException {
        var edge = createDefaultEdge(gec, nodes, t);
        edge.setAttribute(ConfigLoader.colNameModelBuilderProbability, t.getProbability());
        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();
        var provNeigh = mapper.writeValueAsString(t.getProvocativeNeighborName());
        edge.setAttribute(ConfigLoader.colNameModelBuilderProvocativeNeighbours, provNeigh);
        graph.addEdge(edge);
    }

    private static Edge createDefaultEdge(GraphElementsController gec, ArrayList<Node> nodes, Transition t) {
        var sourceName = t.getSourceState().getName();
        var destinationName = t.getDestinationState().getName();

        var sourceNode = nodes.stream().filter(n -> n.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString().equals(sourceName)).findFirst().get();
        var destinationNode = nodes.stream().filter(n -> n.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString().equals(destinationName)).findFirst().get();

        var edge = gec.createEdge(sourceNode, destinationNode, true);
        edge.setLabel(ConfigLoader.modelBuilderLabelTransition);
        edge.setAttribute(ConfigLoader.colNameModelBuilderTransitionType, t.getTransitionType().toString());
        return edge;
    }

    @Override
    public String getName() {
        return "State machine importer";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new ModelBuilderImporterUI();
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public int getPosition() {
        return 4;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
