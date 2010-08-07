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

import org.gephi.streaming.api.event.GraphEvent;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.FilterEvent;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Test cases for the JSON Graph Streaming format.
 * 
 * @author panisson
 *
 */
public class JSONStreamReaderTest {

    protected String resource = "amazon.json";
    protected String streamType = "JSON";

    @Test
    public void testStreamReaderFactory() throws IOException {

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(this);
        StreamReader processor = factory.createStreamReader(streamType, new MockGraphEventHandler(), eventBuilder);
        assertNotNull(processor);
    }

    @Test
    public void testStreamReader() throws IOException {

        URL url = this.getClass().getResource(resource);
        url.openConnection();
        InputStream inputStream = url.openStream();

        MockGraphEventHandler handler = new MockGraphEventHandler();

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(this);
        StreamReader streamReader = factory.createStreamReader(streamType, handler, eventBuilder);
        
        streamReader.processStream(inputStream);
//        assertEquals(2422, count.get());
        assertTrue(handler.getEventCount()>=1405);
//        assertEquals(1405, handler.getEventCount());
//        System.out.println(count.get() + " Events");
    }

    @Test
    public void testReadEvents() throws IOException {
        HeapEventHandler handler = new HeapEventHandler();

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(this);
        final StreamReader streamReader = factory.createStreamReader(streamType, handler, eventBuilder);
        
        final StringBufferedInputStream inputStream = new StringBufferedInputStream();

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    streamReader.processStream(inputStream);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        t.start();

        GraphEvent event;

        inputStream.offer("{\"an\":{\"A\":{\"label\":\"Streaming Node A\",\"size\":2}}}\n\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.ADD, event.getEventType());
        assertEquals(ElementType.NODE, event.getElementType());
        assertEquals(ElementEvent.class, event.getClass());
        assertEquals("A", ((ElementEvent)event).getElementId());

        inputStream.offer("{\"ae\":{\"AB\":{\"source\":\"A\",\"target\":\"B\",\"directed\":false,\"label\":\"Edge AB\",\"size\":2}}}\n\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.ADD, event.getEventType());
        assertEquals(ElementType.EDGE, event.getElementType());
        assertTrue(event instanceof ElementEvent);
        assertEquals("AB", ((ElementEvent)event).getElementId());

        inputStream.offer("{\"cn\":{\"A\":{\"label\":\"Streaming Node A\",\"size\":2}}}\n\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.CHANGE, event.getEventType());
        assertEquals(ElementType.NODE, event.getElementType());
        assertTrue(event instanceof ElementEvent);
        assertEquals("A", ((ElementEvent)event).getElementId());

        inputStream.offer("{\"ce\":{\"AB\":{\"size\":2}}}\n\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.CHANGE, event.getEventType());
        assertEquals(ElementType.EDGE, event.getElementType());
        assertTrue(event instanceof ElementEvent);
        assertEquals("AB", ((ElementEvent)event).getElementId());

        inputStream.offer("{\"dn\":{\"A\":{}}}\n\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.REMOVE, event.getEventType());
        assertEquals(ElementType.NODE, event.getElementType());
        assertTrue(event instanceof ElementEvent);
        assertEquals("A", ((ElementEvent)event).getElementId());

        inputStream.offer("{\"de\":{\"AB\":{}}}\n\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.REMOVE, event.getEventType());
        assertEquals(ElementType.EDGE, event.getElementType());
        assertTrue(event instanceof ElementEvent);
        assertEquals("AB", ((ElementEvent)event).getElementId());

        inputStream.offer("{\"cg\":{\"AB\":{}}}\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.CHANGE, event.getEventType());
        assertEquals(ElementType.GRAPH, event.getElementType());

        inputStream.offer("{\"dn\":{\"filter\":\"ALL\"}}\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.REMOVE, event.getEventType());
        assertEquals(ElementType.NODE, event.getElementType());
        assertTrue(event instanceof FilterEvent);
        FilterEvent filterEvent = (FilterEvent)event;
        assertTrue(filterEvent.getFilter() instanceof NodeFilter);
        NodeFilter nodeFilter = (NodeFilter)filterEvent.getFilter();
        assertTrue(nodeFilter.evaluate(null, null));

        inputStream.offer("{\"de\":{\"filter\":\"ALL\"}}\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.REMOVE, event.getEventType());
        assertEquals(ElementType.EDGE, event.getElementType());
        assertTrue(event instanceof FilterEvent);
        assertTrue(((FilterEvent)event).getFilter() instanceof EdgeFilter);
        EdgeFilter edgeFilter = (EdgeFilter)((FilterEvent)event).getFilter();
        assertTrue(edgeFilter.evaluate(null, null));

        inputStream.offer("{\"dn\":{\"filter\":{\"NodeAttribute\":{\"attribute\":\"id\",\"value\":\"A\"}}}}\r");
        event = handler.getGraphEvent();
        assertNotNull(event);
        assertEquals(EventType.REMOVE, event.getEventType());
        assertEquals(ElementType.NODE, event.getElementType());
        assertTrue(event instanceof FilterEvent);
        filterEvent = (FilterEvent)event;
        assertTrue(filterEvent.getFilter() instanceof NodeFilter);
        nodeFilter = (NodeFilter)filterEvent.getFilter();

        // Incomplete event
        inputStream.offer("{\"de\":{\"filter\":\"ALL\"}\r");
        event = handler.getGraphEvent();
        assertNull(event);

        // Verify if EOF ends the Thread
        inputStream.offer("$");
        try {
            t.join(1000);
        } catch (InterruptedException ex) { }
        assertTrue(t.getState() == t.getState().TERMINATED);
    }

    private class HeapEventHandler implements GraphEventHandler {

        private LinkedList<GraphEvent> eventHeap = new LinkedList<GraphEvent>();

        @Override
        public void handleGraphEvent(GraphEvent event) {
            eventHeap.offer(event);
        }

        public GraphEvent getGraphEvent() {
            if (eventHeap.isEmpty()) {
                return null;
            } else {
                return eventHeap.pop();
            }
        }

    }

    private class StringBufferedInputStream extends InputStream {
        private final StringBuffer buffer = new StringBuffer();

        @Override
        public int read() throws IOException {
            int read = 0;
            while (read == 0) {
                if(buffer.length() > 0) {
                    read = buffer.charAt(0);
                    buffer.deleteCharAt(0);
                    if (read=='$') {
                        synchronized(buffer) {
                            buffer.notifyAll();
                        }
                        return -1;
                    }
                    return read;
                } else {
                    try {
                        synchronized(buffer) {
                            buffer.notifyAll();
                            buffer.wait();
                        }
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return read;
        }

        public void offer(String str) {
            buffer.append(str);
            synchronized(buffer) {
                buffer.notifyAll();
                try {
                    buffer.wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

    }
}
