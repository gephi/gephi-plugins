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
package org.gephi.streaming.server;

import java.util.HashMap;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.OperationSupport;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class GraphChangeListener implements GraphListener, AttributeListener {

    private OperationSupport operationSupport;
    private Graph graph;
    private boolean sendVizData = true;
    
    public GraphChangeListener(Graph graph) {
        this.graph = graph;
        graph.getGraphModel().addGraphListener(this);
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel().getEdgeTable().addAttributeListener(this);
        ac.getModel().getNodeTable().addAttributeListener(this);
    }

    /**
     * @return the operationSupport
     */
    public OperationSupport getOperationSupport() {
        return operationSupport;
    }
    
    public void writeGraph(OperationSupport operationSupport) {
        
        try {
            graph.readLock();
            
            for (Node node: graph.getNodes()) {
                String nodeId = node.getNodeData().getId();
                operationSupport.nodeAdded(nodeId, getNodeAttributes(node));
            }
            
            for (Edge edge: graph.getEdges()) {
                String edgeId = edge.getEdgeData().getId();
                String sourceId = edge.getSource().getNodeData().getId();
                String targetId = edge.getTarget().getNodeData().getId();
                operationSupport.edgeAdded(edgeId, sourceId, targetId, edge.isDirected(), getEdgeAttributes(edge));
            }
        } finally {
            graph.readUnlock();
        }
        
        
    }
    
    /**
     * @param operationSupport the operationSupport to set
     */
    public void setOperationSupport(OperationSupport operationSupport) {
        this.operationSupport = operationSupport;
    }

    @Override
    public void graphChanged(GraphEvent event) {
        
        switch (event.getEventType()) {
            case ADD_EDGES:
                for (Edge edge: event.getData().addedEdges()) {
                    String edgeId = edge.getEdgeData().getId();
                    operationSupport.edgeAdded(edgeId, edge.getSource().getNodeData().getId(), 
                            edge.getTarget().getNodeData().getId(), edge.isDirected(), null);
                    
                    AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
                    for (AttributeValue attributeValue: row.getValues()) {
                        if (attributeValue.getColumn().getIndex()!=PropertiesColumn.EDGE_ID.getIndex()
                            && attributeValue.getValue() != null && !"".equals(attributeValue.getValue()))
                            operationSupport.edgeAttributeAdded(edgeId, attributeValue.getColumn().getTitle().toLowerCase(), attributeValue.getValue());
                    }
                }
            break;
            case ADD_NODES:
                for (Node node: event.getData().addedNodes()) {
                    String nodeId = node.getNodeData().getId();
                    operationSupport.nodeAdded(nodeId, null);

                    AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                    for (AttributeValue attributeValue: row.getValues()) {
                        if (attributeValue.getColumn().getIndex()!=PropertiesColumn.NODE_ID.getIndex()
                            && attributeValue.getValue() != null && !"".equals(attributeValue.getValue()))
                            operationSupport.nodeAttributeAdded(nodeId, attributeValue.getColumn().getTitle().toLowerCase(), attributeValue.getValue());
                    }
                }
            break;
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
