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
package org.gephi.streaming.server.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;

/**
 * @author panisson
 *
 */
public class DynamicGraphWriter extends GraphWriter {
    
    private Graph graph;
    private boolean sendVizData;
    private GraphEventBuilder eventBuilder;

    public DynamicGraphWriter(Graph graph, boolean sendVizData) {
        super(graph, sendVizData);
        this.graph = graph;
        this.sendVizData = sendVizData;
        eventBuilder = new GraphEventBuilder(this);
    }
    
    @Override
    public void writeGraph(GraphEventHandler operationSupport) {

        try {
            graph.readLock();

            double minRange = Double.MAX_VALUE;
            double maxRange = Double.MIN_VALUE;
            Map<Double, List<Object>> creation = new HashMap<Double, List<Object>>();
            Map<Double, List<Object>> remotion = new HashMap<Double, List<Object>>();
            
            for (Node node: graph.getNodes()) {
                TimeInterval range = (TimeInterval)node.getNodeData().getAttributes().getValue("dynamicrange");

                List<Double[]> values = range.getValues();
                for(Double[] rangeValue: values) {
                    double created = rangeValue[0];
                    double removed = rangeValue[1];

                    if (created < minRange) {
                        minRange = created;
                    }
                    if (removed > maxRange) {
                        maxRange = removed;
                    }

                     List<Object> createdAt = creation.get(created);
                     if (createdAt == null) {
                         createdAt = new ArrayList<Object>();
                         creation.put(created, createdAt);
                     }
                     createdAt.add(node);

                     List<Object> removedAt = remotion.get(removed);
                     if (removedAt == null) {
                         removedAt = new ArrayList<Object>();
                         remotion.put(removed, removedAt);
                     }
                     removedAt.add(node);
                }
            }

            for (Edge edge: graph.getEdges()) {
                TimeInterval range = (TimeInterval)edge.getEdgeData().getAttributes().getValue("dynamicrange");

                List<Double[]> values = range.getValues();
                for(Double[] rangeValue: values) {
                    double created = rangeValue[0];
                    double removed = rangeValue[1];

                    if (created < minRange) {
                        minRange = created;
                    }
                    if (removed > maxRange) {
                        maxRange = removed;
                    }

                     List<Object> createdAt = creation.get(created);
                     if (createdAt == null) {
                         createdAt = new ArrayList<Object>();
                         creation.put(created, createdAt);
                     }
                     createdAt.add(edge);

                     List<Object> removedAt = remotion.get(removed);
                     if (removedAt == null) {
                         removedAt = new ArrayList<Object>();
                         remotion.put(removed, removedAt);
                     }
                     removedAt.add(0,edge);
                }
            }

            TreeSet<Double> ordered = new TreeSet<Double>();
            ordered.addAll(creation.keySet());
            ordered.addAll(remotion.keySet());

            for (Double ts: ordered) {
                List<Object> createdAt = creation.get(ts);
                if (createdAt!=null) {
                    for (Object o: createdAt) {
                        if (o instanceof Node) {
                            Node node = (Node)o;
                            String nodeId = node.getNodeData().getId();
                            Map<String, Object> attributes = getNodeAttributes(node);
                            attributes.put("start", ts);
                            operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, attributes));
                        }
                        if (o instanceof Edge) {
                            Edge edge = (Edge)o;
                            String edgeId = edge.getEdgeData().getId();
                            Map<String, Object> attributes = getEdgeAttributes(edge);
                            attributes.put("start", ts);
                            String sourceId = edge.getSource().getNodeData().getId();
                            String targetId = edge.getTarget().getNodeData().getId();

                            operationSupport.handleGraphEvent(eventBuilder.edgeAddedEvent(edgeId, sourceId, targetId, edge.isDirected(), attributes));
                        }
                    }
                }

                List<Object> removedAt = remotion.get(ts);
                if (removedAt!=null) {
                    for (Object o: removedAt) {
                        if (o instanceof Edge) {
                            Edge edge = (Edge)o;
                            String edgeId = edge.getEdgeData().getId();
                            operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, edgeId, null));
                        }
                        if (o instanceof Node) {
                            Node node = (Node)o;
                            String nodeId = node.getNodeData().getId();
                            operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, nodeId, null));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graph.readUnlock();
        }
    }

    private Map<String, Object> getNodeAttributes(Node node) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();

        if (row != null)
            for (AttributeValue attributeValue: row.getValues()) {
                if (attributeValue.getColumn().getIndex()!=PropertiesColumn.NODE_ID.getIndex()
                        && attributeValue.getValue()!=null)
                    attributes.put(attributeValue.getColumn().getTitle(), attributeValue.getValue());
            }

        if (sendVizData) {
            attributes.put("x", node.getNodeData().x());
            attributes.put("y", node.getNodeData().y());
            attributes.put("z", node.getNodeData().z());

            attributes.put("r", node.getNodeData().r());
            attributes.put("g", node.getNodeData().g());
            attributes.put("b", node.getNodeData().b());

            attributes.put("size", node.getNodeData().getSize());
        }

        return attributes;
    }

    private Map<String, Object> getEdgeAttributes(Edge edge) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
        if (row != null)
            for (AttributeValue attributeValue: row.getValues()) {
                if (attributeValue.getColumn().getIndex()!=PropertiesColumn.EDGE_ID.getIndex()
                        && attributeValue.getValue()!=null)
                     attributes.put(attributeValue.getColumn().getTitle(), attributeValue.getValue());
            }

        if (sendVizData) {
            
            attributes.put("r", edge.getEdgeData().r());
            attributes.put("g", edge.getEdgeData().g());
            attributes.put("b", edge.getEdgeData().b());
            
            attributes.put("weight", edge.getWeight());
        }

        return attributes;
    }

}
