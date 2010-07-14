package org.gephi.streaming.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.event.GraphEvent;

/**
 * @author panisson
 *
 */

public class GraphEventContainerImpl implements GraphEventContainer {
    
    private LinkedBlockingQueue<GraphEvent> eventQueue = new LinkedBlockingQueue<GraphEvent>();

    private final GraphEventHandler handler;

    private Report report;
    
    private Object source;

    private boolean stopped = false;
    
    private EventDispatcher dispatcher;
    
    private Object emptyQueueLock = new Object();
    
    /**
     * @param source 
     * 
     */
    public GraphEventContainerImpl(Object source, GraphEventHandler handler) {
        this.report = new Report();
        this.source = source;
        this.handler = handler;
        
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
    
    protected void fireEvent(GraphEvent event) {
        eventQueue.offer(event);
    }

    /**
     * @return all events still in buffer
     */
    public List<GraphEvent> getAllEvents() {
        return new ArrayList<GraphEvent>(this.eventQueue);
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

    public void handleGraphEvent(GraphEvent event) {
        fireEvent(event);
    }
    
    private class EventDispatcher extends Thread {

        public EventDispatcher(Object source) {
            super("EventDispatcher-"+source.toString());
        }

        @Override
        public void run() {
            while (!stopped) {
                try {
                    GraphEvent event = eventQueue.take();
                    handler.handleGraphEvent(event);
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
