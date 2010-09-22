/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.gephi.streaming.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.Graph;
import org.gephi.streaming.api.Graph2EventListener;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.StreamingEndpoint;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamType;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.StreamingController;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author panisson
 */
@ServiceProvider(service = StreamingController.class)
public class StreamingControllerImpl implements StreamingController {

    private static final Logger logger = Logger.getLogger(StreamingControllerImpl.class.getName());

    @Override
    public StreamType getStreamType(String streamType) {
        Collection<? extends StreamType> streamTypes = Lookup.getDefault().lookupAll(StreamType.class);
        for (StreamType type: streamTypes) {
            if(type.getType().equalsIgnoreCase(streamType)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public StreamingConnection connect(StreamingEndpoint endpoint, Graph graph) throws IOException {
        return connect(endpoint, graph, new Report());
    }

    @Override
    public StreamingConnection connect(StreamingEndpoint endpoint, final Graph graph,
            Report report) throws IOException {
        logger.log(Level.FINE, "Connecting to url {0}", endpoint.getUrl().toString());
        
        final Set<FilteredEventEntry> filterededIds = new HashSet<FilteredEventEntry>();

        // Register listener for graph events

        ClientEventHandler handler = new ClientEventHandler(endpoint, filterededIds);
        final Graph2EventListener graph2EventListener = new Graph2EventListener(graph, handler);

        graph.getGraphModel().addGraphListener(graph2EventListener);
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel().addAttributeListener(graph2EventListener);

        if (report!=null) {
            report.setSource(endpoint.getUrl().toString());
        }

        // Connect to stream

        final GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);
        graphUpdaterHandler.setReport(report);
        GraphEventHandler filteredHandler = new GraphEventHandler() {

            @Override
            public void handleGraphEvent(GraphEvent event) {
                logger.log(Level.INFO, "Received event {0}", event.toString());
                if (event instanceof ElementEvent) {
                    ElementEvent elementEvent = (ElementEvent)event;
                    if (elementEvent.getElementId()!=null) {
                        filterededIds.add(new FilteredEventEntry(elementEvent.getElementId(), elementEvent.getElementType(), 0));
                    }
                }

                graphUpdaterHandler.handleGraphEvent(event);
            }
        };

        final GraphEventContainer container =  new GraphEventContainer(filteredHandler);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(endpoint.getUrl());
        StreamReaderFactory readerFactory =
                Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader reader =
                readerFactory.createStreamReader(endpoint.getStreamType(), container, eventBuilder);
        reader.setReport(report);
        StreamingConnection connection = new StreamingConnectionImpl(endpoint, reader, report);
        connection.addStatusListener(
                new StreamingConnection.StatusListener() {

            @Override
                public void onConnectionClosed(StreamingConnection connection) {
                    graph.getGraphModel().removeGraphListener(graph2EventListener);
                    AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
                    ac.getModel().removeAttributeListener(graph2EventListener);

                    container.waitForDispatchAllEvents();
                    container.stop();

                    logger.log(Level.INFO, "Connecion closed");
                }

            @Override
                public void onDataReceived(StreamingConnection connection) { }
            @Override
                public void onError(StreamingConnection connection) { }
            });

        logger.log(Level.INFO, "Connected to url {0}", endpoint.getUrl().toString());

        return connection;
    }

    private class ClientEventHandler implements GraphEventHandler {
        
        private StreamingEndpoint endpoint;
        private Set<FilteredEventEntry> filteredEvents;

        public ClientEventHandler(StreamingEndpoint endpoint, Set<FilteredEventEntry> filterededEvents) {
            this.endpoint = endpoint;
            this.filteredEvents = filterededEvents;
        }

        @Override
        public void handleGraphEvent(GraphEvent event) {
            logger.log(Level.INFO, "{0};{1};Sending event {2}",
                    new Object[]{Thread.currentThread().getId(), System.currentTimeMillis(), event.toString()});
            
            if (event instanceof ElementEvent) {
                ElementEvent elementEvent = (ElementEvent)event;
                FilteredEventEntry entry = new FilteredEventEntry(elementEvent.getElementId(), elementEvent.getElementType(), 0);
                if (!filteredEvents.contains(entry)) {
                    sendEvent(endpoint, event);
                } else {
                    filteredEvents.remove(entry);
                }
            }
        }
    }

    private void sendEvent(final StreamingEndpoint endpoint, GraphEvent event) {
        logger.log(Level.FINE, "Sending event {0}", event.toString());
        try {
            URL url = new URL(endpoint.getUrl(),
                    endpoint.getUrl().getFile()+"?operation=updateGraph&format="
                    + endpoint.getStreamType().getType());

            URLConnection connection = url.openConnection();

            connection.setRequestProperty("Authorization", "Basic " +
                    (new sun.misc.BASE64Encoder().encode((endpoint.getUser()+":"+endpoint.getPassword()).getBytes())));
            
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
            if (timestamp <= 0) {
                this.timestamp = System.currentTimeMillis();
            } else {
                this.timestamp = timestamp;
            }
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
