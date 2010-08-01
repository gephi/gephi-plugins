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

import java.util.Collections;
import java.util.EventObject;
import java.util.Map;

/**
 * The basic streaming graph event representation.
 * 
 * @author panisson
 *
 */
public class GraphEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    
    protected final EventType eventType;
    protected final ElementType elementType;
    protected String eventId;
    protected final Map<String, Object> attributes;

    /**
     * Constructs a graph Event.
     *
     * @param    source    The object on which the Event initially occurred.
     * @param eventType 
     * @param elementType
     * @exception  IllegalArgumentException  if source is null.
     */
    public GraphEvent(Object source, EventType eventType, 
            ElementType elementType, Map<String, Object> attributes) {
        super(source);
        this.eventType = eventType;
        this.elementType = elementType;
        this.attributes = attributes;
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
     * @return the eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @param eventId the eventId to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return new StringBuffer("GraphEvent[")
            .append(this.eventType).append(" ")
            .append(this.elementType).append("]").toString();
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null || obj.getClass() != this.getClass() ) return false;

        GraphEvent e = (GraphEvent)obj;
        return this.elementType == e.elementType
            && this.eventType == e.eventType;
    }

    @Override
    public int hashCode() {
        return this.elementType.hashCode() * 31 + this.eventType.hashCode();
    }

    /**
     * @return the node attributes
     */
    public Map<String, Object> getAttributes() {
        if (attributes==null) return null;
        return Collections.unmodifiableMap(attributes);
    }
}
