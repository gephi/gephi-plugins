package modelBuilder.exporter;


import configLoader.ConfigLoader;
import helper.ObjectMapperHelper;
import simulationModel.node.NodeRole;
import simulationModel.node.NodeState;
import simulationModel.transition.TransitionCondition;
import simulationModel.transition.TransitionNoCondition;
import simulationModel.transition.TransitionType;
import lombok.Setter;
import org.codehaus.jackson.type.TypeReference;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;
import org.openide.util.lookup.ServiceProvider;

import java.io.IOException;
import java.util.List;

import javax.swing.*;
import java.io.File;
import java.util.stream.Collectors;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class ModelBuilderExporter implements PluginGeneralActionsManipulator {
    private Graph graph;

    @Setter
    private File path;
    private NodeRole nodeRole;
    @Setter
    private String modelName;
    @Setter
    private String modeDescription;

    @Override
    public void execute() {
        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        nodeRole = new NodeRole();
        var transitionEdges = List.of(graph.getEdges().toArray()).stream().filter(x -> x.getLabel().equals(ConfigLoader.modelBuilderLabelTransition));

        nodeRole.setName(modelName);
        nodeRole.setDescription(modeDescription);

        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();

        var transitionList = transitionEdges.map(transition -> {
            var source = transition.getSource();
            var destination = transition.getTarget();
            var transitionType = TransitionType.valueOf(transition.getAttribute(ConfigLoader.colNameModelBuilderTransitionType).toString());
            var probability = Double.parseDouble(transition.getAttribute(ConfigLoader.colNameModelBuilderProbability).toString());

            var sourceNodeState = source.getAttribute(ConfigLoader.colNameModelBuilderDescription) != null ?
                    new NodeState(source.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString(), source.getAttribute("Description").toString())
                    :
                    new NodeState(source.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString());
            var destinationNodeState = destination.getAttribute("Description") != null ?
                    new NodeState(destination.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString(), destination.getAttribute("Description").toString())
                    :
                    new NodeState(destination.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString());



            switch (transitionType){
                case noConditionProbability:
                    return new TransitionNoCondition(transitionType, sourceNodeState, destinationNodeState, probability);
                case conditionProbability:
                    List<String> provNeighbours;
                    var provNeighboursContent = transition.getAttribute(ConfigLoader.colNameModelBuilderProvocativeNeighbours).toString();
                    try {
                        provNeighbours = mapper.readValue(provNeighboursContent, new TypeReference<List<String>>() {});
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return new TransitionCondition(transitionType, sourceNodeState, destinationNodeState, probability, provNeighbours);
                default:
                    throw new NotImplementedException();
            }

        }).collect(Collectors.toList());

        nodeRole.setTransitionMap(transitionList);

        try {
            mapper.writeValue(path, nodeRole);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getName() {
        return "State machine exporter";
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
        return new ModelBuilderExporterUI();
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public int getPosition() {
        return 3;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
