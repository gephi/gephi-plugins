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
package org.gephi.streaming.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author panisson
 */
public class StreamingConnection {
    
    private final URL url;
    private final StreamReader streamProcessor;
    private URLConnection connection;
    private ReadableByteChannel channel;
    private final List<StatusListener> listeners =
            Collections.synchronizedList(new ArrayList<StatusListener>());
    private boolean closed = false;
    private Thread readerThread;
    
    public StreamingConnection(final URL url, final StreamReader streamProcessor) throws IOException {
        this.url = url;
        this.streamProcessor = streamProcessor;

        if (url.getProtocol().equals("http")) {
            channel = openNioConnection(url);
        } else {
            connection = url.openConnection();
            connection.connect();
        }

    }
    
    public URL getUrl() {
        return url;
    }

    public void asynchProcess() {
        new Thread("StreamingConnection") {
            @Override
            public void run() {
                synchProcess();
            }
        }.start();
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public synchronized void close() throws IOException {

        if (readerThread!=null) {
            readerThread.interrupt();
            readerThread = null;
        }

        if (connection!=null) {
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

    public void addStatusListener(StatusListener listener) {
        this.listeners.add(listener);
    }

    public void synchProcess() {
        try {

            if (channel==null) {
                InputStream inputStream = connection.getInputStream();
                channel = Channels.newChannel(inputStream);
                readerThread = Thread.currentThread();
            } else {
                readHttpHeader(channel);
            }

            streamProcessor.processStream(channel, new StreamReader.StreamReaderStatusListener() {
//            streamProcessor.processStream(inputStream, new StreamReader.StreamReaderStatusListener() {

                public void onConnectionClosed() {
                    synchronized (listeners) {
                        for (StatusListener listener: listeners)
                            if (listener != null) {
                                listener.onConnectionClosed(StreamingConnection.this);
                            }
                    }
                }

                public void onDataReceived() {
                    synchronized (listeners) {
                        for (StatusListener listener: listeners)
                            if (listener != null) {
                                listener.onDataReceived(StreamingConnection.this);
                            }
                    }
                }

                public void onError() {
                    synchronized (listeners) {
                        for (StatusListener listener: listeners)
                            if (listener != null) {
                                listener.onError(StreamingConnection.this);
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

    public interface StatusListener {
        public void onConnectionClosed(StreamingConnection connection);
        public void onDataReceived(StreamingConnection connection);
        public void onError(StreamingConnection connection);
    }

    private ReadableByteChannel openNioConnection(URL httpurl) throws IOException {
        String hostname = httpurl.getHost( );

        int port = httpurl.getPort( );
        if (port == -1) port = 80; // Use default port if none specified

        String path = httpurl.getFile();

        // Combine the hostname and port into a single address object.
        // java.net.SocketAddress and InetSocketAddress are new in Java 1.4
        SocketAddress serverAddress=new InetSocketAddress(hostname, port);

        // Open a SocketChannel to the server
        SocketChannel server = SocketChannel.open(serverAddress);

        // Put together the HTTP request we'll send to the server.
        String request =
            "GET " + path + " HTTP/1.1\r\n" +  // The request
            "Host: " + hostname + "\r\n" +   // Required in HTTP 1.1
            "Connection: close\r\n" +        // Don't keep connection open
            "User-Agent: Gephi 0.7\r\n" +
            "\r\n";  // Blank line indicates end of request headers

        // Now wrap a CharBuffer around that request string
        CharBuffer requestChars = CharBuffer.wrap(request);

        // Get a Charset object to encode the char buffer into bytes
        Charset charset = Charset.forName("ISO-8859-1");

        // Use the charset to encode the request into a byte buffer
        ByteBuffer requestBytes = charset.encode(requestChars);

        // Finally, we can send this HTTP request to the server.
        server.write(requestBytes);

        return server;
    }

    private void readHttpHeader(ReadableByteChannel channel) throws IOException {
        // Read bytes one by une until end of server header
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int count = 0;
        while ((channel.read(buffer))!=-1) {
            buffer.flip();
            char read = (char)buffer.array()[0];

            if(read=='\n' || read=='\r'){
                count++;
                if (count==4) {
                    break;
                }
            } else {
                count = 0;
            }

            buffer.clear();
        }
    }

}
