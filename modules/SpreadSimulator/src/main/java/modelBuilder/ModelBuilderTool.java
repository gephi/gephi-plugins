package modelBuilder;

import configLoader.ConfigLoader;
import modelBuilder.stateBuilder.StateBuilder;
import modelBuilder.stateBuilder.StateBuilderPanel;
import modelBuilder.transitionBuilder.TransitionBuilder;
import modelBuilder.transitionBuilder.TransitionBuilderPanel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.tools.spi.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = Tool.class)
public class ModelBuilderTool implements Tool {
    private ToolEventListener[] listeners;
    public JLabel statusLabel;
    private ModelBuilderToolUI ui;
    private Node sourceNode;

    public ModelBuilderTool() {
        statusLabel = new JLabel();
        statusLabel.setText(ConfigLoader.modelBuilderToolInfoStatusCreate);
        ui = new ModelBuilderToolUI(this);
        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {
            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
            }
        });
    }

    public void select() {
    }

    public void unselect() {
        this.listeners = null;
        this.sourceNode = null;
        statusLabel.setText(ConfigLoader.modelBuilderToolInfoStatusCreate);
        ui = new ModelBuilderToolUI(this);
    }

    public ToolEventListener[] getListeners() {
        this.listeners = new ToolEventListener[2];
        this.listeners[0] = (NodeClickEventListener) nodes -> {
            Node n = nodes[0];
            if (ModelBuilderTool.this.sourceNode == null) {
                ModelBuilderTool.this.sourceNode = n;
                statusLabel.setText(ConfigLoader.modelBuilderToolInfoStatusLink + sourceNode.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString());
                ui = new ModelBuilderToolUI(this);
            } else if (sourceNode == n){
                ModelBuilderTool.this.sourceNode = null;
                statusLabel.setText(ConfigLoader.modelBuilderToolInfoStatusCreate);
                ui = new ModelBuilderToolUI(this);
            } else {
                var transitionBuilder = new TransitionBuilder();
                var sourceName = sourceNode.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString();
                var destinationName = n.getAttribute(ConfigLoader.colNameModelBuilderNodeState).toString();
                JOptionPane.showMessageDialog(null, new TransitionBuilderPanel(transitionBuilder, sourceName, destinationName));
                transitionBuilder.execute();
                ModelBuilderTool.this.sourceNode = null;
                statusLabel.setText(ConfigLoader.modelBuilderToolInfoStatusCreate);
                ui = new ModelBuilderToolUI(this);
            }

        };
        this.listeners[1] = (MouseClickEventListener) (positionViewport, position3d) -> {
            if (ModelBuilderTool.this.sourceNode != null) {
                ModelBuilderTool.this.sourceNode = null;
                statusLabel.setText(ConfigLoader.modelBuilderToolInfoStatusCreate);
                ui = new ModelBuilderToolUI(this);
            } else{
                var modelBuilder = new StateBuilder();
                JOptionPane.showMessageDialog(null, new StateBuilderPanel(modelBuilder));
                modelBuilder.execute();
            }

        };
        return this.listeners;
    }

    @Override
    public ToolUI getUI() {
        return ui;
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}