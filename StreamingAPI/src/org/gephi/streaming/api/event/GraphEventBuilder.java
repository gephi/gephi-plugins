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
package org.gephi.streaming.api.event;

import java.util.Map;

import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;

/**
 * An event builder
 * 
 * @author Andre' Panisson
 * @see GraphEventContainer
 *
 */
public class GraphEventBuilder {
    
    private Object source;
    
    /**
     * @param container
     */
    public GraphEventBuilder(Object source) {
        this.source = source;
    }

    public GraphEvent graphEvent(ElementType elementType, EventType eventType, String elementId, Map<String, Object> attributes) {
        GraphEvent event = null;
        switch (elementType) {
            case NODE:
                event = new ElementEvent(source, eventType, ElementType.NODE, elementId, attributes);
                break;
            case EDGE:
                if (eventType == EventType.ADD) {
                        String fromNodeId = (String)attributes.remove("source");
                        String toNodeId = (String)attributes.remove("target");
                        Boolean directed = (Boolean)attributes.remove("directed");
                        event = new EdgeAddedEvent(source, elementId, fromNodeId, toNodeId, directed, attributes);
                } else {
                    event = new ElementEvent(source, eventType, ElementType.EDGE, elementId, attributes);
                }
                break;
            case GRAPH:
                event = new ElementEvent(source, eventType, ElementType.GRAPH, elementId, attributes);
                break;
        }
        return event;
    }

    public GraphEvent edgeAddedEvent(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        return new EdgeAddedEvent(source, edgeId, fromNodeId, toNodeId, directed, attributes);
    }
    
}
