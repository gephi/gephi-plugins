package modelBuilder.stateBuilder;


import configLoader.ConfigLoader;
import lombok.Setter;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.util.List;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class StateBuilder implements PluginGeneralActionsManipulator {
    private Graph graph;
    private Table nodeTable;
    @Setter
    private String name;
    @Setter
    private String description;

    public StateBuilder(){
    }
    @Override
    public void execute() {
        if(name == null || name.isEmpty())
            return;

        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        nodeTable = graph.getModel().getNodeTable();
        PrepareTable(this.nodeTable);

        var nodes  = List.of(graph.getNodes().toArray());
        if (nodes.stream().filter(n -> n.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString().equals(name)).count() > 0){
            JOptionPane.showMessageDialog(null, "State " + name  + " already exist");
            return;
        }

        var node = gec.createNode(ConfigLoader.modelBuilderLabelState);
        node.setAttribute(ConfigLoader.colNameModelBuilderNodeState, name);
        node.setAttribute(ConfigLoader.colNameModelBuilderDescription, description);
        graph.addNode(node);
    }

    public static void PrepareTable(Table table) {
        if(!table.hasColumn(ConfigLoader.colNameModelBuilderNodeState))
            table.addColumn(ConfigLoader.colNameModelBuilderNodeState, String.class);
        if(!table.hasColumn(ConfigLoader.colNameModelBuilderDescription))
            table.addColumn(ConfigLoader.colNameModelBuilderDescription, String.class);
    }

    @Override
    public String getName() {
        return "Add state";
    }

    @Override
    public String getDescription() {
        return "Add state - state machine model builder";
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new StateBuilderUI(this);
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

}
