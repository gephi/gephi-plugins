/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.streaming;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.streaming.api.StreamingEndpoint;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.StreamingController;
import org.gephi.streaming.server.ClientManager.ClientManagerListener;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.ServerControllerFactory;
import org.gephi.streaming.server.StreamingServer;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
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
    }

    public void refreshModel() {
        component.refreshModel(model);
    }
    
    public StreamingModel getStreamingModel() {
        return model;
    }

    public void connectToStream() {
        StreamingClientPanel clientPanel = new StreamingClientPanel();

        ValidationPanel vp = StreamingClientPanel.createValidationPanel(clientPanel);
        final DialogDescriptor dd = new DialogDescriptor(vp, "Connect to Stream");
        vp.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
            }
        });

        Object result = DialogDisplayer.getDefault().notify(dd);
        if (!result.equals(NotifyDescriptor.OK_OPTION)) {
            return;
        }
        StreamingEndpoint endpoint = clientPanel.getGraphStreamingEndpoint();
        connectToStream(endpoint);
    }

    public void setSettings() {
        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);

        StreamingSettingsPanel settingsPanel = new StreamingSettingsPanel(server.getServerSettings());
        settingsPanel.setup();
        ValidationPanel vp = StreamingSettingsPanel.createValidationPanel(settingsPanel);
        
        final DialogDescriptor dd = new DialogDescriptor(vp, "Settings");
        vp.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
            }
        });
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
    
    public void disconnect(StreamingConnection connection) {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (IOException e) {
            notifyError("Error disconnecting from connection "+connection.getUrl().toString(), e);
        }
    }
    
    public void startMaster() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        if (project==null) {
            //TODO: Invalid project
            return;
        }
        
        Workspace workspace = projectController.getCurrentWorkspace();
        if (workspace==null) {
            //TODO: Invalid workspace
            return;
        }
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = graphController.getModel().getMixedGraph();

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
        model.setServerRunning(true);
    }
    
    public void stopMaster() {
        model.setServerRunning(false);
        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);
        server.unregister(model.getServerContext());
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

    private void connectToStream(StreamingEndpoint endpoint) {

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
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getHierarchicalMixedGraph();

        // Connect to stream - Streaming API
        StreamingController controller = Lookup.getDefault().lookup(StreamingController.class);
//        final StreamingClient client = new StreamingClient(endpoint, graph);
        final Report report = new Report();
        try {
            //TODO: verify if this listener is really necessary
            StreamingConnection connection = controller.process(endpoint, graph, report, null);
//                new StreamingConnection.StatusListener() {
//                    public void onConnectionClosed(StreamingConnection connection) {
//                        disconnect(connection);
//
//                        // TODO: show stream report
//                        System.out.println("-- Stream report -----\n" +
//                                report.getText() + "--------");
//                    }
//
//                public void onDataReceived(StreamingConnection connection) { }
//                public void onError(StreamingConnection connection) { }
//                });

            model.addConnection(connection, report);

        } catch (IOException ex) {
            notifyError("Unable to connect to stream " + endpoint.getUrl().toString(), ex);
            logger.log(Level.WARNING, "Unable to connect to stream", ex);
            return;
        }
    }
}
