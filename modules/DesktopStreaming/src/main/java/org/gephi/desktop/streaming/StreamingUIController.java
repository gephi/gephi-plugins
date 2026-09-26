/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.streaming;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.lib.validation.DialogDescriptorWithValidation;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamingEndpoint;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.StreamingController;
import org.gephi.streaming.server.ClientManager.ClientManagerListener;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.ServerControllerFactory;
import org.gephi.streaming.server.StreamingServer;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The UI controller to control the UI state.
 *
 * @author panisson
 *
 */
@ServiceProvider(service = StreamingUIController.class)
public class StreamingUIController {

    private static final Logger logger = Logger.getLogger(StreamingUIController.class.getName());
    
    private StreamingModel model;
    private StreamingTopComponent component;
    
    public StreamingUIController() {

      //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                model = new StreamingModel();
                workspace.add(model);
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(StreamingModel.class);
                if (model == null) {
                    model = new StreamingModel();
                    workspace.add(model);
                }
                refreshModel();
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
                model = workspace.getLookup().lookup(StreamingModel.class);
                if (model != null) {
                    if (model.isMasterRunning()) {
                        stopMaster();
                    }
                    model.removeAllConnections();
                    refreshModel();
                }
            }

            public void disable() {
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(StreamingModel.class);
            if (model == null) {
                model = new StreamingModel();
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    public void setTopComponent(StreamingTopComponent component) {
        this.component = component;
        refreshModel();
    }

    public void refreshModel() {
        if (model != null) {
            component.refreshModel(model);
        }
    }
    
    public StreamingModel getStreamingModel() {
        return model;
    }

    /**
     * Shows the StreamingClientPanel and connect to a StreamingEndpoint using
     * the entered info.
     */
    public void connectToStream() {
        StreamingClientPanel clientPanel = new StreamingClientPanel();

        final DialogDescriptor dd = DialogDescriptorWithValidation.dialog(
            StreamingClientPanel.createValidationPanel(clientPanel), "Connect to Stream");

        Object result = DialogDisplayer.getDefault().notify(dd);
        if (!result.equals(NotifyDescriptor.OK_OPTION)) {
            return;
        }
        StreamingEndpoint endpoint = clientPanel.getGraphStreamingEndpoint();
        connectToStream(endpoint);
    }

    /**
     * Synchronize with the Master: disconnect from it, clean the graph and
     * reconnect.
     *
     * @param connection
     */
    public void synchronize(StreamingConnection connection) {

        // Get active graph instance - Project and Graph API
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        if (project==null) {
            logger.log(Level.WARNING, "Project not initialized during synchronize.");
            return;
        }
        Workspace workspace = projectController.getCurrentWorkspace();
        if (workspace==null){
            logger.log(Level.WARNING, "Workspace not initialized during synchronize.");
            return;
        }

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel(workspace);
        Graph graph = graphModel.getGraph();
        graph.clear();

        try {
            connection.close();

            // Connect to stream - Streaming API
            StreamingController controller = Lookup.getDefault().lookup(StreamingController.class);

            connection = controller.connect(connection.getStreamingEndpoint(), graph);
            connection.asynchProcess();

            model.addConnection(connection);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Show the StreamingSettingsPanel and update the settings with the
     * entered info.
     */
    public void setSettings() {
        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);

        StreamingSettingsPanel settingsPanel = new StreamingSettingsPanel(server.getServerSettings());
        settingsPanel.setup();
        
        final DialogDescriptor dd = DialogDescriptorWithValidation.dialog(
            StreamingSettingsPanel.createValidationPanel(settingsPanel), "Settings");
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (!result.equals(NotifyDescriptor.OK_OPTION)) {
            return;
        }
        settingsPanel.unsetup();

        if (server.isStarted()) {
            // TODO: show confirmation dialog before stop/start
            try {
                server.stop();
                server.start();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Start the master associated with the current workspace. Uses the
     * workspace name as the server context.
     */
    public void startMaster() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        if (project==null) {
            logger.log(Level.WARNING, "Project not initialized during startMaster.");
            return;
        }
        
        Workspace workspace = projectController.getCurrentWorkspace();
        if (workspace==null) {
            logger.log(Level.WARNING, "Workspace not initialized during startMaster.");
            return;
        }
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = graphController.getGraphModel(workspace).getGraph();

        WorkspaceInformation wi = workspace.getLookup().lookup(WorkspaceInformation.class);
        String context = "/"+wi.getName().replaceAll(" ", "").toLowerCase();
        
        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);
        ServerControllerFactory controllerFactory = Lookup.getDefault().lookup(ServerControllerFactory.class);
        ServerController serverController = controllerFactory.createServerController(graph);
        
        serverController.getClientManager().addClientManagerListener(
                new ClientManagerListener() {

            public void clientConnected(String client) {
                 model.addConnected(client);
            }

            public void clientDisconnected(String client) {
                model.removeConnected(client);
            }
        });

        server.register(serverController, context);

        model.setServerContext(context);
        model.getMasterNode().getStreamingServerNode().start();
        model.setMasterRunning(true);
    }

    /**
     * Stop the master associated with the current workspace and
     * unregister its context from the server.
     */
    public void stopMaster() {
        model.setMasterRunning(false);
        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);
        server.unregister(model.getServerContext());
        model.getMasterNode().getStreamingServerNode().stop();
    }

    private void notifyError(String userMessage, Throwable t) {
        if (t instanceof OutOfMemoryError) {
            return;
        }
        String message = message = t.toString();
        NotifyDescriptor.Message msg =
                new NotifyDescriptor.Message(
                userMessage+"\n"+message,
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(msg);
        //Logger.getLogger("").log(Level.WARNING, "", t.getCause());
    }

    /**
     * Show the ReportPanel for the connection's Report. The ReportPanel
     * is updated with the Report information, and when some activity occurs in
     * the connection, the panel is updated.
     *
     * @param connection the StreamingConnection to get the Report.
     */
    public void showReport(final StreamingConnection connection) {
        final Report report = connection.getReport();
        final ReportPanel reportPanel = new ReportPanel();
        reportPanel.setData(report);

        final StreamingConnection.StatusListener listener = new StreamingConnection.StatusListener() {

            public void onConnectionClosed(StreamingConnection connection) {
                reportPanel.refreshData(report);
            }

            public void onDataReceived(StreamingConnection connection) {
                reportPanel.refreshData(report);
            }

            public void onError(StreamingConnection connection) {
                reportPanel.refreshData(report);
            }
        };

        connection.addStatusListener(listener);

        DialogDescriptor dd =
                new DialogDescriptor(reportPanel,
                NbBundle.getMessage(StreamingUIController.class, "ReportPanel.title"),
                false, DialogDescriptor.OK_CANCEL_OPTION, null, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                connection.removeStatusListener(listener);
            }
        });

        DialogDisplayer.getDefault().notify(dd);
        
    }

    public void connectToStream(StreamingEndpoint endpoint) {

        // Get active graph instance - Project and Graph API
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        if (project==null)
            projectController.newProject();
        Workspace workspace = projectController.getCurrentWorkspace();
        if (workspace==null)
            workspace = projectController.newWorkspace(projectController.getCurrentProject());
//        projectController.openWorkspace(workspace);

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        Graph graph =  graphModel.getGraph();

        // Connect to stream - Streaming API
        StreamingController controller = Lookup.getDefault().lookup(StreamingController.class);
        try {
            StreamingConnection connection = controller.connect(endpoint, graph);
            connection.asynchProcess();

            model.addConnection(connection);

        } catch (IOException ex) {
            notifyError("Unable to connect to stream " + endpoint.getUrl().toString(), ex);
            logger.log(Level.WARNING, "Unable to connect to stream", ex);
            return;
        }
    }
}
