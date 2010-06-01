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
public class ElementEvent extends GraphEvent {
    
    private static final long serialVersionUID = 1L;
    
    protected final String elementId;

    /**
     * @param source
     * @param eventType
     * @param elementType
     * @param elementId
     */
    public ElementEvent(Object source, EventType eventType,
            ElementType elementType, String elementId) {
        super(source, eventType, elementType);
        this.elementId = elementId;
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
        return new StringBuffer("ElementEvent[")
            .append(this.eventType).append(" ")
            .append(this.elementType).append(" ")
            .append(this.elementId).append("]").toString();
    }

}
