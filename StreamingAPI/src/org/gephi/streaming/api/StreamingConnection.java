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
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * @author panisson
 */
public class StreamingConnection extends Thread {
    
    private final AtomicBoolean inProcess = new AtomicBoolean();
    private final URL url;
    private final StreamReader streamProcessor;
    private InputStream inputStream;
    
    public StreamingConnection(final URL url, final StreamReader streamProcessor) throws IOException {
        this.url = url;
        this.streamProcessor = streamProcessor;
        inProcess.set(true);
        
        URLConnection connection = url.openConnection();
        connection.connect();
        inputStream = connection.getInputStream();
    }
    
    public URL getUrl() {
        return url;
    }

    @Override
    public void run() {
        
        inProcess.set(true);

        try {
            
            streamProcessor.processStream(inputStream);
            
        } catch (IOException e) {
            // Exception during processing
            e.printStackTrace();
        } finally {
            inProcess.set(false);
            synchronized (inProcess) {
                inProcess.notifyAll();
            }
        }
    }
    
    public void waitForFinish() {
        while (inProcess.get()) {
            try {
                synchronized (inProcess) {
                    inProcess.wait();
                }
            } catch (InterruptedException e) {}
        }
    }
    
    public boolean isClosed() {
        return inProcess.get();
    }
    
    public void close() throws IOException {
        inputStream.close();
    }

}
