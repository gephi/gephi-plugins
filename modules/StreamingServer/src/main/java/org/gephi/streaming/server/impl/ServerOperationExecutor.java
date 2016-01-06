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
package org.gephi.streaming.server.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.CompositeGraphEventHandler;
import org.gephi.streaming.api.Graph2EventListener;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class ServerOperationExecutor {
    
    private final GraphBufferedEventHandler graphBufferedOperationSupport;
    private final GraphUpdaterEventHandler graphUpdaterOperationSupport;
    private final Graph graph;
    private final StreamWriterFactory writerFactory;
    private final StreamReaderFactory readerFactory;
    private boolean sendVizData = true;
    private final GraphEventBuilder eventBuilder;
    private ClientManagerImpl clientManager;
    
    public ServerOperationExecutor(Graph graph, ClientManagerImpl clientManager) {
        graphBufferedOperationSupport = new GraphBufferedEventHandler(graph);
        graphUpdaterOperationSupport = new GraphUpdaterEventHandler(graph);
        this.graph = graph;
        this.clientManager = clientManager;
        writerFactory = Lookup.getDefault().lookup(StreamWriterFactory.class);
        readerFactory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        eventBuilder = new GraphEventBuilder(this);

        Graph2EventListener changeListener = new Graph2EventListener(graph, graphBufferedOperationSupport);
        graph.getGraphModel().addGraphListener(changeListener);
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel().addAttributeListener(changeListener);
    }
    
    /**
     * Test with <br>
     * curl http://localhost:8080/graphstream?operation=getGraph&format=DGS
     * 
     * @param format
     * @param outputStream
     */
    public void executeGetGraph(String format, final String clientId, OutputStream outputStream) throws IOException {
        
        final StreamWriter writer = writerFactory.createStreamWriter(format, outputStream);
        writer.startStream();

        GraphEventHandler wrapper = new GraphEventHandler() {

            public void handleGraphEvent(GraphEvent event) {
                try {
                    writer.handleGraphEvent(event);
                } catch (RuntimeException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof SocketException || cause instanceof EOFException) {
                        System.out.println("*Socket closed*");
                        graphBufferedOperationSupport.removeHandler(this);
                        clientManager.remove(clientId);
                    } else {
                        throw e;
                    }
                }
            }
        };

        graphBufferedOperationSupport.addHandler(wrapper);
    }
     
    /**
     * Test with <br>
     * curl "http://localhost:8080/graphstream?operation=getNode&id=0201485419&format=DGS"
     * 
     * @param id
     * @param format
     * @param outputStream
     * @throws IOException
     */
    public void executeGetNode(String id, String format, OutputStream outputStream) throws IOException {
        StreamWriter writer = writerFactory.createStreamWriter(format, outputStream);
        writer.startStream();
        
        graph.readLock();
        try {
            Node node = graph.getNode(id);
            if (node != null) {
                String nodeId = node.getNodeData().getId();
                writer.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, getNodeAttributes(node)));
            }
        } finally {
            graph.readUnlock();
        }
        
        writer.endStream();
    }
    
    /**
     * Test with <br>
     * curl http://localhost:8080/graphstream?operation=getEdge&id=0201485419_0321335708&format=DGS
     * 
     * @param id
     * @param format
     * @param outputStream
     * @throws IOException
     */
    public void executeGetEdge(String id, String format, OutputStream outputStream) throws IOException {
        
        StreamWriter writer = writerFactory.createStreamWriter(format, outputStream);
        writer.startStream();
        
        graph.readLock();
        try {
            Edge edge = graph.getEdge(id);
            if (edge != null) {
                String edgeId = edge.getEdgeData().getId();
                String sourceId = edge.getSource().getNodeData().getId();
                String targetId = edge.getTarget().getNodeData().getId();
                writer.handleGraphEvent(eventBuilder.edgeAddedEvent(edgeId, sourceId, targetId, edge.isDirected(), getEdgeAttributes(edge)));
            }
        } finally {
            graph.readUnlock();
        }
        writer.endStream();
    }
    
    /**
     * Test with: <br>
     * curl "http://localhost:8080/graphstream?operation=updateGraph&format=DGS" -d "DGS004<br>
     * updatedObjects  0 0<br>
     * an 1111"
     * 
     * <p>The entire graph can be loaded using<br>
     * curl "http://localhost:8080/graphstream?operation=updateGraph&format=DGS" --data-binary @amazon.dgs
     * 
     * 
     * @param format
     * @param inputStream
     * @param outputStream
     * @throws IOException
     */
    public void executeUpdateGraph(String format, InputStream inputStream, OutputStream outputStream) 
    throws IOException {
        
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//        String line;
//        while ((line=reader.readLine())!=null)
//            System.out.println(line);
//        outputStream.close();
        
        StreamWriter writer = writerFactory.createStreamWriter(format, outputStream);
        CompositeGraphEventHandler cos = new CompositeGraphEventHandler();
        cos.addHandler(graphUpdaterOperationSupport);
        cos.addHandler(writer);
//        cos.addHandler(graphBufferedOperationSupport);
        
        StreamReader reader = readerFactory.createStreamReader(format, cos, eventBuilder);
        reader.processStream(inputStream);
    }
    
    private Map<String, Object> getNodeAttributes(Node node) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();

        if (row != null)
            for (AttributeValue attributeValue: row.getValues()) {
                if (attributeValue.getColumn().getIndex()!=PropertiesColumn.NODE_ID.getIndex()
                        && attributeValue.getValue()!=null)
                    attributes.put(attributeValue.getColumn().getTitle(), attributeValue.getValue());
            }

        if (sendVizData) {
            attributes.put("x", node.getNodeData().x());
            attributes.put("y", node.getNodeData().y());
            attributes.put("z", node.getNodeData().z());

            attributes.put("r", node.getNodeData().r());
            attributes.put("g", node.getNodeData().g());
            attributes.put("b", node.getNodeData().b());

            attributes.put("size", node.getNodeData().getSize());
        }

        return attributes;
    }

    private Map<String, Object> getEdgeAttributes(Edge edge) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
        if (row != null)
            for (AttributeValue attributeValue: row.getValues()) {
                if (attributeValue.getColumn().getIndex()!=PropertiesColumn.EDGE_ID.getIndex()
                        && attributeValue.getValue()!=null)
                     attributes.put(attributeValue.getColumn().getTitle(), attributeValue.getValue());
            }

        if (sendVizData) {
            
            attributes.put("r", edge.getEdgeData().r());
            attributes.put("g", edge.getEdgeData().g());
            attributes.put("b", edge.getEdgeData().b());
            
            attributes.put("weight", edge.getWeight());
        }

        return attributes;
    }
}
