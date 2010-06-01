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
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import org.gephi.streaming.api.StreamProcessor;

/**
 *
 * @author panisson
 */
public class StreamingClient {
    
    private final AtomicInteger inProcessCount = new AtomicInteger();
    
    public void connectToEndpoint(final URL endpoint, final StreamProcessor streamProcessor) {
        
        inProcessCount.incrementAndGet();
        
        new Thread(endpoint.toString()) {
            public void run() {
                
                final URLConnection connection;

                try {
                    connection = endpoint.openConnection();
                    connection.connect();
                } catch (IOException e) {
                    // Exception in connect: Unable to connect to stream
                    throw new RuntimeException("Unable to connect to stream", e);
                }

                try {
                    
                    
                    InputStream inputStream = connection.getInputStream();
                    streamProcessor.processStream(inputStream);
                    
                    inProcessCount.decrementAndGet();
                    synchronized (inProcessCount) {
                        inProcessCount.notifyAll();
                    }
                    
                } catch (IOException e) {
                    // Exception during processing
                    e.printStackTrace();
                }
            }
        }.start();
        
    }
    
    public void waitForFinish() {
        while (inProcessCount.get() > 0) {
            try {
                synchronized (inProcessCount) {
                    inProcessCount.wait();
                }
            } catch (InterruptedException e) {}
        }
    }

}
