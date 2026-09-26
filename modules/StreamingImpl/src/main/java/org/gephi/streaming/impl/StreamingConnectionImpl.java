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
package org.gephi.streaming.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.StreamingEndpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author panisson
 */
public class StreamingConnectionImpl implements StreamingConnection {

    private static final Logger logger = Logger.getLogger(StreamingConnectionImpl.class.getName());
    
    private final StreamingEndpoint endpoint;
    private final StreamReader streamReader;
    private URLConnection connection;
    private ReadableByteChannel channel;
    private final List<StatusListener> listeners =
            Collections.synchronizedList(new ArrayList<StatusListener>());
    private boolean closed = false;
    private final Report report;
    
    public StreamingConnectionImpl(final StreamingEndpoint endpoint, 
            final StreamReader streamReader, Report report) throws IOException {
        this.endpoint = endpoint;
        this.streamReader = streamReader;
        this.report = report;

        // Workaround to avoid invalid certificate problem
        if (endpoint.getUrl().getProtocol().equalsIgnoreCase("https")) {
            FileObject certs = FileUtil.getConfigFile("cacerts");
            if (certs == null) {
                certs = FileUtil.getConfigRoot().createData("cacerts");
                OutputStream os = certs.getOutputStream();
                FileUtil.copy(this.getClass().getResourceAsStream("cacerts"), os);
                os.flush();
                os.close();
            }
            System.setProperty("javax.net.ssl.trustStore", FileUtil.toFile(certs).getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", "1234567890");
        }

        connection = endpoint.getUrl().openConnection();

        if (endpoint.getUser()!=null && endpoint.getUser().length()>0) {
            
            // Workaround for Bug https://issues.apache.org/jira/browse/CODEC-89
            String base64Encoded = Base64.encodeBase64String((endpoint.getUser()+":"+endpoint.getPassword()).getBytes());
            base64Encoded = base64Encoded.replaceAll("\r\n?", "");

            connection.setRequestProperty("Authorization", "Basic " + base64Encoded);

            // this option is not optimal, as it sets the same authenticator for all connections
//            Authenticator.setDefault(new Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication (endpoint.getUser(), endpoint.getPassword().toCharArray());
//                }
//            });
        }

        
        connection.connect();

    }
    
    @Override
    public StreamingEndpoint getStreamingEndpoint() {
        return endpoint;
    }

    @Override
    public void asynchProcess() {
        new Thread("StreamingConnection["+endpoint.getUrl().toString()+"]") {
            @Override
            public void run() {
                process();
            }
        }.start();
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public synchronized void close() throws IOException {

        if (connection!=null) {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection)connection).disconnect();
            }

            if (connection.getDoOutput()) {
                connection.getOutputStream().close();
            }
            if (connection.getDoInput()) {
                connection.getInputStream().close();
            }
        }

        if (channel != null) {
            channel.close();
        }

        closed = true;
    }

    @Override
    public void addStatusListener(StatusListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeStatusListener(StatusListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void process() {
        try {

            InputStream inputStream = connection.getInputStream();
            streamReader.setStatusListener(new StreamReader.StreamReaderStatusListener() {

                @Override
                public void onStreamClosed() {
                    synchronized (listeners) {
                        for (StatusListener listener: listeners)
                            if (listener != null) {
                                listener.onConnectionClosed(StreamingConnectionImpl.this);
                            }
                    }
                }

                @Override
                public void onDataReceived() {
                    synchronized (listeners) {
                        for (StatusListener listener: listeners)
                            if (listener != null) {
                                listener.onDataReceived(StreamingConnectionImpl.this);
                            }
                    }
                }

                @Override
                public void onError() {
                    synchronized (listeners) {
                        for (StatusListener listener: listeners)
                            if (listener != null) {
                                listener.onError(StreamingConnectionImpl.this);
                            }
                    }
                }
            });

            streamReader.processStream(inputStream);

        } catch (IOException e) {
            logger.log(Level.INFO, null, e);
        } catch (RuntimeException e) {
            // Exception during processing
            logger.log(Level.INFO, null, e);
        } finally {
            closed = true;

            synchronized (listeners) {
                for (StatusListener listener: listeners)
                    if (listener != null) {
                        listener.onConnectionClosed(this);
                    }
            }
        }
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

}
