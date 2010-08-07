/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author panisson
 */
public class FilterFactory {

    public Filter getFilter(ElementType elementType, String filterName, Map<String, Object> parameters) {
        Filter filter = null;

        if (filterName.equalsIgnoreCase("ALL")) {
            if (elementType.equals(ElementType.NODE)) {
                filter = new AbstractNodeFilter() {
                    @Override
                    public boolean evaluate(Graph graph, Node node) {
                        return true;
                    }
                };
            } else if (elementType.equals(ElementType.EDGE)) {
                filter = new AbstractEdgeFilter() {
                    @Override
                    public boolean evaluate(Graph graph, Edge edge) {
                        return true;
                    }
                };
            }
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
            return "NodeAttribute";
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
            return "AllEdges";
        }

        @Override
        public FilterProperty[] getProperties() {
            return null;
        }
    }

}
