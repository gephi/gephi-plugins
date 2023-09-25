package ModelBuilder.StateBuilder;


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

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class ModelBuilder implements PluginGeneralActionsManipulator {
    private Graph graph;
    private Table nodeTable;
    @Setter
    private String name;
    @Setter
    private String description;

    public ModelBuilder(){
    }
    @Override
    public void execute() {
        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        nodeTable = graph.getModel().getNodeTable();
        PrepareTable(this.nodeTable);
        var node = gec.createNode("State");

        node.setAttribute("NodeState", name);
        node.setAttribute("Description", description);

        graph.addNode(node);
    }

    private void PrepareTable(Table table) {
        if(!table.hasColumn("NodeState"))
            table.addColumn("NodeState", String.class);
        if(!table.hasColumn("Description"))
            table.addColumn("Description", String.class);
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
        return new ModelBuilderUI(this);
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
