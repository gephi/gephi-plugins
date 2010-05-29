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

import org.gephi.streaming.api.StreamProcessor;

/**
 *
 * @author panisson
 */
public class StreamingClient implements Runnable {
    
    private URL url;
    private StreamProcessor dataProcessor;
    private InputStream inputStream;
    private boolean started = false;
    
    public StreamingClient() {}

    /**
     * @param url
     * @param dataProcessor
     */
    public StreamingClient(URL url, StreamProcessor dataProcessor) {
        this.url = url;
        this.dataProcessor = dataProcessor;
    }
    
    public void run() {
        try {
            URLConnection connection = this.url.openConnection();
            connection.connect();
            
            InputStream inputStream = connection.getInputStream();
            started = true;
            this.dataProcessor.processStream(inputStream);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void connectToEndpoint(final URL endpoint, final StreamProcessor streamProcessor) {
        
        new Thread() {
            public void run() {
                try {
                    URLConnection connection = endpoint.openConnection();
                    connection.connect();
                    
                    InputStream inputStream = connection.getInputStream();
                    started = true;
                    streamProcessor.processStream(inputStream);
                    
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
        
    }
    
    /**
     * 
     */
    public void stop() {
        if(started) {
            try {
                inputStream.close();
            }
            catch(IOException e) {e.printStackTrace();}
        }
    }

}
