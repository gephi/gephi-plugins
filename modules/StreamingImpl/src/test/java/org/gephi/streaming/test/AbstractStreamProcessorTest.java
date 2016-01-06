/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.StreamingEndpoint;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.impl.GraphEventContainer;
import org.gephi.streaming.impl.StreamingConnectionImpl;
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

        MockGraphEventHandler handler = new MockGraphEventHandler();
        
        // get the event container
        GraphEventContainer container =  new GraphEventContainer(handler);

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader streamReader = factory.createStreamReader(streamType, container, eventBuilder);
        
        streamReader.processStream(inputStream);
        
        container.waitForDispatchAllEvents();
        container.stop();
//        assertEquals(2422, count.get());
        assertTrue(handler.getEventCount()>=1405);
//        assertEquals(1405, handler.getEventCount());
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
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader processor = factory.createStreamReader(streamType, new MockGraphEventHandler(), eventBuilder);
        assertNotNull(processor);
    }

    @Test
    public void testStreamWriterFactory() throws IOException {

        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter processor = factory.createStreamWriter(streamType, new ByteArrayOutputStream());
        assertNotNull(processor);
    }
    
    @Test
    public void testChaining() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamWriterFactory writerFactory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter streamWriter = writerFactory.createStreamWriter(streamType, out);

        InputStream inputStream = this.getClass().getResourceAsStream(resource);
        
        StreamReaderFactory readerfactory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader streamReader = readerfactory.createStreamReader(streamType, streamWriter, eventBuilder);
        
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

        MockGraphEventHandler handler = new MockGraphEventHandler();
        
        GraphEventContainer container =  new GraphEventContainer(handler);
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader streamReader = factory.createStreamReader(streamType, container, eventBuilder);
        Report report = new Report();
        streamReader.setReport(report);
        
        streamReader.processStream(inputStream);
        
        container.waitForDispatchAllEvents();
        container.stop();
        System.out.println(report.getText());
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
        GraphEventContainer container =  new GraphEventContainer(handler);

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader streamReader = factory.createStreamReader(streamType, container, eventBuilder);
        assertNotNull(streamReader);

        StreamingEndpoint endpoint = new StreamingEndpoint();
        endpoint.setUrl(url);

        StreamingConnection connection = new StreamingConnectionImpl(endpoint, streamReader, new Report());

        final AtomicBoolean processing = new AtomicBoolean(true);
        connection.addStatusListener(
            new StreamingConnection.StatusListener() {
            @Override
                public void onConnectionClosed(StreamingConnection connection) {
                    processing.set(false);
                    synchronized (processing) {
                        processing.notifyAll();
                    }
                }
            @Override
                public void onDataReceived(StreamingConnection connection) { }
            @Override
                public void onError(StreamingConnection connection) { }
            });
        connection.asynchProcess();

        synchronized (processing) {
            try {
                while (processing.get()) {
                    processing.wait();
                }
            } catch (InterruptedException e) {}
        }

        container.waitForDispatchAllEvents();
        container.stop();
        
        assertEquals(402, nodeCount.get());
        assertEquals(788, edgeCount.get());
    }
    
    @Test
    public void testSynchFire() throws IOException {

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        Graph graph = graphModel.getGraph();

        GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);

        URL url = this.getClass().getResource(resource);
        
        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader streamReader = factory.createStreamReader(streamType, graphUpdaterHandler, eventBuilder);
        assertNotNull(streamReader);

        StreamingEndpoint endpoint = new StreamingEndpoint();
        endpoint.setUrl(url);
        StreamingConnection connection = new StreamingConnectionImpl(endpoint, streamReader, new Report());

        final AtomicBoolean processing = new AtomicBoolean(true);
        connection.addStatusListener(
            new StreamingConnection.StatusListener() {
            @Override
                public void onConnectionClosed(StreamingConnection connection) {
                    processing.set(false);
                    synchronized (processing) {
                        processing.notifyAll();
                    }
                }
            @Override
                public void onDataReceived(StreamingConnection connection) { }
            @Override
                public void onError(StreamingConnection connection) { }
            });
        connection.asynchProcess();

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
        GraphModel graphModel = graphController.getGraphModel();
        Graph graph = graphModel.getGraph();

        GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);
        
        URL url = this.getClass().getResource(resource);
        
        // get the event operation support
        GraphEventContainer container =  new GraphEventContainer(graphUpdaterHandler);

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader streamReader = factory.createStreamReader(streamType, container, eventBuilder);
        assertNotNull(streamReader);

        StreamingEndpoint endpoint = new StreamingEndpoint();
        endpoint.setUrl(url);
        StreamingConnection connection = new StreamingConnectionImpl(endpoint, streamReader, new Report());

        final AtomicBoolean processing = new AtomicBoolean(true);
        connection.addStatusListener(
            new StreamingConnection.StatusListener() {
            @Override
                public void onConnectionClosed(StreamingConnection connection) {
                    processing.set(false);
                    synchronized (processing) {
                        processing.notifyAll();
                    }
                }
            @Override
                public void onDataReceived(StreamingConnection connection) { }
            @Override
                public void onError(StreamingConnection connection) { }
            });
        connection.asynchProcess();

        synchronized (processing) {
            try {
                while (processing.get()) {
                    processing.wait();
                }
            } catch (InterruptedException e) {}
        }
        
        container.waitForDispatchAllEvents();
        container.stop();
        
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
        GraphEventBuilder eventBuilder = new GraphEventBuilder(resource);
        StreamReader streamReader = readerfactory.createStreamReader(streamType, composite, eventBuilder);
        
        streamWriter1.startStream();
        streamWriter2.startStream();
        streamReader.processStream(inputStream);
        streamWriter1.endStream();
        streamWriter2.endStream();
        
        assertTrue(out1.toByteArray().length>0);
        assertTrue(out2.toByteArray().length>0);
        assertTrue(out1.toByteArray().length==out2.toByteArray().length);
    }
    
}
