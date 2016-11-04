/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carlschroedl.gephi.spanningtree;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JPanel;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.utils.progress.ProgressTicket;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author carlschroedl
 */
public class KruskalsAlgorithmTest {
    
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
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class KruskalsAlgorithm.
     */
    @org.junit.Test
    public void testExecute() throws IOException, URISyntaxException{
        
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;
        File file = Utilities.toFile(getClass().getResource("/com/carlschroedl/gephi/spanningtree/initial/wiki_kruskal_example.gephi").toURI());
        container = importController.importFile(file);
        container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);   //Force DIRECTED
        container.getLoader().setAllowAutoNode(false);  //Don't create missing nodes
        
        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);
        
        
        fail("The test case is a prototype.");
    }

    
}
