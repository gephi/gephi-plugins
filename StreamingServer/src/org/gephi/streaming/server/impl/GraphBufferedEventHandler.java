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
package org.gephi.streaming.server.impl;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterator;
import org.gephi.streaming.api.CompositeGraphEventHandler;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.GraphEventHandler;

/**
 * @author panisson
 *
 */
public class GraphBufferedEventHandler extends CompositeGraphEventHandler {
    
    private GraphUpdaterEventHandler updater;
    private GraphWriter graphWriter;

    public GraphBufferedEventHandler(Graph graph) {
        this.updater = new GraphUpdaterEventHandler(graph);

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
    public void addHandler(GraphEventHandler operationSupport) {
        graphWriter.writeGraph(operationSupport);
        super.addHandler(operationSupport);
    }
}
