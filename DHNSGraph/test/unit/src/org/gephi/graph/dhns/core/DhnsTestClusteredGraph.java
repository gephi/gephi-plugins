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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.graph.HierarchicalUndirectedGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.LevelIterator;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.predicate.Tautology;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu
 */
public class DhnsTestClusteredGraph {

    private Dhns dhnsGlobal;
    private HierarchicalDirectedGraphImpl graphGlobal;
    private Dhns dhnsGlobal2;
    private HierarchicalDirectedGraphImpl graphGlobal2Directed;
    private HierarchicalUndirectedGraphImpl graphGlobal2Undirected;
    private Map<String, AbstractNode> nodeMap;
    private Map<String, Edge> edgeMap;

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
        nodeMap = new HashMap<String, AbstractNode>();
        edgeMap = new HashMap<String, Edge>();

        TreeStructure treeStructure = dhnsGlobal.getGraphStructure().getMainView().getStructure();
        GraphFactoryImpl factory = dhnsGlobal.factory();

        //Nodes
        for (int i = 0; i < 15; i++) {
            AbstractNode node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graphGlobal.addNode(node);
            nodeMap.put(node.getNodeData().getLabel(), node);
        }

        //2
        controller = new DhnsGraphController();
        dhnsGlobal2 = new Dhns(controller, null);
        graphGlobal2Directed = new HierarchicalDirectedGraphImpl(dhnsGlobal2, dhnsGlobal2.getGraphStructure().getMainView());
        graphGlobal2Undirected = new HierarchicalUndirectedGraphImpl(dhnsGlobal2, dhnsGlobal2.getGraphStructure().getMainView());
        treeStructure = dhnsGlobal2.getGraphStructure().getMainView().getStructure();
        factory = dhnsGlobal2.factory();

        //Nodes
        for (int i = 0; i < 3; i++) {
            Node node = factory.newNode();
            graphGlobal2Directed.addNode(node);
        }

        int i = 0;
        for (Node n : graphGlobal2Directed.getTopNodes().toArray()) {
            AbstractNode newC = factory.newNode();
            graphGlobal2Directed.addNode(newC, n);
            nodeMap.put("Leaf " + (i++), newC);
            newC = factory.newNode();
            graphGlobal2Directed.addNode(newC, n);
            nodeMap.put("Leaf " + (i++), newC);
        }

        Node leaf2 = nodeMap.get("Leaf 0");
        Node leaf3 = nodeMap.get("Leaf 1");
        Node leaf5 = nodeMap.get("Leaf 2");
        Node leaf6 = nodeMap.get("Leaf 3");
        Node leaf8 = nodeMap.get("Leaf 4");
        Node leaf9 = nodeMap.get("Leaf 5");

