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

import java.util.Map;

/**
 * @author panisson
 *
 */
public final class EdgeAddedEvent extends ElementEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final String sourceId;
    private final String targetId;
    private final boolean directed;

    /**
     * @param source
     * @param elementId
     * @param sourceId
     * @param targetId
     * @param directed 
     * @param attributes 
     */
    public EdgeAddedEvent(Object source, String elementId,
            String sourceId, String targetId, boolean directed, Map<String, Object> attributes) {
        super(source, EventType.ADD, ElementType.EDGE, elementId, attributes);
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.directed = directed;
    }

    /**
     * @return the sourceId
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * @return the targetId
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * @return the directed
     */
    public boolean isDirected() {
        return directed;
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null || obj.getClass() != this.getClass() ) return false;

        EdgeAddedEvent e = (EdgeAddedEvent)obj;
        return this.elementType == e.elementType
            && this.eventType == e.eventType
            && this.elementId.equals(e.elementId)
            && this.sourceId.equals(e.sourceId)
            && this.targetId.equals(e.targetId)
            && this.directed == e.directed;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + elementType.hashCode();
        hash = hash * 31 + eventType.hashCode();
        hash = hash * 31 + elementId.hashCode();
        hash = hash * 31 + sourceId.hashCode();
        hash = hash * 31 + targetId.hashCode();
        hash = hash << 1 + (directed?1:0);
        return hash;
    }
}
