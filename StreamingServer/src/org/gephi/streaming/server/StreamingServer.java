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

import org.gephi.streaming.server.impl.ServerControllerImpl;


/**
 * The streaming server instance.
 * 
 * @author panisson
 *
 */
public interface StreamingServer {

    /**
     * This is used to register a ServerController under a given server
     * context. 
     * <p>For example, registering a ServerController under the
     * context <code>/streaming</code> will result that the ServerController
     * will be accessible under the address 
     * <code>http://localhost:8080/streaming</code>
     * 
     * @param controller - the ServerController to be accessible under the 
     * context
     * @param context - the context that the controller will be accessible
     */
    public void register(ServerController controller, String context);

    /**
     * This is used to unregister a ServerController already registered
     * under the given context.
     * 
     * @param context - the context to be unregistered
     */
    public void unregister(String context);

    /**
     * Start the server with the configured parameters.
     * 
     * @throws IOException
     */
    public void start() throws IOException;

    /**
     * Stop the server.
     * 
     * @throws IOException
     */
    public void stop() throws IOException;
    
    /**
     * This is used to verify if the server is currently started.
     * 
     * @return true if the server is started
     */
    public boolean isStarted();
    
    public ServerSettings getServerSettings();

}