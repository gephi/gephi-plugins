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

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.CompositeOperationSupport;
import org.gephi.streaming.api.GraphUpdaterOperationSupport;
import org.gephi.streaming.api.OperationSupport;

/**
 * @author panisson
 *
 */
public class GraphBufferedOperationSupport extends CompositeOperationSupport {
    
    private Graph graph;
    private GraphUpdaterOperationSupport updater;

    public GraphBufferedOperationSupport(Graph graph) {
        this.graph = graph;
        this.updater = new GraphUpdaterOperationSupport(graph);
    }
    
    @Override
    public void addOperationSupport(OperationSupport operationSupport) {
        writeGraph(operationSupport);
        super.addOperationSupport(operationSupport);
    }

    @Override
    public void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed) {
        updater.edgeAdded(edgeId, fromNodeId, toNodeId, directed);
        super.edgeAdded(edgeId, fromNodeId, toNodeId, directed);
    }

    @Override
    public void edgeAttributeAdded(String edgeId, String attributeName,
            Object value) {
        updater.edgeAttributeAdded(edgeId, attributeName, value);
        super.edgeAttributeAdded(edgeId, attributeName, value);
    }

    @Override
    public void edgeAttributeChanged(String edgeId, String attributeName,
            Object newValue) {
        updater.edgeAttributeChanged(edgeId, attributeName, newValue);
        super.edgeAttributeChanged(edgeId, attributeName, newValue);
    }

    @Override
    public void edgeAttributeRemoved(String edgeId, String attributeName) {
        updater.edgeAttributeRemoved(edgeId, attributeName);
        super.edgeAttributeRemoved(edgeId, attributeName);
    }

    @Override
    public void edgeRemoved(String edgeId) {
        updater.edgeRemoved(edgeId);
        super.edgeRemoved(edgeId);
    }

    @Override
    public void graphAttributeAdded(String attributeName, Object value) {
        updater.graphAttributeAdded(attributeName, value);
        super.graphAttributeAdded(attributeName, value);
    }

    @Override
    public void graphAttributeChanged(String attributeName, Object newValue) {
        updater.graphAttributeChanged(attributeName, newValue);
        super.graphAttributeChanged(attributeName, newValue);
    }

    @Override
    public void graphAttributeRemoved(String attributeName) {
        updater.graphAttributeRemoved(attributeName);
        super.graphAttributeRemoved(attributeName);
    }

    @Override
    public void nodeAdded(String nodeId) {
        updater.nodeAdded(nodeId);
        super.nodeAdded(nodeId);
    }

    @Override
    public void nodeAttributeAdded(String nodeId, String attributeName,
            Object value) {
        updater.nodeAttributeAdded(nodeId, attributeName, value);
        super.nodeAttributeAdded(nodeId, attributeName, value);
    }

    @Override
    public void nodeAttributeChanged(String nodeId, String attributeName,
            Object newValue) {
        updater.nodeAttributeChanged(nodeId, attributeName, newValue);
        super.nodeAttributeChanged(nodeId, attributeName, newValue);
    }

    @Override
    public void nodeAttributeRemoved(String nodeId, String attributeName) {
        updater.nodeAttributeRemoved(nodeId, attributeName);
        super.nodeAttributeRemoved(nodeId, attributeName);
    }

    @Override
    public void nodeRemoved(String nodeId) {
        updater.nodeRemoved(nodeId);
        super.nodeRemoved(nodeId);
    }
    
    private void writeGraph(OperationSupport operationSupport) {
        
        try {
            graph.readLock();
            
            for (Node node: graph.getNodes()) {
                String nodeId = node.getNodeData().getId();
                operationSupport.nodeAdded(nodeId);
                
                AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();

                if (row != null)
                    for (AttributeValue attributeValue: row.getValues()) {
                        if (attributeValue.getColumn().getIndex()!=PropertiesColumn.NODE_ID.getIndex())
                            operationSupport.nodeAttributeAdded(nodeId, attributeValue.getColumn().getTitle(), attributeValue.getValue());
                    }
            }
            
            for (Edge edge: graph.getEdges()) {
                String edgeId = edge.getEdgeData().getId();
                String sourceId = edge.getSource().getNodeData().getId();
                String targetId = edge.getTarget().getNodeData().getId();
                operationSupport.edgeAdded(edgeId, sourceId, targetId, edge.isDirected());
                
                AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
                if (row != null)
                    for (AttributeValue attributeValue: row.getValues()) {
                        if (attributeValue.getColumn().getIndex()!=PropertiesColumn.EDGE_ID.getIndex())
                            operationSupport.edgeAttributeAdded(edgeId, attributeValue.getColumn().getTitle(), attributeValue.getValue());
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graph.readUnlock();
        }
    }

}
