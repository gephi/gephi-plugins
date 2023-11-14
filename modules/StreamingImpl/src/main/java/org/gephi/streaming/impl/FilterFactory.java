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

package org.gephi.streaming.impl;

import java.util.Map;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.event.ElementType;

/**
 * A factory to create Filter objects
 *
 * @author panisson
 */
public class FilterFactory {

    /**
     * Create a new Filter object. The filter type depends on the element type,
     * if a node
     *
     * @param elementType
     * @param filterName
     * @param parameters
     * @return
     */
    public Filter getFilter(ElementType elementType, String filterName, Map<String, Object> parameters) {
        Filter filter = null;

        if (elementType.equals(ElementType.NODE)) {
            filter = getNodeFilter(filterName, parameters);
        } else if (elementType.equals(ElementType.EDGE)) {
            filter = getEdgeFilter(filterName, parameters);
        }

        return filter;
    }

    /**
     * Create a new NodeFilter object.
     *
     * @param filterName the filter's name
     * @param parameters the parameters to create the filter
     * @return a new NodeFilter object
     */
    public NodeFilter getNodeFilter(String filterName, Map<String, Object> parameters) {
        NodeFilter filter = null;

        if (filterName.equalsIgnoreCase("ALL")) {
            filter = new AbstractNodeFilter() {
                @Override
                public boolean evaluate(Graph graph, Node node) {
                    return true;
                }
            };
        }

        if (filterName.equalsIgnoreCase("NodeAttribute")) {
            final String attributeName = (String)parameters.get("attribute");
            final String attributeValue = parameters.get("value").toString();
            filter = new AbstractNodeFilter() {
                @Override
                public boolean evaluate(Graph graph, Node node) {
                    Object attribute = node.getAttribute(attributeName);
                    if (attribute==null) {
                        if (attributeValue==null) return true;
                        else return false;
                    }
                    String value = attribute.toString();
                    return attributeValue.equals(value);
                }
            };
        }

        return filter;
    }

    /**
     * Create a new EdgeFilter object.
     *
     * @param filterName the filter's name
     * @param parameters the parameters to create the filter
     * @return a new EdgeFilter object
     */
    public EdgeFilter getEdgeFilter(String filterName, Map<String, Object> parameters) {
        EdgeFilter filter = null;

        if (filterName.equalsIgnoreCase("ALL")) {
            filter = new AbstractEdgeFilter() {
                @Override
                public boolean evaluate(Graph graph, Edge edge) {
                    return true;
                }
            };
        }

        if (filterName.equalsIgnoreCase("EdgeAttribute")) {
            final String attributeName = (String)parameters.get("attribute");
            final String attributeValue = parameters.get("value").toString();
            filter = new AbstractEdgeFilter() {
                @Override
                public boolean evaluate(Graph graph, Edge edge) {
                    
                    Object attribute = edge.getAttribute(attributeName);
                    if (attribute==null) {
                        if (attributeValue==null) return true;
                        else return false;
                    }
                    String value = attribute.toString();
                    return attributeValue.equals(value);
                }
            };
        }

        return filter;
    }

    private abstract class AbstractNodeFilter implements NodeFilter {
        @Override
        public boolean init(Graph graph) {
            return true;
        }

        @Override
        public void finish() {
        }

        @Override
        public String getName() {
            return "NodeFilter";
        }

        @Override
        public FilterProperty[] getProperties() {
            return null;
        }
    }

    private abstract class AbstractEdgeFilter implements EdgeFilter {

        @Override
        public boolean init(Graph graph) {
            return true;
        }

        @Override
        public void finish() {
        }

        @Override
        public String getName() {
            return "EdgeFilter";
        }

        @Override
        public FilterProperty[] getProperties() {
            return null;
        }
    }
}
