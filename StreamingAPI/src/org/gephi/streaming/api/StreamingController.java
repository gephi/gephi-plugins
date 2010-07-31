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
 * Controller to connect to graph streams.
 *
 * @author panisson
 */
public interface StreamingController {

    /**
     * This is used to connect to a StreamingEndpoint and update the Graph object
     * with the events received in the stream.
     *
     * @param endpoint - the StreamingEndpoint to connect to
     * @param graph - the Graph that will be updated with the events
     * @return the streaming connection
     * @throws IOException
     */
    public StreamingConnection process(StreamingEndpoint endpoint, Graph graph)
            throws IOException;

    /**
     * This is used to connect to a StreamingEndpoint and update the Graph object
     * with the events received in the stream. The Report object will be updated
     * with useful information, and a listener can be used to listen to
     * the status of the connection.
     *
     * @param endpoint - the StreamingEndpoint to connect to
     * @param graph - the Graph that will be updated with the events
     * @param report - the Report object that will be updated with information
     * @param statusListener - the listener that will listen to connection status,
     * can be null.
     * @return the streaming connection
     * @throws IOException
     */
    public StreamingConnection process(StreamingEndpoint endpoint, Graph graph,
            Report report, StreamingConnection.StatusListener statusListener)
            throws IOException;

    /**
     * Utility function to get the stream implementation for a given stream type.
     *
     * @param streamType
     * @return the stream type implementation
     */
    public StreamType getStreamType(String streamType);
}
