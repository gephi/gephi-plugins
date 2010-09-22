/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.streaming.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.event.GraphEvent;

/**
 * @author panisson
 *
 */

public class GraphEventContainer implements GraphEventHandler {
    
    private LinkedBlockingQueue<GraphEvent> eventQueue = new LinkedBlockingQueue<GraphEvent>();

    private final GraphEventHandler handler;

    private Report report;

    private boolean stopped = false;
    
    private Thread dispatcher;
    
    private Object emptyQueueLock = new Object();

    /**
     * Set the source of the data put in the container. Could be the stream's URL.
     * @param source the original source of data.
     * @throws NullPointerException if <code>source</code> is <code>null</code>
     */
    public GraphEventContainer(GraphEventHandler handler) {
        this.report = new Report();
        this.handler = handler;
        
        dispatcher = new Thread(new EventDispatcher(), "EventDispatcher");
        dispatcher.start();
    }

    public Report getReport() {
        return this.report;
    }
    
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
    
    private class EventDispatcher implements Runnable {

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
