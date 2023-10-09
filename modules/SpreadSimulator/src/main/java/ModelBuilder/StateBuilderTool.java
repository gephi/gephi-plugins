package ModelBuilder;

import ModelBuilder.StateBuilder.StateBuilder;
import ModelBuilder.StateBuilder.StateBuilderPanel;
import ModelBuilder.TransitionBuilder.TransitionBuilder;
import ModelBuilder.TransitionBuilder.TransitionBuilderPanel;
import org.gephi.graph.api.*;
import org.gephi.tools.spi.*;
import org.gephi.visualization.VizController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = Tool.class)
public class StateBuilderTool implements Tool {

    private final StateBuilderToolUI ui = new StateBuilderToolUI();

    @Override
    public void select() {
    }

    @Override
    public void unselect() {
    }

    @Override
    public ToolEventListener[] getListeners() {
        return new ToolEventListener[]{
                (MouseClickEventListener) (positionViewport, position3d) -> {
                    var modelBuilder = new StateBuilder();
                    JOptionPane.showMessageDialog(null, new StateBuilderPanel(modelBuilder));
                    modelBuilder.execute();
                },
                (NodeClickEventListener) nodes -> {
                    //idk ??
                }
        };
    }

    @Override
    public ToolUI getUI() {
        return ui;
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}