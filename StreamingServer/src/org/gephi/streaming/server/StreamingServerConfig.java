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

package org.gephi.streaming.server;

import org.openide.util.NbPreferences;

/**
 * An object that represent the server configuration and preferences.
 * @author panisson
 */
public class StreamingServerConfig {

    //Const Default Config
    public static final String PORT = "StreamingServerConfig.port";
    public static final String USE_SSL = "StreamingServerConfig.useSSL";
    public static final String SSL_PORT = "StreamingServerConfig.sslPort";
    public static final String USER = "StreamingServerConfig.user";
    public static final String PASSWORD = "StreamingServerConfig.password";
    public static final String BASIC_AUTHENTICATION = "StreamingServerConfig.basicAuthentication";

    //Default values
    public static final int DEFAULT_PORT = 8080;
    public static final boolean DEFAULT_USE_SSL = true;
    public static final int DEFAULT_SSL_PORT = 8443;
    public static final String DEFAULT_USER = "gephi";
    public static final String DEFAULT_PASSWORD = "gephi";
    public static final boolean DEFAULT_BASIC_AUTHENTICATION = false;

    //Preferences
    protected int port = NbPreferences.forModule(StreamingServerConfig.class).getInt(PORT, DEFAULT_PORT);
    protected boolean useSSL = NbPreferences.forModule(StreamingServerConfig.class).getBoolean(USE_SSL, DEFAULT_USE_SSL);
    protected int sslPort = NbPreferences.forModule(StreamingServerConfig.class).getInt(SSL_PORT, DEFAULT_SSL_PORT);
    protected String user = NbPreferences.forModule(StreamingServerConfig.class).get(USER, DEFAULT_USER);
    protected String password = NbPreferences.forModule(StreamingServerConfig.class).get(PASSWORD, DEFAULT_PASSWORD);
    protected boolean basicAuthentication = NbPreferences.forModule(StreamingServerConfig.class).getBoolean(BASIC_AUTHENTICATION, DEFAULT_BASIC_AUTHENTICATION);

    /**
     * Used to get the HTTP port that the server is configured to listen.
     *
     * @return the configured port
     */
    public int getPort() {
        return port;
    }

    /**
     * Used to configure a new HTTP port that the server will listen
     *
     * @param port the port to set
     */
    public void setPort(int port) {
        NbPreferences.forModule(StreamingServerConfig.class).putInt(PORT, port);
        this.port = port;
    }

    /**
     * @return the useSSL
     */
    public boolean isUseSSL() {
        return useSSL;
    }

    /**
     * @param useSSL the useSSL to set
     */
    public void setUseSSL(boolean useSSL) {
        NbPreferences.forModule(StreamingServerConfig.class).putBoolean(USE_SSL, useSSL);
        this.useSSL = useSSL;
    }

    /**
     * @return the sslPort
     */
    public int getSslPort() {
        return sslPort;
    }

    /**
     * @param sslPort the sslPort to set
     */
    public void setSslPort(int sslPort) {
        NbPreferences.forModule(StreamingServerConfig.class).putInt(SSL_PORT, sslPort);
        this.sslPort = sslPort;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        NbPreferences.forModule(StreamingServerConfig.class).put(USER, user);
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        NbPreferences.forModule(StreamingServerConfig.class).put(PASSWORD, password);
        this.password = password;
    }

    /**
     * @return the basicAuthentication
     */
    public boolean isBasicAuthentication() {
        return basicAuthentication;
    }

    /**
     * @param basicAuthentication the basicAuthentication to set
     */
    public void setBasicAuthentication(boolean basicAuthentication) {
        NbPreferences.forModule(StreamingServerConfig.class).putBoolean(BASIC_AUTHENTICATION, basicAuthentication);
        this.basicAuthentication = basicAuthentication;
    }
}
