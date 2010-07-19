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
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.Graph;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.openide.util.Lookup;


/**
 *
 * @author panisson
 */
public class StreamingClient {

    private final Report report;
    private Graph graph;
    
    public StreamingClient(Graph graph) {
        this.graph = graph;
        this.report = new Report();
    }

    public StreamingConnection process(GraphStreamingEndpoint endpoint) throws IOException {
        return process(endpoint, null);
    }

    public StreamingConnection process(GraphStreamingEndpoint endpoint,
                StreamingConnectionStatusListener statusListener) throws IOException {


        GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);
        graphUpdaterHandler.setReport(getReport());

        GraphEventContainerFactory containerfactory =
                Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        final GraphEventContainer container =
                containerfactory.newGraphEventContainer(graphUpdaterHandler);

        GraphEventBuilder eventBuilder = new GraphEventBuilder(endpoint.getUrl());
        StreamReaderFactory readerFactory =
                Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader reader =
                readerFactory.createStreamReader(endpoint.getStreamType(), container, eventBuilder);
        reader.setReport(getReport());

        StreamingConnection connection = new StreamingConnection(endpoint.getUrl(), reader);
        connection.addStreamingConnectionStatusListener(
                new StreamingConnectionStatusListener() {

                public void onConnectionClosed(StreamingConnection connection) {
                    container.waitForDispatchAllEvents();
                    container.stop();

                    // TODO: show stream report
                    System.out.println("-- Stream report -----\n"+getReport().getText()+"--------");
                }
            });
        if (statusListener!=null) {
            connection.addStreamingConnectionStatusListener(statusListener);
        }
        connection.start();

        final AtomicInteger counter = new AtomicInteger();
        GraphEventHandler eventHandler = new GraphEventHandler() {
            public void handleGraphEvent(GraphEvent event) {
                counter.incrementAndGet();
            }
        };

        return connection;
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

}
