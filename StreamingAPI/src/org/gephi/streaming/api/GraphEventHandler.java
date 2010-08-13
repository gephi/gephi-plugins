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

import org.gephi.streaming.api.event.GraphEvent;

/**
 * This interface defines the basic event handling operations
 * of the Graph Streaming API.<br>
 * Implementations of this interface can be used to handle events and implement
 * what should be done when an event occurred (update the workspace, 
 * export to a specific format, send through the network etc.).
 * It could be used also to chain several
 * event handlers (the handlers does something with the event and dispatches it to another
 * event handler).
 * 
 * @author Andre' Panisson
 *
 */
public interface GraphEventHandler {

    /**
     * This is the basic event handling operation that will be called when
     * an event is received.
     * 
     * @param event - the event to be handled
     */
    public void handleGraphEvent(GraphEvent event);

}
