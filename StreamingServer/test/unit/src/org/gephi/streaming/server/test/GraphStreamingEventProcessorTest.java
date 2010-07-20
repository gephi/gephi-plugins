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
package org.gephi.streaming.server.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.data.attributes.api.AttributeController;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.CompositeGraphEventHandler;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.GraphStreamingUtils;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.StreamingConnection;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.server.FilteredGraphEventHandler;
import org.gephi.streaming.server.GraphChangeListener;
import org.junit.Test;
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
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
//        projectController.openWorkspace(workspace);
        
        String streamType = "DGS";
        URL url = this.getClass().getResource(DGS_RESOURCE);

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel();
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        
        Graph graph = graphModel.getHierarchicalMixedGraph();
        GraphChangeListener listener = new GraphChangeListener(graph);
        
        graphModel.addGraphListener(listener);

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

        listener.setOperationSupport(new FilteredGraphEventHandler(streamWriter, processedEvents));
//        listener.setOperationSupport(streamWriter);

        StreamingConnection connection = 
                GraphStreamingUtils.connectToStream(url, streamType, composite);
        
        final AtomicBoolean processing = new AtomicBoolean(true);
        connection.addStatusListener(
            new StreamingConnection.StatusListener() {
                public void onConnectionClosed(StreamingConnection connection) {
                    processing.set(false);
                    synchronized (processing) {
                        processing.notifyAll();
                    }
                }
                public void onDataReceived(StreamingConnection connection) { }
                public void onError(StreamingConnection connection) { }
            });
        connection.asynchProcess();

        synchronized (processing) {
            try {
                while (processing.get()) {
                    processing.wait();
                }
            } catch (InterruptedException e) {}
        }

        System.out.println(out.toString());
        
    }

}
