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
package org.gephi.streaming.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamingConnection;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author panisson
 */
public class StreamingConnectionImpl implements StreamingConnection {
    
    private final URL url;
    private final StreamReader streamProcessor;
    private URLConnection connection;
    private ReadableByteChannel channel;
    private final List<StatusListener> listeners =
            Collections.synchronizedList(new ArrayList<StatusListener>());
    private boolean closed = false;
    
    public StreamingConnectionImpl(final URL url, final StreamReader streamProcessor) throws IOException {
        this.url = url;
        this.streamProcessor = streamProcessor;

        // Workaround to avoid invalid certificate problem
        if (url.getProtocol().equalsIgnoreCase("https")) {
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

        connection = url.openConnection();
        connection.connect();

    }
    
    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void asynchProcess() {
        new Thread("StreamingConnection["+url.toString()+"]") {
            @Override
            public void run() {
                synchProcess();
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
    public void synchProcess() {
        try {

            InputStream inputStream = connection.getInputStream();

            streamProcessor.processStream(inputStream, new StreamReader.StreamReaderStatusListener() {

                @Override
                public void onConnectionClosed() {
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

        } catch (IOException e) {
            // Exception during processing
            e.printStackTrace();
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

}
