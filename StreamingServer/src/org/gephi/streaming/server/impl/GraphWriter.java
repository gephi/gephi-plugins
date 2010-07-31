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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.CompositeGraphEventHandler;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;

/**
 * @author panisson
 *
 */
public class GraphWriter extends CompositeGraphEventHandler {
    
    private Graph graph;
    private boolean sendVizData;
    private final GraphEventBuilder eventBuilder;

    public GraphWriter(Graph graph, boolean sendVizData) {
        this.graph = graph;
        this.sendVizData = sendVizData;
        eventBuilder = new GraphEventBuilder(this);
    }
    
    public void writeGraph(GraphEventHandler operationSupport) {

        Set<String> writtenNodes = new HashSet<String>();
        Set<String> writtenEdges = new HashSet<String>();
        
        try {
            graph.readLock();
            
            for (Node node: graph.getNodes()) {
                String nodeId = node.getNodeData().getId();
                operationSupport.handleGraphEvent(
                        eventBuilder.graphEvent(ElementType.NODE,
                        EventType.ADD, nodeId, getNodeAttributes(node)));
                writtenNodes.add(nodeId);

                for (Edge edge: graph.getEdges(node)) {
                    String edgeId = edge.getEdgeData().getId();
                    if (writtenEdges.contains(edgeId)) continue;
                    String sourceId = edge.getSource().getNodeData().getId();
                    String targetId = edge.getTarget().getNodeData().getId();
                    if (writtenNodes.contains(sourceId)
                        && writtenNodes.contains(targetId) ) {
                        operationSupport.handleGraphEvent(
                                eventBuilder.edgeAddedEvent(edgeId, sourceId,
                                targetId, edge.isDirected(), getEdgeAttributes(edge)));
                        writtenEdges.add(edgeId);
                    }
                }

            }
            
//            for (Edge edge: graph.getEdges()) {
//                String edgeId = edge.getEdgeData().getId();
//                String sourceId = edge.getSource().getNodeData().getId();
//                String targetId = edge.getTarget().getNodeData().getId();
//                operationSupport.handleGraphEvent(
//                        eventBuilder.edgeAddedEvent(edgeId, sourceId,
//                        targetId, edge.isDirected(), getEdgeAttributes(edge)));
//            }

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
