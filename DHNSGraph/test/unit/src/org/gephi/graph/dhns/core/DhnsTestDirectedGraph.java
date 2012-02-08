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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
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
public class DhnsTestDirectedGraph {

    private Dhns dhnsGlobal;
    private HierarchicalDirectedGraphImpl graphGlobal;
    private Map<String, Node> nodeMap;
    private Map<String, Edge> edgeMap;

    public DhnsTestDirectedGraph() {
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
        graphGlobal = new HierarchicalDirectedGraphImpl(dhnsGlobal, dhnsGlobal.getGraphStructure().getMainView());
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

        graphGlobal.addEdge(edge1);
        graphGlobal.addEdge(edge2);
        graphGlobal.addEdge(edge3);
        graphGlobal.addEdge(edge4);
        graphGlobal.addEdge(edge5);

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
    public void testTreeList() {
        DurableTreeList durableTreeList = new DurableTreeList(null);
        List<AbstractNode> expected = new ArrayList<AbstractNode>();
        for (int i = 0; i < 10; i++) {
            AbstractNode node = new AbstractNode(i, 0, 0, 0, 0, null);
            expected.add(node);
            durableTreeList.add(node);
        }

        TreeListIterator treeListIterator = new TreeListIterator(durableTreeList);
        for (; treeListIterator.hasNext();) {
            AbstractNode node = treeListIterator.next();
            if (node.getId() == 4) {
                treeListIterator.remove();
            }
        }

        //Expected array
        List<AbstractNode> expected1 = new ArrayList<AbstractNode>();
        List<AbstractNode> actual = new ArrayList<AbstractNode>();
        for (int i = 0; i < durableTreeList.size; i++) {
            AbstractNode node = durableTreeList.get(i);
            actual.add(node);
        }
        expected1.addAll(expected);
        expected1.remove(4);
        assertArrayEquals(expected1.toArray(), actual.toArray());

        durableTreeList.remove(2);

        //Expected array
        List<AbstractNode> expected2 = new ArrayList<AbstractNode>();
        actual = new ArrayList<AbstractNode>();
        for (int i = 0; i < durableTreeList.size; i++) {
            AbstractNode node = durableTreeList.get(i);
            actual.add(node);
        }
        expected2.addAll(expected);
        expected2.remove(4);
        expected2.remove(2);
        assertArrayEquals(expected2.toArray(), actual.toArray());

        treeListIterator = new TreeListIterator(durableTreeList);
        for (; treeListIterator.hasNext();) {
            AbstractNode node = treeListIterator.next();
            if (node.getId() == 5) {
                treeListIterator.remove();
            }
        }

        durableTreeList.remove(5);

        //Expected array
        List<AbstractNode> expected3 = new ArrayList<AbstractNode>();
        actual = new ArrayList<AbstractNode>();
        for (int i = 0; i < durableTreeList.size; i++) {
            AbstractNode node = durableTreeList.get(i);
            actual.add(node);
        }
        expected3.addAll(expected);
        expected3.remove(4);
        expected3.remove(2);
        expected3.remove(3);
        expected3.remove(5);
        assertArrayEquals(expected3.toArray(), actual.toArray());
    }

    @Test
    public void testAddNode() {
        System.out.println("testAddNode");
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        HierarchicalDirectedGraphImpl graph = new HierarchicalDirectedGraphImpl(dhns, dhns.getGraphStructure().getMainView());
        TreeStructure treeStructure = dhns.getGraphStructure().getMainView().getStructure();
        GraphFactoryImpl factory = dhns.factory();

        for (int i = 0; i < 10; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graph.addNode(node);
            System.out.println("Node " + i + " added. Id = " + node.getId());
        }

        graph.readLock();

        //Test
        assertEquals("root size", 11, treeStructure.getTreeSize());
        assertEquals("graph size", 10, graph.getNodeCount());

        for (int i = 0; i < 10; i++) {
            AbstractNode n = treeStructure.getNodeAt(i);
            assertEquals("AbstractNode pre", i, n.getPre());
            assertEquals("AbstractNode id", i, n.getId());
            assertEquals("AbstractNode enabled", i > 0, n.isEnabled());
            assertEquals("AbstractNode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("AbstractNode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("AbstractNode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }

        int i = 1;
        for (Node node : graph.getNodes()) {
            assertEquals("node iterator", i, node.getId());
            i++;
        }

        graph.readUnlock();
    }

    @Test
    public void testRemoveNode() {
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        HierarchicalDirectedGraphImpl graph = new HierarchicalDirectedGraphImpl(dhns, dhns.getGraphStructure().getMainView());
        TreeStructure treeStructure = dhns.getGraphStructure().getMainView().getStructure();
        GraphFactoryImpl factory = dhns.factory();

        Node first = null;
        Node middle = null;
        Node end = null;
        for (int i = 0; i < 10; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graph.addNode(node);
            System.out.println("Node " + i + " added. Id = " + node.getId());

            if (i == 0) {
                first = node;
            } else if (i == 4) {
                middle = node;
            } else if (i == 9) {
                end = node;
            }
        }

        graph.removeNode(first);

        //Test1
        System.out.print("Test1 nodes: ");
        for (int i = 0; i < treeStructure.getTreeSize(); i++) {
            AbstractNode n = treeStructure.getNodeAt(i);
            System.out.print(n.getId() + " ");
            assertEquals("AbstractNode pre", i, n.getPre());
            assertEquals("AbstractNode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("AbstractNode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("AbstractNode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }
        System.out.println();
        //End Test1

        graph.removeNode(middle);

        //Test2
        System.out.print("Test2 nodes: ");
        for (int i = 0; i < treeStructure.getTreeSize(); i++) {
            AbstractNode n = treeStructure.getNodeAt(i);
            System.out.print(n.getId() + " ");
            assertEquals("AbstractNode pre", i, n.getPre());
            assertEquals("AbstractNode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("AbstractNode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("AbstractNode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }
        System.out.println();
        //End Test2


        graph.removeNode(end);

        //Test3
        System.out.print("Test3 nodes: ");
        for (int i = 0; i < treeStructure.getTreeSize(); i++) {
            AbstractNode n = treeStructure.getNodeAt(i);
            System.out.print(n.getId() + " ");
            assertEquals("AbstractNode pre", i, n.getPre());
            assertEquals("AbstractNode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("AbstractNode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("AbstractNode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }
        System.out.println();
        //End Test3

        assertFalse(graph.contains(first));
        assertFalse(graph.contains(middle));
        assertFalse(graph.contains(end));

        AbstractNode AbstractNode = (AbstractNode) first;
        assertNull(AbstractNode.avlNode);
        assertNull(AbstractNode.parent);

        //Test
        assertEquals("tree size", 8, treeStructure.getTreeSize());
    }

    @Test
    public void testContainsNode() {

        Node node = nodeMap.get("Node 1");
        boolean contains = graphGlobal.contains(node);

        //Test
        assertTrue("contains node", contains);
        assertFalse("not contains node", graphGlobal.contains(nodeMap.get("Fake Node 1")));
    }

    @Test
    public void testClearNodes() {

        TreeStructure treeStructure = dhnsGlobal.getGraphStructure().getMainView().getStructure();
        graphGlobal.clear();

        //Test
        assertEquals("clear nodes", 1, treeStructure.getTreeSize());
        assertEquals("clear nodes", 0, graphGlobal.getNodeCount());
        assertEquals("clear nodes", treeStructure.getRoot(), treeStructure.getNodeAt(0));

        assertFalse("not contains anymore", graphGlobal.contains(nodeMap.get("Node 1")));

        AbstractNode AbstractNode = (AbstractNode) nodeMap.get("Node 2");
        assertNull("clean clear", AbstractNode.avlNode);
        assertNull("clean clear", AbstractNode.parent);
    }

    @Test
    public void testAddEdge() {
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        HierarchicalDirectedGraphImpl graph = new HierarchicalDirectedGraphImpl(dhns, dhns.getGraphStructure().getMainView());
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
        AbstractNode AbstractNode1 = (AbstractNode) node1;
        AbstractNode AbstractNode2 = (AbstractNode) node2;

        AbstractEdge edge = AbstractNode1.getEdgesOutTree().getItem(AbstractNode2.getNumber());
        assertNotNull("find OUT edge", edge);
        assertTrue("contains OUT edge", AbstractNode1.getEdgesOutTree().contains(edge));

        AbstractEdge edge2 = AbstractNode2.getEdgesInTree().getItem(AbstractNode1.getNumber());
        assertNotNull("find IN edge", edge);
        assertTrue("contains IN edge", AbstractNode2.getEdgesInTree().contains(edge2));

        assertSame("edges equal", edge, edge2);

        assertEquals("edges count", 1, graph.getEdgeCount());

        //Test factoryedge
        graph.addEdge(edge);
        assertEquals("edges count", 1, graph.getEdgeCount());

        //Test self loop
        graph.addEdge(node3, node3);

        AbstractNode AbstractNode3 = (AbstractNode) node3;

        AbstractEdge edge3 = AbstractNode3.getEdgesOutTree().getItem(AbstractNode3.getNumber());
        assertNotNull("find OUT edge", edge);
        assertTrue("contains OUT edge", AbstractNode3.getEdgesOutTree().contains(edge3));

        AbstractEdge edge4 = AbstractNode3.getEdgesInTree().getItem(AbstractNode3.getNumber());
        assertNotNull("find IN edge", edge);
        assertTrue("contains IN edge", AbstractNode3.getEdgesInTree().contains(edge3));

        assertSame("edges equal", edge3, edge4);

        assertTrue("is self loop", edge3.isSelfLoop());
    }

    @Test
    public void testRemoveEdge() {
        GraphFactoryImpl factory = dhnsGlobal.factory();
        AbstractNode node3 = (AbstractNode) nodeMap.get("Node 1");
        AbstractNode node4 = (AbstractNode) nodeMap.get("Node 2");
        AbstractEdge edge = factory.newEdge(node3, node4);

        graphGlobal.addEdge(edge);
        assertTrue(graphGlobal.contains(edge));

        graphGlobal.removeEdge(edge);
        AbstractEdge edge3 = node3.getEdgesOutTree().getItem(node4.getNumber());
        assertNull("OUT null", edge3);
        assertFalse("contains OUT edge", node3.getEdgesOutTree().contains(edge));

        AbstractEdge edge4 = node4.getEdgesInTree().getItem(node3.getNumber());
        assertNull("IN null", edge4);
        assertFalse("contains IN edge", node3.getEdgesInTree().contains(edge));

        assertFalse(graphGlobal.contains(edge));

        Edge edge7 = edgeMap.get("7-7");
        assertTrue(graphGlobal.contains(edge7));
        graphGlobal.removeEdge(edge7);
        assertFalse(graphGlobal.contains(edge7));
    }

    @Test
    public void testRemoveEdge2() {
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        HierarchicalDirectedGraphImpl graph = new HierarchicalDirectedGraphImpl(dhns, dhns.getGraphStructure().getMainView());
        TreeStructure treeStructure = dhns.getGraphStructure().getMainView().getStructure();
        GraphFactoryImpl factory = dhns.factory();

        Node node1 = factory.newNode();
        Node node2 = factory.newNode();
        graph.addNode(node1);
        graph.addNode(node2);

        AbstractEdge edge1 = factory.newEdge(node1, node2, 3f, true);
        graph.addEdge(edge1);
        AbstractEdge edge2 = factory.newEdge(node2, node1, 1f, true);
        graph.addEdge(edge2);

        graph.removeEdge(edge2);

        assertEquals(edge1, graph.getEdges().toArray()[0]);
        assertFalse(graph.contains(edge2));

        graph.removeEdge(edge1);
        assertFalse(graph.contains(edge1));
        assertEquals(0, graph.getEdgeCount());
    }

    @Test
    public void testGetEdges() {

        //Test1
        Edge[] expected = new Edge[5];
        expected[0] = edgeMap.get("4-4");
        expected[1] = edgeMap.get("4-5");
        expected[2] = edgeMap.get("5-6");
        expected[3] = edgeMap.get("6-5");
        expected[4] = edgeMap.get("7-7");
        Edge[] actual = new Edge[5];

        int i = 0;
        System.out.print("testGetEdges: ");
        for (Edge e : graphGlobal.getEdges()) {
            Node s = e.getSource();
            Node t = e.getTarget();
            Edge ed = edgeMap.get((s.getId()-1) + "-" + (t.getId()-1));
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
            Edge ed = edgeMap.get((s.getId()-1) + "-" + (t.getId()-1));
            assertSame("edge iterator", e, ed);
            System.out.print("#" + s.getId() + "-" + t.getId() + " ");
            actual[i++] = e;
        }
        System.out.println();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetEdgesNode() {

        //Test1
        System.out.print("testGetEdgesNode: ");
        Edge[] expected = new Edge[3];
        expected[0] = edgeMap.get("5-6");
        expected[1] = edgeMap.get("4-5");
        expected[2] = edgeMap.get("6-5");
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
    public void testSucessors() {
        //Test
        System.out.print("testSucessors: ");
        Node[] expected = new Node[1];
        expected[0] = nodeMap.get("Node 6");
        Node[] actual = new Node[1];

        int i = 0;
        Node node5 = nodeMap.get("Node 5");
        for (Node n : graphGlobal.getSuccessors(node5)) {
            System.out.print(n.getId() + " ");
            actual[i++] = n;
            assertTrue(graphGlobal.isSuccessor(node5, n));
        }
        System.out.println();
        assertArrayEquals(expected, actual);

        //Test Self loop
        Node[] array = graphGlobal.getSuccessors(nodeMap.get("Node 7")).toArray();
        assertEquals("self loop array length 0", 0, array.length);
    }

    @Test
    public void testPredecessors() {
        //Test
        System.out.print("testPredecessors: ");
        Node[] expected = new Node[2];
        expected[0] = nodeMap.get("Node 4");
        expected[1] = nodeMap.get("Node 6");
        Node[] actual = new Node[2];

        int i = 0;
        Node node5 = nodeMap.get("Node 5");
        for (Node n : graphGlobal.getPredecessors(node5)) {
            System.out.print(n.getId() + " ");
            actual[i++] = n;
            assertTrue(graphGlobal.isPredecessor(node5, n));
        }
        System.out.println();
        assertArrayEquals(expected, actual);

        //Test Self loop
        Node[] array = graphGlobal.getSuccessors(nodeMap.get("Node 7")).toArray();
        assertEquals("self loop array length 0", 0, array.length);
    }

    @Test
    public void testNeighbors() {
        System.out.print("testNeighbors: ");
        Node[] expected = new Node[2];
        expected[0] = nodeMap.get("Node 6");
        expected[1] = nodeMap.get("Node 4");
        Node[] actual = new Node[2];

        int i = 0;
        Node node5 = nodeMap.get("Node 5");
        for (Node n : graphGlobal.getNeighbors(node5)) {
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

        assertEquals(3, graphGlobal.getDegree(node5));
        assertEquals(3, graphGlobal.getDegree(node4));
        assertEquals(2, graphGlobal.getDegree(node7));
    }

    @Test
    public void testAdjacent() {
        Node node4 = nodeMap.get("Node 4");
        Node node5 = nodeMap.get("Node 5");
        Node node6 = nodeMap.get("Node 6");
        Edge edge1 = edgeMap.get("4-5");
        Edge edge2 = edgeMap.get("5-6");

        assertTrue(graphGlobal.isAdjacent(node4, node5));
        assertFalse(graphGlobal.isAdjacent(node4, node6));
        assertTrue(graphGlobal.isAdjacent(edge1, edge2));
    }

    @Test
    public void testClearEdgesNode() {
        Node node4 = nodeMap.get("Node 4");
        Node node5 = nodeMap.get("Node 5");
        Edge edge1 = edgeMap.get("4-4");
        Edge edge2 = edgeMap.get("5-6");
        graphGlobal.clearEdges(node5);
        graphGlobal.clearEdges(node4);

        assertEquals(0, graphGlobal.getDegree(node5));
        assertEquals(0, graphGlobal.getDegree(node4));
        assertFalse(graphGlobal.contains(edge2));
        assertFalse(graphGlobal.contains(edge1));
        assertFalse(graphGlobal.isAdjacent(node4, node5));
        //assertFalse(graphGlobal.isAdjacent(edge1, edge2));        //Fail because no test verifying edge belongs to the structure
    }

    @Test
    public void testGetEdge() {
        Node node4 = nodeMap.get("Node 4");
        Node node5 = nodeMap.get("Node 5");

        assertNotNull(graphGlobal.getEdge(node4, node5));
        Edge selfLoop = graphGlobal.getEdge(node4, node4);
        assertTrue(graphGlobal.isSelfLoop(selfLoop));
    }

    @Test
    public void testGetInEdges() {

        //Test1
        System.out.print("testGetInEdges: ");
        Edge[] expected = new Edge[2];
        expected[0] = edgeMap.get("4-5");
        expected[1] = edgeMap.get("6-5");
        Edge[] actual = new Edge[2];

        int i = 0;
        Node node5 = nodeMap.get("Node 5");
        for (Edge e : graphGlobal.getInEdges(node5)) {
            Node s = e.getSource();
            Node t = e.getTarget();
            System.out.print("#" + s.getId() + "-" + t.getId() + " ");
            actual[i++] = e;
        }
        System.out.println();
        assertArrayEquals(expected, actual);

        //Test2
        assertEquals(graphGlobal.getInEdges(nodeMap.get("Node 4")).toArray()[0], edgeMap.get("4-4"));
    }

    @Test
    public void testGetOutEdges() {

        //Test1
        System.out.print("testGetOutEdges: ");
        Edge[] expected = new Edge[1];
        expected[0] = edgeMap.get("5-6");
        Edge[] actual = new Edge[1];

        int i = 0;
        Node node5 = nodeMap.get("Node 5");
        for (Edge e : graphGlobal.getOutEdges(node5)) {
            Node s = e.getSource();
            Node t = e.getTarget();
            System.out.print("#" + s.getId() + "-" + t.getId() + " ");
            actual[i++] = e;
        }
        System.out.println();
        assertArrayEquals(expected, actual);

        //Test2
        assertEquals(graphGlobal.getOutEdges(nodeMap.get("Node 4")).toArray()[0], edgeMap.get("4-4"));
    }

    @Test
    public void testEdgesCounting() {
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        GraphViewImpl view = dhns.getGraphStructure().getMainView();
        HierarchicalDirectedGraphImpl graph = new HierarchicalDirectedGraphImpl(dhns, view);
        TreeStructure treeStructure = view.getStructure();
        GraphFactoryImpl factory = dhns.factory();

        AbstractNode node1 = factory.newNode();
        AbstractNode node2 = factory.newNode();
        AbstractNode node3 = factory.newNode();
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);
        AbstractEdge edge1 = factory.newEdge(node1, node2);
        AbstractEdge edge2 = factory.newEdge(node2, node1);
        AbstractEdge edge3 = factory.newEdge(node3, node3);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);

        assertEquals(3, view.getEdgesCountTotal());
        assertEquals(1, view.getMutualEdgesTotal());
        assertEquals(3, view.getEdgesCountEnabled());
        assertEquals(1, view.getMutualEdgesEnabled());

        assertEquals(1, node1.getEnabledInDegree());
        assertEquals(1, node1.getEnabledOutDegree());
        assertEquals(1, node1.getEnabledMutualDegree());
        assertEquals(1, node2.getEnabledInDegree());
        assertEquals(1, node2.getEnabledOutDegree());
        assertEquals(1, node2.getEnabledMutualDegree());
        assertEquals(1, node3.getEnabledInDegree());
        assertEquals(1, node3.getEnabledOutDegree());
        assertEquals(0, node3.getEnabledMutualDegree());

        graph.clearEdges(node3);
        assertEquals(2, view.getEdgesCountTotal());
        assertEquals(1, view.getMutualEdgesTotal());
        assertEquals(0, node3.getEnabledInDegree());
        assertEquals(0, node3.getEnabledOutDegree());
        assertEquals(0, node3.getEnabledMutualDegree());

        graph.clearEdges(node1);
        assertEquals(0, view.getEdgesCountTotal());
        assertEquals(0, view.getMutualEdgesTotal());
        assertEquals(0, node1.getEnabledInDegree());
        assertEquals(0, node1.getEnabledOutDegree());
        assertEquals(0, node1.getEnabledMutualDegree());
        assertEquals(0, node2.getEnabledInDegree());
        assertEquals(0, node2.getEnabledOutDegree());
        assertEquals(0, node2.getEnabledMutualDegree());

        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);

        graph.removeEdge(edge3);
        assertEquals(2, view.getEdgesCountTotal());
        assertEquals(1, view.getMutualEdgesTotal());
        assertEquals(2, view.getEdgesCountEnabled());
        assertEquals(1, view.getMutualEdgesEnabled());
        assertEquals(0, node3.getEnabledInDegree());
        assertEquals(0, node3.getEnabledOutDegree());
        assertEquals(0, node3.getEnabledMutualDegree());

        graph.removeEdge(edge1);
        assertEquals(1, view.getEdgesCountTotal());
        assertEquals(0, view.getMutualEdgesTotal());
        assertEquals(1, view.getEdgesCountEnabled());
        assertEquals(0, view.getMutualEdgesEnabled());
        assertEquals(1, node1.getEnabledInDegree());
        assertEquals(0, node1.getEnabledOutDegree());
        assertEquals(0, node1.getEnabledMutualDegree());
        assertEquals(0, node2.getEnabledInDegree());
        assertEquals(1, node2.getEnabledOutDegree());
        assertEquals(0, node2.getEnabledMutualDegree());

        graph.addEdge(edge1);
        assertEquals(2, view.getEdgesCountTotal());
        assertEquals(1, view.getMutualEdgesTotal());
        assertEquals(2, view.getEdgesCountEnabled());
        assertEquals(1, view.getMutualEdgesEnabled());
        assertEquals(1, node1.getEnabledInDegree());
        assertEquals(1, node1.getEnabledOutDegree());
        assertEquals(1, node1.getEnabledMutualDegree());
        assertEquals(1, node2.getEnabledInDegree());
        assertEquals(1, node2.getEnabledOutDegree());
        assertEquals(1, node2.getEnabledMutualDegree());
    }
}
