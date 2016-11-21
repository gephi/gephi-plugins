package com.carlschroedl.gephi.spanningtree;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.graph.impl.utils.MapDeepEquals;
import org.gephi.io.importer.api.Container;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.impl.ImportControllerImpl;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.io.processor.plugin.MultiProcessor;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 *
 * @author carlschroedl
 */
public class KruskalsAlgorithmTest {

    KruskalsAlgorithm instance;
    GraphModelLoader loader;
    ProjectController projectController;
    private static final String PATH = "/com/carlschroedl/gephi/spanningtree/initial/wiki_kruskal_example_initial.graphml";

    public KruskalsAlgorithmTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        loader = new GraphModelLoader(projectController);
        instance = new KruskalsAlgorithm();
    }

    @After
    public void tearDown() {
        projectController.closeCurrentProject();
    }

    /**
     * Test of execute method, of class KruskalsAlgorithm.
     */
    @org.junit.Test
    public void testExecute() {
        GraphModel a = loader.fromFile(PATH);
        GraphModel b = loader.fromFile(PATH);
        assertTrue(GraphTopologyEquals.graphsHaveSameTopology(a.getGraph(), b.getGraph()));
    }

}
