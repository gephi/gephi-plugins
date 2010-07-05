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
public class FilterEvent extends GraphEvent {
    
    private static final long serialVersionUID = 1L;
    
    protected final Filter filter;

    /**
     * @param source
     * @param eventType
     * @param elementType
     * @param filter 
     */
    public FilterEvent(Object source, EventType eventType,
            ElementType elementType, Filter filter) {
        super(source, eventType, elementType);
        this.filter = filter;
    }

    /**
     * @return the filter
     */
    public Filter getFilter() {
        return filter;
    }
    
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) return true;
		if ( obj == null || obj.getClass() != this.getClass() ) return false;
		
		FilterEvent e = (FilterEvent)obj;
		return this.elementType == e.elementType
			&& this.eventType == e.eventType
			&& this.filter.equals(e.filter);
	}

	@Override
	public int hashCode() {
		return elementType.hashCode() * 31 + eventType.hashCode();
	}

}
