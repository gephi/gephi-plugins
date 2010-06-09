package org.gephi.streaming.server.test;

import java.io.IOException;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.StreamingServer;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * From linux, you can connect to the server using:<br>
 * wget -qO- http://localhost:8080/graphstream
 * 
 * @author panisson
 *
 */
public class MainServer {
	
    @Test
    public void testMainServer() throws IOException {

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel();

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();

        StreamingServer server = Lookup.getDefault().lookup(StreamingServer.class);

        ServerController serverController = new ServerController(graphModel.getHierarchicalMixedGraph());
        server.register(serverController, "/graphstream");

        server.start();
    }

   public static void main(String[] list) throws Exception {
       MainServer server = new MainServer();
       server.testMainServer();
   }
}