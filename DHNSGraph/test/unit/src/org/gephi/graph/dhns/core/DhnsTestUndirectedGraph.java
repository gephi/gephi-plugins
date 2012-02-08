/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.graph.dhns.core;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalUndirectedGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class DhnsTestUndirectedGraph {

    private Dhns dhnsGlobal;
    private HierarchicalUndirectedGraphImpl graphGlobal;
    private Map<String, Node> nodeMap;
    private Map<String, Edge> edgeMap;

    public DhnsTestUndirectedGraph() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        DhnsGraphController controller = new DhnsGraphController();
        dhnsGlobal = new Dhns(controller, null);
        graphGlobal = new HierarchicalUndirectedGraphImpl(dhnsGlobal, dhnsGlobal.getGraphStructure().getMainView());
        HierarchicalDirectedGraphImpl diGraph = new HierarchicalDirectedGraphImpl(dhnsGlobal, dhnsGlobal.getGraphStructure().getMainView());
        nodeMap = new HashMap<String, Node>();
        edgeMap = new HashMap<String, Edge>();

        TreeStructure treeStructure = dhnsGlobal.getGraphStructure().getMainView().getStructure();
        GraphFactoryImpl factory = dhnsGlobal.factory();

        //Nodes
        //System.out.println("-----Global-----");
        for (int i = 0; i < 10; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graphGlobal.addNode(node);
            nodeMap.put(node.getNodeData().getLabel(), node);
            //System.out.println("Node " + i + " added. Id = " + node.getId());
        }
        //System.out.println("---End Global---");

        //Alone node
        Node fakeNode1 = factory.newNode();
        Node fakeNode2 = factory.newNode();
        nodeMap.put("Fake Node 1", fakeNode1);
        nodeMap.put("Fake Node 2", fakeNode2);

        //Edges
        Node node1 = nodeMap.get("Node 1");
        Node node2 = nodeMap.get("Node 2");
        Node node3 = nodeMap.get("Node 3");
        Node node4 = nodeMap.get("Node 4");
        Node node5 = nodeMap.get("Node 5");
        Node node6 = nodeMap.get("Node 6");
        Node node7 = nodeMap.get("Node 7");
        Node node8 = nodeMap.get("Node 8");

        AbstractEdge edge1 = factory.newEdge(node4, node5);
        AbstractEdge edge2 = factory.newEdge(node5, node6);
        AbstractEdge edge3 = factory.newEdge(node6, node5);
        AbstractEdge edge4 = factory.newEdge(node7, node7);
        AbstractEdge edge5 = factory.newEdge(node4, node4);

        diGraph.addEdge(edge1);
        diGraph.addEdge(edge2);
        diGraph.addEdge(edge3);
        diGraph.addEdge(edge4);
        diGraph.addEdge(edge5);

        edgeMap.put("4-5", edge1);
        edgeMap.put("5-6", edge2);
        edgeMap.put("6-5", edge3);
        edgeMap.put("7-7", edge4);
        edgeMap.put("4-4", edge5);
    }

    @After
    public void tearDown() {
        nodeMap.clear();
        dhnsGlobal = null;
        graphGlobal = null;
    }

    @Test
    public void testAddEdge() {
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        HierarchicalUndirectedGraphImpl graph = new HierarchicalUndirectedGraphImpl(dhns, dhns.getGraphStructure().getMainView());
        TreeStructure treeStructure = dhns.getGraphStructure().getMainView().getStructure();
        GraphFactoryImpl factory = dhns.factory();

        Node node1 = factory.newNode();
        Node node2 = factory.newNode();
        Node node3 = factory.newNode();
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);

        //Test normal edge
        graph.addEdge(node1, node2);
        AbstractNode preNode1 = (AbstractNode) node1;
        AbstractNode preNode2 = (AbstractNode) node2;

        AbstractEdge edge = preNode1.getEdgesOutTree().getItem(preNode2.getNumber());
        assertNotNull("find OUT edge", edge);
        assertTrue("contains OUT edge", preNode1.getEdgesOutTree().contains(edge));

        AbstractEdge edge2 = preNode2.getEdgesInTree().getItem(preNode1.getNumber());
        assertNotNull("find IN edge", edge);
        assertTrue("contains IN edge", preNode2.getEdgesInTree().contains(edge2));
        assertSame("edges equal", edge, edge2);
        assertEquals("edges count", 1, graph.getEdgeCount());

        //Test mutual edge add fail
        graph.addEdge(node2, node1);

        assertNull("cant find OUT edge", preNode2.getEdgesOutTree().getItem(preNode1.getNumber()));
        assertEquals("edges count", 1, graph.getEdgeCount());


        //Test factoryedge
        graph.addEdge(edge);
        assertEquals("edges count", 1, graph.getEdgeCount());

        //Test self loop
        graph.addEdge(node3, node3);

        AbstractNode preNode3 = (AbstractNode) node3;

        AbstractEdge edge3 = preNode3.getEdgesOutTree().getItem(preNode3.getNumber());
        assertNotNull("find OUT edge", edge);
        assertTrue("contains OUT edge", preNode3.getEdgesOutTree().contains(edge3));

        AbstractEdge edge4 = preNode3.getEdgesInTree().getItem(preNode3.getNumber());
        assertNotNull("find IN edge", edge);
        assertTrue("contains IN edge", preNode3.getEdgesInTree().contains(edge3));

        assertSame("edges equal", edge3, edge4);

        assertTrue("is self loop", edge3.isSelfLoop());
    }

    @Test
    public void testRemoveEdge() {
        GraphFactoryImpl factory = dhnsGlobal.factory();
        AbstractNode node3 = (AbstractNode) nodeMap.get("Node 1");
        AbstractNode node4 = (AbstractNode) nodeMap.get("Node 2");
        AbstractNode node5 = (AbstractNode) nodeMap.get("Node 5");
        AbstractNode node6 = (AbstractNode) nodeMap.get("Node 6");
        AbstractEdge edge = factory.newEdge(node3, node4);

        graphGlobal.addEdge(edge);

        graphGlobal.removeEdge(edge);
        AbstractEdge edge3 = node3.getEdgesOutTree().getItem(node4.getNumber());
        assertNull("OUT null", edge3);
        assertFalse("contains OUT edge", node3.getEdgesOutTree().contains(edge));

        AbstractEdge edge4 = node4.getEdgesInTree().getItem(node3.getNumber());
        assertNull("IN null", edge4);
        assertFalse("contains IN edge", node3.getEdgesInTree().contains(edge));
        assertFalse(graphGlobal.contains(edge));

        graphGlobal.removeEdge(edgeMap.get("5-6"));

        assertNull(node5.getEdgesOutTree().getItem(node6.getNumber()));
        assertNull(node6.getEdgesOutTree().getItem(node5.getNumber()));
        assertNull(node5.getEdgesInTree().getItem(node6.getNumber()));
        assertNull(node6.getEdgesInTree().getItem(node5.getNumber()));

        AbstractNode node7 = (AbstractNode) nodeMap.get("Node 7");
        graphGlobal.removeEdge(edgeMap.get("7-7"));

        assertNull(node7.getEdgesOutTree().getItem(node7.getNumber()));
        assertNull(node7.getEdgesInTree().getItem(node7.getNumber()));
    }

    @Test
    public void testGetEdges() {

        //Test1
        Edge[] expected = new Edge[4];
        expected[0] = edgeMap.get("4-4");
        expected[1] = edgeMap.get("4-5");
        expected[2] = edgeMap.get("5-6");
        expected[3] = edgeMap.get("7-7");
        Edge[] actual = new Edge[4];

        int i = 0;
        System.out.print("testGetEdges: ");
        for (Edge e : graphGlobal.getEdges()) {
            Node s = e.getSource();
            Node t = e.getTarget();
            Edge ed = edgeMap.get((s.getId() - 1) + "-" + (t.getId() - 1));
            assertSame("edge iterator", e, ed);
            System.out.print("#" + s.getId() + "-" + t.getId() + " ");
            actual[i++] = e;
        }
        System.out.println();
        assertArrayEquals(expected, actual);
        assertEquals("edge count", i, graphGlobal.getEdgeCount());


        graphGlobal.removeNode(nodeMap.get("Node 5"));

        //Test2
        expected = new Edge[2];
        expected[0] = edgeMap.get("4-4");
        expected[1] = edgeMap.get("7-7");
        actual = new Edge[2];
        i = 0;
        System.out.print("testGetEdges: ");
        for (Edge e : graphGlobal.getEdges()) {
            Node s = e.getSource();
            Node t = e.getTarget();
            Edge ed = edgeMap.get((s.getId() - 1) + "-" + (s.getId() - 1));
            assertSame("edge iterator", e, ed);
            System.out.print("#" + s.getId() + "-" + t.getId() + " ");
            actual[i++] = e;
        }
        System.out.println();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetUndirecteEdge() {

        Node node5 = nodeMap.get("Node 5");
        Node node6 = nodeMap.get("Node 6");

        Edge edge1 = graphGlobal.getEdge(node5, node6);
        Edge edge2 = graphGlobal.getEdge(node5, node6);
        assertSame(edge1, edge2);
        assertSame(edgeMap.get("5-6"), edge1);

        assertNull(graphGlobal.getEdge(nodeMap.get("Node 1"), nodeMap.get("Node 2")));
        assertNull(graphGlobal.getEdge(nodeMap.get("Node 5"), nodeMap.get("Node 5")));

        assertNotNull(graphGlobal.getEdge(nodeMap.get("Node 4"), nodeMap.get("Node 5")));
    }

    @Test
    public void testGetEdgesNode() {

        graphGlobal.addEdge(nodeMap.get("Node 5"), nodeMap.get("Node 3"));
        edgeMap.put("5-3", graphGlobal.getEdge(nodeMap.get("Node 5"), nodeMap.get("Node 3")));

        //Test1
        System.out.print("testGetEdgesNode: ");
        Edge[] expected = new Edge[3];
        expected[0] = edgeMap.get("5-3");
        expected[1] = edgeMap.get("5-6");
        expected[2] = edgeMap.get("4-5");
        Edge[] actual = new Edge[3];

        int i = 0;
        for (Edge e : graphGlobal.getEdges(nodeMap.get("Node 5"))) {
            Node s = e.getSource();
            Node t = e.getTarget();
            System.out.print("#" + s.getId() + "-" + t.getId() + " ");
            actual[i++] = e;
        }
        System.out.println();
        assertArrayEquals(expected, actual);

        //Test2
        System.out.print("testGetEdgesNode: ");
        expected = new Edge[1];
        expected[0] = edgeMap.get("7-7");
        actual = new Edge[1];
        i = 0;
        for (Edge e : graphGlobal.getEdges(nodeMap.get("Node 7"))) {
            Node s = e.getSource();
            Node t = e.getTarget();
            System.out.print("#" + s.getId() + "-" + t.getId() + " ");
            actual[i++] = e;
        }
        System.out.println();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testNeighbors() {
        System.out.print("testNeighbors: ");
        Node[] expected = new Node[2];
        expected[0] = nodeMap.get("Node 6");
        expected[1] = nodeMap.get("Node 4");
        Node[] actual = new Node[2];

        int i = 0;
        for (Node n : graphGlobal.getNeighbors(nodeMap.get("Node 5"))) {
            System.out.print(n.getId() + " ");
            actual[i++] = n;
        }
        System.out.println();
        assertArrayEquals(expected, actual);

        //Test Self loop
        Node[] array = graphGlobal.getNeighbors(nodeMap.get("Node 7")).toArray();
        assertEquals("self loop array length 0", 0, array.length);
    }

    @Test
    public void testOpposite() {
        Node node4 = nodeMap.get("Node 4");
        Node node5 = nodeMap.get("Node 5");
        Edge edge1 = edgeMap.get("4-5");
        Edge edge2 = edgeMap.get("4-4");

        assertEquals(node5, graphGlobal.getOpposite(node4, edge1));
        assertEquals(node4, graphGlobal.getOpposite(node4, edge2));
    }

    @Test
    public void testDegree() {
        Node node5 = nodeMap.get("Node 5");
        Node node4 = nodeMap.get("Node 4");
        Node node7 = nodeMap.get("Node 7");

        assertEquals(2, graphGlobal.getDegree(node5));
        assertEquals(3, graphGlobal.getDegree(node4));
        assertEquals(2, graphGlobal.getDegree(node7));
    }

    @Test
    public void testEdgeCount() {
        assertEquals(4, graphGlobal.getEdgeCount());
    }

    @Test
    public void testAdjacent() {
        Node node4 = nodeMap.get("Node 4");
        Node node5 = nodeMap.get("Node 5");
        Node node6 = nodeMap.get("Node 6");
        Edge edge1 = edgeMap.get("4-5");
        Edge edge2 = edgeMap.get("5-6");
        Edge edge3 = edgeMap.get("6-5");

        //assertTrue(graphGlobal.isAdjacent(node4, node4));
        assertTrue(graphGlobal.isAdjacent(node5, node6));
        assertFalse(graphGlobal.isAdjacent(node4, node6));
        assertTrue(graphGlobal.isAdjacent(edge1, edge2));
        graphGlobal.isAdjacent(edge2, edge3);
    }
}
