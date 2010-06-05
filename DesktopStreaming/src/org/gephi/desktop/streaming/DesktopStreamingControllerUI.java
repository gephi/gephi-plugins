/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.streaming;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.DefaultGraphStreamingEventProcessor;
import org.gephi.streaming.api.GraphStreamingEndpoint;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author panisson
 */
public class DesktopStreamingControllerUI {

    private StreamingEndpointPanel panel = null;

    public DesktopStreamingControllerUI() {
        
    }

    public void connectToStream() {

        String title = NbBundle.getMessage(DesktopStreamingControllerUI.class, "StreamingEndpointPanel.ui.dialog.title");
        JPanel vpanel = getPanel();
        final DialogDescriptor dd = new DialogDescriptor(vpanel, title);
        if (vpanel instanceof ValidationPanel) {
            ValidationPanel vp = (ValidationPanel) vpanel;
            vp.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                }
            });
        }

        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(NotifyDescriptor.CANCEL_OPTION) || result.equals(NotifyDescriptor.CLOSED_OPTION)) {
            return;
        }

        GraphStreamingEndpoint endpoint = panel.getGraphStreamingEndpoint();
        connectToStream(endpoint);
    }

    private JPanel getPanel() {
        if (panel == null) {
            panel = new StreamingEndpointPanel();
        }
        return StreamingEndpointPanel.createValidationPanel(panel);
    }

     public void connectToStream(GraphStreamingEndpoint endpoint) {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
//        projectController.openWorkspace(workspace);

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();

        DefaultGraphStreamingEventProcessor eventProcessor = new DefaultGraphStreamingEventProcessor(graphModel.getHierarchicalMixedGraph());
        eventProcessor.process(endpoint);
     }

}
