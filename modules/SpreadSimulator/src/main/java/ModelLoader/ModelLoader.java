package ModelLoader;


import SimulationModel.Node.NodeState;
import lombok.Setter;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class ModelLoader implements PluginGeneralActionsManipulator {

    @Setter
    private NodeState nodeState;

    @Override
    public void execute() {
        if(nodeState == null)
            return;
        try {
            Files.createDirectories(Path.of("models/" + nodeState.getName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "Load model";
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
        return new ModelLoaderUI();
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
