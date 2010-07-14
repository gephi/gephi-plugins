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

/**
 * A container for Graph Event objects. Its role is to host all events collected by one or more
 * StreamReaders. The events will be dispatched by the GraphEventDispatcher in synchronous or
 * asynchronous way, and listeners can be registered using the GraphEventDispatcher in order to
 * react to the collected events.
 * <p> See {@link GraphEventOperationSupport} for how to load events in the container and see
 * {@link GraphEventDispatcher} and {@link GraphEventHandler} for how to listen to the events.
 *
 * @author Andre' Panisson
 */
public interface GraphEventContainer extends GraphEventHandler {

    /**
     * Set the source of the data put in the container. Could be the stream's URL.
     * @param source the original source of data.
     * @throws NullPointerException if <code>source</code> is <code>null</code>
     */
    public void setSource(Object source);

    /**
     * If exists, returns the source of the data.
     * @return the source of the data, or <code>null</code> if source is not defined.
     */
    public Object getSource();

    /**
     * Set a report this container can use to report issues detected when loading the container. Report
     * are used to log info and issues during load process. Only one report can be associated to a
     * container.
     * @param report set <code>report</code> as the default report for this container
     * @throws NullPointerException if <code>report</code> is <code>null</code>
     */
    public void setReport(Report report);

    /**
     * Returns the report associated to this container, if exists.
     * @return the report set for this container or <code>null</code> if no report is defined
     */
    public Report getReport();

    /**
     * Stops the current container, releasing its resources (stopping threads, etc...)
     */
    public void stop();

    /**
     * Wait until all events in this container are dispatched
     */
    public void waitForDispatchAllEvents();
    
}
