package com.carlschroedl.gephi.plugin.minimumspanningtree;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.EdgeImpl;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.util.Lookup;

public class GraphTopologyEqualsTest {

    GraphModelLoader loader;
    ProjectController projectController;
    private static final String PATH = "/com/carlschroedl/gephi/plugin/minimumspanningtree/initial/wiki_kruskal_example_initial.graphml";

    public GraphTopologyEqualsTest() {
    }

    @Before
    public void setUp() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        loader = new GraphModelLoader(projectController);
    }

    @After
    public void tearDown() {
        projectController.closeCurrentProject();
    }

    @Test
    public void testEmptyGraphsEqual(){
        GraphModel a0 = loader.fromScratch();
        GraphModel b0 = loader.fromScratch();
        assertTrue(GraphTopologyEquals.graphsHaveSameTopology(a0.getGraph(), b0.getGraph()));
    }
    
    @Test
    public void testSameGraphsAreEqual() {
        GraphModel a = loader.fromFile(PATH);
        GraphModel b = loader.fromFile(PATH);
        assertTrue(GraphTopologyEquals.graphsHaveSameTopology(a.getGraph(), b.getGraph()));
        
    }
    
    @Test
    public void testDifferentNodeCounts() {
        GraphModel a = loader.fromScratch();
        GraphModel b = loader.fromScratch();
        NodeImpl n = new NodeImpl(Integer.MAX_VALUE);
        b.getGraph().addNode(n);
        assertFalse("two graphs are not equal if they have different node counts", GraphTopologyEquals.graphsHaveSameTopology(a.getGraph(), b.getGraph()));
    }
    
    @Test
    public void testDifferentEdgeCount() {
        GraphModel aModel = loader.fromScratch();
        GraphModel bModel = loader.fromScratch();
        Graph a = aModel.getGraph();
        Graph b = bModel.getGraph();
        NodeImpl n1a = new NodeImpl(Integer.MAX_VALUE);
        NodeImpl n2a = new NodeImpl(Integer.MIN_VALUE);
        a.addNode(n1a);
        a.addNode(n2a);
        
        NodeImpl n1b = new NodeImpl(Integer.MAX_VALUE);
        NodeImpl n2b = new NodeImpl(Integer.MIN_VALUE);
        b.addNode(n1b);
        b.addNode(n2b);
        //add an edge between the new nodes in one graph, but not the other
        Edge e;
        e = new EdgeImpl(Integer.MAX_VALUE - 1, n1a, n2a, 0, 1, true);
        a.addEdge(e);
        assertFalse("two graphs are not equal if they have different numbers of edges", GraphTopologyEquals.graphsHaveSameTopology(a, b));
    }

        @Test
    public void testDifferentEdgeDirection() {
        final String ID_1 = "" + Integer.MAX_VALUE;
        final String ID_2 = "" + Integer.MIN_VALUE;
        final int WEIGHT = 1;
        GraphModel aModel = loader.fromScratch();
        GraphModel bModel = loader.fromScratch();
        GraphFactory aFactory = aModel.factory();
        GraphFactory bFactory = bModel.factory();
        Graph a = aModel.getGraph();
        Graph b = bModel.getGraph();
        Node n1a = aFactory.newNode(ID_1);
        Node n2a = aFactory.newNode(ID_2);
        a.addNode(n1a);
        a.addNode(n2a);
        
        Node n1b = bFactory.newNode(ID_1);
        Node n2b = bFactory.newNode(ID_2);
        b.addNode(n1b);
        b.addNode(n2b);
        //add an edge between the new nodes in one graph, but not the other
        Edge ea= aFactory.newEdge(n1a, n2a, 0, WEIGHT, true);
        a.addEdge(ea);
        
        //edge in the 'b' graph points in the opposite direction
        Edge eb= bFactory.newEdge(n2b, n1b, 0, WEIGHT, true);
        b.addEdge(eb);
        assertFalse("two graphs are not equal if they have different directions of edges", GraphTopologyEquals.graphsHaveSameTopology(a, b));
    }

    @Test
    public void testSameTotalEdgeCountButDifferingEdgePlacement() {
        final String ID_1 = "" + Integer.MAX_VALUE;
        final String ID_2 = "" + Integer.MIN_VALUE;
        final String ID_3 = "" + 0;
        final int WEIGHT = 1;
        GraphModel aModel = loader.fromScratch();
        GraphModel bModel = loader.fromScratch();
        GraphFactory aFactory = aModel.factory();
        GraphFactory bFactory = bModel.factory();
        Graph a = aModel.getGraph();
        Graph b = bModel.getGraph();
        Node n1a = aFactory.newNode(ID_1);
        Node n2a = aFactory.newNode(ID_2);
        Node n3a = aFactory.newNode(ID_3);
        a.addNode(n1a);
        a.addNode(n2a);
        a.addNode(n3a);
        
        Node n1b = bFactory.newNode(ID_1);
        Node n2b = bFactory.newNode(ID_2);
        Node n3b = bFactory.newNode(ID_3);
        b.addNode(n1b);
        b.addNode(n2b);
        b.addNode(n3b);
        //add an edge between node 1 and 2 in graph a
        Edge ea= aFactory.newEdge(n1a, n2a, 0, WEIGHT, true);
        a.addEdge(ea);
        //add an edge between node 2 and 3 in graph b
        Edge eb= bFactory.newEdge(n2b, n3b, 0, WEIGHT, true);
        b.addEdge(eb);
        assertFalse("two graphs are not equal if the same node in both graphs have different numbers of edges", GraphTopologyEquals.graphsHaveSameTopology(a, b));
    }
    
    @Test
    public void testGraphsWithDifferentNodeIds(){
        //ensure graphs are not equal when node ids match even though the node counts match
        
        GraphModel aModel = loader.fromScratch();
        GraphModel bModel = loader.fromScratch();
        GraphFactory aFactory = aModel.factory();
        GraphFactory bFactory = bModel.factory();
        Graph a = aModel.getGraph();
        Graph b = bModel.getGraph();
        //create nodes with different ids
        Node aNode = aFactory.newNode("0");
        Node bNode = bFactory.newNode("1");
        
        a.addNode(aNode);
        b.addNode(bNode);
        
        assertFalse("two graphs are not equal if one graph has a node that the other does not", GraphTopologyEquals.graphsHaveSameTopology(a, b));
    }
}
