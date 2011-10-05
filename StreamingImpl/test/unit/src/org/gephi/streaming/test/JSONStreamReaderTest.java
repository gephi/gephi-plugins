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
