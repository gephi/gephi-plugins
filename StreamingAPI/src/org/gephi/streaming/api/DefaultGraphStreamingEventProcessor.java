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
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.event.ElementAttributeEvent;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;
//import org.gephi.timeline.api.TimelineController;
import org.gephi.streaming.impl.StreamingClient;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class DefaultGraphStreamingEventProcessor implements GraphEventListener {
    
    private Workspace workspace;
    private GraphFactory factory;
    private HierarchicalGraph graph;

    private Map<String, Integer> nodeStringToInt = new HashMap<String, Integer>();
    private Map<String, Integer> edgeStringToInt = new HashMap<String, Integer>();
    
    public DefaultGraphStreamingEventProcessor(Workspace workspace) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (workspace == null) {
            workspace = pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        this.workspace = workspace;
        
      //Architecture
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
//        TimelineController timelineController = Lookup.getDefault().lookup(TimelineController.class);

        graph = graphModel.getHierarchicalMixedGraph();
        factory = graphModel.factory();
    }
    
    /**
     * @return the graph
     */
    public HierarchicalGraph getGraph() {
        return graph;
    }
    
    public void process(URL url, String streamType) {
        StreamProcessorFactory processorFactory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = processorFactory.createStreamProcessor(streamType);
        
        processor.getContainer().getGraphEventDispatcher().addEventListener(this);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
    }
    
    @Override
    public void onGraphEvent(GraphEvent event) {

        System.out.println(event);

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

                        if (node.getNodeData().getAttributes() != null) {
                            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                            row.setValue(attributeEvent.getAttributeColumn(), attributeEvent.getAttributeValue());
                        }

                    } break;

                    case REMOVE: {
                        if (node.getNodeData().getAttributes() != null) {
                            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                            row.setValue(attributeEvent.getAttributeColumn(), null);
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
                                row.setValue(attributeEvent.getAttributeColumn(), attributeEvent.getAttributeValue());
                            }

                        } break;

                        case REMOVE: {
                            if (edge.getEdgeData().getAttributes() != null) {
                                AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
                                row.setValue(attributeEvent.getAttributeColumn(), null);
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

    public void process(GraphStreamingEndpoint endpoint) {
        StreamProcessorFactory processorFactory = Lookup.getDefault().lookup(StreamProcessorFactory.class);
        StreamProcessor processor = processorFactory.createStreamProcessor(endpoint.getStreamType());

        processor.getContainer().getGraphEventDispatcher().addEventListener(this);

        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(endpoint.getUrl(), processor);
    }

}
