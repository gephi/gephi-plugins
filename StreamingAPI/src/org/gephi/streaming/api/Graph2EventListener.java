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

package org.gephi.streaming.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEventBuilder;

/**
 *
 * @author panisson
 */
public class Graph2EventListener implements GraphListener, AttributeListener {

    private GraphEventHandler eventHandler;
    private GraphEventBuilder eventBuilder;
    private Graph graph;
    private boolean sendVizData = true;

    public Graph2EventListener(Graph graph, GraphEventHandler eventHandler) {
        this.graph = graph;
        eventBuilder = new GraphEventBuilder(this);
        this.eventHandler = eventHandler;
    }

    @Override
    public void graphChanged(GraphEvent event) {

        switch (event.getEventType()) {
            case ADD_EDGES:
                for (Edge edge: event.getData().addedEdges()) {
                    String edgeId = edge.getEdgeData().getId();
                    eventHandler.handleGraphEvent(eventBuilder.edgeAddedEvent(edgeId, edge.getSource().getNodeData().getId(),
                            edge.getTarget().getNodeData().getId(), edge.isDirected(), getEdgeAttributes(edge)));
                }
            break;
            case ADD_NODES:
                for (Node node: event.getData().addedNodes()) {
                    String nodeId = node.getNodeData().getId();
                    eventHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, getNodeAttributes(node)));
                }
            break;
            case MOVE_NODES:
                for (Node node: event.getData().movedNodes()) {
                    String nodeId = node.getNodeData().getId();
                    eventHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, nodeId, getNodeAttributes(node)));
                }
                break;
            case REMOVE_EDGES:
                for (Edge edge: event.getData().removedEdges()) {
                    String edgeId = edge.getEdgeData().getId();
                    eventHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, edgeId, null));
                }
                break;
            case REMOVE_NODES:
                for (Node node: event.getData().removedNodes()) {
                    String nodeId = node.getNodeData().getId();
                    eventHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, nodeId, null));
                }
        }

    }

    @Override
    public void attributesChanged(AttributeEvent event) {
        switch (event.getEventType()) {
        case ADD_COLUMN:
            break;
        case REMOVE_COLUMN:
            break;
        case SET_VALUE:

            Map<String, List<AttributeValue>> nodeChangeTable = new HashMap<String, List<AttributeValue>>();
            Map<String, List<AttributeValue>> edgeChangeTable = new HashMap<String, List<AttributeValue>>();

            for (int i=0; i<event.getData().getTouchedObjects().length; i++) {
                Object data = event.getData().getTouchedObjects()[i];
                AttributeValue value = event.getData().getTouchedValues()[i];

                String id;
                if (data instanceof NodeData) {
                    NodeData nodeData = (NodeData)data;
                    id = nodeData.getId();
                    if (graph.getNode(id)==null) {
                        continue;
                    }
                    List<AttributeValue> values = nodeChangeTable.get(id);
                    if (values==null) {
                        values = new ArrayList<AttributeValue>();
                        nodeChangeTable.put(id, values);
                    }
                    values.add(value);

                } else if (data instanceof EdgeData) {
                    EdgeData edgeData = (EdgeData)data;
                    id = edgeData.getId();
                    if (graph.getEdge(id)==null) {
                        continue;
                    }

                    List<AttributeValue> values = edgeChangeTable.get(id);
                    if (values==null) {
                        values = new ArrayList<AttributeValue>();
                        edgeChangeTable.put(id, values);
                    }
                    values.add(value);
                } else {
                    throw new RuntimeException("Unrecognized graph object type");
                }
            }
            
            for (Map.Entry<String, List<AttributeValue>> entry: nodeChangeTable.entrySet()) {
                Map<String, Object> attributes = new HashMap<String, Object>();
                for (AttributeValue value: entry.getValue()) {
                    if (value.getColumn().getIndex() != PropertiesColumn.NODE_ID.getIndex()
                       && value.getColumn().getIndex() != PropertiesColumn.EDGE_ID.getIndex())
                        attributes.put(value.getColumn().getTitle(), value.getValue());
                }
                
                if (!attributes.isEmpty()) {
                    org.gephi.streaming.api.event.GraphEvent streamingEvent =
                            eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, entry.getKey(), attributes);
                    eventHandler.handleGraphEvent(streamingEvent);
                }
            }

            for (Map.Entry<String, List<AttributeValue>> entry: edgeChangeTable.entrySet()) {
                Map<String, Object> attributes = new HashMap<String, Object>();
                for (AttributeValue value: entry.getValue()) {
                    if (value.getColumn().getIndex() != PropertiesColumn.NODE_ID.getIndex()
                       && value.getColumn().getIndex() != PropertiesColumn.EDGE_ID.getIndex())
                        attributes.put(value.getColumn().getTitle(), value.getValue());
                }

                if (!attributes.isEmpty()) {
                    org.gephi.streaming.api.event.GraphEvent streamingEvent =
                            eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, entry.getKey(), attributes);
                    eventHandler.handleGraphEvent(streamingEvent);
                }
            }

            break;
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
