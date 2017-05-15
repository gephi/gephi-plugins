/*
Copyright 2008-2016 Gephi
Authors : Andre Panisson <panisson@gmail.com>
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

Portions Copyrighted 2016 Gephi Consortium.
 */
package org.gephi.streaming.api;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * Utility methods for retrieving node/edge attributes.
 *
 * @author panisson
 */
public class AttributeUtils {
    private static final boolean sendVizData = true;

    public static Map<String, Object> getNodeAttributes(Node node) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        for (Column column : node.getAttributeColumns()) {
            if (!column.isProperty()) {
                Object value = node.getAttribute(column);
                if (value != null) {
                    attributes.put(column.getTitle(), value);
                }
            }
        }
        if (sendVizData) {
            attributes.put("x", node.x());
            attributes.put("y", node.y());
            attributes.put("z", node.z());
            attributes.put("r", node.r());
            attributes.put("g", node.g());
            attributes.put("b", node.b());
            attributes.put("size", node.size());
        }
        return attributes;
    }

    public static Map<String, Object> getEdgeAttributes(Edge edge) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        for (Column column : edge.getAttributeColumns()) {
            if (!column.isProperty()) {
                Object value = edge.getAttribute(column);
                if (value != null) {
                    attributes.put(column.getTitle(), value);
                }
            }
        }
        if (sendVizData) {
            attributes.put("r", edge.r());
            attributes.put("g", edge.g());
            attributes.put("b", edge.b());
            attributes.put("weight", edge.getWeight());
        }
        return attributes;
    }
    
}
