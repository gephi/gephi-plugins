/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.carlschroedl.gephi.spanningtree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import javax.swing.JPanel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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
import org.gephi.io.importer.impl.ImportControllerImpl;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author carlschroedl
 */
public class KruskalsAlgorithmTest {
    
    KruskalsAlgorithm instance;
    static ProjectController projectController;
    private static final String PATH = "/com/carlschroedl/gephi/spanningtree/initial/wiki_kruskal_example.gephi";
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
    
    private static boolean nodesHaveSameEdges(EdgeIterable aEdges, EdgeIterable bEdges){
        boolean equal = true;
        Collection<Edge> aEdgeCollection = aEdges.toCollection();
        for(Edge bEdge : bEdges){
            if(!aEdgeCollection.contains(bEdge)){
                equal = false;
                break;
            }
        }
        return equal;
    }
    
    private static boolean equalGraphs(Graph a, Graph b){
        boolean equal = true;
        a.writeLock();
        b.writeLock();
        try{
            if(a.getNodeCount() == b.getNodeCount() && a.getEdgeCount() == b.getEdgeCount()){
                for(Node n : a.getNodes()){
                    if(!b.contains(n)){
                        equal = false;
                        break;
                    } else if(!nodesHaveSameEdges(a.getEdges(n), b.getEdges(n))){
                           equal = false; 
                    }
                }
            } else {
                equal = false;
            }            
        } finally {
            a.writeUnlock();
            b.writeUnlock();
        }
        return equal;
    }
    /**
     * 
     * This method imports the file from the specified classpath-relative url
     * into a graph. Each graph gets it's own workspace. The workspaces are not
     * cleaned up after use. Workspaces should be cleaned up by ProjectController
     * 
     * @param path classpath resource
     * @return the graph model of the file at 'path'
     */
    private GraphModel getGraphModelFromFile(String path){
	GraphModel graphModelFromFile = null;
        projectController.newWorkspace(projectController.getCurrentProject());
	ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        
	Container container;
	try {
            URL url = this.getClass().getResource(PATH);
            URI uri = url.toURI();
            File file = new File(uri);
            container = importController.importFile(file);
	} catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
	} catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

	importController.process(container, new DefaultProcessor(), projectController.getCurrentWorkspace());
	graphModelFromFile = projectController.getCurrentProject().getLookup().lookup(GraphModel.class);
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
    public void testExecute(){
        GraphModel a = getGraphModelFromFile(PATH);
        GraphModel b = getGraphModelFromFile(PATH);
        assertTrue(equalGraphs(a.getGraph(), b.getGraph()));
    }

    
}
