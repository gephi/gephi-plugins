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
