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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.PropertiesAssociations.EdgeProperties;
import org.gephi.streaming.api.PropertiesAssociations.NodeProperties;

/**
 * @author panisson
 *
 */
public class GraphUpdaterOperationSupport extends AbstractOperationSupport {
    
    private Graph graph;
    private GraphFactory factory;
    protected PropertiesAssociations properties = new PropertiesAssociations();
    
    /**
     * @param graph
     */
    public GraphUpdaterOperationSupport(Graph graph) {
        this.graph = graph;
        this.factory = graph.getGraphModel().factory();
        
        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.ID, "id");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
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

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAdded(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        
        Edge edge = graph.getEdge(edgeId);
        if (edge!=null) return;
        
        Node source = graph.getNode(fromNodeId);
        Node target = graph.getNode(toNodeId);
        if(source!=null && target!=null) {
            edge = factory.newEdge(source, target, 1.0f, directed);
            
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
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeChanged(java.lang.String, Map)
     */
    @Override
    public void edgeChanged(String edgeId, Map<String, Object> attributes) {
        Edge edge = graph.getEdge(edgeId);
        if (edge!=null) {
            
            graph.writeLock();
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addEdgeAttribute(edge, entry.getKey(), entry.getValue());
                }
            }
            
            graph.writeUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeAdded(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void edgeAttributeAdded(String edgeId, String attributeName,
            Object value) {
        
        Edge edge = graph.getEdge(edgeId);
        if (edge==null) return;
        
        graph.writeLock();
        this.addEdgeAttribute(edge, attributeName, value);
        graph.writeUnlock();

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeChanged(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void edgeAttributeChanged(String edgeId, String attributeName,
            Object newValue) {
        
        edgeAttributeAdded(edgeId, attributeName, newValue);
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeRemoved(java.lang.String, java.lang.String)
     */
    @Override
    public void edgeAttributeRemoved(String edgeId, String attributeName) {
        edgeAttributeAdded(edgeId, attributeName, null);
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeRemoved(java.lang.String)
     */
    @Override
    public void edgeRemoved(String edgeId) {
        Edge edge = graph.getEdge(edgeId);
        if (edge!=null) {
            graph.writeLock();
            graph.removeEdge(edge);
            graph.writeUnlock();
        }
    }
    
    /*
     * Unsupported operations
     */
    
//    /* (non-Javadoc)
//     * @see org.gephi.streaming.api.OperationSupport#graphAttributeAdded(java.lang.String, java.lang.Object)
//     */
//    @Override
//    public void graphAttributeAdded(String attributeName, Object value) {
//        System.out.println("Unsupported operation: graphAttributeAdded");
//    }
//
//    /* (non-Javadoc)
//     * @see org.gephi.streaming.api.OperationSupport#graphAttributeChanged(java.lang.String, java.lang.Object)
//     */
//    @Override
//    public void graphAttributeChanged(String attributeName, Object newValue) {
//        System.out.println("Unsupported operation: graphAttributeChanged");
//    }
//
//    /* (non-Javadoc)
//     * @see org.gephi.streaming.api.OperationSupport#graphAttributeRemoved(java.lang.String)
//     */
//    @Override
//    public void graphAttributeRemoved(String attributeName) {
//        System.out.println("Unsupported operation: graphAttributeRemoved");
//    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAdded(java.lang.String)
     */
    @Override
    public void nodeAdded(String nodeId, Map<String, Object> attributes) {
        Node node = graph.getNode(nodeId);
        if (node==null) {
            node = factory.newNode();
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                }
            }
            
            graph.writeLock();
            graph.addNode(node);
            graph.setId(node, nodeId);
            graph.writeUnlock();
        }
    }
    
    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeChanged(java.lang.String)
     */
    @Override
    public void nodeChanged(String nodeId, Map<String, Object> attributes) {
        Node node = graph.getNode(nodeId);
        if (node!=null) {
            
            graph.writeLock();
            
            if (attributes!=null && attributes.size() > 0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                }
            }
            
            graph.writeUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeAdded(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void nodeAttributeAdded(String nodeId, String attributeName,
            Object value) {
        nodeAttributeChanged(nodeId, attributeName, value);
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeChanged(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void nodeAttributeChanged(String nodeId, String attributeName,
            Object newValue) {
        
        Node node = graph.getNode(nodeId);
        if (node==null) return;
        
        graph.writeLock();
        this.addNodeAttribute(node, attributeName, newValue);
        graph.writeUnlock();
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeRemoved(java.lang.String, java.lang.String)
     */
    @Override
    public void nodeAttributeRemoved(String nodeId, String attributeName) {
        Node node = graph.getNode(nodeId);
        if (node==null) return;
        
        graph.writeLock();
        
        if (node.getNodeData().getAttributes() != null) {
            node.getNodeData().getAttributes().setValue(attributeName, null);
        }
        
        graph.writeUnlock();
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeRemoved(java.lang.String)
     */
    @Override
    public void nodeRemoved(String nodeId) {
        Node node = graph.getNode(nodeId);
        if (node!=null)
            graph.writeLock();
            graph.removeNode(node);
            graph.writeUnlock();
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
                String label = value.toString();
                if (label != null) {
                    node.getNodeData().setLabel(label);
                }
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

}
