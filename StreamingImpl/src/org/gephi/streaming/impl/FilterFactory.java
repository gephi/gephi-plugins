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
                    Object attribute = node.getNodeData().getAttributes().getValue(attributeName);
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
                    Object attribute = edge.getEdgeData().getAttributes().getValue(attributeName);
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
