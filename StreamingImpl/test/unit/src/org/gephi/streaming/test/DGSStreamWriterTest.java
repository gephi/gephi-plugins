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
public class DGSStreamWriterTest {

    protected String resource = "amazon_0201485419_400.dgs";
    protected String streamType = "DGS";

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
