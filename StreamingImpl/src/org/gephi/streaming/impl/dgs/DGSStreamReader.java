/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.streaming.impl.dgs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.Map;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEventBuilder;

/**
 * A stream reader for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public class DGSStreamReader extends StreamReader implements DGSParserListener {
    
    /**
     * @param handler the GraphEventHandler to which the events will be delegated
     */
    public DGSStreamReader(GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        super(handler, eventBuilder);
    }

    @Override
    public void processStream(InputStream inputStream) {
        
        DGSParser parser = new DGSParser(inputStream, this, report, listener);
        try {
            parser.parse();
        } catch (IOException e) {
        } finally {
            if (report!=null)
                report.log("Stream closed at "+new Date());
        }

        if (listener!=null) {
            listener.onStreamClosed();
        }
    }

    @Override
    public void onEdgeAdded(String graphName, String edgeId, String fromTag,
            String toTag, boolean directed, Map<String, Object>  attributes) {
        handler.handleGraphEvent(eventBuilder.edgeAddedEvent(edgeId, fromTag, toTag, directed, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }
    
    @Override
    public void onEdgeChanged(String sourceId, String edgeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, edgeId, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onEdgeRemoved(String sourceId, String edgeId) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, edgeId, null));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onGraphChanged(Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.GRAPH, EventType.CHANGE, null, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onNodeAdded(String sourceId, String nodeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }
    
    @Override
    public void onNodeChanged(String sourceId, String nodeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, nodeId, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onNodeRemoved(String sourceId, String nodeId) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, nodeId, null));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onStepBegins(String graphName, double time) {
      //TODO
        System.out.println("onStepBegins: Not implemented");
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public String toString() {
        return "DGSStreamProcessor";
    }

    @Override
    public void processStream(ReadableByteChannel channel) throws IOException {
        this.processStream(Channels.newInputStream(channel));
    }
}