        graphGlobal2Directed.addEdge(leaf2, leaf5);
        graphGlobal2Directed.addEdge(leaf2, leaf6);
        graphGlobal2Directed.addEdge(leaf3, leaf2);
        graphGlobal2Directed.addEdge(leaf3, leaf5);
        graphGlobal2Directed.addEdge(leaf6, leaf8);
        graphGlobal2Directed.addEdge(leaf9, leaf8);
        graphGlobal2Directed.addEdge(leaf8, leaf9);
        graphGlobal2Directed.addEdge(leaf9, leaf5);
        graphGlobal2Directed.addEdge(leaf5, leaf9);
        graphGlobal2Directed.addEdge(leaf9, leaf9);
    }

    @After
    public void tearDown() {
        nodeMap.clear();
        dhnsGlobal = null;
        graphGlobal = null;
        dhnsGlobal2 = null;
        graphGlobal2Directed = null;
        graphGlobal2Undirected = null;
    }

    @Test
    public void testMoveDurableTreeList() {

        TreeStructure treeStructure = new TreeStructure(null);

        AbstractNode p0 = treeStructure.getRoot();
        AbstractNode p1 = new AbstractNode(1, 0, 0, 0, 0, null);
        AbstractNode p2 = new AbstractNode(2, 0, 0, 0, 0, null);
        AbstractNode p3 = new AbstractNode(3, 0, 0, 0, 0, null);
        AbstractNode p4 = new AbstractNode(4, 0, 0, 0, 0, null);
        AbstractNode p5 = new AbstractNode(5, 0, 0, 0, 0, null);
        AbstractNode p6 = new AbstractNode(6, 0, 0, 0, 0, null);
        AbstractNode p7 = new AbstractNode(7, 0, 0, 0, 0, null);

        treeStructure.insertAsChild(p1, p0);
        treeStructure.insertAsChild(p2, p1);
        treeStructure.insertAsChild(p4, p0);
        treeStructure.insertAsChild(p5, p4);
        treeStructure.insertAsChild(p6, p4);
        treeStructure.insertAsChild(p3, p1);
        treeStructure.insertAsChild(p7, p0);

        //Test if ID = pre
        for (Iterator<AbstractNode> itr = treeStructure.getTree().iterator(1); itr.hasNext();) {
            AbstractNode n = itr.next();
            assertEquals(n.getId(), n.getPre());
        }

        //Move p1 -> p4
        treeStructure.move(p1, p4);

        //Write expected array
        int[] expected = new int[treeStructure.getTreeSize() - 1];
        int index = 0;
        for (Iterator<AbstractNode> itr = treeStructure.getTree().iterator(1); itr.hasNext();) {
            AbstractNode n = itr.next();
            expected[index] = n.getId();
        }

        treeStructure.move(p1, p0);
        treeStructure.move(p1, p4);

        int[] actual = new int[treeStructure.getTreeSize() - 1];
        for (Iterator<AbstractNode> itr = treeStructure.getTree().iterator(1); itr.hasNext();) {
            AbstractNode n = itr.next();
            actual[index] = n.getId();
        }
        assertArrayEquals(expected, actual);

        treeStructure.move(p3, p4);
        treeStructure.move(p3, p5);

        //treeStructure.showTreeAsTable();
    }

    @Test
    public void testMoveToGroup() {

        TreeStructure treeStructure = dhnsGlobal.getGraphStructure().getMainView().getStructure();

        AbstractNode target = (AbstractNode) nodeMap.get("Node 10");
        for (int i = 1; i < 5; i++) {
            AbstractNode ch = (AbstractNode) nodeMap.get("Node " + i);
            int oldSize = target.size;

            graphGlobal.moveToGroup(ch, target);

            assertEquals(oldSize + 1, target.size);
            assertEquals(ch.parent, target);
            assertEquals(target.getPre() + target.size, ch.getPre());
        }

        for (int i = 1; i < 5; i++) {
            AbstractNode ch = (AbstractNode) nodeMap.get("Node " + i);
            int oldSize = target.size;
            graphGlobal.removeFromGroup(ch);

            assertEquals(oldSize - 1, target.size);
            assertEquals(ch.parent, target.parent);
        }

        //Hierarchy consistency
        try {
            checkHierarchy(treeStructure);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        //treeStructure.showTreeAsTable();
    }

    @Test
    public void testGroup() {

        TreeStructure treeStructure = dhnsGlobal.getGraphStructure().getMainView().getStructure();
        int oldSize = graphGlobal.getNodeCount();

        Node[] groupArray = new Node[5];
        for (int i = 1; i < 6; i++) {
            groupArray[i - 1] = nodeMap.get("Node " + i);
        }

        AbstractNode group = (AbstractNode) graphGlobal.groupNodes(groupArray);

        assertEquals(oldSize + 1 - groupArray.length, graphGlobal.getNodeCount());
        assertEquals(groupArray.length, group.size);
        assertEquals(graphGlobal.getHeight(), 1);

        int i = 0;
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), group.getPre()); itr.hasNext();) {
            AbstractNode node = itr.next();
            assertEquals(group.pre + i, node.getPre());
            i++;
        }
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        //treeStructure.showTreeAsTable();
    }

    @Test
    public void testUnGroup() {
        TreeStructure treeStructure = dhnsGlobal.getGraphStructure().getMainView().getStructure();
        int oldSize = graphGlobal.getNodeCount();

        Node[] groupArray = new Node[5];
        for (int i = 1; i < 6; i++) {
            groupArray[i - 1] = nodeMap.get("Node " + i);
        }

        AbstractNode group = (AbstractNode) graphGlobal.groupNodes(groupArray);

        graphGlobal.ungroupNodes(group);

        assertEquals(0, group.size);
        for (Node n : groupArray) {
            AbstractNode pn = (AbstractNode) n;
            assertEquals(1, pn.level);
            assertSame(treeStructure.getRoot(), pn.parent);
        }
        //treeStructure.showTreeAsTable();
        assertEquals(oldSize, graphGlobal.getNodeCount());

        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        //treeStructure.showTreeAsTable();
    }

    @Test
    public void testView() {
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        HierarchicalGraph graph = new HierarchicalDirectedGraphImpl(dhns, dhns.getGraphStructure().getMainView());

        TreeStructure treeStructure = dhns.getGraphStructure().getMainView().getStructure();
        GraphFactoryImpl factory = dhns.factory();

        //Nodes
        for (int i = 0; i < 5; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graph.addNode(node);
        }

        for (Node n : graph.getTopNodes().toArray()) {
            Node newC = factory.newNode();
            graph.addNode(newC, n);
        }

        //treeStructure.showTreeAsTable();

        //Test getNodes()
        for (Node n : graph.getNodes()) {
            assertEquals(1, graph.getChildrenCount(n));
        }

        //Test isInView
        for (Node n : graph.getNodes()) {
            assertTrue(graph.isInView(n));
            assertFalse(graph.isInView(graph.getChildren(n).toArray()[0]));
        }

        //Test resetView
        //treeStructure.showTreeAsTable();
        graph.resetViewToLeaves();
        for (Node n : graph.getNodes()) {
            assertEquals(1, graph.getLevel(n));
            assertFalse(graph.isInView(graph.getParent(n)));
        }

        graph.resetViewToTopNodes();
        for (Node n : graph.getNodes()) {
            assertEquals(0, graph.getLevel(n));
        }

        graph.resetViewToLevel(1);
        for (Node n : graph.getNodes()) {
            assertEquals(1, graph.getLevel(n));
            assertFalse(graph.isInView(graph.getParent(n)));
        }

        for (Node n : graph.getNodes(1).toArray()) {
            graph.removeFromGroup(n);
        }

        //treeStructure.showTreeAsTable();
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testMetaEdgesDirected() {
        TreeStructure treeStructure = dhnsGlobal2.getGraphStructure().getMainView().getStructure();

        Node metaNode0 = graphGlobal2Directed.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Directed.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Directed.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Directed.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Directed.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Directed.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Directed.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Directed.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Directed.getChildren(metaNode2).toArray()[1];

        //Get meta edges
        Edge[] metaEdges = graphGlobal2Directed.getMetaEdges().toArray();
        assertEquals(3, metaEdges.length);
        MetaEdgeImpl metaEdge01 = (MetaEdgeImpl) metaEdges[0];
        MetaEdgeImpl metaEdge12 = (MetaEdgeImpl) metaEdges[1];
        MetaEdgeImpl metaEdge21 = (MetaEdgeImpl) metaEdges[2];

        assertSame(metaNode0, metaEdge01.getSource());
        assertSame(metaNode1, metaEdge01.getTarget());
        assertSame(metaNode1, metaEdge12.getSource());
        assertSame(metaNode2, metaEdge12.getTarget());
        assertSame(metaNode2, metaEdge21.getSource());
        assertSame(metaNode1, metaEdge21.getTarget());

        //Meta edge content
        /*Edge[] metaEdge01content = graphGlobal2Directed.getMetaEdgeContent(metaEdge01).toArray();
        assertEquals(3, metaEdge01content.length);
        assertSame(metaEdge01content[0], graphGlobal2Directed.getEdge(leaf2, leaf5));
        assertSame(metaEdge01content[1], graphGlobal2Directed.getEdge(leaf2, leaf6));
        assertSame(metaEdge01content[2], graphGlobal2Directed.getEdge(leaf3, leaf5));

        Edge[] metaEdge12content = graphGlobal2Directed.getMetaEdgeContent(metaEdge12).toArray();
        assertEquals(2, metaEdge12content.length);
        assertSame(metaEdge12content[0], graphGlobal2Directed.getEdge(leaf6, leaf8));
        assertSame(metaEdge12content[1], graphGlobal2Directed.getEdge(leaf5, leaf9));

        Edge[] metaEdge21content = graphGlobal2Directed.getMetaEdgeContent(metaEdge21).toArray();
        assertEquals(1, metaEdge21content.length);
        assertSame(metaEdge21content[0], graphGlobal2Directed.getEdge(leaf9, leaf5));*/

        //Degree
        assertEquals(2, graphGlobal2Directed.getMetaInDegree(metaNode1));
        assertEquals(1, graphGlobal2Directed.getMetaOutDegree(metaNode1));
        assertEquals(1, graphGlobal2Directed.getMetaOutDegree(metaNode0));
        assertEquals(3, graphGlobal2Directed.getMetaDegree(metaNode1));

        Edge[] metaInEdges1 = graphGlobal2Directed.getMetaInEdges(metaNode1).toArray();
        assertEquals(2, metaInEdges1.length);
        assertSame(metaInEdges1[0], metaEdge01);
        assertSame(metaInEdges1[1], metaEdge21);

        Edge[] metaOutEdges1 = graphGlobal2Directed.getMetaOutEdges(metaNode1).toArray();
        assertEquals(1, metaOutEdges1.length);
        assertSame(metaOutEdges1[0], metaEdge12);

        Edge[] metaEdges1 = graphGlobal2Directed.getMetaEdges(metaNode1).toArray();
        assertEquals(3, metaEdges1.length);
        assertSame(metaEdges1[0], metaEdge12);
        assertSame(metaEdges1[1], metaEdge01);
        assertSame(metaEdges1[2], metaEdge21);

        //Remove edge
        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf2, leaf5));
