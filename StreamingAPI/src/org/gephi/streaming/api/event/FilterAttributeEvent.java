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

import org.gephi.filters.spi.Filter;

/**
 * @author panisson
 *
 */
public class FilterAttributeEvent extends FilterEvent {
    
    private static final long serialVersionUID = 1L;
    
    private final String attributeName;
    
    private final Object attributeValue;

    /**
     * @param source
     * @param eventType
     * @param elementType
     * @param filter 
     * @param attributeName 
     * @param attributeValue 
     */
    public FilterAttributeEvent(Object source, EventType eventType,
            ElementType elementType, Filter filter,
            String attributeName, Object attributeValue) {
        super(source, eventType, elementType, filter);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    /**
     * @return the attributeColumn
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @return the attributeValue
     */
    public Object getAttributeValue() {
        return attributeValue;
    }
    
    @Override
    public String toString() {
        return new StringBuffer("FilterAttributeEvent[")
            .append(this.getEventType()).append(" Attribute ")
            .append(this.attributeName).append(" on ")
            .append(this.getElementType()).append("]").toString();
    }
    
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) return true;
		if ( obj == null || obj.getClass() != this.getClass() ) return false;
		
		FilterAttributeEvent e = (FilterAttributeEvent)obj;
		return this.elementType == e.elementType
			&& this.eventType == e.eventType
			&& this.filter.equals(e.filter)
			&& this.attributeName.equals(e.attributeName)
			&& (this.attributeValue==null)?e.attributeValue==null:this.attributeValue.equals(e.attributeValue);
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + elementType.hashCode();
		hash = hash * 31 + eventType.hashCode();
		hash = hash * 31 + attributeName.hashCode();
		hash = hash * 31 + (attributeValue == null ? 0 : attributeValue.hashCode());
		return hash;
	}

}
