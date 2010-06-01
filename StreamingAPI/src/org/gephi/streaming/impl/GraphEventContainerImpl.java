package org.gephi.streaming.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.streaming.api.ContainerLoader;
import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.GraphEventDispatcher;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.event.ElementAttributeEvent;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementEvent;
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
    
    protected List<GraphEventListener> listeners = new ArrayList<GraphEventListener>();

    private Report report;
    
    private Object source;

    private boolean stopped = false;
    
    private EventDispatcher dispatcher;
    
    private Object emptyQueueLock = new Object();
    
    /**
     * @param source 
     * 
     */
    public GraphEventContainerImpl(Object source) {
        this.report = new Report();
        this.source = source;
        
        dispatcher = new EventDispatcher(source);
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
        fireEvent(event);
    }

    @Override
    public void edgeAttributeAdded(String edgeId, AttributeColumn attributeColumn, Object value) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.ADD, ElementType.EDGE, edgeId, attributeColumn, value);
        fireEvent(event);
    }

    @Override
    public void edgeAttributeChanged(String edgeId,
            AttributeColumn attributeColumn, Object newValue) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.EDGE, edgeId, attributeColumn, newValue);
        fireEvent(event);
    }

    @Override
    public void edgeAttributeRemoved(String edgeId,
            AttributeColumn attributeColumn) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.REMOVE, ElementType.EDGE, edgeId, attributeColumn, null);
        fireEvent(event);
    }

    @Override
    public void edgeRemoved(String edgeId) {
        GraphEvent event = new ElementEvent(source, EventType.REMOVE, ElementType.EDGE, edgeId);
        fireEvent(event);
    }

    @Override
    public void graphAttributeAdded(AttributeColumn attributeColumn,
            Object value) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.ADD, ElementType.GRAPH, null, attributeColumn, value);
        fireEvent(event);
        
    }

    @Override
    public void graphAttributeChanged(AttributeColumn attributeColumn,
            Object newValue) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.GRAPH, null, attributeColumn, newValue);
        fireEvent(event);
    }

    @Override
    public void graphAttributeRemoved(AttributeColumn attributeColumn) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.GRAPH, null, attributeColumn, null);
        fireEvent(event);
    }

    @Override
    public void nodeAdded(String nodeId) {
        GraphEvent event = new ElementEvent(source, EventType.ADD, ElementType.NODE, nodeId);
        fireEvent(event);
    }

    @Override
    public void nodeAttributeAdded(String nodeId,
            AttributeColumn attributeColumn, Object value) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.ADD, ElementType.NODE, nodeId, attributeColumn, value);
        fireEvent(event);
    }

    @Override
    public void nodeAttributeChanged(String nodeId,
            AttributeColumn attributeColumn, Object newValue) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.NODE, nodeId, attributeColumn, newValue);
        fireEvent(event);
    }

    @Override
    public void nodeAttributeRemoved(String nodeId,
            AttributeColumn attributeColumn) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.REMOVE, ElementType.NODE, nodeId, attributeColumn, null);
        fireEvent(event);
    }

    @Override
    public void nodeRemoved(String nodeId) {
        GraphEvent event = new ElementEvent(source, EventType.REMOVE, ElementType.NODE, nodeId);
        fireEvent(event);
    }
    
    protected void fireEvent(GraphEvent event) {
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

    public void stop() {
        stopped = true;
        dispatcher.interrupt();
    }
    
    public void waitForDispatchAllEvents() {
        while (eventQueue.size() > 0) {
            try {
                synchronized(emptyQueueLock) {
                    emptyQueueLock.wait();
                }
            } catch (InterruptedException e) {}
        }
    }
    
    private class EventDispatcher extends Thread {

        public EventDispatcher(Object source) {
            super("EventDispatcher-"+source.toString());
        }

        public void run() {
            while (!stopped) {
                try {
                    GraphEvent event = eventQueue.take();
                    for (GraphEventListener listener: listeners) {
                        listener.onGraphEvent(event);
                    }
                } catch (InterruptedException e) {
                    // Container was closed
                    //e.printStackTrace();
                } finally {
                    // notify threads that are waiting for empty queue
                    if (eventQueue.size() == 0)
                        synchronized(emptyQueueLock) {
                            emptyQueueLock.notifyAll();
                        }
                }
            }
        }
    }
}