//        assertEquals(2, graphGlobal2Directed.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf3, leaf2));
//        assertEquals(2, graphGlobal2Directed.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf3, leaf5));
        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf2, leaf6));
        assertEquals(0, graphGlobal2Directed.getMetaEdges(metaNode0).toArray().length);

        graphGlobal2Directed.clearMetaEdges(metaNode1);
        graphGlobal2Directed.clearMetaEdges(metaNode2);

        assertEquals(0, graphGlobal2Directed.getMetaEdges().toArray().length);
        assertEquals(0, graphGlobal2Directed.getMetaEdges(metaNode1).toArray().length);
        assertEquals(0, graphGlobal2Directed.getMetaEdges(metaNode2).toArray().length);

        //treeStructure.showTreeAsTable();
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void testMetaEdgesUndirected() {
        TreeStructure treeStructure = dhnsGlobal2.getGraphStructure().getMainView().getStructure();

        Node metaNode0 = graphGlobal2Undirected.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Undirected.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Undirected.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[1];

        //Get meta edges
        Edge[] metaEdges = graphGlobal2Undirected.getMetaEdges().toArray();
        assertEquals(2, metaEdges.length);
        MetaEdgeImpl metaEdge01 = (MetaEdgeImpl) metaEdges[0];
        MetaEdgeImpl metaEdge12 = (MetaEdgeImpl) metaEdges[1];

        assertSame(metaNode0, metaEdge01.getSource());
        assertSame(metaNode1, metaEdge01.getTarget());
        assertSame(metaNode1, metaEdge12.getSource());
        assertSame(metaNode2, metaEdge12.getTarget());

        //Degree
        assertEquals(2, graphGlobal2Undirected.getMetaDegree(metaNode1));
        assertEquals(1, graphGlobal2Undirected.getMetaDegree(metaNode0));
        assertEquals(1, graphGlobal2Undirected.getMetaDegree(metaNode2));

        Edge[] metaEdges1 = graphGlobal2Undirected.getMetaEdges(metaNode1).toArray();
        assertEquals(2, metaEdges1.length);
        assertSame(metaEdges1[0], metaEdge12);
        assertSame(metaEdges1[1], metaEdge01);

        //Meta edge content
        /*Edge[] metaEdge01content = graphGlobal2Undirected.getMetaEdgeContent(metaEdge01).toArray();
        assertEquals(3, metaEdge01content.length);
        assertSame(metaEdge01content[0], graphGlobal2Undirected.getEdge(leaf2, leaf5));
        assertSame(metaEdge01content[1], graphGlobal2Undirected.getEdge(leaf2, leaf6));
        assertSame(metaEdge01content[2], graphGlobal2Undirected.getEdge(leaf3, leaf5));

        Edge[] metaEdge12content = graphGlobal2Undirected.getMetaEdgeContent(metaEdge12).toArray();
        assertEquals(2, metaEdge12content.length);
        assertSame(metaEdge12content[0], graphGlobal2Undirected.getEdge(leaf6, leaf8));
        assertSame(metaEdge12content[1], graphGlobal2Undirected.getEdge(leaf5, leaf9));*/

        //Remove edge
        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf2, leaf5));
