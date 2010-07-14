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
package org.gephi.streaming.impl.dgs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;

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
    public DGSStreamReader(GraphEventHandler handler) {
        super(handler);
    }

    @Override
    public void processStream(InputStream inputStream) {
        
        DGSParser parser = new DGSParser(inputStream, this);
        try {
            parser.parse();
        } catch (IOException e) {
             System.out.println("Stream closed");
        }
    }

    @Override
    public void onEdgeAdded(String graphName, String edgeId, String fromTag,
            String toTag, boolean directed, Map<String, Object>  attributes) {
        handler.handleGraphEvent(eventBuilder.edgeAddedEvent(edgeId, fromTag, toTag, directed, attributes));
    }
    
    @Override
    public void onEdgeChanged(String sourceId, String edgeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, edgeId, attributes));
    }

    @Override
    public void onEdgeRemoved(String sourceId, String edgeId) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, edgeId, null));
    }

    @Override
    public void onGraphAttributeAdded(String sourceId, String attribute,
            Object value) {
        //TODO
        System.out.println("onGraphAttributeAdded: Not implemented");
    }

    @Override
    public void onGraphAttributeChanged(String sourceId, String attribute,
            Object oldValue, Object newValue) {
      //TODO
        System.out.println("onGraphAttributeChanged: Not implemented");
    }

    @Override
    public void onGraphAttributeRemoved(String sourceId, String attribute) {
      //TODO
        System.out.println("onGraphAttributeRemoved: Not implemented");
    }

    @Override
    public void onNodeAdded(String sourceId, String nodeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, attributes));
    }
    
    @Override
    public void onNodeChanged(String sourceId, String nodeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, nodeId, attributes));
    }

    @Override
    public void onNodeRemoved(String sourceId, String nodeId) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, nodeId, null));
    }

    @Override
    public void onStepBegins(String graphName, double time) {
      //TODO
        System.out.println("onStepBegins: Not implemented");
    }

    @Override
    public String toString() {
        return "DGSStreamProcessor";
    }
}
