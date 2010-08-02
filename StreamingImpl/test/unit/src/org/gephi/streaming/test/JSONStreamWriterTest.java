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

import java.io.ByteArrayOutputStream;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * Test cases for the JSON Graph Streaming format.
 * 
 * @author panisson
 *
 */
public class JSONStreamWriterTest {

    protected String resource = "amazon.json";
    protected String streamType = "JSON";

    @Test
    public void testStreamWriterFactory() throws IOException {

        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter processor = factory.createStreamWriter(streamType, new ByteArrayOutputStream());
        assertNotNull(processor);
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

    }

    @Test
    public void testWriteEvents() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter streamWriter = factory.createStreamWriter(streamType, out);

        streamWriter.startStream();
        for (GraphEvent event: new EventFactory().getGraphEvents()) {
            streamWriter.handleGraphEvent(event);
        }
        streamWriter.endStream();
    }

    private class EventFactory {
        private List<GraphEvent> events = new ArrayList<GraphEvent>();

        public EventFactory() {
            GraphEventBuilder eventBuilder = new GraphEventBuilder(this);
            Map<String,Object> attributes = new HashMap<String, Object>();
            attributes.put("size", 2);
            GraphEvent event;
            event = eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "A", attributes);
            events.add(event);
            event = eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "B", attributes);
            events.add(event);
            event = eventBuilder.edgeAddedEvent("AB", "A", "B", false, attributes);
            events.add(event);
            event = eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "A", attributes);
            events.add(event);
            event = eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, "A", attributes);
            events.add(event);
            event = eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, "AB", attributes);
            events.add(event);
            event = eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, "AB", attributes);
            events.add(event);
            event = eventBuilder.graphEvent(ElementType.GRAPH, EventType.CHANGE, null, attributes);
            events.add(event);
        }

        public List<GraphEvent> getGraphEvents() {
            return events;
        }
    }

}
