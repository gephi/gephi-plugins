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
package org.gephi.streaming.server;

import java.util.Map;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterator;
import org.gephi.streaming.api.CompositeOperationSupport;
import org.gephi.streaming.api.GraphUpdaterOperationSupport;
import org.gephi.streaming.api.OperationSupport;

/**
 * @author panisson
 *
 */
public class GraphBufferedOperationSupport extends CompositeOperationSupport {
    
    private GraphUpdaterOperationSupport updater;
    private GraphWriter graphWriter;

    public GraphBufferedOperationSupport(Graph graph) {
        this.updater = new GraphUpdaterOperationSupport(graph);

        Node firstNode = null;
        NodeIterator iterator = graph.getNodes().iterator();
        if (iterator.hasNext())
            firstNode = iterator.next();
        if (firstNode!=null && firstNode.getNodeData().getAttributes().getValue("dynamicrange")!=null) {
            this.graphWriter = new DynamicGraphWriter(graph, false);
        } else {
            this.graphWriter = new GraphWriter(graph, true);
        }
    }
    
    @Override
    public void addOperationSupport(OperationSupport operationSupport) {
        writeGraph(operationSupport);
        super.addOperationSupport(operationSupport);
    }

    @Override
    public void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        updater.edgeAdded(edgeId, fromNodeId, toNodeId, directed, attributes);
        super.edgeAdded(edgeId, fromNodeId, toNodeId, directed, attributes);
    }

    @Override
    public void edgeRemoved(String edgeId) {
        updater.edgeRemoved(edgeId);
        super.edgeRemoved(edgeId);
    }

    @Override
    public void graphAttributeAdded(String attributeName, Object value) {
        updater.graphAttributeAdded(attributeName, value);
        super.graphAttributeAdded(attributeName, value);
    }

    @Override
    public void graphAttributeChanged(String attributeName, Object newValue) {
        updater.graphAttributeChanged(attributeName, newValue);
        super.graphAttributeChanged(attributeName, newValue);
    }

    @Override
    public void graphAttributeRemoved(String attributeName) {
        updater.graphAttributeRemoved(attributeName);
        super.graphAttributeRemoved(attributeName);
    }

    @Override
    public void nodeAdded(String nodeId, Map<String, Object> attributes) {
        updater.nodeAdded(nodeId, attributes);
        super.nodeAdded(nodeId, attributes);
    }

    @Override
    public void nodeRemoved(String nodeId) {
        updater.nodeRemoved(nodeId);
        super.nodeRemoved(nodeId);
    }
    
    private void writeGraph(OperationSupport operationSupport) {
        graphWriter.writeGraph(operationSupport);
    }
}
