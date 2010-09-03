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
package org.gephi.streaming.api;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.NodeFilter;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.PropertiesAssociations.EdgeProperties;
import org.gephi.streaming.api.PropertiesAssociations.NodeProperties;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.FilterEvent;
import org.gephi.streaming.api.event.GraphEvent;

/**
 * This is a GraphEventHandler implementation used to update a graph
 * when an event is received.
 *
 * @author panisson
 *
 */
public class GraphUpdaterEventHandler implements GraphEventHandler {

    private static final Logger logger = Logger.getLogger(GraphUpdaterEventHandler.class.getName());

    private final Graph graph;
    private final GraphFactory factory;
    protected PropertiesAssociations properties = new PropertiesAssociations();
    protected Report report;
    
    /**
     * This is used to create a new GraphEventHandler that will update a graph
     * when an event is received.
     *
     * @param graph - the Graph to be updated
     */
    public GraphUpdaterEventHandler(Graph graph) {
        this.graph = graph;
        this.factory = graph.getGraphModel().factory();
        
        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.ID, "id");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.Z, "z");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");
        properties.addNodePropertyAssociation(NodeProperties.R, "r");
        properties.addNodePropertyAssociation(NodeProperties.G, "g");
        properties.addNodePropertyAssociation(NodeProperties.B, "b");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.SOURCE, "source");
        properties.addEdgePropertyAssociation(EdgeProperties.TARGET, "target");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    /**
     * @param report the report to set
     */
    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public void handleGraphEvent(GraphEvent event) {
        try {
            doHandleGraphEvent(event);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            if (report!=null) {
                Issue issue = new Issue(e, Issue.Level.WARNING);
                report.logIssue(issue);
            }
        }
    }

    private void doHandleGraphEvent(GraphEvent event) {

        if (event instanceof ElementEvent) {
            ElementEvent elementEvent = (ElementEvent)event;

            switch (event.getElementType()) {
            case NODE:
                switch (event.getEventType()) {
                    case ADD:
                        this.nodeAdded(elementEvent.getElementId(), elementEvent.getAttributes());
                        break;
                    case CHANGE:
                        this.nodeChanged(elementEvent.getElementId(), elementEvent.getAttributes());
                        break;
                    case REMOVE:
                        this.nodeRemoved(elementEvent.getElementId());
                        break;
                }
                break;
            case EDGE:
                switch (event.getEventType()) {
                    case ADD:
                        EdgeAddedEvent eaEvent = (EdgeAddedEvent)event;
                        this.edgeAdded(elementEvent.getElementId(), eaEvent.getSourceId(),
                                eaEvent.getTargetId(), eaEvent.isDirected(),
                                elementEvent.getAttributes());
                        break;
                    case CHANGE:
                        this.edgeChanged(elementEvent.getElementId(),
                                elementEvent.getAttributes());
                        break;
                    case REMOVE:
                        this.edgeRemoved(elementEvent.getElementId());
                        break;
                }
                break;
            }
        } else if (event instanceof FilterEvent) {
            FilterEvent filterEvent = (FilterEvent)event;
            applyFilter(filterEvent);
        }
    }

    private void log(String message) {
        if (report!=null) {
            Issue issue = new Issue(message, Issue.Level.INFO);
            report.logIssue(issue);
        }
        logger.warning(message);
    }

    private void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        
        Edge edge = graph.getEdge(edgeId);
        if (edge!=null) {
            log("Edge added event ignored for edge "+edgeId+": Edge already exists");
            return;
        }
        
        Node source = graph.getNode(fromNodeId);
        if (source==null) {
            log("Edge added event ignored for edge "+edgeId+": Source node "+fromNodeId+" not found");
            return;
        }

        Node target = graph.getNode(toNodeId);
        if (target==null) {
            log("Edge added event ignored for edge "+edgeId+": Target node "+toNodeId+" not found");
            return;
        }

        if(source!=null && target!=null) {
            edge = factory.newEdge(edgeId, source, target, 1.0f, directed);
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addEdgeAttribute(edge, entry.getKey(), entry.getValue());
                }
            }
            
            graph.writeLock();
            graph.addEdge(edge);
            graph.setId(edge, edgeId);
            graph.writeUnlock();
        }
    }
    
    private void edgeChanged(String edgeId, Map<String, Object> attributes) {
        Edge edge = graph.getEdge(edgeId);
        if (edge!=null) {
            
            graph.writeLock();
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addEdgeAttribute(edge, entry.getKey(), entry.getValue());
                }
            }
            
            graph.writeUnlock();
        } else {
            log("Edge changed event ignored for edge "+edgeId+": Edge not found");
        }
    }

    private void edgeRemoved(String edgeId) {
        Edge edge = graph.getEdge(edgeId);
        if (edge!=null) {
            graph.writeLock();
            graph.removeEdge(edge);
            graph.writeUnlock();
        } else {
            log("Edge removed event ignored for edge "+edgeId+": Edge not found");
        }
    }

    private void nodeAdded(String nodeId, Map<String, Object> attributes) {
        Node node = graph.getNode(nodeId);
        if (node==null) {
            node = factory.newNode(nodeId);
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                }
            }
            
            graph.writeLock();
            graph.setId(node, nodeId);
            graph.addNode(node);
            graph.writeUnlock();
        } else {
            log("Node added event ignored for node "+nodeId+": Node already exists");
        }
    }
    
    private void nodeChanged(String nodeId, Map<String, Object> attributes) {
        Node node = graph.getNode(nodeId);
        if (node!=null) {
            
            graph.writeLock();
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                }
            }
            
            graph.writeUnlock();
        } else {
            log("Node changed event ignored for node "+nodeId+": Node not found");
        }
    }

    private void nodeRemoved(String nodeId) {
        Node node = graph.getNode(nodeId);
        if (node!=null) {
            graph.writeLock();

            for (Edge edge: graph.getEdges(node).toArray()) {
                graph.removeEdge(edge);
            }

            graph.removeNode(node);
            graph.writeUnlock();
        } else {
            log("Node changed event ignored for node "+nodeId+": Node not found");
        }
    }
    
    private void addNodeAttribute(Node node, String attributeName, Object value) {
        NodeProperties p = properties.getNodeProperty(attributeName);
        if (p != null) {
            injectNodeProperty(p, value, node);
        }
        else if (node.getNodeData().getAttributes() != null) {
            node.getNodeData().getAttributes().setValue(attributeName, value);
        }
    }
    
    private void addEdgeAttribute(Edge edge, String attributeName, Object value) {
        EdgeProperties p = properties.getEdgeProperty(attributeName);
        if (p != null) {
            injectEdgeProperty(p, value, edge);
        }
        else if (edge.getEdgeData().getAttributes() != null) {
            edge.getEdgeData().getAttributes().setValue(attributeName, value);
        }
    }
    
    private void injectNodeProperty(NodeProperties p, Object value, Node node) {
        switch (p) {
            case ID:
                String id = value.toString();
                if (id != null) {
                    graph.setId(node, id);
                }
                break;
            case LABEL:
                String label = (value!=null)?value.toString():null;
                node.getNodeData().setLabel(label);
                break;
            case X:
                float x = Float.valueOf(value.toString());
                if (x != 0) {
                    node.getNodeData().setX(x);
                }
                break;
            case Y:
                float y = Float.valueOf(value.toString());
                if (y != 0) {
                    node.getNodeData().setY(y);
                }
                break;
            case Z:
                float z = Float.valueOf(value.toString());
                if (z != 0) {
                    node.getNodeData().setZ(z);
                }
                break;
            case R:
                float r = Float.valueOf(value.toString());
                node.getNodeData().setR(r);
                break;
            case G:
                float g = Float.valueOf(value.toString());
                node.getNodeData().setG(g);
                break;
            case B:
                float b = Float.valueOf(value.toString());
                node.getNodeData().setB(b);
                break;
            case SIZE:
                float size = Float.valueOf(value.toString());
                node.getNodeData().setSize(size);
                break;
        }
    }
    
    private void injectEdgeProperty(EdgeProperties p, Object value,
            Edge edge) {
        
        switch (p) {
            case ID:
                String id = value.toString();
                if (id != null) {
                    graph.setId(edge, id);
                }
                break;
            case LABEL:
                String label = value.toString();
                if (label != null) {
                    edge.getEdgeData().setLabel(label);
                }
                break;
            /*
             * Unsupported set of SOURCE and TARGET
             */
//            case SOURCE:
//                String source = value.toString();
//                if (source != null) {
//                    Node sourceNode = graph.getNode(source);
//                    edge.setSource(sourceNode);
//                }
//                break;
//            case TARGET:
//                String target = value.toString();
//                if (target != null) {
//                    Node targetNode = graph.getNode(target);
//                    edge.setTarget(targetNode);
//                }
//                break;
            case WEIGHT:
                float weight = Float.valueOf(value.toString());
                if (weight != 0) {
                    edge.setWeight(weight);
                }
                break;
            case R:
                float r = Float.valueOf(value.toString());
                edge.getEdgeData().setR(r);
                break;
            case G:
                float g = Float.valueOf(value.toString());
                edge.getEdgeData().setG(g);
                break;
            case B:
                float b = Float.valueOf(value.toString());
                edge.getEdgeData().setB(b);
                break;
        }
    }

    private void applyFilter(FilterEvent filterEvent) {
        
        Filter filter = filterEvent.getFilter();
        if (filter instanceof NodeFilter) {
            NodeFilter nodeFilter = (NodeFilter)filter;
            Map<String, Object> attributes = filterEvent.getAttributes();
            for (Node node: graph.getNodes().toArray()) {
                if (nodeFilter.evaluate(graph, node)) {

                    switch (filterEvent.getEventType()) {
                        case ADD:
                            log("Unsupported FilterEvent of type ADD");
                            break;
                        case CHANGE:
                            graph.writeLock();
                            if (attributes!=null && attributes.size() > 0) {
                                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                                    this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                                }
                            }
                            graph.writeUnlock();
                            break;
                        case REMOVE:
                            graph.writeLock();
                            graph.removeNode(node);
                            graph.writeUnlock();
                            break;
                    }
                }
            }
        }

        if (filter instanceof EdgeFilter) {
            EdgeFilter edgeFilter = (EdgeFilter)filter;
            Map<String, Object> attributes = filterEvent.getAttributes();
            for (Edge edge: graph.getEdges().toArray()) {
                if (edgeFilter.evaluate(graph, edge)) {

                    switch (filterEvent.getEventType()) {
                        case ADD:
                            log("Unsupported FilterEvent of type ADD");
                            break;
                        case CHANGE:
                            graph.writeLock();
                            if (attributes!=null && attributes.size() > 0) {
                                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                                    this.addEdgeAttribute(edge, entry.getKey(), entry.getValue());
                                }
                            }
                            graph.writeUnlock();
                            break;
                        case REMOVE:
                            graph.writeLock();
                            graph.removeEdge(edge);
                            graph.writeUnlock();
                            break;
                    }
                }
            }
        }
        
    }

}
