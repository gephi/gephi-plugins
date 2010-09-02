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

import java.util.HashMap;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeController;
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
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class GraphChangeListener implements GraphListener, AttributeListener {

    private GraphEventHandler operationSupport;
    private Graph graph;
    private boolean sendVizData = true;
    private GraphEventBuilder eventBuilder;
    private GraphWriter graphWriter;
    
    public GraphChangeListener(Graph graph) {
        this.graph = graph;
        graph.getGraphModel().addGraphListener(this);
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel().addAttributeListener(this);
        eventBuilder = new GraphEventBuilder(this);
         graphWriter = new GraphWriter(graph, true);
    }

    /**
     * @return the operationSupport
     */
    public GraphEventHandler getOperationSupport() {
        return operationSupport;
    }
    
    public void writeGraph(GraphEventHandler operationSupport) {

        graphWriter.writeGraph(operationSupport);
    }
    
    /**
     * @param operationSupport the operationSupport to set
     */
    public void setOperationSupport(GraphEventHandler operationSupport) {
        this.operationSupport = operationSupport;
    }

    @Override
    public void graphChanged(GraphEvent event) {
        
        switch (event.getEventType()) {
            case ADD_EDGES:
                for (Edge edge: event.getData().addedEdges()) {
                    String edgeId = edge.getEdgeData().getId();
                    operationSupport.handleGraphEvent(eventBuilder.edgeAddedEvent(edgeId, edge.getSource().getNodeData().getId(),
                            edge.getTarget().getNodeData().getId(), edge.isDirected(), getEdgeAttributes(edge)));
                }
            break;
            case ADD_NODES:
                for (Node node: event.getData().addedNodes()) {
                    String nodeId = node.getNodeData().getId();
                    operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, getNodeAttributes(node)));
                }
            break;
            case MOVE_NODES:
                for (Node node: event.getData().movedNodes()) {
                    String nodeId = node.getNodeData().getId();
                    operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, nodeId, getNodeAttributes(node)));
                }
                break;
            case REMOVE_EDGES:
                for (Edge edge: event.getData().removedEdges()) {
                    String edgeId = edge.getEdgeData().getId();
                    operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, edgeId, null));
                }
                break;
            case REMOVE_NODES:
                for (Node node: event.getData().removedNodes()) {
                    String nodeId = node.getNodeData().getId();
                    operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, nodeId, null));
                }
        }
        
    }

    @Override
    public void attributesChanged(AttributeEvent event) {
        System.out.println(event.getEventType());
        switch (event.getEventType()) {
        case ADD_COLUMN:
            break;
        case REMOVE_COLUMN:
            break;
        case SET_VALUE:
            for (Object data: event.getData().getTouchedObjects()) {
                ElementType elementType;
                String id;
                if (data instanceof NodeData) {
                    elementType = ElementType.NODE;
                    NodeData nodeData = (NodeData)data;
                    id = nodeData.getId();

                } else if (data instanceof EdgeData) {
                    elementType = ElementType.EDGE;
                    EdgeData edgeData = (EdgeData)data;
                    id = edgeData.getId();
                } else {
                    throw new RuntimeException("Unrecognized graph object type");
                }

                Map<String, Object> attributes = new HashMap<String, Object>();
                for (AttributeValue value: event.getData().getTouchedValues()) {
                    attributes.put(value.getColumn().getTitle(), value.getValue());
                }

                org.gephi.streaming.api.event.GraphEvent streamingEvent =
                        eventBuilder.graphEvent(elementType, EventType.CHANGE, id, attributes);
                operationSupport.handleGraphEvent(streamingEvent);
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
