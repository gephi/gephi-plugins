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
