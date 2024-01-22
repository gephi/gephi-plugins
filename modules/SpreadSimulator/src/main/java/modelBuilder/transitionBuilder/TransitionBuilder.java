package modelBuilder.transitionBuilder;


import configLoader.ConfigLoader;
import helper.ObjectMapperHelper;
import simulationModel.transition.TransitionType;
import lombok.Setter;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;

import java.io.IOException;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class TransitionBuilder implements PluginGeneralActionsManipulator {
    private Graph graph;
    private Table edgeTable;
    @Setter
    private Node sourceNode;
    @Setter
    private Node destinationNode;
    @Setter
    private TransitionType transitionType;
    @Setter
    private List<String> provocativeNeighbours;
    @Setter
    private Double probability;

    public TransitionBuilder(){
    }
    @Override
    public void execute() {
        if(sourceNode == destinationNode){
            JOptionPane.showMessageDialog(null, "Cannot create loop.");
            return;
        }

        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        edgeTable = graph.getModel().getEdgeTable();
        PrepareTable(this.edgeTable);
        var edge = gec.createEdge(sourceNode, destinationNode, true);

        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();

        edge.setAttribute(ConfigLoader.colNameModelBuilderTransitionType, transitionType.toString());
        try {
            edge.setAttribute(ConfigLoader.colNameModelBuilderProvocativeNeighbours, mapper.writeValueAsString(provocativeNeighbours));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        edge.setLabel(ConfigLoader.modelBuilderLabelTransition);
        edge.setAttribute(ConfigLoader.colNameModelBuilderProbability, probability);

        graph.addEdge(edge);
    }

    public static void PrepareTable(Table table) {
        if(!table.hasColumn(ConfigLoader.colNameModelBuilderTransitionType))
            table.addColumn(ConfigLoader.colNameModelBuilderTransitionType, String.class);
        if(!table.hasColumn(ConfigLoader.colNameModelBuilderProvocativeNeighbours))
            table.addColumn(ConfigLoader.colNameModelBuilderProvocativeNeighbours, String.class);
        if(!table.hasColumn(ConfigLoader.colNameModelBuilderProbability))
            table.addColumn(ConfigLoader.colNameModelBuilderProbability, Double.class);
    }

    @Override
    public String getName() {
        return "Add transition";
    }

    @Override
    public String getDescription() {
        return "Add transition - add transition to model";
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new TransitionBuilderUI(this);
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public int getPosition() {
        return 2;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

}
