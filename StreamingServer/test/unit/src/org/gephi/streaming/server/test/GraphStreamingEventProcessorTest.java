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
package org.gephi.streaming.server.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeController;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.streaming.api.CompositeGraphEventHandler;
import org.gephi.streaming.api.Graph2EventListener;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.StreamingEndpoint;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.impl.StreamingConnectionImpl;
import org.gephi.streaming.server.impl.FilteredGraphEventHandler;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class GraphStreamingEventProcessorTest {
    
    private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";
    
    @Test
    public void testAll() throws IOException {
        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        projectController.newWorkspace(projectController.getCurrentProject());
        
        String streamType = "DGS";
        URL url = this.getClass().getResource(DGS_RESOURCE);

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel();
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        
        Graph graph = graphModel.getHierarchicalMixedGraph();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamWriterFactory factory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        StreamWriter streamWriter = factory.createStreamWriter(streamType, out);

        GraphEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);

        final Set<GraphEvent> processedEvents = Collections.synchronizedSet(new HashSet<GraphEvent>());
        GraphEventHandler eventCollector = new GraphEventHandler() {

            public void handleGraphEvent(GraphEvent event) {
                processedEvents.add(event);
            }
        };

        GraphEventHandler composite = new CompositeGraphEventHandler(graphUpdaterHandler, eventCollector);

        Graph2EventListener listener = new Graph2EventListener(graph, new FilteredGraphEventHandler(streamWriter, processedEvents));
//        Graph2EventListener listener = new Graph2EventListener(graph, streamWriter);
        graphModel.addGraphListener(listener);
        ac.getModel().addAttributeListener(listener);

        StreamingConnection connection = 
                connectToStream(url, streamType, composite);
        
        connection.process();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println(out.toString());
        
    }

   public static StreamingConnection connectToStream(
            URL url, String streamType, GraphEventHandler handler)
    throws IOException {
        StreamReaderFactory readerFactory =
                Lookup.getDefault().lookup(StreamReaderFactory.class);

        GraphEventBuilder eventBuilder = new GraphEventBuilder(url);
        StreamReader reader =
                readerFactory.createStreamReader(streamType, handler,
                eventBuilder);

        StreamingEndpoint endpoint = new StreamingEndpoint();
        endpoint.setUrl(url);
        StreamingConnection connection = new StreamingConnectionImpl(endpoint, reader, new Report());

        return connection;
    }
}
