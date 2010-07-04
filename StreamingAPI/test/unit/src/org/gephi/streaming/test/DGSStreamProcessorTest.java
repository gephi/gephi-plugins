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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.AbstractOperationSupport;
import org.gephi.streaming.api.CompositeOperationSupport;
import org.gephi.streaming.api.DefaultGraphStreamingEventProcessor;
import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.GraphEventContainerFactory;
import org.gephi.streaming.api.GraphEventOperationSupport;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamType;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.StreamingClient;
import org.gephi.streaming.api.event.ElementAttributeEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;
import org.gephi.streaming.impl.GraphEventContainerImpl;
import org.gephi.streaming.impl.dgs.DGSStreamType;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * Test cases for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public class DGSStreamProcessorTest {
    
    protected String resource = "amazon_0201485419_400.dgs";
    protected String streamType = "DGS";

    @Test
    public void testStreamReader() throws IOException {
        
        InputStream inputStream = this.getClass().getResourceAsStream(resource);
        
        // get the event operation support
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventOperationSupport operator = new GraphEventOperationSupport(containerfactory.newGraphEventContainer(resource));
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader streamReader = factory.createStreamReader(streamType, operator);
        
        final AtomicInteger count = new AtomicInteger();
        
        GraphEventListener listener = new GraphEventListener() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                count.incrementAndGet();
            }
        };
        operator.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        streamReader.processStream(inputStream);
        
        operator.getContainer().waitForDispatchAllEvents();
        assertEquals(2422, count.get());
//        System.out.println(count.get() + " Events");
    }

    @Test
    public void testStreamWriter() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter streamWriter = factory.createStreamWriter(streamType, out);

        // write triangle
        streamWriter.startStream();
        streamWriter.nodeAdded("A");
        streamWriter.nodeAdded("B");
        streamWriter.nodeAdded("C");
        streamWriter.edgeAdded("AB", "A", "B", false);
        streamWriter.edgeAdded("BC", "B", "C", false);
        streamWriter.edgeAdded("CA", "C", "A", false);
        streamWriter.endStream();

        System.out.println(new String(out.toByteArray()));
    }
    
    @Test
    public void testStreamReaderFactory() throws IOException {
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, new AbstractOperationSupport(){});
        assertNotNull(processor);

        StreamType streamType = new DGSStreamType();
        processor = factory.createStreamReader(streamType, new AbstractOperationSupport(){});
        assertNotNull(processor);
    }

    @Test
    public void testStreamWriterFactory() throws IOException {

        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter processor = factory.createStreamWriter(streamType, new ByteArrayOutputStream());
        assertNotNull(processor);

        StreamType streamType = new DGSStreamType();
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
                catch(IOException e) {e.printStackTrace();}

            }
        }.start();
        
        // get the event operation support
        
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventOperationSupport operator = new GraphEventOperationSupport(containerfactory.newGraphEventContainer(resource));
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader dataProcessor = factory.createStreamReader(streamType, operator);
        
        final AtomicInteger count = new AtomicInteger();
        
        GraphEventListener listener = new GraphEventListener() {
            @Override
            public void onGraphEvent(GraphEvent event) {
                count.incrementAndGet();
            }
        };
        
        operator.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        dataProcessor.processStream(inputStream);
        
        operator.getContainer().waitForDispatchAllEvents();
        System.out.println(count.get() + " Events");
    }
    
    @Test
    public void testAll() throws IOException {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        
        URL url = this.getClass().getResource(resource);
        
        // get the event operation support
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventOperationSupport operator = new GraphEventOperationSupport(containerfactory.newGraphEventContainer(url));
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, operator);
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
        
        operator.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
        
        client.waitForFinish();
        operator.getContainer().waitForDispatchAllEvents();
        
        assertEquals(402, nodeCount.get());
        assertEquals(788, edgeCount.get());
    }
    
    @Test
    public void testSynchFire() throws IOException {
        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        
        // get the event operation support
        GraphEventContainer container = new GraphEventContainerImpl(resource){

           @Override
            public void fireEvent(GraphEvent event) {
               for (GraphEventListener listener: listeners) {
                   listener.onGraphEvent(event);
               }
            }
            
        };
        GraphEventOperationSupport operator = new GraphEventOperationSupport(container);
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, operator);
        assertNotNull(processor);
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        
        DefaultGraphStreamingEventProcessor listener = new DefaultGraphStreamingEventProcessor(graphModel.getHierarchicalMixedGraph());
        operator.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        URL url = this.getClass().getResource(resource);
        
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
        
        URL url = this.getClass().getResource(resource);
        
        // get the event operation support
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventOperationSupport operator = new GraphEventOperationSupport(containerfactory.newGraphEventContainer(url));
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, operator);
        assertNotNull(processor);
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        
        DefaultGraphStreamingEventProcessor listener = new DefaultGraphStreamingEventProcessor(graphModel.getHierarchicalMixedGraph());
        operator.getContainer().getGraphEventDispatcher().addEventListener(listener);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
        
        client.waitForFinish();
        operator.getContainer().waitForDispatchAllEvents();
        
        assertEquals(402, listener.getGraph().getNodeCount());
        assertEquals(788, listener.getGraph().getEdgeCount());
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
        
        CompositeOperationSupport composite = new CompositeOperationSupport();
        composite.addOperationSupport(streamWriter1);
        composite.addOperationSupport(streamWriter2);

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
    
    @Test
    public void testDGS2JSON() throws IOException {
    	
    	OutputStream out = new FileOutputStream("/tmp/a.json");
    	
    	StreamWriterFactory writerFactory = Lookup.getDefault().lookup(StreamWriterFactory.class);
//    	 ByteArrayOutputStream out = new ByteArrayOutputStream();
    	StreamWriter streamWriter = writerFactory.createStreamWriter("JSON", out);
    	
    	StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, streamWriter);
        
        InputStream inputStream = this.getClass().getResourceAsStream(resource);
        processor.processStream(inputStream);
        
//        System.out.println(new String(out.toByteArray()));
        
    }
    
}
