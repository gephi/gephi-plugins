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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author panisson
 */
public class StreamingConnection extends Thread {
    
    private final URL url;
    private final StreamReader streamProcessor;
    private InputStream inputStream;
    private final List<StreamingConnectionStatusListener> listeners =
            Collections.synchronizedList(new ArrayList<StreamingConnectionStatusListener>());
    private boolean closed = false;
    
    public StreamingConnection(final URL url, final StreamReader streamProcessor) throws IOException {
        this.url = url;
        this.streamProcessor = streamProcessor;
        
        URLConnection connection = url.openConnection();
        connection.connect();
        inputStream = connection.getInputStream();
    }
    
    public URL getUrl() {
        return url;
    }

    @Override
    public void run() {

        try {
            
            streamProcessor.processStream(inputStream);
            
        } catch (IOException e) {
            // Exception during processing
            e.printStackTrace();
        } finally {
            closed = true;

            
            synchronized (listeners) {
                for (StreamingConnectionStatusListener listener: listeners)
                    if (listener != null) {
                        listener.onConnectionClosed(this);
                    }
            }
        }
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public void close() throws IOException {
        inputStream.close();
        closed = true;
    }

    public void addStreamingConnectionStatusListener(StreamingConnectionStatusListener listener) {
        this.listeners.add(listener);
    }

}
