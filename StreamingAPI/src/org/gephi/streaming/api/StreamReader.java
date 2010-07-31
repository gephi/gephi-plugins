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

import org.gephi.streaming.api.event.GraphEventBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * Read events from a stream.
 * An implementation of this class should read information
 * from the InputStream, create the appropriate GraphEvent
 * and send it to the GraphEventHandler handler.
 *
 * @author Andre' Panisson
 */
public abstract class StreamReader {

    /**
     * The handler that will handle events
     */
    protected final GraphEventHandler handler;
    /**
     * The event builder to create graph events
     */
    protected final GraphEventBuilder eventBuilder;
    /**
     * The report to add useful information
     */
    protected Report report;
    /**
     * A listener to receive status notifications
     */
    protected StreamReaderStatusListener listener;
    
    /**
     * Create a new StreamReader that will send to the handler
     * the events created using the event builder.
     *
     * @param handler the GraphEventHandler to which the events will be delegated
     */
    public StreamReader(GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        this.handler = handler;
        this.eventBuilder = eventBuilder;
    }

    /**
     * Used to get the Report where information is being added.
     *
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    /**
     * Used to set the Report where information will be added.
     *
     * @param report the report to set
     */
    public void setReport(Report report) {
        this.report = report;
    }

    /**
     * Read from the InputStream and send the appropriate event
     * to the GraphEventHandler
     * 
     * @param inputStream the InputStream to read from.
     * @throws IOException when unable to connect to the InputStream
     */
    public abstract void processStream(InputStream inputStream) throws IOException;

    /**
     * Read from the channel and send the appropriate event
     * to the GraphEventHandler
     *
     * @param channel the ReadableByteChannel to read from.
     * @throws IOException when unable to connect to the InputStream
     */
    public abstract void processStream(ReadableByteChannel channel) throws IOException;

    /**
     * Set a listener to asynchronously receive status notifications.
     * @param listener the listener to be notifiedConnection
     */
    public void setStatusListener(StreamReaderStatusListener listener) {
        this.listener = listener;
    }

    /**
     * This is the listener interface to asynchronously receive status notifications.
     * It should be registered using setStatusListener().
     */
    public interface StreamReaderStatusListener {
        /**
         * Called when the stream is closed
         */
        public void onStreamClosed();
        /**
         * Called when data is received
         */
        public void onDataReceived();
        /**
         * Called when error occurs
         */
        public void onError();
    }

}
