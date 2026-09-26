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
package org.gephi.streaming.server.impl.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.WebSocketFactory;
import org.gephi.streaming.server.AuthenticationFilter;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.StreamingServer;
import org.gephi.streaming.server.StreamingServerConfig;
import org.gephi.streaming.server.impl.BasicAuthenticationFilter;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = StreamingServer.class)
public class StreamingServerImpl implements StreamingServer {
    
    private static final Logger logger =  Logger.getLogger(StreamingServerImpl.class.getName());
    
    private StreamingServerConfig settings;
    
    private Map<String, ContextContainer> containers = 
            Collections.synchronizedMap(new HashMap<String, ContextContainer>());
    private Server server;
    private ServletContextHandler context;

    private boolean started = false;

    public StreamingServerImpl() {
        settings = new StreamingServerConfig();
    }
    
    public StreamingServerConfig getServerSettings() {
        return settings;
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#register(org.gephi.streaming.server.ServerController, java.lang.String)
     */
    public void register(ServerController controller, String context) {
        logger.log(Level.INFO, "Registering controller at context {0}", context);

        if (!this.started) {
            try {
                this.start();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        
        ContextContainer contextContainer = new ContextContainer(controller);
        containers.put(context, contextContainer);
        this.context.addServlet(new ServletHolder(contextContainer), context+"/*");
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#unregister(java.lang.String)
     */
    public void unregister(String context) {
        logger.log(Level.INFO, "Unregistering controller at context {0}", context);
        ContextContainer contextContainer = containers.remove(context);
        try {
            removeServlet(this.context, contextContainer);
        } catch (ServletException ex) {
            Exceptions.printStackTrace(ex);
        }
        contextContainer.getServerController().stop();

        if (containers.isEmpty()) {
            try {
                this.stop();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#start()
     */
    public synchronized void start() throws IOException {
        
        if (!started) {
            logger.info("Starting StreamingServer...");
            
            server = new Server();
 
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(settings.getPort());
            server.addConnector(connector);
            
            if (settings.isUseSSL()) {
                
                try {
                    SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
                    ssl_connector.setPort(settings.getSslPort());

                    KeyManagerFactory kmf = null;
                    KeyStore keyStore = KeyStore.getInstance("PKCS12");
                    InputStream keyStoreInputStream = null;
                    try {
                        keyStoreInputStream = this.getClass().getResourceAsStream("localhost.p12");
                        keyStore.load(keyStoreInputStream, "12345678".toCharArray());
                    } finally {
                        if (keyStoreInputStream != null) {
                            keyStoreInputStream.close();
                        }
                    }

                    kmf = KeyManagerFactory.getInstance("SunX509");
                    kmf.init(keyStore, "12345678".toCharArray());

                    TrustManagerFactory tmf = null;
                    // Uncomment this to use a non-default trust manager.
                    // tmf = TrustManagerFactory.getInstance("PKIX");
                    // tmf.init(keyStore);

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(kmf != null ? kmf.getKeyManagers() : null,
                        tmf != null ? tmf.getTrustManagers() : null, null);

                    SslContextFactory cf = ssl_connector.getSslContextFactory();
                    cf.setSslContext(sslContext);
    //                cf.setKeyStore(jetty_home + "/etc/keystore");
    //                cf.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
    //                cf.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

                    server.addConnector(ssl_connector);
                    
                } catch (UnrecoverableKeyException e) {
                    logger.log(Level.WARNING, null, e);
                } catch (KeyManagementException e) {
                    logger.log(Level.WARNING, null, e);
                } catch (NoSuchAlgorithmException e) {
                    logger.log(Level.WARNING, null, e);
                } catch (CertificateException e) {
                    logger.log(Level.WARNING, null, e);
                } catch (KeyStoreException e) {
                    logger.log(Level.WARNING, null, e);
                }
            }
            
	    context = new ServletContextHandler();
	    context.setContextPath("/");
	    server.setHandler(context);
            
            try {
                server.start();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            
            started = true;
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "StreamingServer started at {0}", new Date());
                
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#stop()
     */
    public synchronized void stop() throws IOException {

        if (!containers.isEmpty()) {
            for (ContextContainer container: containers.values()) {
                container.getServerController().stop();
            }
        }
        
        if (started) {
            try {
                server.stop();
                started = false;

                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "StreamingServer stopped at {0}", new Date());

                }
                
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#isStarted()
     */
    public boolean isStarted() {
        return started;
    }

    private class ContextContainer extends HttpServlet {
        
        private final ServerController serverController;
        private final AuthenticationFilter authenticationFilter;
        private WebSocketFactory wsFactory;
        
        public ContextContainer(ServerController serverController) {
            this.serverController = serverController;
            
            settings = new StreamingServerConfig();
            authenticationFilter = new BasicAuthenticationFilter();
            authenticationFilter.setUser(settings.getUser());
            authenticationFilter.setPassword(settings.getPassword());
            authenticationFilter.setAuthenticationEnabled(settings.isBasicAuthentication());
            
             wsFactory = new WebSocketFactory(
                     new GephiWebSocketAcceptor(serverController.getServerOperationExecutor(), 
                             serverController.getClientManager()));
        }
        
        public ServerController getServerController() {
            return serverController;
        }

        @Override
        public void service(HttpServletRequest request, HttpServletResponse response) 
                throws ServletException, IOException {
            
            if (wsFactory.acceptWebSocket(request, response)) return;
            
            if (!authenticationFilter.authenticate(request, response))
                return;

            serverController.handle(request, response);
        }
        
    }

    private void removeServlet(ServletContextHandler context, Servlet servlet)
            throws ServletException {
        ServletHandler handler = context.getServletHandler();

        /* A list of all the servlets that don't implement the class 'servlet',
        (i.e. They should be kept in the context */
        List<ServletHolder> servlets = new ArrayList<ServletHolder>();

        /* The names all the servlets that we remove so we can drop the mappings too */
        Set<String> names = new HashSet<String>();

        for (ServletHolder holder : handler.getServlets()) {
            /* If it is the class we want to remove, then just keep track of its name */
            if (servlet.equals(holder.getServlet())) {
                names.add(holder.getName());
            } else /* We keep it */ {
                servlets.add(holder);
            }
        }

        List<ServletMapping> mappings = new ArrayList<ServletMapping>();

        for (ServletMapping mapping : handler.getServletMappings()) {
            /* Only keep the mappings that didn't point to one of the servlets we removed */
            if (!names.contains(mapping.getServletName())) {
                mappings.add(mapping);
            }
        }

        /* Set the new configuration for the mappings and the servlets */
        handler.setServletMappings(mappings.toArray(new ServletMapping[0]));
        handler.setServlets(servlets.toArray(new ServletHolder[0]));

    }

}
