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

package org.gephi.streaming.test;

import static org.junit.Assert.*;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.impl.Graph2EventListener;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author panisson
 */
public class Graph2EventListenerTest {

    @Test
    public void test() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        projectController.newWorkspace(projectController.getCurrentProject());

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getHierarchicalMixedGraph();
        GraphFactory factory = graphModel.factory();

        MockGraphEventHandler handler = new MockGraphEventHandler();
        final Graph2EventListener graph2EventListener = new Graph2EventListener(graph, handler);
        graph.getGraphModel().addGraphListener(graph2EventListener);

        Node node1 = factory.newNode();
        graph.addNode(node1);
        Node node2 = factory.newNode();
        graph.addNode(node2);

        Edge edge = factory.newEdge(node1, node2);
        graph.addEdge(edge);

        graph.removeEdge(edge);
        graph.removeNode(node1);
        graph.removeNode(node2);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) { }

        assertEquals(6, handler.getEventCount());
    }

}
