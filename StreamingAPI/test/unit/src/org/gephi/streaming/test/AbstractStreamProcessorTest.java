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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.Graph;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.CompositeGraphEventHandler;
import org.gephi.streaming.api.DefaultGraphStreamingEventProcessor;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.GraphEventContainerFactory;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.StreamingConnectionStatusListener;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.impl.GraphEventContainerImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * Test cases for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
@Ignore
public abstract class AbstractStreamProcessorTest {
    
    protected String resource = "amazon_0201485419_400.dgs";
    protected String streamType = "DGS";

    @Test
    public void testStreamReader() throws IOException {
        
        InputStream inputStream = this.getClass().getResourceAsStream(resource);

        final AtomicInteger count = new AtomicInteger();

        GraphEventHandler handler = new GraphEventHandler() {
            @Override
            public void handleGraphEvent(GraphEvent event) {
                count.incrementAndGet();
            }
        };
        
        // get the event operation support
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventContainer container = containerfactory.newGraphEventContainer(resource, handler);

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader streamReader = factory.createStreamReader(streamType, container);
        
        streamReader.processStream(inputStream);
        
        container.waitForDispatchAllEvents();
//        assertEquals(2422, count.get());
        assertEquals(1405, count.get());
//        System.out.println(count.get() + " Events");
    }

    @Test
    public void testStreamWriter() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter streamWriter = factory.createStreamWriter(streamType, out);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(this);

        // write triangle
        streamWriter.startStream();
        streamWriter.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "A", null));
        streamWriter.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "B", null));
        streamWriter.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "C", null));
        streamWriter.handleGraphEvent(eventBuilder.edgeAddedEvent("AB", "A", "B", false, null));
        streamWriter.handleGraphEvent(eventBuilder.edgeAddedEvent("BC", "B", "C", false, null));
        streamWriter.handleGraphEvent(eventBuilder.edgeAddedEvent("CA", "C", "A", false, null));
        streamWriter.endStream();

        System.out.println(new String(out.toByteArray()));
    }
    
    @Test
    public void testStreamReaderFactory() throws IOException {
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, new MockOperationSupport());
        assertNotNull(processor);

        processor = factory.createStreamReader(streamType, new MockOperationSupport());
        assertNotNull(processor);
    }

    @Test
    public void testStreamWriterFactory() throws IOException {

        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter processor = factory.createStreamWriter(streamType, new ByteArrayOutputStream());
        assertNotNull(processor);

        processor = factory.createStreamWriter(streamType, new ByteArrayOutputStream());
        assertNotNull(processor);
    }
    
    @Test
    public void testChaining() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamWriterFactory writerFactory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter streamWriter = writerFactory.createStreamWriter(streamType, out);

        InputStream inputStream = this.getClass().getResourceAsStream(resource);
        
        StreamReaderFactory readerfactory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader streamReader = readerfactory.createStreamReader(streamType, streamWriter);
        
        streamWriter.startStream();
        streamReader.processStream(inputStream);
        streamWriter.endStream();
        
        assertTrue(out.toByteArray().length>0);

