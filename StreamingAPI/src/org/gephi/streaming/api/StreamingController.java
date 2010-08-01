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

import java.io.IOException;
import org.gephi.graph.api.Graph;

/**
 * Controller to connect to graph streams and update graphs accordingly.
 *
 * @author panisson
 */
public interface StreamingController {

    /**
     * This is used to connect to a StreamingEndpoint. The connection will be
     * bound with the Graph object: any event received in the stream will cause
     * an update in the graph, and updates in the graph will cause an event
     * to be sent to the StreamingEndpoint.
     *
     * @param endpoint - the StreamingEndpoint to connect to
     * @param graph - the Graph that will be updated with the events
     * @return the streaming connection
     * @throws IOException
     */
    public StreamingConnection connect(StreamingEndpoint endpoint, Graph graph)
            throws IOException;

    /**
     * This is used to connect to a StreamingEndpoint. The connection will be
     * bound with the Graph object: any event received in the stream will cause
     * an update in the graph, and updates in the graph will cause an event
     * to be sent to the StreamingEndpoint. The Report object will be updated
     * with useful information.
     *
     * @param endpoint - the StreamingEndpoint to connect to
     * @param graph - the Graph that will be updated with the events
     * @param report - the Report object that will be updated with information
     * @return the streaming connection
     * @throws IOException
     */
    public StreamingConnection connect(StreamingEndpoint endpoint, Graph graph,
            Report report)
            throws IOException;

    /**
     * Utility function to get the stream implementation for a given stream type.
     *
     * @param streamType
     * @return the stream type implementation
     */
    public StreamType getStreamType(String streamType);
}
