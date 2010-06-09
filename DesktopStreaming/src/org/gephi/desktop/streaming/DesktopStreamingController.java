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
import org.gephi.streaming.api.DefaultGraphStreamingEventProcessor;
import org.gephi.streaming.api.GraphStreamingEndpoint;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.StreamingServer;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class DesktopStreamingController {
    
    public void connectToStream(GraphStreamingEndpoint endpoint) {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        if (project==null)
            projectController.newProject();
//        Workspace workspace = projectController.getCurrentWorkspace();
//        if (workspace==null)
          Workspace   workspace = projectController.newWorkspace(projectController.getCurrentProject());
//        projectController.openWorkspace(workspace);

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();

        DefaultGraphStreamingEventProcessor eventProcessor = new DefaultGraphStreamingEventProcessor(graphModel.getHierarchicalMixedGraph());
        eventProcessor.process(endpoint);
     }
    
    public void exposeWorkspaceAsStream() {
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
        
        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);
        ServerController serverController = new ServerController(graph);
        
        server.register(serverController, /*TODO: workspace.toString()*/ "/graphstream");

        try {
            server.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
