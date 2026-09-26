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
package org.gephi.streaming.server.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
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
import static org.junit.Assert.assertTrue;
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

//        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
//        ac.getModel();
        
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        
        Graph graph = graphModel.getGraph();

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
        GraphObserver graphObserver = graphModel.createGraphObserver(graph, true);
//        Graph2EventListener listener = new Graph2EventListener(graph, streamWriter);
//        graphModel.addGraphListener(listener);
//        ac.getModel().addAttributeListener(listener);

        StreamingConnection connection = 
                connectToStream(url, streamType, composite);
        
        connection.process();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertTrue(graphObserver.hasGraphChanged());
        listener.graphChanged(graphObserver.getDiff());

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
