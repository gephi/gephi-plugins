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
package org.gephi.streaming.api.event;

import java.util.EventObject;

/**
 * @author panisson
 *
 */
public class GraphEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    
    private final EventType eventType;
    private final ElementType elementType;
    private final String elementId;

    /**
     * Constructs a graph Event.
     *
     * @param    source    The object on which the Event initially occurred.
     * @param eventType 
     * @param elementType 
     * @param elementId 
     * @exception  IllegalArgumentException  if source is null.
     */
    public GraphEvent(Object source, EventType eventType, 
            ElementType elementType, String elementId) {
        super(source);
        this.eventType = eventType;
        this.elementType = elementType;
        this.elementId = elementId;
    }

    /**
     * @return the eventType
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @return the elementType
     */
    public ElementType getElementType() {
        return elementType;
    }

    /**
     * @return the elementId
     */
    public String getElementId() {
        return elementId;
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return new StringBuffer("GraphEvent[")
            .append(this.eventType).append(" ")
            .append(this.elementType).append(" ")
            .append(this.elementId).append("]").toString();
    }

}
