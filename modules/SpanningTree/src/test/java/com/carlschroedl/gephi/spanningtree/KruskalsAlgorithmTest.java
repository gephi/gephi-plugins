/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    static ProjectController projectController;
    private static final String PATH = "/com/carlschroedl/gephi/spanningtree/initial/wiki_kruskal_example_initial.graphml";

    public KruskalsAlgorithmTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
    }

    @AfterClass
    public static void tearDownClass() {
        projectController.closeCurrentProject();
    }


    /**
     *
     * This method imports the file from the specified classpath-relative url
     * into a graph. Each graph gets it's own workspace. The workspaces are not
     * cleaned up after use. Workspaces should be cleaned up by
     * ProjectController
     *
     * @param path classpath resource
     * @return the graph model of the file at 'path'
     */
    private GraphModel getGraphModelFromFile(String path) {
        GraphModel graphModelFromFile = null;
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        projectController.openWorkspace(workspace);
        GraphModel gm = new GraphModelImpl();
        workspace.add(gm);
        ImportController ic = new ImportControllerImpl();
        workspace.add(ic);

        Lookup lookup = workspace.getLookup();

        Container container;
        try {
            URL url = this.getClass().getResource(PATH);
            URI uri = url.toURI();
            File file = new File(uri);
            container = ic.importFile(file);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        Processor processor = new MultiProcessor();
        processor.setWorkspace(workspace);
        ic.process(container, new DefaultProcessor(), projectController.getCurrentWorkspace());
        graphModelFromFile = lookup.lookup(GraphModel.class);
        return graphModelFromFile;
    }

    @Before
    public void setUp() {
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
        GraphModel a = getGraphModelFromFile(PATH);
        GraphModel b = getGraphModelFromFile(PATH);
        assertTrue(GraphTopologyEquals.graphsHaveSameTopology(a.getGraph(), b.getGraph()));
    }

}
