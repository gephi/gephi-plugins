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
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.openide.util.Lookup;


/**
 *
 * @author panisson
 */
public class StreamingClient {

    private static final Logger logger = Logger.getLogger(StreamingClient.class.getName());

    private final Report report;
    private Graph graph;
    private GraphStreamingEndpoint endpoint;
    Set<FilteredEventEntry> filterededIds = new HashSet<FilteredEventEntry>();
    
    public StreamingClient(GraphStreamingEndpoint endpoint, Graph graph) {
        this.graph = graph;
        this.report = new Report();
        this.endpoint = endpoint;
    }

    public StreamingConnection process(GraphStreamingEndpoint endpoint) throws IOException {
        return process((StreamingConnection.StatusListener)null);
    }

    public StreamingConnection process(StreamingConnection.StatusListener statusListener)
            throws IOException {

        logger.log(Level.FINE, "Connecting to url {0}", endpoint.getUrl().toString());

        final GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);
        graphUpdaterHandler.setReport(getReport());
        GraphEventHandler filteredHandler = new GraphEventHandler() {

            public void handleGraphEvent(GraphEvent event) {
                if (event instanceof ElementEvent) {
                    ElementEvent elementEvent = (ElementEvent)event;
                    filterededIds.add(new FilteredEventEntry(elementEvent.getElementId(), elementEvent.getElementType(), 0));
                }

                graphUpdaterHandler.handleGraphEvent(event);
            }
        };

        GraphEventContainerFactory containerfactory =
                Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        final GraphEventContainer container =
                containerfactory.newGraphEventContainer(filteredHandler);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(endpoint.getUrl());
        StreamReaderFactory readerFactory =
                Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader reader =
                readerFactory.createStreamReader(endpoint.getStreamType(), container, eventBuilder);
        reader.setReport(getReport());
        StreamingConnection connection = new StreamingConnection(endpoint.getUrl(), reader);
        connection.addStatusListener(
                new StreamingConnection.StatusListener() {

                public void onConnectionClosed(StreamingConnection connection) {
                    container.waitForDispatchAllEvents();
                    container.stop();
                }

                public void onDataReceived(StreamingConnection connection) { }
                public void onError(StreamingConnection connection) { }
            });
        if (statusListener!=null) {
            connection.addStatusListener(statusListener);
        }
        connection.asynchProcess();
        ClientEventHandler handler = new ClientEventHandler();

        Graph2EventListener graph2EventListener = new Graph2EventListener(graph, handler);

        logger.log(Level.INFO, "Connected to url {0}", endpoint.getUrl().toString());

        return connection;
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    private class ClientEventHandler implements GraphEventHandler {

        public void handleGraphEvent(GraphEvent event) {
            logger.log(Level.INFO, "Handling event {0}", event.toString());
            if (event instanceof ElementEvent) {
                ElementEvent elementEvent = (ElementEvent)event;
                if (!filterededIds.contains(new FilteredEventEntry(elementEvent.getElementId(), elementEvent.getElementType(), 0))) {
                    sendEvent(event);
                }
            }
        }
    }

    private void sendEvent(GraphEvent event) {
        logger.log(Level.INFO, "Sending event {0}", event.toString());
        try {
            URL url = new URL(endpoint.getUrl(), 
                    endpoint.getUrl().getFile()+"?operation=updateGraph&format="
                    + endpoint.getStreamType().getType());

            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.connect();

            StreamWriterFactory writerFactory =
                    Lookup.getDefault().lookup(StreamWriterFactory.class);
            OutputStream out = connection.getOutputStream();
            StreamWriter writer = writerFactory.createStreamWriter(endpoint.getStreamType(), out);
            writer.handleGraphEvent(event);
            out.flush();
            out.close();
            connection.getInputStream().close();
            
        } catch (IOException ex) {
            logger.log(Level.WARNING, null, ex);
        }
    }

    private class FilteredEventEntry {

        private final String elementId;
        private final ElementType elementType;
        private final long timestamp;

        public FilteredEventEntry(String elementId, ElementType elementType, long timestamp) {
            this.elementId = elementId;
            this.elementType = elementType;
            this.timestamp = timestamp;
        }
        
        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) return true;
            if ( obj == null || obj.getClass() != this.getClass() ) return false;

            FilteredEventEntry e = (FilteredEventEntry)obj;
            return this.elementType == e.elementType
                && this.elementId.equals(e.elementId);
        }

        @Override
        public int hashCode() {
            return this.elementType.hashCode() * 31 + this.elementId.hashCode();
        }
    }

}
