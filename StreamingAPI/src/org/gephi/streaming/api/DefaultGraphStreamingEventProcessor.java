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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.PropertiesAssociations.EdgeProperties;
import org.gephi.streaming.api.PropertiesAssociations.NodeProperties;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementAttributeEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class DefaultGraphStreamingEventProcessor implements GraphEventListener {
    
    private GraphFactory factory;
    private Graph graph;

    private Map<String, Integer> nodeStringToInt = new HashMap<String, Integer>();
    private Map<String, Integer> edgeStringToInt = new HashMap<String, Integer>();
    
    //PropertiesAssociations
    protected PropertiesAssociations properties = new PropertiesAssociations();
    
    public DefaultGraphStreamingEventProcessor(Workspace workspace) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (workspace == null) {
            workspace = pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        
      //Architecture
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
//        TimelineController timelineController = Lookup.getDefault().lookup(TimelineController.class);

        graph = graphModel.getHierarchicalMixedGraph();
        factory = graphModel.factory();
        
        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.ID, "id");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.SOURCE, "source");
        properties.addEdgePropertyAssociation(EdgeProperties.TARGET, "target");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
    }
    
    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
    
    public void process(URL url, String streamType) {
        StreamReaderFactory processorFactory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = processorFactory.createStreamReader(streamType);

        GraphEventOperationSupport eventOperationSupport = new GraphEventOperationSupport();
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventContainer container = containerfactory.newGraphEventContainer(eventOperationSupport);
        eventOperationSupport.setContainer(container);
        
        container.getGraphEventDispatcher().addEventListener(this);
        processor.setOperationSupport(eventOperationSupport);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
    }
    
    public void process(GraphStreamingEndpoint endpoint) {
        this.process(endpoint.getUrl(), endpoint.getStreamType().getType());
    }
    
    @Override
    public void onGraphEvent(GraphEvent event) {

//        System.out.println(event);

        graph.writeLock();
        
        if (event instanceof ElementEvent) {
            if (event instanceof ElementAttributeEvent) {

                ElementAttributeEvent attributeEvent = (ElementAttributeEvent)event;

                if(event.getElementType() == ElementType.NODE) {

                    Integer id = nodeStringToInt.get(attributeEvent.getElementId());
                    Node node = graph.getNode(id);
                    if (node==null) {
                        node = factory.newNode();
                        graph.addNode(node);
                        nodeStringToInt.put(attributeEvent.getElementId(), node.getId());
                    }

                    switch (event.getEventType()) {

                    case ADD:
                    case CHANGE: {
                        
                        NodeProperties p = properties.getNodeProperty(attributeEvent.getAttributeName());
                        if (p != null) {
                            injectNodeProperty(p, attributeEvent.getAttributeValue(), node.getNodeData());
                        }
                        else if (node.getNodeData().getAttributes() != null) {
                            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                            row.setValue(attributeEvent.getAttributeName(), attributeEvent.getAttributeValue());
                        }

                    } break;

                    case REMOVE: {
                        if (node.getNodeData().getAttributes() != null) {
                            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                            row.setValue(attributeEvent.getAttributeName(), null);
                        }
                    }
                    }
                }
                
                else if(event.getElementType() == ElementType.EDGE) {

                    Integer id = edgeStringToInt.get(attributeEvent.getElementId());
                    Edge edge = graph.getEdge(id);

                    if (edge!=null) {

                        switch (event.getEventType()) {

                        case ADD:
                        case CHANGE: {
                            if (edge.getEdgeData().getAttributes() != null) {
                                AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
                                row.setValue(attributeEvent.getAttributeName(), attributeEvent.getAttributeValue());
                            }

                        } break;

                        case REMOVE: {
                            if (edge.getEdgeData().getAttributes() != null) {
                                AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
                                row.setValue(attributeEvent.getAttributeName(), null);
                            }
                        }
                        }

                    }

                }
            }
            
            else {
                    
               ElementEvent elementEvent = (ElementEvent)event;

               if(event.getElementType() == ElementType.NODE) {

                   switch (event.getEventType()) {

                   case ADD: {
                       Node n = factory.newNode();
                       graph.addNode(n);
                       nodeStringToInt.put(elementEvent.getElementId(), n.getId());
                   } break;

                   case CHANGE: {
                       System.out.println("Invalid change operation on node "+elementEvent.getElementId());

                   } break;

                   case REMOVE: {
                       Integer id = nodeStringToInt.get(elementEvent.getElementId());
                       Node node = graph.getNode(id);
                       if (node!=null)
                           graph.removeNode(node);
                   }
                   }
               }
               else if(event.getElementType() == ElementType.EDGE) {

                   switch (event.getEventType()) {

                   case ADD: {
                       EdgeAddedEvent edgeAddedEvent = (EdgeAddedEvent) event;

                       int sourceId = nodeStringToInt.get(edgeAddedEvent.getSourceId());
                       Node source = graph.getNode(sourceId);
                       int targetId = nodeStringToInt.get(edgeAddedEvent.getTargetId());
                       Node target = graph.getNode(targetId);
                       Edge edge = factory.newEdge(source, target, 1.0f, edgeAddedEvent.isDirected());
                       graph.addEdge(edge);
                       edgeStringToInt.put(elementEvent.getElementId(), edge.getId());
                   } break;

                   case CHANGE: {
                       System.out.println("Invalid change operation on edge "+elementEvent.getElementId());
                   } break;

                   case REMOVE: {
                       Integer id = edgeStringToInt.get(elementEvent.getElementId());
                       Edge edge = graph.getEdge(id);
                       if(edge!=null) graph.removeEdge(edge);
                   }
                   }

               }
            }
        }

        graph.writeUnlock();
    }
    
    private void injectNodeProperty(NodeProperties p, Object value, NodeData nodeData) {
        switch (p) {
            case ID:
                String id = value.toString();
                if (id != null) {
                    nodeData.setId(id);
                }
                break;
            case LABEL:
                String label = value.toString();
                if (label != null) {
                    nodeData.setLabel(label);
                }
                break;
            case X:
                float x = Float.valueOf(value.toString());
                if (x != 0) {
                    nodeData.setX(x);
                }
                break;
            case Y:
                float y = Float.valueOf(value.toString());
                if (y != 0) {
                    nodeData.setY(y);
                }
                break;
            case Z:
                float z = Float.valueOf(value.toString());
                if (z != 0) {
                    nodeData.setZ(z);
                }
                break;
            case R:
                break;
            case G:
                break;
            case B:
                break;
        }
    }

}
