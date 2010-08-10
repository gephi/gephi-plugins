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
import java.net.URL;

/**
 * This interface represents a connection to a stream.
 *
 * @author panisson
 */
public interface StreamingConnection {

    /**
     * This is used to get the URL where this object is connected.
     * @return the URL
     */
    public StreamingEndpoint getStreamingEndpoint();

    /**
     * This is used to get the Report object where information is stored.
     * @return the Report object
     */
    public Report getReport();

    /**
     * This is used to close the connection
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * This is used to verify if the connection is closed
     * @return true if connection closed, false otherwise
     */
    public boolean isClosed();

    /**
     * This is used to connect to the URL and process asynchronously the
     * events received. A new thread will be created and the process()
     * will be called.
     */
    public void asynchProcess();

    /**
     * This is used to connect to the URL and process the
     * events received. The method returns only when the connection is closed.
     */
    public void process();

    /**
     * Set a listener to asynchronously receive status notifications.
     * @param listener the listener to be notifiedConnection
     */
    public void addStatusListener(StatusListener listener);

    /**
     * Remove the StatusListener from the listeners
     * @param listener the StatusListener to remove
     */
    public void removeStatusListener(StatusListener listener);

    /**
     * This is the listener interface to asynchronously receive status notifications.
     * It should be registered using setStatusListener().
     */
    public interface StatusListener {
        public void onConnectionClosed(StreamingConnection connection);
        public void onDataReceived(StreamingConnection connection);
        public void onError(StreamingConnection connection);
    }

}
