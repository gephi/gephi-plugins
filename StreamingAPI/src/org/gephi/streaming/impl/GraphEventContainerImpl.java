package org.gephi.streaming.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.streaming.api.ContainerLoader;
import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.GraphEventDispatcher;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.event.AttributeEvent;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;

/**
 * @author panisson
 *
 */

public class GraphEventContainerImpl  implements ContainerLoader, GraphEventContainer, GraphEventDispatcher {
    
    private LinkedBlockingQueue<GraphEvent> eventQueue = new LinkedBlockingQueue<GraphEvent>();
    
    private List<GraphEventListener> listeners = new ArrayList<GraphEventListener>();

    private Report report;
    
    private Object source;
    
    /**
     * @param source 
     * 
     */
    public GraphEventContainerImpl(Object source) {
        this.report = new Report();
        this.source = source;
        
        EventDispatcher dispatcher = new EventDispatcher();
        dispatcher.start();
    }
    
    @Override
    public Object getSource() {
        return source;
    }
    
    @Override
    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public ContainerLoader getLoader() {
        return this;
    }

    @Override
    public Report getReport() {
        return this.report;
    }
    
    @Override
    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public GraphEventDispatcher getGraphEventDispatcher() {
        return this;
    }

    @Override
    public void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed) {
        EdgeAddedEvent event = new EdgeAddedEvent(source, edgeId, fromNodeId, toNodeId, directed);
        eventQueue.offer(event);
    }

    @Override
    public void edgeAttributeAdded(String edgeId, AttributeColumn attributeColumn, Object value) {
        AttributeEvent event = new AttributeEvent(source, EventType.ADD, ElementType.EDGE, edgeId, attributeColumn, value);
        eventQueue.offer(event);
    }

    @Override
    public void edgeAttributeChanged(String edgeId,
            AttributeColumn attributeColumn, Object newValue) {
        AttributeEvent event = new AttributeEvent(source, EventType.CHANGE, ElementType.EDGE, edgeId, attributeColumn, newValue);
        eventQueue.offer(event);
    }

    @Override
    public void edgeAttributeRemoved(String edgeId,
            AttributeColumn attributeColumn) {
        AttributeEvent event = new AttributeEvent(source, EventType.REMOVE, ElementType.EDGE, edgeId, attributeColumn, null);
        eventQueue.offer(event);
    }

    @Override
    public void edgeRemoved(String edgeId) {
        GraphEvent event = new GraphEvent(source, EventType.REMOVE, ElementType.EDGE, edgeId);
        eventQueue.offer(event);
    }

    @Override
    public void graphAttributeAdded(AttributeColumn attributeColumn,
            Object value) {
        AttributeEvent event = new AttributeEvent(source, EventType.ADD, ElementType.GRAPH, null, attributeColumn, value);
        eventQueue.offer(event);
        
    }

    @Override
    public void graphAttributeChanged(AttributeColumn attributeColumn,
            Object newValue) {
        AttributeEvent event = new AttributeEvent(source, EventType.CHANGE, ElementType.GRAPH, null, attributeColumn, newValue);
        eventQueue.offer(event);
    }

    @Override
    public void graphAttributeRemoved(AttributeColumn attributeColumn) {
        AttributeEvent event = new AttributeEvent(source, EventType.CHANGE, ElementType.GRAPH, null, attributeColumn, null);
        eventQueue.offer(event);
    }

    @Override
    public void nodeAdded(String nodeId) {
        GraphEvent event = new GraphEvent(source, EventType.ADD, ElementType.NODE, nodeId);
        eventQueue.offer(event);
    }

    @Override
    public void nodeAttributeAdded(String nodeId,
            AttributeColumn attributeColumn, Object value) {
        AttributeEvent event = new AttributeEvent(source, EventType.ADD, ElementType.NODE, nodeId, attributeColumn, value);
        eventQueue.offer(event);
    }

    @Override
    public void nodeAttributeChanged(String nodeId,
            AttributeColumn attributeColumn, Object newValue) {
        AttributeEvent event = new AttributeEvent(source, EventType.CHANGE, ElementType.NODE, nodeId, attributeColumn, newValue);
        eventQueue.offer(event);
    }

    @Override
    public void nodeAttributeRemoved(String nodeId,
            AttributeColumn attributeColumn) {
        AttributeEvent event = new AttributeEvent(source, EventType.REMOVE, ElementType.NODE, nodeId, attributeColumn, null);
        eventQueue.offer(event);
    }

    @Override
    public void nodeRemoved(String nodeId) {
        GraphEvent event = new GraphEvent(source, EventType.REMOVE, ElementType.NODE, nodeId);
        eventQueue.offer(event);
    }

    /**
     * @return all events still in buffer
     */
    public List<GraphEvent> getAllEvents() {
        return new ArrayList<GraphEvent>(this.eventQueue);
    }

    @Override
    public void addEventListener(GraphEventListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    @Override
    public void removeEventListener(GraphEventListener listener) {
        listeners.remove(listener);
    }
    
    private class EventDispatcher extends Thread {
        public void run() {
            while (true) {
                try {
                    GraphEvent event = eventQueue.take();
                    for (GraphEventListener listener: listeners) {
                        listener.onGraphEvent(event);
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
