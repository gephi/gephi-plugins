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

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.streaming.api.GraphStreamingEndpoint;
import org.gephi.streaming.api.StreamingClient;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.StreamingServer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = StreamingController.class)
public class StreamingController {
    
    private StreamingModel model;
    private StreamingTopComponent component;
    
    public StreamingController() {

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
        final DialogDescriptor dd = new DialogDescriptor(clientPanel, "Connect to Stream");
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (!result.equals(NotifyDescriptor.OK_OPTION)) {
            return;
        }
        GraphStreamingEndpoint endpoint = clientPanel.getGraphStreamingEndpoint();
        connectToStream(endpoint);
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
        
//        GraphController graphController = workspace.getLookup().lookup(GraphController.class);
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = graphController.getModel().getMixedGraph();

        WorkspaceInformation wi = workspace.getLookup().lookup(WorkspaceInformation.class);
        String context = "/"+wi.getName().replaceAll(" ", "").toLowerCase();
        
        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);
        ServerController serverController = new ServerController(graph);

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

    private void connectToStream(GraphStreamingEndpoint endpoint) {

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
        final StreamingClient client = new StreamingClient(graph);
        try {
            StreamingConnection connection = client.process(endpoint,
                new StreamingConnection.StatusListener() {
                    public void onConnectionClosed(StreamingConnection connection) {
                        disconnect(connection);

                        // TODO: show stream report
                        System.out.println("-- Stream report -----\n" +
                                client.getReport().getText() + "--------");
                    }

                public void onReceivingData(StreamingConnection connection) { }
                });

            model.addConnection(connection, client.getReport());

        } catch (IOException ex) {
            notifyError("Unable to connect to stream " + endpoint.getUrl().toString(), ex);
            return;
        }
    }
}
