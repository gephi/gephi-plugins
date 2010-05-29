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
package org.gephi.streaming.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.gephi.project.api.ProjectController;
import org.gephi.streaming.api.StreamProcessor;
import org.gephi.streaming.api.StreamProcessorFactory;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;
import org.gephi.streaming.impl.GraphEventContainerImpl;
import org.gephi.streaming.impl.StreamingClient;
import org.gephi.streaming.impl.dgs.DGSStreamProcessor;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * Test cases for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public class DGSDataProcessorTest {
    
    private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";

    @Test
    public void testProcess() throws IOException {
        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        
        InputStream inputStream = this.getClass().getResourceAsStream(DGS_RESOURCE);
        
        StreamProcessor streamProcessor = new DGSStreamProcessor();
        streamProcessor.processStream(inputStream);
        
        GraphEventContainerImpl container = (GraphEventContainerImpl)streamProcessor.getContainer();
        List<GraphEvent> events = container.getAllEvents();
        System.out.println(events.size() + " Events");
    }
    
    @Test
    public void testFactory() throws IOException {
        
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = factory.createStreamProcessor("DGS");
        assertNotNull(processor);
    }
    
//    @Test
    public void testStreamingProcess() throws IOException {
        
        final InputStream fileInputStream = this.getClass().getResourceAsStream(DGS_RESOURCE);
        
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) { }
                return fileInputStream.read();
            }
        };
        
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    fileInputStream.close();
                }
                catch(InterruptedException e) {}
                catch(IOException e) {e.printStackTrace();}

            }
        }.start();
        
        StreamProcessor dataProcessor = new DGSStreamProcessor();
        dataProcessor.processStream(inputStream);
        
        GraphEventContainerImpl container = (GraphEventContainerImpl)dataProcessor.getContainer();
        List<GraphEvent> events = container.getAllEvents();
        System.out.println(events.size() + " Events: " + events);
    }
    
    @Test
    public void testAll() throws IOException {
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = factory.createStreamProcessor("DGS");
        assertNotNull(processor);
        
        GraphEventListener listener = new GraphEventListener() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                System.out.println(event);
            }
        };
        processor.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        URL url = this.getClass().getResource(DGS_RESOURCE);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
        
        try {
            Thread.sleep(1000);
        }catch(InterruptedException e) {};
    }
    
}
