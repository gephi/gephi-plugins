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

import org.gephi.data.attributes.api.AttributeColumn;

/**
 * @author panisson
 *
 */
public class AttributeEvent extends GraphEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final AttributeColumn attributeColumn;
    
    private final Object attributeValue;

    /**
     * @param source
     * @param eventType
     * @param elementType
     * @param elementId
     * @param attributeColumn 
     * @param attributeValue 
     */
    public AttributeEvent(Object source, EventType eventType,
            ElementType elementType, String elementId,
            AttributeColumn attributeColumn, Object attributeValue) {
        super(source, eventType, elementType, elementId);
        this.attributeColumn = attributeColumn;
        this.attributeValue = attributeValue;
    }

    /**
     * @return the attributeColumn
     */
    public AttributeColumn getAttributeColumn() {
        return attributeColumn;
    }

    /**
     * @return the attributeValue
     */
    public Object getAttributeValue() {
        return attributeValue;
    }
    
    @Override
    public String toString() {
        return new StringBuffer("AttributeEvent[")
            .append(this.getEventType()).append(" Attribute ")
            .append(this.attributeColumn).append(" on ")
            .append(this.getElementType()).append(" ")
            .append(this.getElementId()).append("]").toString();
    }

}
