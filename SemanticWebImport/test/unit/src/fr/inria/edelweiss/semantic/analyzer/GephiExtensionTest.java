/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */

package fr.inria.edelweiss.semantic.analyzer;

import fr.inria.edelweiss.sparql.GephiUtils;
import fr.inria.edelweiss.sparql.corese.CoreseDriver;
import fr.inria.edelweiss.sparql.corese.CoreseDriverTest;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.html.StyleSheet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Lookup;




import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ukhy
 */
public class GephiExtensionTest {
    
    
    private static final Logger LOGGER = Logger.getLogger(CoreseDriverTest.class.getName());
    private static final String HUMAN_RDF_RESOURCE_URL = "/test_files/human_2007_09_11.rdf";
    private static final String HUMAN_RUL_RESOURCE_URL = "/test_files/human_2007_09_11.rul";
    private static final String HUMAN_RDFS_RESOURCE_URL = "/test_files/human_2007_09_11.rdfs";
    private static final String INCORRECT_SPARQL_QUERY = "This is not a correct SPARQL query !!!";
    
    private static final String GEPHI_REQUEST =
            "PREFIX gephi:<http://gephi.org/> construct{?x ?r ?y  .} where {?x ?r ?y}";
    
    public GephiExtensionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isGephiExtension method, of class GephiExtension.
     */
    @Test
    public void testIsGephiExtension() {
        String name = "http://gephi.org/gephi:label";
        boolean expResult = true;
        boolean result = GephiExtension.isGephiExtension(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of processGephiExtension method, of class GephiExtension.
     */
    @Test
    public void testProcessGephiExtension_Label() {    
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String actualResult = instance.sparqlQuery(GEPHI_REQUEST);    
        String sourceLabel = "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Eve";
        String edgeLabel = "http://gephi.org/label";
        String targetLabel = "test";
        GephiUtils gephiUtil= new GephiUtils(model);        
        GephiExtension.processGephiExtension(sourceLabel, edgeLabel, targetLabel, gephiUtil);
        thenNoBlankNode(model);
        thenNodesLabelEquals(model, "test");
    }
    
    @Test
    public void testProcessGephiExtension_Size() {       
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String actualResult = instance.sparqlQuery(GEPHI_REQUEST);
        String sourceLabel = "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Eve";
        String edgeLabel = "http://gephi.org/size";
        String targetLabel = "100";
        GephiUtils gephiUtil= new GephiUtils(model);        
        GephiExtension.processGephiExtension(sourceLabel, edgeLabel, targetLabel, gephiUtil);
        thenNoBlankNode(model);
        thenNodesSizeEquals(model, 100);
    }
    
    @Test
    public void testProcessGephiExtension_Color() {       
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String actualResult = instance.sparqlQuery(GEPHI_REQUEST);
        String sourceLabel = "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Eve";
        String edgeLabel = "http://gephi.org/color";
        String targetLabel = "BLUE";
        GephiUtils gephiUtil= new GephiUtils(model);        
        GephiExtension.processGephiExtension(sourceLabel, edgeLabel, targetLabel, gephiUtil);
        thenNoBlankNode(model);
        thenNodesColorEquals(model, "BLUE");
    }
    
    @Test
    public void testProcessGephiExtension_ColorR() {       
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String actualResult = instance.sparqlQuery(GEPHI_REQUEST);
        String sourceLabel = "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Eve";
        String edgeLabel = "http://gephi.org/color_r";
        String targetLabel = "100";
        GephiUtils gephiUtil= new GephiUtils(model);        
        GephiExtension.processGephiExtension(sourceLabel, edgeLabel, targetLabel, gephiUtil);
        thenNoBlankNode(model);
        thenNodesColorREquals(model, "100");
    }
    
    @Test
    public void testProcessGephiExtension_ColorG() {       
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String actualResult = instance.sparqlQuery(GEPHI_REQUEST);
        String sourceLabel = "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Eve";
        String edgeLabel = "http://gephi.org/color_g";
        String targetLabel = "80";
        GephiUtils gephiUtil= new GephiUtils(model);        
        GephiExtension.processGephiExtension(sourceLabel, edgeLabel, targetLabel, gephiUtil);
        thenNoBlankNode(model);
        thenNodesColorGEquals(model, "80");
    }
    
    @Test
    public void testProcessGephiExtension_ColorB() {       
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String actualResult = instance.sparqlQuery(GEPHI_REQUEST);
        String sourceLabel = "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Eve";
        String edgeLabel = "http://gephi.org/color_b";
        String targetLabel = "50";
        GephiUtils gephiUtil= new GephiUtils(model);        
        GephiExtension.processGephiExtension(sourceLabel, edgeLabel, targetLabel, gephiUtil);
        thenNoBlankNode(model);
        thenNodesColorBEquals(model, "50");
    }
   
    private GraphModel whenCreateNewGraphModel() {
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        pc.renameWorkspace(pc.getCurrentWorkspace(), "RDF Request Graph");
        Workspace currentWorkspace = pc.getCurrentWorkspace();
        final GraphController currentGraphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel result = currentGraphController.getModel(currentWorkspace);
        assertNotNull(result);
        return result;
    }

    private CoreseDriver whenSetHumanExample() {
        final CoreseDriver result = new CoreseDriver();
        final String rdfFileName = this.getClass().getResource(HUMAN_RDF_RESOURCE_URL).getPath();
        final String rulFileName = this.getClass().getResource(HUMAN_RUL_RESOURCE_URL).getPath();
        final String rdfsFileName = this.getClass().getResource(HUMAN_RDFS_RESOURCE_URL).getPath();
        result.getParameters().addResources(new String[]{rdfFileName, rulFileName, rdfsFileName});
        result.init();
        return result;
    }

    private void thenNoBlankNode(final GraphModel model) {
        for (Node n : model.getGraph().getNodes()) {
            assertFalse(GephiUtils.isBlankNode(n.getNodeData().getLabel()));
        }
    }
    
    private void thenNodesLabelEquals(final GraphModel model, final String string) {
        for (Node n : model.getGraph().getNodes()) {
        assertEquals("Compare label of nodes",string, n.getNodeData().getLabel());
        }
    }
    private void thenNodesSizeEquals(final GraphModel model, final int valueSize) {
        for (Node n : model.getGraph().getNodes()) {
        assertEquals("Compare size of nodes", valueSize, (int)n.getNodeData().getSize());
        }
    }
    
    private void thenNodesColorEquals(final GraphModel model, final String string) {
        StyleSheet style = new StyleSheet();
        Color temp;
        temp = style.stringToColor(string);
        for (Node n : model.getGraph().getNodes()) {
          Color c = new Color(n.getNodeData().r(),n.getNodeData().g(),n.getNodeData().b());
        assertEquals("Compare color of nodes", temp, c);
        }
    }
    
    private void thenNodesColorREquals(final GraphModel model, final String string) {
        double actualValue, expectedValue;
        float rValue;
        expectedValue = GephiUtils.convertFloatColor(string);
        for (Node n : model.getGraph().getNodes()) {
            rValue = n.getNodeData().r();
            actualValue = (double)rValue;
        assertEquals("Compare color R value of nodes", expectedValue,actualValue,0 );
        }
    }
    private void thenNodesColorGEquals(final GraphModel model, final String string) {
        double actualValue, expectedValue;
        float rValue;
        expectedValue = GephiUtils.convertFloatColor(string);
        for (Node n : model.getGraph().getNodes()) {
            rValue = n.getNodeData().g();
            actualValue = (double)rValue;
        assertEquals("Compare color G value of nodes", expectedValue,actualValue,0 );
        }
    }
    private void thenNodesColorBEquals(final GraphModel model, final String string) {
        double actualValue, expectedValue;
        float rValue;
        expectedValue = GephiUtils.convertFloatColor(string);
        for (Node n : model.getGraph().getNodes()) {
            rValue = n.getNodeData().b();
            actualValue = (double)rValue;
        assertEquals("Compare color B value of nodes", expectedValue,actualValue,0 );
        }
    }

}
