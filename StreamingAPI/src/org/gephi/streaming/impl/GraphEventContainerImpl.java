package org.gephi.streaming.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.GraphEventDispatcher;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;

/**
 * @author panisson
 *
 */

public class GraphEventContainerImpl implements GraphEventContainer, GraphEventDispatcher {
    
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
    
    public void fireEvent(GraphEvent event) {
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
