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
package org.gephi.streaming.server;

import org.gephi.graph.api.Graph;

/**
 * Used to create a server controller associated with a graph instance.
 * The controller will handle requests and responses and update the graph
 * with received information, and will listen to graph changes in order to
 * send events to connected clients.
 *
 * @author panisson
 *
 */
public interface ServerControllerFactory {

    /**
     * Create a server controller associated with the given graph.
     * @param graph - the Graph to associate
     * @return a new server controller instance
     */
    public ServerController createServerController(Graph graph);

}
