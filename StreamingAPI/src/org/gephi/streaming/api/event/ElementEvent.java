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

import java.util.Collections;
import java.util.Map;

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
     * @param attributes 
     */
    public ElementEvent(Object source, EventType eventType,
            ElementType elementType, String elementId, Map<String, Object> attributes) {
        super(source, eventType, elementType, attributes);
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
            .append(this.elementId).append(": ")
            .append(this.attributes).append("]").toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null || obj.getClass() != this.getClass() ) return false;

        ElementEvent e = (ElementEvent)obj;
        return this.elementType == e.elementType
            && this.eventType == e.eventType
            && this.elementId.equals(e.elementId);
    }

    @Override
    public int hashCode() {
        return (elementType.hashCode() * 31 + eventType.hashCode()) * 31 
                + ((elementId!=null)?elementId.hashCode():0);
    }

}
