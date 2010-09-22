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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.impl.RequestWrapper;
import org.gephi.streaming.server.impl.ResponseWrapper;
import org.gephi.streaming.server.impl.ServerControllerImpl;
import org.openide.util.Lookup;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * From linux, you can connect to the server using:<br>
 * wget -qO- http://localhost:8080/graphstream
 * 
 * @author panisson
 *
 */
public class MainServer2 implements Container {
	
	private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";
	
	private ServerController serverController;
	
	public MainServer2() {
	    
	    ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel();
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
	    
        serverController = new ServerControllerImpl(graphModel.getHierarchicalMixedGraph());
	    
	    final InputStream fileInputStream = this.getClass().getResourceAsStream(DGS_RESOURCE);
        
        final InputStream inputStream = new InputStream() {
            private int count = 0;

            @Override
            public int read() throws IOException {
                count++;
                if (count%10 == 0)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) { }
                return fileInputStream.read();
            }
        };

//        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
//        final StreamReader streamReader = factory.createStreamReader("DGS", serverController.getGraphBufferedOperationSupport());
//
//        new Thread() {
//            @Override
//            public void run() {
//
//                try {
//                    streamReader.processStream(inputStream);
//
//                } catch (IOException e) {
//                    // Exception during processing
//                    e.printStackTrace();
//                }
//            }
//        }.start();
	}

   public void handle(Request request, Response response) {
	   long time = System.currentTimeMillis();

	   response.set("Content-Type", "text/plain");
	   response.set("Server", "Gephi/0.7 alpha4");
	   response.setDate("Date", time);
	   response.setDate("Last-Modified", time);
	   
	   try {
		   serverController.handle(new RequestWrapper(request), new ResponseWrapper(response, null));
		   
	   } catch (Exception e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
   }

   public static void main(String[] list) throws Exception {
      Container container = new MainServer2();
      Connection connection = new SocketConnection(container);
      SocketAddress address = new InetSocketAddress(8080);

      connection.connect(address);
      
      
   }
}