//        System.out.println(new String(out.toByteArray()));
    }
    
    @Test
    public void testStreamingProcess() throws IOException {
        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        
        final InputStream fileInputStream = this.getClass().getResourceAsStream(resource);
        
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
                catch(IOException e) {}

            }
        }.start();

        final AtomicInteger count = new AtomicInteger();

        GraphEventHandler handler = new GraphEventHandler() {
            @Override
            public void handleGraphEvent(GraphEvent event) {
                count.incrementAndGet();
            }
        };
        
        // get the event operation support
        
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventContainer container = containerfactory.newGraphEventContainer(resource, handler);
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader dataProcessor = factory.createStreamReader(streamType, container);
        
        dataProcessor.processStream(inputStream);
        
        container.waitForDispatchAllEvents();
        System.out.println(count.get() + " Events");
    }
    
    @Test
    public void testAll() throws IOException {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        
        URL url = this.getClass().getResource(resource);

        final AtomicInteger nodeCount = new AtomicInteger();
        final AtomicInteger edgeCount = new AtomicInteger();

        GraphEventHandler handler = new GraphEventHandler() {
            @Override
            public void handleGraphEvent(GraphEvent event) {
                if (event.getElementType() == ElementType.NODE && event.getEventType() == EventType.ADD)
                    nodeCount.incrementAndGet();
                if (event.getElementType() == ElementType.EDGE && event.getEventType() == EventType.ADD)
                    edgeCount.incrementAndGet();
            }
        };

        // get the event operation support
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventContainer container = containerfactory.newGraphEventContainer(resource, handler);

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, container);
        assertNotNull(processor);

        StreamingConnection connection = new StreamingConnection(url, processor);

        final AtomicBoolean processing = new AtomicBoolean(true);
        connection.setStreamingConnectionStatusListener(
            new StreamingConnectionStatusListener() {
                public void onConnectionClosed(StreamingConnection connection) {
                    processing.set(false);
                    synchronized (processing) {
                        processing.notifyAll();
                    }
                }
            });
        connection.start();

        synchronized (processing) {
            try {
                while (processing.get()) {
                    processing.wait();
                }
            } catch (InterruptedException e) {}
        }

        container.waitForDispatchAllEvents();
        
        assertEquals(402, nodeCount.get());
        assertEquals(788, edgeCount.get());
    }
    
    @Test
    public void testSynchFire() throws IOException {

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getHierarchicalMixedGraph();

        GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);

        URL url = this.getClass().getResource(resource);
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, graphUpdaterHandler);
        assertNotNull(processor);
        
        StreamingConnection connection = new StreamingConnection(url, processor);

        final AtomicBoolean processing = new AtomicBoolean(true);
        connection.setStreamingConnectionStatusListener(
            new StreamingConnectionStatusListener() {
                public void onConnectionClosed(StreamingConnection connection) {
                    processing.set(false);
                    synchronized (processing) {
                        processing.notifyAll();
                    }
                }
            });
        connection.start();

        synchronized (processing) {
            try {
                while (processing.get()) {
                    processing.wait();
                }
            } catch (InterruptedException e) {}
        }
        
        assertEquals(402, graph.getNodeCount());
        assertEquals(788, graph.getEdgeCount());
    }
    
    @Test
    public void testAsynchFire() throws IOException {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getHierarchicalMixedGraph();

        GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);
        
        URL url = this.getClass().getResource(resource);
        
        // get the event operation support
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventContainer container = containerfactory.newGraphEventContainer(resource, graphUpdaterHandler);

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, container);
        assertNotNull(processor);

        StreamingConnection connection = new StreamingConnection(url, processor);

        final AtomicBoolean processing = new AtomicBoolean(true);
        connection.setStreamingConnectionStatusListener(
            new StreamingConnectionStatusListener() {
                public void onConnectionClosed(StreamingConnection connection) {
                    processing.set(false);
                    synchronized (processing) {
                        processing.notifyAll();
                    }
                }
            });
        connection.start();

        synchronized (processing) {
            try {
                while (processing.get()) {
                    processing.wait();
                }
            } catch (InterruptedException e) {}
        }
        
        container.waitForDispatchAllEvents();
        
        assertEquals(402, graph.getNodeCount());
        assertEquals(788, graph.getEdgeCount());
    }
    
//    @Test
    public void getTimesSynchFire() throws IOException {
        for (int i=0; i<30; i++) {
            testSynchFire();
        }
    }
    
//    @Test
    public void getTimesAsynchFire() throws IOException {
        for (int i=0; i<30; i++) {
            testAsynchFire();
        }
    }
    
    @Test
    public void testCompositeOperationSupport() throws IOException {
        StreamWriterFactory writerFactory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        StreamWriter streamWriter1 = writerFactory.createStreamWriter(streamType, out1);
        
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        StreamWriter streamWriter2 = writerFactory.createStreamWriter(streamType, out2);
        
        CompositeGraphEventHandler composite = new CompositeGraphEventHandler();
        composite.addHandler(streamWriter1);
        composite.addHandler(streamWriter2);

        InputStream inputStream = this.getClass().getResourceAsStream(resource);
        
        StreamReaderFactory readerfactory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader streamReader = readerfactory.createStreamReader(streamType, composite);
        
        streamWriter1.startStream();
        streamWriter2.startStream();
        streamReader.processStream(inputStream);
        streamWriter1.endStream();
        streamWriter2.endStream();
        
        assertTrue(out1.toByteArray().length>0);
        assertTrue(out2.toByteArray().length>0);
        assertTrue(out1.toByteArray().length==out2.toByteArray().length);
    }

    private static class MockOperationSupport implements GraphEventHandler {

        public void handleGraphEvent(GraphEvent event) {
        }

    }
    
}
