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
package org.gephi.streaming.server;

import java.io.IOException;
import java.io.OutputStream;

import org.gephi.graph.api.Graph;

/**
 * @author panisson
 *
 */
public class ServerController {
    
    private final ServerOperationExecutor executor;
    
    public ServerController(Graph graph) {
        executor = new ServerOperationExecutor(graph);
    }

    public void handle(Request request, Response response) {
        
        long time = System.currentTimeMillis();

        response.add("Content-Type", "text/plain");
        response.add("Server", "Gephi/0.7 alpha4");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        
        try {
            String operation = request.getParameter("operation");
            if(operation==null) {
                executeError(response, "Invalid operation");
                return;
            }
            String format = request.getParameter("format");
            if(format==null) {
                executeError(response, "Invalid format");
                return;
            }
            OutputStream outputStream = response.getOutputStream();
            System.out.println("Handling request for operation "+operation+", format "+format);
            
            if (operation.equals("getGraph")) {
                executor.executeGetGraph(format, outputStream);
                
            } else if (operation.equals("getNode")) {
                // gets the node id and write info to output stream
                String id = request.getParameter("id");
                if(id==null) {
                    executeError(response, "Invalid id");
                    return;
                }
                executor.executeGetNode(id, format, outputStream);
                
            } else if (operation.equals("getEdge")) {
                // gets the edge id and write info to output stream
                String id = request.getParameter("id");
                if(id==null) {
                    executeError(response, "Invalid id");
                    return;
                }
                executor.executeGetEdge(id, format, outputStream);
                
            } else if (operation.equals("updateGraph")) {
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
    
    private void executeError(Response response, String message) throws IOException {
        response.setCode(501);
        response.getPrintStream().println(message);
        response.getOutputStream().close();
        
    }
    
}
