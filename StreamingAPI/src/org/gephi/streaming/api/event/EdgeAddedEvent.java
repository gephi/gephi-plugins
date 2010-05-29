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

/**
 * @author panisson
 *
 */
public final class EdgeAddedEvent extends GraphEvent{
    
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
     */
    public EdgeAddedEvent(Object source, String elementId,
            String sourceId, String targetId, boolean directed) {
        super(source, EventType.ADD, ElementType.EDGE, elementId);
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
}
