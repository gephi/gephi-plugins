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
import java.util.concurrent.atomic.AtomicInteger;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.StreamProcessor;
import org.gephi.streaming.api.StreamProcessorFactory;
import org.gephi.streaming.api.event.ElementAttributeEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;
import org.gephi.streaming.api.DefaultGraphStreamingEventProcessor;
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
        
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor streamProcessor = factory.createStreamProcessor("DGS");
        
        final AtomicInteger count = new AtomicInteger();
        
        GraphEventListener listener = new GraphEventListener() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                count.incrementAndGet();
            }
        };
        streamProcessor.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        streamProcessor.processStream(inputStream);
        
        streamProcessor.getContainer().waitForDispatchAllEvents();
        assertEquals(2422, count.get());
//        System.out.println(count.get() + " Events");
    }
    
    @Test
    public void testFactory() throws IOException {
        
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = factory.createStreamProcessor("DGS");
        assertNotNull(processor);
    }
    
    @Test
    public void testStreamingProcess() throws IOException {
        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        
        final InputStream fileInputStream = this.getClass().getResourceAsStream(DGS_RESOURCE);
        
        InputStream inputStream = new InputStream() {
            private int count = 0;
            
            @Override
            public int read() throws IOException {
                count++;
                if (count%100 == 0)
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) { }
                return fileInputStream.read();
            }
        };
        
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    fileInputStream.close();
                }
                catch(InterruptedException e) {}
                catch(IOException e) {e.printStackTrace();}

            }
        }.start();
        
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor dataProcessor = factory.createStreamProcessor("DGS");
        
        final AtomicInteger count = new AtomicInteger();
        
        GraphEventListener listener = new GraphEventListener() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                count.incrementAndGet();
            }
        };
        dataProcessor.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        dataProcessor.processStream(inputStream);
        
        dataProcessor.getContainer().waitForDispatchAllEvents();
        System.out.println(count.get() + " Events");
    }
    
    @Test
    public void testAll() throws IOException {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = factory.createStreamProcessor("DGS");
        assertNotNull(processor);
        
        final AtomicInteger nodeCount = new AtomicInteger();
        final AtomicInteger edgeCount = new AtomicInteger();
        
        GraphEventListener listener = new GraphEventListener() {
            @Override
            public void onGraphEvent(GraphEvent event) {
//                System.out.println(event);
                if (!(event instanceof ElementAttributeEvent)) {
                    if (event.getElementType() == ElementType.NODE && event.getEventType() == EventType.ADD)
                        nodeCount.incrementAndGet();
                    if (event.getElementType() == ElementType.EDGE && event.getEventType() == EventType.ADD)
                        edgeCount.incrementAndGet();
                }
            }
        };
        processor.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        URL url = this.getClass().getResource(DGS_RESOURCE);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
        
        client.waitForFinish();
        processor.getContainer().waitForDispatchAllEvents();
        
        assertEquals(402, nodeCount.get());
        assertEquals(788, edgeCount.get());
    }
    
    @Test
    public void testSynchFire() throws IOException {
        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = factory.createStreamProcessor("DGS");
        
        processor.setContainer(new GraphEventContainerImpl(processor){

           @Override
            protected void fireEvent(GraphEvent event) {
               for (GraphEventListener listener: listeners) {
                   listener.onGraphEvent(event);
               }
            }
            
        });
        
        assertNotNull(processor);
        
        DefaultGraphStreamingEventProcessor listener = new DefaultGraphStreamingEventProcessor(workspace);
        processor.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        URL url = this.getClass().getResource(DGS_RESOURCE);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
        
        client.waitForFinish();
        
        assertEquals(402, listener.getGraph().getNodeCount());
        assertEquals(788, listener.getGraph().getEdgeCount());
    }
    
    @Test
    public void testAsynchFire() throws IOException {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        
        StreamProcessorFactory factory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = factory.createStreamProcessor("DGS");
        assertNotNull(processor);
        
        DefaultGraphStreamingEventProcessor listener = new DefaultGraphStreamingEventProcessor(workspace);
        processor.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        URL url = this.getClass().getResource(DGS_RESOURCE);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
        
        client.waitForFinish();
        processor.getContainer().waitForDispatchAllEvents();
        
        assertEquals(402, listener.getGraph().getNodeCount());
        assertEquals(788, listener.getGraph().getEdgeCount());
    }
    
    @Test
    public void getTimesSynchFire() throws IOException {
        for (int i=0; i<30; i++) {
            testSynchFire();
        }
    }
    
    @Test
    public void getTimesAsynchFire() throws IOException {
        for (int i=0; i<30; i++) {
            testAsynchFire();
        }
    }
    
}
