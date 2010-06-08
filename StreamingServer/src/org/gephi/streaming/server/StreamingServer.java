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
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.openide.util.lookup.ServiceProvider;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = StreamingServer.class)
public class StreamingServer {
    
    private int port = 8080;
    private boolean useSSL = true;
    private int sslPort = 8443;
    
    private Map<String, ServerController> controllers = Collections.synchronizedMap(new HashMap<String, ServerController>());
    private Connection serverConnection;
    private ContextContainer contextContainer;
    
    public StreamingServer() {
        contextContainer = new ContextContainer();
    }
    
    public void register(ServerController controller, String context) {
        controllers.put(context, controller);
    }
    
    public void unregister(String context) {
        controllers.remove(context);
    }
    
    public void start() throws IOException {
        serverConnection = new SocketConnection(contextContainer);
        SocketAddress address = new InetSocketAddress(port);
        serverConnection.connect(address);
        
        if (useSSL)
            startSSL();
    }
    
    public void stop() throws IOException {
        serverConnection.close();
    }
    
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public int getSSLPort() {
        return sslPort;
    }

    public void setSSLPort(int sslPort) {
        this.sslPort = sslPort;
    }

    private class ContextContainer implements Container {

        @Override
        public void handle(Request request, Response response) {
            String context = request.getPath().getPath();
            ServerController controller = controllers.get(context);
            if (controller==null) {
                System.out.println("Invalid context: "+context);
                response.setCode(401);
                try {
                    response.close();
                } catch (IOException e) {}
            } else {
                controller.handle(request, response);
            }
        }
    }
    
    private void startSSL() throws IOException {
        SocketAddress address = new InetSocketAddress(sslPort);
        Connection connection = new SocketConnection(contextContainer);
        
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
            // Uncomment this to use a different trust manager.
            // tmf = TrustManagerFactory.getInstance("PKIX");
            // tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf != null ? kmf.getKeyManagers() : null,
                    tmf != null ? tmf.getTrustManagers() : null, null);

            connection.connect(address, sslContext);
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

}
