package ModelBuilder.TransitionBuilder;


import Helper.ObjectMapperHelper;
import SimulationModel.Transition.TransitionType;
import lombok.Getter;
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
        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        edgeTable = graph.getModel().getEdgeTable();
        PrepareTable(this.edgeTable);
        var edge = gec.createEdge(sourceNode, destinationNode, true);

        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();

        edge.setAttribute("TransitionType", transitionType.toString());
        try {
            edge.setAttribute("ProvocativeNeighbours", mapper.writeValueAsString(provocativeNeighbours));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        edge.setLabel("Transition");
        edge.setAttribute("Probability", probability);

        graph.addEdge(edge);
    }

    private void PrepareTable(Table table) {
        if(!table.hasColumn("TransitionType"))
            table.addColumn("TransitionType", String.class);
        if(!table.hasColumn("ProvocativeNeighbours"))
            table.addColumn("ProvocativeNeighbours", String.class);
        if(!table.hasColumn("Probability"))
            table.addColumn("Probability", Double.class);
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
