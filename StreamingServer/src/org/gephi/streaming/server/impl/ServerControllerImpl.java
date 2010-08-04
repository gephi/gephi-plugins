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
package org.gephi.streaming.server.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.gephi.graph.api.Graph;
import org.gephi.streaming.server.Request;
import org.gephi.streaming.server.Response;
import org.gephi.streaming.server.ServerController;

/**
 * @author panisson
 *
 */
public class ServerControllerImpl implements ServerController {

    private ClientManagerImpl clientManager;
    
    private enum Operations {
        GET_GRAPH("getGraph"),
        GET_NODE("getNode"), 
        GET_EDGE("getEdge"),
        UPDATE_GRAPH("updateGraph")
        ;
        
        private final String url;
        private Operations(String url) {
            this.url = url;
        }
        
        public String getURL() {
            return url;
        }
    }
    
    private final ServerOperationExecutor executor;
    
    public ServerControllerImpl(Graph graph) {
        clientManager = new ClientManagerImpl();
        executor = new ServerOperationExecutor(graph, clientManager);
        
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.ServerController#getClientManager()
     */
    public ClientManagerImpl getClientManager() {
        return clientManager;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.ServerController#handle(org.gephi.streaming.server.Request, org.gephi.streaming.server.Response)
     */
    public void handle(Request request, Response response) {
        
        long time = System.currentTimeMillis();

        response.add("Content-Type", "text/plain");
        response.add("Server", "Gephi/0.7 alpha4");
        response.add("Connection", "close");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        
        try {


            String operation = request.getParameter("operation");
            if(operation==null) {
                // Default operation is GET_GRAPH
                operation = Operations.GET_GRAPH.getURL();
            }
            String format = request.getParameter("format");
            if(format==null) {
                // Default format is JSON
                format = "JSON";
            }
            OutputStream outputStream = response.getOutputStream();
            System.out.println("Handling request for operation "+operation+", format "+format);
            
            if (operation.equals(Operations.GET_GRAPH.getURL())) {
                executor.executeGetGraph(request, response);
                
            } else if (operation.equals(Operations.GET_NODE.getURL())) {
                // gets the node id and write info to output stream
                String id = request.getParameter("id");
                if(id==null) {
                    executeError(response, "Invalid id");
                    return;
                }
                executor.executeGetNode(id, format, outputStream);
                
            } else if (operation.equals(Operations.GET_EDGE.getURL())) {
                // gets the edge id and write info to output stream
                String id = request.getParameter("id");
                if(id==null) {
                    executeError(response, "Invalid id");
                    return;
                }
                executor.executeGetEdge(id, format, outputStream);
                
            } else if (operation.equals(Operations.UPDATE_GRAPH.getURL())) {
                executor.executeUpdateGraph(format, request.getInputStream(), outputStream);
            } else {
                executeError(response, "Invalid operation: "+operation);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                executeError(response, "Error: "+e.getMessage());
            } catch (IOException e1) { }
            return;
        }
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.ServerController#stop()
     */
    public void stop() {
        clientManager.stopAll();
    }
    
    private void executeError(Response response, String message) throws IOException {
        response.setCode(500);
        response.setText("Internal Server Error");
        response.getPrintStream().println(message);
        response.getOutputStream().close();
        
    }
    
}
