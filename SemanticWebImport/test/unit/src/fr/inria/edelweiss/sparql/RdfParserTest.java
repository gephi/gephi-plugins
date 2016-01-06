/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class RdfParserTest {

    public RdfParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSeveralLinks() {
        GephiUtils.createProject();
        GraphModel model = GephiUtils.getCurrentGraph().getGraphModel();
        RdfParser parser = new RdfParser(ClassLoader.getSystemResourceAsStream("test_files/several_links.rdf"), model,2);
        Graph resultingGraph = parser.parse();
        Set<String> ids = new HashSet<String>();

        for (Node n:resultingGraph.getNodes()) {
            System.err.println(n.getNodeData().getLabel());
        }
        for (Edge n:resultingGraph.getEdges()) {
            System.err.println(n.getEdgeData().getLabel());
        }


        assertEquals("Node count", 3, resultingGraph.getNodeCount());
        assertEquals("Edge count", 2, resultingGraph.getEdgeCount());
    }

    /**
     * Test of parse method, of class RdfParser.
     */
    @Test
    public void testUniqueEdgeId() {
        GephiUtils.createProjectIfEmpty();
        GraphModel model = GephiUtils.getCurrentGraph().getGraphModel();
        RdfParser parser = new RdfParser(ClassLoader.getSystemResourceAsStream("test_files/dump_humans.rdf"), model,2);
        Graph resultingGraph = parser.parse();
        Set<String> ids = new HashSet<String>();
        int nbEdges = 0;
        for (Edge edge : resultingGraph.getEdges()) {
            ids.add(edge.getEdgeData().getId());
            ++nbEdges;
        }
        assertEquals(nbEdges, ids.size());
    }
}
