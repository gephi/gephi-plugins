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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        
        this.factory = graph.getModel().factory();
        
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
        properties.addNodePropertyAssociation(NodeProperties.COLOR, "color");
        properties.addNodePropertyAssociation(NodeProperties.FIXED, "fixed");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.SOURCE, "source");
        properties.addEdgePropertyAssociation(EdgeProperties.TARGET, "target");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
        properties.addEdgePropertyAssociation(EdgeProperties.R, "r");
        properties.addEdgePropertyAssociation(EdgeProperties.G, "g");
        properties.addEdgePropertyAssociation(EdgeProperties.B, "b");
        properties.addEdgePropertyAssociation(EdgeProperties.COLOR, "color");
        properties.addEdgePropertyAssociation(EdgeProperties.SIZE, "size");
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
            logger.log(Level.INFO, e.getMessage(), e);
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
            case GRAPH:
                this.graphChanged(elementEvent.getAttributes());
                break;
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
    
    private void graphChanged(Map<String, Object> attributes) {
        graph.writeLock();

        if (attributes!=null && attributes.size() > 0) {
            for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                graph.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        graph.writeUnlock();
    }

    private void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        
        Edge edge = graph.getEdge(edgeId);
        if (edge!=null) {
            log("Edge added event ignored for edge "+edgeId+": Edge already exists");
            return;
        }
        
        graph.writeLock();
        try {
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

            edge = factory.newEdge(edgeId, source, target,0 , 1.0f, directed);

            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addEdgeAttribute(edge, entry.getKey(), entry.getValue());
                }
            }


            graph.addEdge(edge);
        
        } finally {
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
            graph.removeEdge(edge);
        } else {
            log("Edge removed event ignored for edge "+edgeId+": Edge not found");
        }
    }

    private void nodeAdded(String nodeId, Map<String, Object> attributes) {
        Node node = graph.getNode(nodeId);
        if (node==null) {
            node = factory.newNode(nodeId);
            
            //Default size:
            //https://github.com/gephi/gephi/issues/1447
            node.setSize(10);
            boolean positionSet = false;
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    NodeProperties p = properties.getNodeProperty(entry.getKey());
                    if(p == NodeProperties.X ||p == NodeProperties.Y || p == NodeProperties.Z) {
                        positionSet = true;
                    }
                    this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                }
            }
            
            if(!positionSet) {
                //Set a random position by default:
                //https://github.com/gephi/gephi/issues/1447
                node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
            }
            
            // graph.setId(node, nodeId);
            graph.addNode(node);
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
            graph.removeNode(node);
        } else {
            log("Node changed event ignored for node "+nodeId+": Node not found");
        }
    }
    
    private void addNodeAttribute(Node node, String attributeName, Object value) {
        NodeProperties p = properties.getNodeProperty(attributeName);
        if (p != null) {
            injectNodeProperty(p, value, node);
        } else {
            if (!graph.getModel().getNodeTable().hasColumn(attributeName)) {
                graph.getModel().getNodeTable().addColumn(attributeName, value.getClass());
            }
            node.setAttribute(attributeName, value);
        }
    }
    
    private void addEdgeAttribute(Edge edge, String attributeName, Object value) {
        EdgeProperties p = properties.getEdgeProperty(attributeName);
        if (p != null) {
            injectEdgeProperty(p, value, edge);
        } else {
            if (!graph.getModel().getEdgeTable().hasColumn(attributeName)) {
                graph.getModel().getEdgeTable().addColumn(attributeName, value.getClass());
            }
        edge.setAttribute(attributeName, value);
        }
    }
    
    private void injectNodeProperty(NodeProperties p, Object value, Node node) {
        switch (p) {
            case ID:
                String id = value.toString();
                if (id != null) {
                    // graph.setId(node, id);
                }
                break;
            case LABEL:
                String label = (value!=null)?value.toString():null;
                node.setLabel(label);
                break;
            case FIXED:
                boolean fixed = (value!=null)?Boolean.valueOf(value.toString()):false;
                node.setFixed(fixed);
                break;
            case X:
                float x = Float.valueOf(value.toString());
                node.setX(x);
                break;
            case Y:
                float y = Float.valueOf(value.toString());
                node.setY(y);
                break;
            case Z:
                float z = Float.valueOf(value.toString());
                node.setZ(z);
                break;
            case R:
                float r = Float.valueOf(value.toString());
                if (r < 0.0 || r > 1.0) {
                    throw new IllegalArgumentException("Color parameter outside of expected range: " + r);
                }
                node.setR(r);
                break;
            case G:
                float g = Float.valueOf(value.toString());
                if (g < 0.0 || g > 1.0) {
                    throw new IllegalArgumentException("Color parameter outside of expected range: " + g);
                }
                node.setG(g);
                break;
            case B:
                float b = Float.valueOf(value.toString());
                if (b < 0.0 || b > 1.0) {
                    throw new IllegalArgumentException("Color parameter outside of expected range: " + b);
                }
                node.setB(b);
                break;
            case COLOR:
                Integer color = (value!=null)?Integer.decode(value.toString()):0;
                int i = color.intValue();
                node.setR(((i >> 16) & 0xFF)/255.f);
                node.setG(((i >> 8) & 0xFF)/255.f);
                node.setB((i & 0xFF)/255.f);
                break;
            case SIZE:
                float size = Float.valueOf(value.toString());
                if(size > 0) {
                    node.setSize(size);
                }
                break;
        }
    }
    
    private void injectEdgeProperty(EdgeProperties p, Object value,
            Edge edge) {
        
        switch (p) {
            case ID:
                String id = value.toString();
                if (id != null) {
                    // graph.setId(edge, id);
                }
                break;
            case LABEL:
                String label = value.toString();
                if (label != null) {
                    edge.setLabel(label);
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
                if (r < 0.0 || r > 1.0) {
                    throw new IllegalArgumentException("Color parameter outside of expected range: " + r);
                }
                edge.setR(r);
                break;
            case G:
                float g = Float.valueOf(value.toString());
                if (g < 0.0 || g > 1.0) {
                    throw new IllegalArgumentException("Color parameter outside of expected range: " + g);
                }
                edge.setG(g);
                break;
            case B:
                float b = Float.valueOf(value.toString());
                if (b < 0.0 || b > 1.0) {
                    throw new IllegalArgumentException("Color parameter outside of expected range: " + b);
                }
                edge.setB(b);
                break;
            case COLOR:
                Integer color = (value!=null)?Integer.decode(value.toString()):0;
                int i = color.intValue();
                edge.setR(((i >> 16) & 0xFF)/255.f);
                edge.setG(((i >> 8) & 0xFF)/255.f);
                edge.setB((i & 0xFF)/255.f);
                break;
            case SIZE:
                float size = Float.valueOf(value.toString());
                edge.setWeight(size);
                break;
        }
    }

    private void applyFilter(FilterEvent filterEvent) {
        
        graph.writeLock();
        try {
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
                                if (attributes!=null && attributes.size() > 0) {
                                    for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                                        this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                                    }
                                }

                                break;
                            case REMOVE:
                                graph.removeNode(node);
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
                                if (attributes!=null && attributes.size() > 0) {
                                    for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                                        this.addEdgeAttribute(edge, entry.getKey(), entry.getValue());
                                    }
                                }
                                break;
                            case REMOVE:
                                graph.removeEdge(edge);
                                break;
                        }
                    }
                }
            }
        } finally {
            graph.writeUnlock();
        }
    }

}
