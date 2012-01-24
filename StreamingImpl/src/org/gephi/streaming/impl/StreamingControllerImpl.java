/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.streaming.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
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
                    Base64.encodeBase64((endpoint.getUser()+":"+endpoint.getPassword()).getBytes()));
            
            connection.setDoOutput(true);
            connection.connect();

            StreamWriterFactory writerFactory =
                    Lookup.getDefault().lookup(StreamWriterFactory.class);

            OutputStream out = null;
            try {
                out = connection.getOutputStream();
            } catch (UnknownServiceException e) {
                // protocol doesn't support output
                return;
            }
            StreamWriter writer = writerFactory.createStreamWriter(endpoint.getStreamType(), out);
            writer.handleGraphEvent(event);
            out.flush();
            out.close();
            connection.getInputStream().close();

        } catch (IOException ex) {
            logger.log(Level.FINE, null, ex);
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
