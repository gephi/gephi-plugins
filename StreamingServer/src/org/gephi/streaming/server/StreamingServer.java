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
     * This is used to get the currently configured HTTP port.
     * 
     * @return the configured HTTP port
     */
    public int getPort();

    /**
     * This is used to set the HTTP port to be used by the server.
     * Note that setting the HTTP port does not immediately change the
     * port the server is listening, for this you should 
     * call stop() and start()
     * 
     * @param port the port to configure.
     */
    public void setPort(int port);

    /**
     * This is used to verify if the server is configured to use
     * SSL connections.
     * 
     * @return true if the server uses SSL
     */
    public boolean isUseSSL();

    /**
     * This is used to set the server configuration to use 
     * SSL connections
     * 
     * @param useSSL - set it to true if the server should use SSL
     */
    public void setUseSSL(boolean useSSL);

    /**
     * This is used to get the currently configured HTTPS port.
     * 
     * @return the configured HTTPS port
     */
    public int getSSLPort();

    /**
     * This is used to set the HTTPS port to be used by the server.
     * Note that setting the HTTPS port does not immediately change the
     * port the server is listening, for this you should 
     * call stop() and start()
     * 
     * @param sslPort the port to configure HTTPS.
     */
    public void setSSLPort(int sslPort);

    /**
     * This is used to verify if the server is currently started.
     * 
     * @return true if the server is started
     */
    public boolean isStarted();

    /**
     * This is used to get the currently configured authentication filter.
     * With the returning object it is possible to set the user
     * and password for authentication.
     * 
     * @return the filter that makes the authentication
     */
    public AuthenticationFilter getAuthenticationFilter();

    /**
     * This is used to set the authentication filter to be used.
     * 
     * @param authenticationFilter the authentication filter to be used.
     */
    public void setAuthenticationFilter(
            AuthenticationFilter authenticationFilter);

}