//        assertEquals(2, graphGlobal2Undirected.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf3, leaf2));
//        assertEquals(2, graphGlobal2Undirected.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf3, leaf5));
        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf2, leaf6));
        assertEquals(0, graphGlobal2Undirected.getMetaEdges(metaNode0).toArray().length);

        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf5, leaf9));
//        assertEquals(1, graphGlobal2Undirected.getMetaEdgeContent(metaEdge12).toArray().length);

        graphGlobal2Undirected.clearMetaEdges(metaNode1);
        graphGlobal2Undirected.clearMetaEdges(metaNode2);

        assertEquals(0, graphGlobal2Undirected.getMetaEdges().toArray().length);
        assertEquals(0, graphGlobal2Undirected.getMetaEdges(metaNode1).toArray().length);
        assertEquals(0, graphGlobal2Undirected.getMetaEdges(metaNode2).toArray().length);

        //treeStructure.showTreeAsTable();
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void testInnerOuterEdgesDirected() {

        Node metaNode0 = graphGlobal2Directed.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Directed.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Directed.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Directed.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Directed.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Directed.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Directed.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Directed.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Directed.getChildren(metaNode2).toArray()[1];

        //Inner
        Edge[] innerEdges = graphGlobal2Directed.getInnerEdges(metaNode0).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf3, leaf2), innerEdges[0]);

        //Outer
        Edge[] outerEdges = graphGlobal2Directed.getOuterEdges(metaNode0).toArray();
        assertEquals(3, outerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf2, leaf5), outerEdges[0]);
        assertSame(graphGlobal2Directed.getEdge(leaf2, leaf6), outerEdges[1]);
        assertSame(graphGlobal2Directed.getEdge(leaf3, leaf5), outerEdges[2]);

        //Inner with self loop
        innerEdges = graphGlobal2Directed.getInnerEdges(metaNode2).toArray();
        assertEquals(3, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf8, leaf9), innerEdges[0]);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf8), innerEdges[1]);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf9), innerEdges[2]);

        //Inner from self loop node
        innerEdges = graphGlobal2Directed.getInnerEdges(leaf9).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf9), innerEdges[0]);

        //Outer with mutual
        System.out.println();
        outerEdges = graphGlobal2Directed.getOuterEdges(metaNode2).toArray();
        assertEquals(3, outerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf6, leaf8), outerEdges[0]);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf5), outerEdges[1]);
        assertSame(graphGlobal2Directed.getEdge(leaf5, leaf9), outerEdges[2]);
    }

    @Test
    public void testInnerOuterEdgesUndirected() {

        Node metaNode0 = graphGlobal2Undirected.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Undirected.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Undirected.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[1];

        //Inner
        Edge[] innerEdges = graphGlobal2Undirected.getInnerEdges(metaNode0).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf3, leaf2), innerEdges[0]);

        //Outer
        Edge[] outerEdges = graphGlobal2Undirected.getOuterEdges(metaNode0).toArray();
        assertEquals(3, outerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf2, leaf5), outerEdges[0]);
        assertSame(graphGlobal2Undirected.getEdge(leaf2, leaf6), outerEdges[1]);
        assertSame(graphGlobal2Undirected.getEdge(leaf3, leaf5), outerEdges[2]);

        //Inner with self loop
        innerEdges = graphGlobal2Undirected.getInnerEdges(metaNode2).toArray();
        assertEquals(2, innerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf8, leaf9), innerEdges[0]);
        assertSame(graphGlobal2Undirected.getEdge(leaf9, leaf9), innerEdges[1]);

        //Inner from self loop node
        innerEdges = graphGlobal2Undirected.getInnerEdges(leaf9).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf9, leaf9), innerEdges[0]);

        //Outer with mutual
        outerEdges = graphGlobal2Undirected.getOuterEdges(metaNode2).toArray();
        assertEquals(2, outerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf6, leaf8), outerEdges[0]);
        assertSame(graphGlobal2Undirected.getEdge(leaf5, leaf9), outerEdges[1]);
    }

    @Test
    public void testLevel() {
        //Height
        assertEquals(1, graphGlobal2Directed.getHeight());

        //Level
        Node nodeLevel0 = graphGlobal2Directed.getTopNodes().toArray()[0];
        Node nodeLevel1 = graphGlobal2Directed.getChildren(nodeLevel0).toArray()[0];
        assertEquals(0, graphGlobal2Directed.getLevel(nodeLevel0));
        assertEquals(1, graphGlobal2Directed.getLevel(nodeLevel1));

        //getNodes(level) 0
        assertArrayEquals(graphGlobal2Directed.getTopNodes().toArray(), graphGlobal2Directed.getNodes(0).toArray());

        //getNodes(level) 1
        ArrayList<Node> nodesLevel1 = new ArrayList<Node>();
        nodesLevel1.addAll(Arrays.asList(graphGlobal2Directed.getChildren(graphGlobal2Directed.getTopNodes().toArray()[0]).toArray()));
        nodesLevel1.addAll(Arrays.asList(graphGlobal2Directed.getChildren(graphGlobal2Directed.getTopNodes().toArray()[1]).toArray()));
        nodesLevel1.addAll(Arrays.asList(graphGlobal2Directed.getChildren(graphGlobal2Directed.getTopNodes().toArray()[2]).toArray()));
        assertArrayEquals(nodesLevel1.toArray(), graphGlobal2Directed.getNodes(1).toArray());

        //Levelsize
        assertEquals(graphGlobal2Directed.getLevelSize(0), 3);
        assertEquals(graphGlobal2Directed.getLevelSize(1), 6);
        assertEquals(graphGlobal.getLevelSize(0), 15);

        //Level after move
        graphGlobal2Directed.moveToGroup(nodeLevel0, graphGlobal2Directed.getTopNodes().toArray()[1]);
        assertEquals(2, graphGlobal2Directed.getHeight());

        //Level after cancel the last move
        graphGlobal2Directed.moveToGroup(nodeLevel0, dhnsGlobal2.getGraphStructure().getMainView().getStructure().getRoot());
        assertEquals(1, graphGlobal2Directed.getHeight());
    }

    @Test
    public void testMetaEdgesAfterGrouping() {
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        TreeStructure treeStructure = dhns.getGraphStructure().getMainView().getStructure();
        HierarchicalDirectedGraphImpl graph = new HierarchicalDirectedGraphImpl(dhns, dhns.getGraphStructure().getMainView());
        GraphFactoryImpl factory = dhns.factory();

        //Add Node
        Node node1 = factory.newNode();
        Node node2 = factory.newNode();
        Node node3 = factory.newNode();
        Node node4 = factory.newNode();
        Node node5 = factory.newNode();
        Node node6 = factory.newNode();
        graph.addNode(node1);
        graph.addNode(node4);
        graph.addNode(node2, node1);
        graph.addNode(node3, node1);
        graph.addNode(node5, node4);
        graph.addNode(node6);

        //Add edge
        Edge edge23 = factory.newEdge(node2, node3);
        Edge edge35 = factory.newEdge(node3, node5);
        Edge edge34 = factory.newEdge(node3, node4);
        Edge edge15 = factory.newEdge(node1, node5);
        Edge edge36 = factory.newEdge(node3, node6);
        graph.addEdge(edge23);
        graph.addEdge(edge35);
        graph.addEdge(edge34);
        graph.addEdge(edge15);
        graph.addEdge(edge36);

        //Check before all presence of meta edge
        /*MetaEdge metaEdge14 = graph.getMetaEdge(node1, node4);
        assertNotNull(metaEdge14);
        Edge[] metaEdgeContent14 = graph.getMetaEdgeContent(metaEdge14).toArray();
        assertEquals(3, metaEdgeContent14.length);
        assertSame(metaEdgeContent14[0], edge35);
        assertSame(metaEdgeContent14[1], edge34);
        assertSame(metaEdgeContent14[2], edge15);
        assertNotNull(graph.getMetaEdge(node1, node6));

        //Move node3 to node5
        graph.moveToGroup(node3, node5);
        assertSame(metaEdge14, graph.getMetaEdge(node1, node4));
        metaEdge14 = graph.getMetaEdge(node1, node4);
        assertNotNull(metaEdge14);
        Edge[] metaEdgeContent14_2 = graph.getMetaEdgeContent(metaEdge14).toArray();
        assertEquals(2, metaEdgeContent14_2.length);
        assertSame(metaEdgeContent14_2[0], edge23);
        assertSame(metaEdgeContent14_2[1], edge15);
        assertNotNull(graph.getMetaEdge(node4, node6));

        //Inverse operation, move node3 to node1
        graph.moveToGroup(node3, node1);
        assertSame(metaEdge14, graph.getMetaEdge(node1, node4));
        metaEdge14 = graph.getMetaEdge(node1, node4);
        assertNotNull(metaEdge14);
        assertArrayEquals(metaEdgeContent14, graph.getMetaEdgeContent(metaEdge14).toArray());
        assertNull(graph.getMetaEdge(node4, node6));
        assertNotNull(graph.getMetaEdge(node1, node6));

        //Move an enabled node to an enabled parent, move node1 to node4
        graph.moveToGroup(node1, node4);
        assertFalse(graph.isInView(node1));
        AbstractNode AbstractNode1 = (AbstractNode) node1;
        assertEquals(0, AbstractNode1.getMetaEdgesInTree().getCount());
        assertEquals(0, AbstractNode1.getMetaEdgesOutTree().getCount());
        Edge[] metaEdges4 = graph.getMetaEdges(node4).toArray();
        assertEquals(1, metaEdges4.length);
        Edge metaEdge46 = metaEdges4[0];
        assertSame(metaEdge46, graph.getMetaEdge(node4, node6));
        Edge[] metaEdgeContent46 = graph.getMetaEdgeContent(metaEdge46).toArray();
        assertEquals(1, metaEdgeContent46.length);
        assertSame(metaEdgeContent46[0], edge36);

        //Expand node4 and check meta edges

        graph.expand(node4);
        Edge[] metaEdges1 = graph.getMetaEdges(node1).toArray();
        assertEquals(2, metaEdges1.length);
        assertEquals(graph.getMetaEdge(node1, node5), metaEdges1[0]);
        assertEquals(graph.getMetaEdge(node1, node6), metaEdges1[1]);

        //Expand node1 and check meta edges
        graph.expand(node1);
        assertEquals(0, graph.getMetaEdges().toArray().length);
        Edge[] metaEdges2 = graph.getMetaEdges(node2).toArray();
        assertEquals(0, metaEdges2.length);
        Edge[] metaEdges3 = graph.getMetaEdges(node3).toArray();
        assertEquals(0, metaEdges3.length);

        //Move a node with enabled descendants to an enabled parent, move node1 to node6
        graph.moveToGroup(node1, node6);
        Edge[] metaEdges = graph.getMetaEdges().toArray();
        assertEquals(1, metaEdges.length);
        assertSame(metaEdges[0], graph.getMetaEdge(node6, node5));
        Edge[] metaEdgesContent65 = graph.getMetaEdgeContent(metaEdges[0]).toArray();
        assertEquals(2, metaEdgesContent65.length);
        assertSame(edge35, metaEdgesContent65[0]);
        assertSame(edge15, metaEdgesContent65[1]);

        //Test after retract
        graph.retract(node4);
        metaEdges = graph.getMetaEdges().toArray();
        assertEquals(1, metaEdges.length);
        assertSame(metaEdges[0], graph.getMetaEdge(node6, node4));
        Edge[] metaEdgesContent64 = graph.getMetaEdgeContent(metaEdges[0]).toArray();
        assertEquals(3, metaEdgesContent64.length);
        assertSame(edge35, metaEdgesContent64[0]);
        assertSame(edge34, metaEdgesContent64[1]);
        assertSame(edge15, metaEdgesContent64[2]);

        //treeStructure.showTreeAsTable();
        try {
        checkHierarchy(treeStructure);
        } catch (Exception ex) {
        Exceptions.printStackTrace(ex);
        }

        //Grouping
        for (Node n : graphGlobal2Directed.getTopNodes().toArray()) {
        graphGlobal2Directed.ungroupNodes(n);
        }
        assertEquals(0, graphGlobal2Directed.getMetaEdges().toArray().length);
        graphGlobal2Directed.resetViewToLeaves();
        Node[] allNodes = graphGlobal2Directed.getNodes().toArray();
        Node newGroup9 = graphGlobal2Directed.groupNodes(new Node[]{allNodes[3], allNodes[4]});

        Edge[] allMetaEdges = graphGlobal2Directed.getMetaEdges().toArray();
        assertEquals(3, allMetaEdges.length);
        assertSame(graphGlobal2Directed.getMetaEdge(nodeMap.get("Leaf 0"), newGroup9), allMetaEdges[0]);
        assertSame(graphGlobal2Directed.getMetaEdge(nodeMap.get("Leaf 5"), newGroup9), allMetaEdges[1]);
        assertSame(graphGlobal2Directed.getMetaEdge(newGroup9, nodeMap.get("Leaf 5")), allMetaEdges[2]);

        graphGlobal2Directed.ungroupNodes(newGroup9);
        assertEquals(0, graphGlobal2Directed.getMetaEdges().toArray().length);

        //dhnsGlobal2.getGraphStructure().getStructure().showTreeAsTable();
        try {
        checkHierarchy(dhnsGlobal2.getGraphStructure().getStructure());
        } catch (Exception ex) {
        Exceptions.printStackTrace(ex);
        }*/
    }

    @Test
    public void testEdgesAndMetaEdges() {
        //dhnsGlobal2.getGraphStructure().getMainView().getStructure().showTreeAsTable();
        Edge[] metaedges = graphGlobal2Directed.getMetaEdges().toArray();
        int metaEdgesCount = metaedges.length;
        assertEquals(3, metaEdgesCount);
        Node parent0 = graphGlobal2Directed.getTopNodes().toArray()[0];
        graphGlobal2Directed.expand(parent0);
        assertEquals(4, graphGlobal2Directed.getMetaEdges().toArray().length);
        assertEquals(1, graphGlobal2Directed.getMetaEdges(nodeMap.get("Leaf 0")).toArray().length);
        assertEquals(1, graphGlobal2Directed.getMetaEdges(nodeMap.get("Leaf 1")).toArray().length);
        assertEquals(1, graphGlobal2Directed.getEdges().toArray().length);
        Edge uniqueEdge = graphGlobal2Directed.getEdges().toArray()[0];
        assertSame(nodeMap.get("Leaf 0"), uniqueEdge.getTarget());
        assertSame(nodeMap.get("Leaf 1"), uniqueEdge.getSource());
        assertEquals(5, graphGlobal2Directed.getEdgesAndMetaEdges().toArray().length);
    }

    @Test
    public void testFlattenTopNodes() {
        GraphViewImpl view = dhnsGlobal2.getGraphStructure().getMainView();
        //view.getStructure().showTreeAsTable();

        view.getStructureModifier().flatten();

        assertEquals(0, graphGlobal2Directed.getHeight());

        assertEquals(3, view.getEdgesCountTotal());
        assertEquals(3, view.getEdgesCountEnabled());
        assertEquals(1, view.getMutualEdgesEnabled());
        assertEquals(1, view.getMutualEdgesTotal());
        assertEquals(3, view.getNodesEnabled());

        Node n1 = graphGlobal2Directed.getTopNodes().toArray()[0];
        Node n4 = graphGlobal2Directed.getTopNodes().toArray()[1];
        Node n7 = graphGlobal2Directed.getTopNodes().toArray()[2];

        AbstractEdge edge14 = (AbstractEdge) graphGlobal.getEdge(n1, n4);
        AbstractEdge edge47 = (AbstractEdge) graphGlobal.getEdge(n4, n7);
        AbstractEdge edge74 = (AbstractEdge) graphGlobal.getEdge(n7, n4);
        assertNotNull(edge14);
        assertNotNull(edge47);
        assertNotNull(edge74);

        assertEquals(0, graphGlobal2Directed.getMetaEdges().toArray().length);
        assertEquals(3, graphGlobal2Directed.getLevelSize(0));

        //dhnsGlobal2.getGraphStructure().getMainView().getStructure().showTreeAsTable();
    }

    @Test
    public void testFlattenLeaves() {
        GraphViewImpl view = dhnsGlobal2.getGraphStructure().getMainView();
        Node n1 = graphGlobal2Directed.getTopNodes().toArray()[0];
        Node n4 = graphGlobal2Directed.getTopNodes().toArray()[1];
        Node n7 = graphGlobal2Directed.getTopNodes().toArray()[2];

        graphGlobal2Directed.expand(n1);
        graphGlobal2Directed.expand(n4);
        graphGlobal2Directed.expand(n7);

        view.getStructure().showTreeAsTable();

        view.getStructureModifier().flatten();

        assertEquals(0, graphGlobal2Directed.getHeight());
        assertEquals(6, graphGlobal2Directed.getLevelSize(0));
        assertEquals(6, view.getNodesEnabled());

        dhnsGlobal2.getGraphStructure().getMainView().getStructure().showTreeAsTable();
    }

    @Test
    public void testEdgesCountingExpandRetract() {
        GraphViewImpl view = dhnsGlobal2.getGraphStructure().getMainView();
        assertEquals(0, view.getEdgesCountEnabled());
        assertEquals(10, view.getEdgesCountTotal());
        Node n1 = graphGlobal2Directed.getTopNodes().toArray()[0];
        graphGlobal2Directed.expand(n1);
        assertEquals(1, view.getEdgesCountEnabled());
        assertEquals(0, view.getMutualEdgesEnabled());
        assertEquals(1, nodeMap.get("Leaf 0").getEnabledInDegree());
        assertEquals(1, nodeMap.get("Leaf 1").getEnabledOutDegree());
        graphGlobal2Directed.retract(n1);
        assertEquals(0, view.getEdgesCountEnabled());
        assertEquals(0, nodeMap.get("Leaf 0").getEnabledInDegree());
        assertEquals(0, nodeMap.get("Leaf 1").getEnabledOutDegree());
        assertEquals(0, nodeMap.get("Leaf 0").getEnabledOutDegree());
        assertEquals(0, nodeMap.get("Leaf 1").getEnabledInDegree());
        assertEquals(0, view.getMutualEdgesEnabled());
        Node n7 = graphGlobal2Directed.getTopNodes().toArray()[2];
        graphGlobal2Directed.expand(n7);
        assertEquals(3, view.getEdgesCountEnabled());
        assertEquals(1, nodeMap.get("Leaf 4").getEnabledInDegree());
        assertEquals(1, nodeMap.get("Leaf 4").getEnabledOutDegree());
        assertEquals(2, nodeMap.get("Leaf 5").getEnabledOutDegree());
        assertEquals(2, nodeMap.get("Leaf 5").getEnabledInDegree());
        assertEquals(1, nodeMap.get("Leaf 4").getEnabledMutualDegree());
        assertEquals(1, nodeMap.get("Leaf 5").getEnabledMutualDegree());
        assertEquals(1, view.getMutualEdgesEnabled());
        graphGlobal2Directed.retract(n7);
        assertEquals(0, view.getEdgesCountEnabled());
        assertEquals(0, nodeMap.get("Leaf 4").getEnabledInDegree());
        assertEquals(0, nodeMap.get("Leaf 4").getEnabledOutDegree());
        assertEquals(0, nodeMap.get("Leaf 5").getEnabledOutDegree());
        assertEquals(0, nodeMap.get("Leaf 5").getEnabledInDegree());
        assertEquals(0, nodeMap.get("Leaf 4").getEnabledMutualDegree());
        assertEquals(0, nodeMap.get("Leaf 5").getEnabledMutualDegree());
        assertEquals(0, view.getMutualEdgesEnabled());
        Node n4 = graphGlobal2Directed.getTopNodes().toArray()[1];
        graphGlobal2Directed.expand(n4);
        graphGlobal2Directed.expand(n7);
        assertEquals(1, nodeMap.get("Leaf 2").getEnabledInDegree());
        assertEquals(1, nodeMap.get("Leaf 2").getEnabledOutDegree());
        assertEquals(1, nodeMap.get("Leaf 2").getEnabledMutualDegree());
        assertEquals(1, nodeMap.get("Leaf 3").getEnabledOutDegree());
        assertEquals(0, nodeMap.get("Leaf 3").getEnabledInDegree());
        assertEquals(1, nodeMap.get("Leaf 4").getEnabledOutDegree());
        assertEquals(2, nodeMap.get("Leaf 4").getEnabledInDegree());
        assertEquals(3, nodeMap.get("Leaf 5").getEnabledOutDegree());
        assertEquals(3, nodeMap.get("Leaf 5").getEnabledInDegree());
        assertEquals(2, nodeMap.get("Leaf 5").getEnabledMutualDegree());
        assertEquals(1, nodeMap.get("Leaf 4").getEnabledMutualDegree());
        assertEquals(2, view.getMutualEdgesEnabled());
        assertEquals(6, view.getEdgesCountEnabled());
        graphGlobal2Directed.expand(n1);
        assertEquals(2, view.getMutualEdgesEnabled());
        assertEquals(10, view.getEdgesCountEnabled());
    }

    @Test
    public void testEdgesCountingReseting() {
        GraphViewImpl view = dhnsGlobal2.getGraphStructure().getMainView();
        graphGlobal2Directed.resetViewToLeaves();
        assertEquals(2, view.getMutualEdgesEnabled());
        assertEquals(10, view.getEdgesCountEnabled());
        LevelIterator lvlIterator = new LevelIterator(view.getStructure(), 2, Tautology.instance);
        for (; lvlIterator.hasNext();) {
            AbstractNode node = lvlIterator.next();
            assertEquals(node.getEdgesInTree().getCount(), node.getEnabledInDegree());
            assertEquals(node.getEdgesOutTree().getCount(), node.getEnabledOutDegree());
            int expectedMutual = 0;
            ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
            for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                AbstractEdge edge = edgeIterator.next();
                AbstractNode target = edge.getTarget(view.getViewId());
                if (node != target && target.getEdgesOutTree().hasNeighbour(node)) {
                    expectedMutual++;
                }
            }
            assertEquals(expectedMutual, node.getEnabledMutualDegree());
        }
        graphGlobal2Directed.resetViewToTopNodes();
        assertEquals(0, view.getMutualEdgesEnabled());
        assertEquals(0, view.getEdgesCountEnabled());
        graphGlobal2Directed.resetViewToLevel(1);
        assertEquals(2, view.getMutualEdgesEnabled());
        assertEquals(10, view.getEdgesCountEnabled());
        LevelIterator lvlIterator2 = new LevelIterator(view.getStructure(), 2, Tautology.instance);
        for (; lvlIterator2.hasNext();) {
            AbstractNode node = lvlIterator2.next();
            assertEquals(node.getEdgesInTree().getCount(), node.getEnabledInDegree());
            assertEquals(node.getEdgesOutTree().getCount(), node.getEnabledOutDegree());
            int expectedMutual = 0;
            ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
            for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                AbstractEdge edge = edgeIterator.next();
                AbstractNode target = edge.getTarget(view.getViewId());
                if (node != target && target.getEdgesOutTree().hasNeighbour(node)) {
                    expectedMutual++;
                }
            }
            assertEquals(expectedMutual, node.getEnabledMutualDegree());
        }
    }

    @Test
    public void testEdgesCountingGrouping() {
        GraphViewImpl view = dhnsGlobal2.getGraphStructure().getMainView();
        Node n1 = graphGlobal2Directed.getTopNodes().toArray()[0];
        graphGlobal2Directed.ungroupNodes(n1);
        assertEquals(0, view.getMutualEdgesEnabled());
        assertEquals(1, view.getEdgesCountEnabled());
        graphGlobal2Directed.groupNodes(new Node[]{nodeMap.get("Leaf 0"), nodeMap.get("Leaf 1")});
        n1 = graphGlobal2Directed.getTopNodes().toArray()[2];
        graphGlobal2Directed.expand(n1);
        assertEquals(0, view.getMutualEdgesEnabled());
        assertEquals(1, view.getEdgesCountEnabled());
    }

    @Test
    public void testEvents() {
        GraphListener gl = new GraphListener() {

            public void graphChanged(GraphEvent event) {
                System.out.println("graph changed " + event.getEventType());
            }
        };
        dhnsGlobal.addGraphListener(gl);
    }

    public void checkHierarchy(TreeStructure treeStructure) throws Exception {

        int count = 0;
        AbstractNode[] array = new AbstractNode[treeStructure.getTreeSize()];

        //Pre test
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();

            assertEquals("node pre test", node.pre, count);
            array[count] = node;
            count++;
        }

        //Post test
        Arrays.sort(array, new Comparator<AbstractNode>() {

            public int compare(AbstractNode o1, AbstractNode o2) {
                if (o1.post > o2.post) {
                    return 1;
                } else if (o1.post < o2.post) {
                    return -1;
                }
                throw new IllegalArgumentException("duplicated post numbers");
            }
        });
        for (int i = 0; i < array.length; i++) {
            assertEquals(i, array[i].post);
        }
    }
}
