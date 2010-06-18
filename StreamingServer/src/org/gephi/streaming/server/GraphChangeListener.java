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

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
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
                operationSupport.nodeAdded(nodeId);
                
                AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                for (AttributeValue attributeValue: row.getValues()) {
                    operationSupport.nodeAttributeAdded(nodeId, attributeValue.getColumn().getTitle(), attributeValue.getValue());
                }
            }
            
            for (Edge edge: graph.getEdges()) {
                String edgeId = edge.getEdgeData().getId();
                String sourceId = edge.getSource().getNodeData().getId();
                String targetId = edge.getTarget().getNodeData().getId();
                operationSupport.edgeAdded(edgeId, sourceId, targetId, edge.isDirected());
                
                AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
                for (AttributeValue attributeValue: row.getValues()) {
                    operationSupport.edgeAttributeAdded(edgeId, attributeValue.getColumn().getTitle(), attributeValue.getValue());
                }
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
        System.out.println(event.getEventType());
        
//        try {
//            graph.readLock();
//            
//            for (Node node: graph.getNodes()) {
//                String nodeId = node.getNodeData().getId();
//                
//                Boolean fired = (Boolean)node.getNodeData().getAttributes().getValue("EVENT_FIRED");
//                
//                if(!fired) {
//                    operationSupport.nodeAdded(nodeId);
//                
//                    AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
//                    for (AttributeValue attributeValue: row.getValues()) {
//                        operationSupport.nodeAttributeAdded(nodeId, attributeValue.getColumn().getTitle(), attributeValue.getValue());
//                    }
//                } else {
//                    AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
//                    for (AttributeValue attributeValue: row.getValues()) {
//                        operationSupport.nodeAttributeChanged(nodeId, attributeValue.getColumn().getTitle(), attributeValue.getValue());
//                    }
//                }
//            }
//            
//            for (Edge edge: graph.getEdges()) {
//                String edgeId = edge.getEdgeData().getId();
//                String sourceId = edge.getSource().getNodeData().getId();
//                String targetId = edge.getTarget().getNodeData().getId();
//                operationSupport.edgeAdded(edgeId, sourceId, targetId, edge.isDirected());
//                
//                AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
//                for (AttributeValue attributeValue: row.getValues()) {
//                    operationSupport.edgeAttributeAdded(edgeId, attributeValue.getColumn().getTitle(), attributeValue.getValue());
//                }
//            }
//        } finally {
//            graph.readUnlock();
//        }
        
        switch (event.getEventType()) {
            case ADD_EDGES:
            break;
            case ADD_NODES:
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

}
