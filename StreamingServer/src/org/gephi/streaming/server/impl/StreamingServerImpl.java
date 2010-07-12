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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.gephi.streaming.server.AuthenticationFilter;
import org.gephi.streaming.server.Request;
import org.gephi.streaming.server.Response;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.StreamingServer;
import org.openide.util.lookup.ServiceProvider;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = StreamingServer.class)
public class StreamingServerImpl implements StreamingServer {
    
    private static final Logger logger =  Logger.getLogger(StreamingServerImpl.class.getName());
    
    private int port = 8080;
    private boolean useSSL = true;
    private int sslPort = 8443;
    private boolean started = false;
    
    private Map<String, ServerController> controllers = Collections.synchronizedMap(new HashMap<String, ServerController>());
    private Connection serverConnection;
    private ContextContainer contextContainer;
    private AuthenticationFilter authenticationFilter;

    private Connection sslServerConnection;

    public StreamingServerImpl() {
        contextContainer = new ContextContainer();
        authenticationFilter = new BasicAuthenticationFilter();
        authenticationFilter.setUser("gephi");
        authenticationFilter.setPassword("gephi");
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#register(org.gephi.streaming.server.ServerController, java.lang.String)
     */
    public void register(ServerController controller, String context) {
        logger.log(Level.INFO, "Registering controller at context {0}", context);
        controllers.put(context, controller);

        if (!this.started) {
            try {
                this.start();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#unregister(java.lang.String)
     */
    public void unregister(String context) {
        logger.log(Level.INFO, "Unregistering controller at context {0}", context);
        controllers.remove(context);

        if (controllers.isEmpty()) {
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
            serverConnection = new SocketConnection(contextContainer);
            SocketAddress address = new InetSocketAddress(port);
            serverConnection.connect(address);
            
            logger.log(Level.INFO, "HTTP Listening at port {0}", port);
            
            if (useSSL)
                startSSL();
            
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
        if (started) {
            serverConnection.close();
            if (useSSL)
                stopSSL();
            started = false;
            
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "StreamingServer stopped at {0}", new Date());
                
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#getPort()
     */
    public int getPort() {
        return port;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#setPort(int)
     */
    public void setPort(int port) {
        this.port = port;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#isUseSSL()
     */
    public boolean isUseSSL() {
        return useSSL;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#setUseSSL(boolean)
     */
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#getSSLPort()
     */
    public int getSSLPort() {
        return sslPort;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#setSSLPort(int)
     */
    public void setSSLPort(int sslPort) {
        this.sslPort = sslPort;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#isStarted()
     */
    public boolean isStarted() {
        return started;
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#getAuthenticationFilter()
     */
    public AuthenticationFilter getAuthenticationFilter() {
        return authenticationFilter;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#setAuthenticationFilter(org.gephi.streaming.server.AuthenticationFilter)
     */
    public void setAuthenticationFilter(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    private class ContextContainer implements Container {

        @Override
        public void handle(org.simpleframework.http.Request request, 
                org.simpleframework.http.Response response) {
            
            Request requestWrapper = new RequestWrapper(request);
            Response responseWrapper = new ResponseWrapper(response);
            
            if (!authenticationFilter.authenticate(requestWrapper, responseWrapper))
                return;
            
            String context = request.getPath().getPath();
            ServerController controller = controllers.get(context);
            if (controller==null) {
                logger.log(Level.WARNING, "Invalid context: {0}", context);
                response.setCode(404);
                
                try {
                    response.getPrintStream().println("HTTP 404: Context "+context+" not found.");
                    response.close();
                } catch (IOException e) {}
                
            } else {
                controller.handle(requestWrapper, responseWrapper);
            }
        }
        
    }
    
    private void startSSL() throws IOException {
        SocketAddress address = new InetSocketAddress(sslPort);
        sslServerConnection = new SocketConnection(contextContainer);
        
        try {
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

            sslServerConnection.connect(address, sslContext);
            
            logger.log(Level.INFO, "HTTPS Listening at port {0}", sslPort);
            
        } catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopSSL() throws IOException {
        sslServerConnection.close();
    }

}
