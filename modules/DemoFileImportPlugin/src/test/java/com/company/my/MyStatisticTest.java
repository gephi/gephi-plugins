package com.company.my;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.impl.ImportControllerImpl;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public class MyStatisticTest {
    
    private static String PATH = "/com/company/my/karate.gml";
    private File file;
    private ProjectController projectController;
    private Container container;
    
    public MyStatisticTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws URISyntaxException {
        file = new File(getClass().getResource(PATH).toURI());
        if(!file.exists() || !file.canRead()){
            throw new RuntimeException("file '" + PATH + "' does not exist, or cannot be read");
        } else {
            System.out.println("Confirmed: file '" + PATH + "' exists and can be read.");
        }
        //results are the same with this suggested alternate:
//        file = Utilities.toFile(getClass().getResource(PATH).toURI());
        
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        projectController.newWorkspace(projectController.getCurrentProject());
    }
    
    @After
    public void tearDown() {
        projectController.closeCurrentWorkspace();
        projectController.closeCurrentProject();
    }
    
    @org.junit.Test
    public void testFileGettingImportControllerFromLookup() throws Exception{

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        container = importController.importFile(file);
        assertNotNull(container);
        importController.process(container, new DefaultProcessor(), projectController.getCurrentWorkspace());

    }

    @org.junit.Test
    public void testFileGettingImportControllerImplDirectly() throws Exception{

        ImportController importController = new ImportControllerImpl();
        container = importController.importFile(file);
        assertNotNull(container);
        importController.process(container, new DefaultProcessor(), projectController.getCurrentWorkspace());

    }

    @org.junit.Test
    public void testFileGettingImportControllerFromUI() throws Exception{

        ImportControllerUI importControllerUi = Lookup.getDefault().lookup(ImportControllerUI.class);
        ImportController importController = importControllerUi.getImportController();

        container = importController.importFile(file);
        assertNotNull(container);
        importController.process(container, new DefaultProcessor(), projectController.getCurrentWorkspace());

    }    
    
}


