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
package org.gephi.streaming.test;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
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
        graphModel = graphController.getGraphModel();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdateGraph() {
        Graph graph = graphModel.getGraph();
        GraphUpdaterEventHandler operationSupport = new GraphUpdaterEventHandler(graph);
        Map<String,Object> attributes = new HashMap<String, Object>();
        GraphEventBuilder eventBuilder = new GraphEventBuilder(this);

        attributes.clear();
        attributes.put("label", "1");
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "1", attributes));
        Node node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("1", node1.getAttribute("label"));

        attributes.clear();
        attributes.put("label", "2");
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "2", attributes));
        Node node2 = graph.getNode("2");
        assertNotNull(node2);
        assertEquals("2", node2.getAttribute("label"));

        attributes.clear();
        attributes.put("weight", 0.5f);
        operationSupport.handleGraphEvent(eventBuilder.edgeAddedEvent("1_2", "1", "2", false, attributes));
        Edge edge1_2 = graph.getEdge("1_2");
        assertNotNull(edge1_2);
        assertEquals(0.5f, edge1_2.getWeight(), 1.0e-5);

        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, "1", attributes));
        node1 = graph.getNode("1");
        assertNull(node1);

        edge1_2 = graph.getEdge("1_2");
        assertNull(edge1_2);

        attributes.clear();
        attributes.put("label", "1");
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "1", attributes));
        node1 = graph.getNode("1");
        assertNotNull(node1);

        attributes.clear();
        attributes.put("weight", 0.1f);
        operationSupport.handleGraphEvent(eventBuilder.edgeAddedEvent("1_2", "1", "2", false, attributes));
        edge1_2 = graph.getEdge("1_2");
        assertNotNull(edge1_2);
        assertEquals(0.1f, edge1_2.getWeight(), 1.0e-5);

        attributes.clear();
        attributes.put("label", "Node 1");
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "1", attributes));
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getAttribute("label"));

        attributes.clear();
        attributes.put("label", null);
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "1", attributes));
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertNull("Label should be null but was "+
                node1.getAttribute("label"),
            node1.getAttribute("label"));

        attributes.clear();
        attributes.put("label", "Node 1");
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "1", attributes));

        attributes.clear();
        attributes.put("myattribute", "myattributevalue");
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "1", attributes));
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getAttribute("label"));
        assertEquals("myattributevalue", node1.getAttribute("myattribute"));

        attributes.clear();
        attributes.put("myattribute", null);
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "1", attributes));
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getAttribute("label"));
        assertNull(node1.getAttribute("myattribute"));

        attributes.clear();
        attributes.put("label", "Node 1");
        attributes.put("size", 5);
        attributes.put("r", 0.5);
        attributes.put("g", 0.6);
        attributes.put("b", 0.7);
        attributes.put("x", 1);
        attributes.put("y", 2);
        attributes.put("z", 3);
        operationSupport.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "1", attributes));
        node1 = graph.getNode("1");
        assertNotNull(node1);
        assertEquals("Node 1", node1.getAttribute("label"));
        assertEquals(5, node1.size(), 1.0e-5);
        assertEquals(0.5, node1.r(), 1.0e-2);
        assertEquals(0.6, node1.g(), 1.0e-2);
        assertEquals(0.7, node1.b(), 1.0e-2);
        assertEquals(1, node1.x(), 1.0e-5);
        assertEquals(2, node1.y(), 1.0e-5);
        assertEquals(3, node1.z(), 1.0e-5);

    }

}
