/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
    protected Double timestamp = null;
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

    public void setTimestamp(Double timestamp) {
        this.timestamp = timestamp;
    }
    
    public Double getTimestamp() {
        return this.timestamp;
    }
}
