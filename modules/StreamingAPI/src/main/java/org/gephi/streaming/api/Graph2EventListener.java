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

package org.gephi.streaming.api;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphDiff;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TableDiff;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEventBuilder;

/**
 *
 * @author panisson
 */
public class Graph2EventListener { // implements GraphListener, AttributeListener {

    private GraphEventHandler eventHandler;
    private GraphEventBuilder eventBuilder;
    private Graph graph;

    public Graph2EventListener(Graph graph, GraphEventHandler eventHandler) {
        this.graph = graph;
        eventBuilder = new GraphEventBuilder(this);
        this.eventHandler = eventHandler;
    }

    public void graphChanged(GraphDiff graphDiff) {
        
        for (Node node: graphDiff.getAddedNodes()) {
            String nodeId = node.getId().toString();
            org.gephi.streaming.api.event.GraphEvent e = 
                    eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, 
                    AttributeUtils.getNodeAttributes(node));
            eventHandler.handleGraphEvent(e);
        }
        
        for (Edge edge: graphDiff.getAddedEdges()) {
            String edgeId = edge.getId().toString();
            org.gephi.streaming.api.event.GraphEvent e = 
                    eventBuilder.edgeAddedEvent(edgeId, edge.getSource().getId().toString(),
                    edge.getTarget().getId().toString(), edge.isDirected(), 
                    AttributeUtils.getEdgeAttributes(edge));
            eventHandler.handleGraphEvent(e);
        }
        
        for (Edge edge: graphDiff.getRemovedEdges()) {
            String edgeId = edge.getId().toString();
            org.gephi.streaming.api.event.GraphEvent e = 
                    eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, edgeId, null);
            eventHandler.handleGraphEvent(e);
        }
        
        for (Node node: graphDiff.getRemovedNodes()) {
            String nodeId = node.getId().toString();
            org.gephi.streaming.api.event.GraphEvent e = 
                    eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, nodeId, null);
            eventHandler.handleGraphEvent(e);
        }
    }

    public void attributesChanged(TableDiff tableDiff) {
        //TODO: Reimplement this method, check diffs in columnObserver when available
        
//        switch (event.getEventType()) {
//        case ADD_COLUMN:
//            break;
//        case REMOVE_COLUMN:
//            break;
//        case SET_VALUE:
//
//            Map<String, List<AttributeValue>> nodeChangeTable = new HashMap<String, List<AttributeValue>>();
//            Map<String, List<AttributeValue>> edgeChangeTable = new HashMap<String, List<AttributeValue>>();
//            List<AttributeValue> graphChangeList = new ArrayList<AttributeValue>();
//
//            for (int i=0; i<event.getData().getTouchedObjects().length; i++) {
//                Object data = event.getData().getTouchedObjects()[i];
//                AttributeValue value = event.getData().getTouchedValues()[i];
//
//                String id;
//                if (data instanceof NodeData) {
//                    NodeData nodeData = (NodeData)data;
//                    id = nodeData.getId();
//                    if (graph.getNode(id)==null) {
//                        continue;
//                    }
//                    List<AttributeValue> values = nodeChangeTable.get(id);
//                    if (values==null) {
//                        values = new ArrayList<AttributeValue>();
//                        nodeChangeTable.put(id, values);
//                    }
//                    values.add(value);
//
//                } else if (data instanceof EdgeData) {
//                    EdgeData edgeData = (EdgeData)data;
//                    id = edgeData.getId();
//                    if (graph.getEdge(id)==null) {
//                        continue;
//                    }
//
//                    List<AttributeValue> values = edgeChangeTable.get(id);
//                    if (values==null) {
//                        values = new ArrayList<AttributeValue>();
//                        edgeChangeTable.put(id, values);
//                    }
//                    values.add(value);
//                } else if (data instanceof GraphView) {
//                    graphChangeList.add(value);
//                    
//                } else {
//                    throw new RuntimeException("Unrecognized graph object type");
//                }
//            }
//            
//            Map<String, Object> graphAttributes = new HashMap<String, Object>();
//            for (AttributeValue value: graphChangeList) {
//                graphAttributes.put(value.getColumn().getTitle(), value.getValue());
//            }
//            if (!graphAttributes.isEmpty()) {
//                org.gephi.streaming.api.event.GraphEvent streamingEvent =
//                        eventBuilder.graphEvent(ElementType.GRAPH, EventType.CHANGE, null, graphAttributes);
//                eventHandler.handleGraphEvent(streamingEvent);
//            }
//            
//            for (Map.Entry<String, List<AttributeValue>> entry: nodeChangeTable.entrySet()) {
//                Map<String, Object> attributes = new HashMap<String, Object>();
//                for (AttributeValue value: entry.getValue()) {
//                    if (value.getColumn().getIndex() != PropertiesColumn.NODE_ID.getIndex()
//                       && value.getColumn().getIndex() != PropertiesColumn.EDGE_ID.getIndex())
//                        attributes.put(value.getColumn().getTitle(), value.getValue());
//                }
//                
//                if (!attributes.isEmpty()) {
//                    org.gephi.streaming.api.event.GraphEvent streamingEvent =
//                            eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, entry.getKey(), attributes);
//                    eventHandler.handleGraphEvent(streamingEvent);
//                }
//            }
//
//            for (Map.Entry<String, List<AttributeValue>> entry: edgeChangeTable.entrySet()) {
//                Map<String, Object> attributes = new HashMap<String, Object>();
//                for (AttributeValue value: entry.getValue()) {
//                    if (value.getColumn().getIndex() != PropertiesColumn.NODE_ID.getIndex()
//                       && value.getColumn().getIndex() != PropertiesColumn.EDGE_ID.getIndex())
//                        attributes.put(value.getColumn().getTitle(), value.getValue());
//                }
//
//                if (!attributes.isEmpty()) {
//                    org.gephi.streaming.api.event.GraphEvent streamingEvent =
//                            eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, entry.getKey(), attributes);
//                    eventHandler.handleGraphEvent(streamingEvent);
//                }
//            }
//
//            break;
//        }
    }
}
