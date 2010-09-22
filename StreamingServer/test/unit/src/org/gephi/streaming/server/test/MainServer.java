/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.streaming.server.test;

import java.io.IOException;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.server.AuthenticationFilter;
import org.gephi.streaming.server.StreamingServer;
import org.gephi.streaming.server.impl.BasicAuthenticationFilter;
import org.gephi.streaming.server.impl.ServerControllerImpl;
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
        
        AuthenticationFilter authenticationFilter = new BasicAuthenticationFilter();
        
        authenticationFilter.setUser(server.getServerSettings().getUser());
        authenticationFilter.setPassword(server.getServerSettings().getPassword());
        authenticationFilter.setAuthenticationEnabled(server.getServerSettings().isBasicAuthentication());

        ServerControllerImpl serverController = new ServerControllerImpl(graphModel.getHierarchicalMixedGraph());
        server.register(serverController, "/graphstream");

        server.start();
    }

   public static void main(String[] list) throws Exception {
       MainServer server = new MainServer();
       server.testMainServer();
   }
}