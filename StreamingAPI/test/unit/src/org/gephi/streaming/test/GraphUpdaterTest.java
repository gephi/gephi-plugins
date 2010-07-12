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

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.streaming.api.GraphUpdaterOperationSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;

public class GraphUpdaterTest {

    private GraphModel graphModel;

    public GraphUpdaterTest() {
    }

    @Before
    public void setUp() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdateGraph() {
        Graph graph = graphModel.getMixedGraph();
        GraphUpdaterOperationSupport operationSupport = new GraphUpdaterOperationSupport(graph);
        Map<String,Object> attributes = new HashMap<String, Object>();

        attributes.clear();
        attributes.put("label", "1");
        operationSupport.nodeAdded("1", attributes);
        Node node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("1", node1.getNodeData().getAttributes().getValue("label"));

        attributes.clear();
        attributes.put("label", "2");
        operationSupport.nodeAdded("2", attributes);
        Node node2 = graph.getNode("2");
        assertNotNull(node2);
        assertEquals("2", node2.getNodeData().getAttributes().getValue("label"));

        attributes.clear();
        attributes.put("weight", 0.5f);
        operationSupport.edgeAdded("1_2", "1", "2", false, attributes);
        Edge edge1_2 = graph.getEdge("1_2");
        assertNotNull(edge1_2);
        assertEquals(0.5f, edge1_2.getWeight(), 1.0e-5);

        operationSupport.nodeRemoved("1");
        node1 = graph.getNode("1");
        assertNull(node1);

        edge1_2 = graph.getEdge("1_2");
        assertNull(edge1_2);

        attributes.clear();
        attributes.put("label", "1");
        operationSupport.nodeAdded("1", attributes);
        node1 = graph.getNode("1");
        assertNotNull(node1);

        attributes.clear();
        attributes.put("weight", 0.1f);
        operationSupport.edgeAdded("1_2", "1", "2", false, attributes);
        edge1_2 = graph.getEdge("1_2");
        assertNotNull(edge1_2);
        assertEquals(0.1f, edge1_2.getWeight(), 1.0e-5);

        attributes.clear();
        attributes.put("label", "Node 1");
        operationSupport.nodeChanged("1", attributes);
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getNodeData().getAttributes().getValue("label"));

        attributes.clear();
        attributes.put("label", null);
        operationSupport.nodeChanged("1", attributes);
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertNull("Label should be null but was "+
                node1.getNodeData().getAttributes().getValue("label"),
            node1.getNodeData().getAttributes().getValue("label"));

        attributes.clear();
        attributes.put("label", "Node 1");
        operationSupport.nodeChanged("1", attributes);

        attributes.clear();
        attributes.put("myattribute", "myattributevalue");
        operationSupport.nodeChanged("1", attributes);
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getNodeData().getAttributes().getValue("label"));
        assertEquals("myattributevalue", node1.getNodeData().getAttributes().getValue("myattribute"));

        attributes.clear();
        attributes.put("myattribute", null);
        operationSupport.nodeChanged("1", attributes);
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getNodeData().getAttributes().getValue("label"));
        assertNull(node1.getNodeData().getAttributes().getValue("myattribute"));

        attributes.clear();
        attributes.put("label", "Node 1");
        attributes.put("size", 5);
        attributes.put("r", 0.5);
        attributes.put("g", 0.6);
        attributes.put("b", 0.7);
        attributes.put("x", 1);
        attributes.put("y", 2);
        attributes.put("z", 3);
        operationSupport.nodeChanged("1", attributes);
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getNodeData().getAttributes().getValue("label"));
        assertEquals(5, node1.getNodeData().getSize(), 1.0e-5);
        assertEquals(0.5, node1.getNodeData().r(), 1.0e-5);
        assertEquals(0.6, node1.getNodeData().g(), 1.0e-5);
        assertEquals(0.7, node1.getNodeData().b(), 1.0e-5);
        assertEquals(1, node1.getNodeData().x(), 1.0e-5);
        assertEquals(2, node1.getNodeData().y(), 1.0e-5);
        assertEquals(3, node1.getNodeData().z(), 1.0e-5);

    }

}
