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
