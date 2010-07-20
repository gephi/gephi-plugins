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
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author panisson
 */
public class GraphStreamingUtils {

    public static StreamingConnection connectToStream(
            GraphStreamingEndpoint endpoint, GraphEventHandler handler)
    throws IOException {
        return connectToStream(endpoint.getUrl(), endpoint.getStreamType(), handler);
    }

    public static StreamingConnection connectToStream(
            URL url, StreamType streamType, GraphEventHandler handler)
    throws IOException {
        GraphEventContainerFactory containerfactory =
                Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        StreamReaderFactory readerFactory =
                Lookup.getDefault().lookup(StreamReaderFactory.class);

        final GraphEventContainer container =
                containerfactory.newGraphEventContainer(handler);

        Report report = new Report();
        GraphEventBuilder eventBuilder = new GraphEventBuilder(url);
        StreamReader reader =
                readerFactory.createStreamReader(streamType, container,
                eventBuilder);
        reader.setReport(report);

        StreamingConnection connection = new StreamingConnection(url, reader);

        connection.addStatusListener(
                new StreamingConnection.StatusListener() {

            public void onConnectionClosed(StreamingConnection connection) {
                container.waitForDispatchAllEvents();
                container.stop();
            }

            public void onDataReceived(StreamingConnection connection) { }
            public void onError(StreamingConnection connection) { }
        });
        
        return connection;
    }

    public static StreamingConnection connectToStream(
            URL url, String streamType, GraphEventHandler handler)
    throws IOException {
        GraphEventContainerFactory containerfactory =
                Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        StreamReaderFactory readerFactory =
                Lookup.getDefault().lookup(StreamReaderFactory.class);

        final GraphEventContainer container =
                containerfactory.newGraphEventContainer(handler);

        Report report = new Report();
        GraphEventBuilder eventBuilder = new GraphEventBuilder(url);
        StreamReader reader =
                readerFactory.createStreamReader(streamType, container,
                eventBuilder);
        reader.setReport(report);

        StreamingConnection connection = new StreamingConnection(url, reader);

        connection.addStatusListener(
                new StreamingConnection.StatusListener() {

            public void onConnectionClosed(StreamingConnection connection) {
                container.waitForDispatchAllEvents();
                container.stop();
            }

            public void onDataReceived(StreamingConnection connection) { }
            public void onError(StreamingConnection connection) { }
        });

        return connection;
    }

}
