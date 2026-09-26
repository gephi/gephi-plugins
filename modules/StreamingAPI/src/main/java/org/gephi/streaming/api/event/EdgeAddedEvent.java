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
