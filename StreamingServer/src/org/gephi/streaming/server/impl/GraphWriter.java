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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.streaming.api.CompositeGraphEventHandler;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;

/**
 * @author panisson
 *
 */
public class GraphWriter extends CompositeGraphEventHandler {
    
    private Graph graph;
    private boolean sendVizData;
    private final GraphEventBuilder eventBuilder;

    public GraphWriter(Graph graph, boolean sendVizData) {
        this.graph = graph;
        this.sendVizData = sendVizData;
        eventBuilder = new GraphEventBuilder(this);
    }
    
    public void writeGraph(GraphEventHandler operationSupport) {

        Set<String> writtenNodes = new HashSet<String>();
        Set<String> writtenEdges = new HashSet<String>();
        
        try {
            graph.readLock();
            
            for (Node node: graph.getNodes()) {
                String nodeId = node.getNodeData().getId();
                operationSupport.handleGraphEvent(
                        eventBuilder.graphEvent(ElementType.NODE,
                        EventType.ADD, nodeId, getNodeAttributes(node)));
                writtenNodes.add(nodeId);

                for (Edge edge: graph.getEdges(node)) {
                    String edgeId = edge.getEdgeData().getId();
                    if (writtenEdges.contains(edgeId)) continue;
                    String sourceId = edge.getSource().getNodeData().getId();
                    String targetId = edge.getTarget().getNodeData().getId();
                    if (writtenNodes.contains(sourceId)
                        && writtenNodes.contains(targetId) ) {
                        operationSupport.handleGraphEvent(
                                eventBuilder.edgeAddedEvent(edgeId, sourceId,
                                targetId, edge.isDirected(), getEdgeAttributes(edge)));
                        writtenEdges.add(edgeId);
                    }
                }

            }
            
//            for (Edge edge: graph.getEdges()) {
//                String edgeId = edge.getEdgeData().getId();
//                String sourceId = edge.getSource().getNodeData().getId();
//                String targetId = edge.getTarget().getNodeData().getId();
//                operationSupport.handleGraphEvent(
//                        eventBuilder.edgeAddedEvent(edgeId, sourceId,
//                        targetId, edge.isDirected(), getEdgeAttributes(edge)));
//            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graph.readUnlock();
        }
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
