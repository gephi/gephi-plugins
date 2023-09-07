package Simulation;


import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeState;
import SimulationModel.Transition.Transition;
import lombok.Setter;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class Simulation implements PluginGeneralActionsManipulator {

    @Setter
    private NodeState nodeState;

    @Override
    public void execute() {
        DataTablesController dtc = Lookup.getDefault().lookup(DataTablesController.class);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        var mapper = ObjectMapperHelper.CustomObjectMapperCreator();
        var nodes = List.of(graph.getNodes().toArray());
        for (Node node: nodes) {
            var transition = node.getAttribute("TransitionMap");
            try {
                Map<String, Transition> transitionMap = mapper.readValue(transition.toString(), Map.class);
                transitionMap.values();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public String getName() {
        return "Step Simulation";
    }

    @Override
    public String getDescription() {
        return "Load model option provides creation new model folders and load model SIM";
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

}
