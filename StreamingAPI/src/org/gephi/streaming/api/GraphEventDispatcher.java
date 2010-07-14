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
 * Graph Events are dispatched by the GraphEventDispatcher in synchronous or
 * asynchronous way, and listeners can be registered using the GraphEventDispatcher in order to
 * react to the collected events.
 * <p> See {@link GraphEventOperationSupport} for how to load events in the container and see
 * {@link GraphEventDispatcher} and {@link GraphEventHandler} for how to listen to the events.
 * 
 * @author panisson
 * @see GraphEventContainer
 *
 */
public interface GraphEventDispatcher {
    
    /**
     * Add a GraphEventHandler to the list of handlers
     * 
     * @param listener
     */
    public void addEventHandler(GraphEventHandler handler);
    
    /**
     * Remove an event handler
     * @param handler the GraphEventHandler instance to be removed
     */
    public void removeEventHandler(GraphEventHandler handler);

}
