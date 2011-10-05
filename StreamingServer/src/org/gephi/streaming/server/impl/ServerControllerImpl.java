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
