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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
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
import org.gephi.streaming.server.StreamingServerConfig;
import org.openide.util.lookup.ServiceProvider;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerProcessor;
import org.simpleframework.transport.Processor;
import org.simpleframework.transport.ProcessorServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.Socket;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.util.buffer.FileAllocator;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = StreamingServer.class)
public class StreamingServerImpl implements StreamingServer {
    
    private static final Logger logger =  Logger.getLogger(StreamingServerImpl.class.getName());
    
    private StreamingServerConfig settings;
    private AuthenticationFilter authenticationFilter;
    
    private Map<String, ServerController> controllers = Collections.synchronizedMap(new HashMap<String, ServerController>());
    private Connection serverConnection;
    private ContextContainer contextContainer;

    private Connection sslServerConnection;
    private boolean started = false;
    private boolean sslStarted = false;

    public StreamingServerImpl() {
        contextContainer = new ContextContainer();

        settings = new StreamingServerConfig();

        authenticationFilter = new BasicAuthenticationFilter();
        authenticationFilter.setUser(settings.getUser());
        authenticationFilter.setPassword(settings.getPassword());
        authenticationFilter.setAuthenticationEnabled(settings.isBasicAuthentication());
    }
    
    public StreamingServerConfig getServerSettings() {
        return settings;
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
        ServerController controller = controllers.remove(context);
        controller.stop();

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

            Processor processor = new ContainerProcessor(contextContainer, new FileAllocator(), 1);
            Server server = new SocketSnoopServer(new ProcessorServer(processor));
            serverConnection = new SocketConnection(server);
            SocketAddress address = new InetSocketAddress(settings.getPort());
            serverConnection.connect(address);

//            serverConnection = new SocketConnection(contextContainer);
//            SocketAddress address = new InetSocketAddress(settings.getPort());
//            serverConnection.connect(address);
            
            logger.log(Level.INFO, "HTTP Listening at port {0}", settings.getPort());
            
            if (settings.isUseSSL())
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

        if (!controllers.isEmpty()) {
            for (ServerController controller: controllers.values()) {
                controller.stop();
            }
        }

        if (started) {
            serverConnection.close();
            started = false;
            
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "StreamingServer stopped at {0}", new Date());
                
            }
        }
        if (sslStarted) {
            sslServerConnection.close();
            sslStarted = false;
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.StreamingServer#isStarted()
     */
    public boolean isStarted() {
        return started;
    }

    private class ContextContainer implements Container {

        @Override
        public void handle(org.simpleframework.http.Request request, 
                org.simpleframework.http.Response response) {

            Request requestWrapper = new RequestWrapper(request);
            SocketChannel channel = (SocketChannel)request.getAttribute(RequestWrapper.SOCKET_REFERENCE_KEY);
            Response responseWrapper = new ResponseWrapper(response, channel);
            
            if (!authenticationFilter.authenticate(requestWrapper, responseWrapper))
                return;
            
            String context = request.getPath().getPath();
            int endIndex = context.indexOf("/", 1);
            if (endIndex<0) endIndex=context.length();
            context = context.substring(0, endIndex);

            ServerController controller = controllers.get(context);
            if (controller==null) {
                logger.log(Level.WARNING, "Invalid context: {0}", context);
                response.setCode(Status.NOT_FOUND.getCode());
                response.setText(Status.NOT_FOUND.getDescription());

                long time = System.currentTimeMillis();
                
                response.add("Content-Type", "text/plain");
                response.add("Server", "Gephi/0.7 alpha4");
                response.add("Connection", "close");
                response.setDate("Date", time);
                response.setDate("Last-Modified", time);

                try {
                    response.commit();
                    response.getPrintStream().println("404 Not Found");
//                    response.getPrintStream().close();
                    request.getInputStream().close();
                    response.getOutputStream().close();
                    response.close();
                } catch (IOException e) {}
                
            } else {
                controller.handle(requestWrapper, responseWrapper);
            }
        }
        
    }
    
    private void startSSL() throws IOException {
        SocketAddress address = new InetSocketAddress(settings.getSslPort());
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
            
            sslStarted = true;
            
            logger.log(Level.INFO, "HTTPS Listening at port {0}", settings.getSslPort());
            
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

    /**
     * This class is necessary to put the SocketChannel reference
     * as an attribute that can be accessed from the request
     * (request.getAttribute("mySocket")). The reference will
     * be used to verify if the client has closed the connection
     * in the middle of the streaming.
     */
    private class SocketSnoopServer implements Server {
        private final Server realServer ;
        public SocketSnoopServer (Server realServer ){
           this.realServer  = realServer ;
        }
        public void process(Socket socket)throws IOException{
           Map atts = socket.getAttributes();
           SocketChannel channel = socket.getChannel();
           atts.put(RequestWrapper.SOCKET_REFERENCE_KEY, channel);
           realServer.process(socket);
        }
        public void stop() throws IOException{
           realServer.stop();
        }
    }

}
