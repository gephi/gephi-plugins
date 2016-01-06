/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.corese;

import fr.inria.edelweiss.sparql.GephiUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author edemairy
 */
public class CoreseDriverTest {

    private static final Logger LOGGER = Logger.getLogger(CoreseDriverTest.class.getName());
    private static final String HUMAN_RDF_RESOURCE_URL = "/test_files/human_2007_09_11.rdf";
    private static final String HUMAN_RUL_RESOURCE_URL = "/test_files/human_2007_09_11.rul";
    private static final String HUMAN_RDFS_RESOURCE_URL = "/test_files/human_2007_09_11.rdfs";
    private static final String INCORRECT_SPARQL_QUERY = "This is not a correct SPARQL query !!!";
    private static final String GEPHI_SET_LABEL_REQUEST =
            "PREFIX gephi:<http://gephi.org/> construct{?x ?r ?y  . ?x gephi:label \"test\" . ?y gephi:label \"test\"} where {?x ?r ?y}";
    private static final String GEPHI_ADD_ATTRIBUTE_REQUEST =
            "PREFIX gephi:<http://gephi.org/> construct{?x ?r ?y  . ?x gephi:newAttribute \"test\" . ?y gephi:newAttribute \"test\"} where {?x ?r ?y}";

    /**
     * Test of init method, of class CoreseDriver.
     */
    @Test
    public final void testInit() {
        final String rdfFileName = this.getClass().getResource(HUMAN_RDF_RESOURCE_URL).getPath();
        final String rulFileName = this.getClass().getResource(HUMAN_RUL_RESOURCE_URL).getPath();
        final String rdfsFileName = this.getClass().getResource(HUMAN_RDFS_RESOURCE_URL).getPath();
        final CoreseDriver instance = new CoreseDriver();
        instance.getParameters().addResources(new String[]{rdfFileName, rulFileName, rdfsFileName});
        instance.init();
        assertNotNull(instance);
    }

    /**
     * Empty file names should only be silently ignored.
     */
    @Test
    public final void testInitEmptyFilenames() {
        final String rdfFileName = "";
        final String rulFileName = "";
        final String rdfsFileName = "";
        final CoreseDriver instance = new CoreseDriver();
        instance.getParameters().addResources(new String[]{rdfFileName, rulFileName, rdfsFileName});
        instance.init();
        assertNotNull(instance);
    }

    /**
     * Test of init method, of class CoreseDriver.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testInitException() {
        final String rdfFileName = "no_dir/no_existing_file.rdf";
        final String rulFileName = "no_dir/no_existing_file.rul";
        final String rdfsFileName = "no_dir/no_existing_file.rdfs";
        final CoreseDriver instance = new CoreseDriver();
        instance.getParameters().addResources(new String[]{rdfFileName, rulFileName, rdfsFileName});
        instance.init();
        assertNotNull(instance);
    }

    /**
     * Test of getNodeTypes method, of class CoreseDriver.
     */
    @Test
    public void testSPARQLQuery() throws IOException, SAXException, ParserConfigurationException {
        final String rdfFileName = this.getClass().getResource(HUMAN_RDF_RESOURCE_URL).getPath();
        final String rulFileName = this.getClass().getResource(HUMAN_RUL_RESOURCE_URL).getPath();
        final String rdfsFileName = this.getClass().getResource(HUMAN_RDFS_RESOURCE_URL).getPath();
        final CoreseDriver instance = new CoreseDriver();
        instance.getParameters().addResources(new String[]{rdfFileName, rulFileName, rdfsFileName});
        instance.init();
        final GraphModel model = whenCreateNewGraphModel();
        String result = instance.sparqlQuery(
                "PREFIX humans:<http://www.inria.fr/2007/09/11/humans.rdfs#>\n"
                + " construct { ?x rdf:type rdfs:Class }\n"
                + " where { ?x rdf:type rdfs:Class \n"
                + " filter (?x ~ \"humans\")}");
        String expectedResult = getExpectedResultFromResource("/test_files/testSparqlQueryExpectedResult.xml");

        assertEquals(
                buildPredecessorTable(expectedResult), buildPredecessorTable(result));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testInvalidSPARQLQuery() {
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String result = instance.sparqlQuery(INCORRECT_SPARQL_QUERY);
        assertNull(result);
    }

    @Test
    public final void testGephiSetLabel() {
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String resultQuery = instance.sparqlQuery(GEPHI_SET_LABEL_REQUEST);
        thenNoBlankNode(model);
        thenNodesLabelEquals(model, "test");
    }

    @Test
    public final void testGephiAddAttributeLabel() {
        final CoreseDriver instance = whenSetHumanExample();
        final GraphModel model = whenCreateNewGraphModel();
        String resultQuery = instance.sparqlQuery(GEPHI_ADD_ATTRIBUTE_REQUEST);
        thenNoBlankNode(model);
        thenNodesHaveAttributeEquals(model, "newAttribute", "test");
    }

    @Test
    public final void testReadURL() throws IOException, SAXException, ParserConfigurationException {
        final String rdfFileName = "http://localhost/Tetris.rdf";
        final String rulFileName = this.getClass().getResource(HUMAN_RUL_RESOURCE_URL).getPath();
        final String rdfsFileName = this.getClass().getResource(HUMAN_RDFS_RESOURCE_URL).getPath();
        CoreseDriver instance = new CoreseDriver();
        instance.getParameters().addResources(new String[]{rdfFileName, rulFileName, rdfsFileName});

        instance.init();

       GraphModel model = whenCreateNewGraphModel();

        String resultQuery = instance.sparqlQuery(
                " prefix dbpedia-owl: <http://dbpedia.org/ontology/> \n"
                + " construct { \n"
                + "   ?x dbpedia-owl:developer ?y \n"
                + " } where {\n"
                + "   ?x  dbpedia-owl:developer ?y \n"
                + " }");

        assertEquals(buildPredecessorTable(resultQuery), buildPredecessorTable(getExpectedResultFromResource("/test_files/resultTetris.rdf")));
    }

    @Test
    public void testConstruct() throws IOException, SAXException, ParserConfigurationException {
        final String rdfFileName = this.getClass().getResource(HUMAN_RDF_RESOURCE_URL).getPath();
        final String rulFileName = this.getClass().getResource(HUMAN_RUL_RESOURCE_URL).getPath();
        final String rdfsFileName = this.getClass().getResource(HUMAN_RDFS_RESOURCE_URL).getPath();

        CoreseDriver instance = new CoreseDriver();
        instance.getParameters().addResources(new String[]{rdfFileName, rulFileName, rdfsFileName});
        instance.init();

        //GraphModel model = whenCreateNewGraphModel();
        String actualResult = instance.sparqlQuery(
                " construct { ?v1 ?r ?v2 } where { ?v1 ?r ?v2 }");

        String expectedResult = getExpectedResultFromResource("/test_files/resultConstructHumans.xml");


        List<String> doc1Predecessors = buildPredecessorTable(actualResult);
        List<String> doc2Predecessors = buildPredecessorTable(expectedResult);
        assertEquals(doc1Predecessors, doc2Predecessors);

    }

    public GraphModel initModel() {
        GraphModel model = whenCreateNewGraphModel();

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel attributesModel = ac.getModel();
        if (!attributesModel.getNodeTable().hasColumn("namespace")) {
            attributesModel.getNodeTable().addColumn("namespace", AttributeType.STRING);
        }
        return model;
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
            assertEquals(string, n.getNodeData().getLabel());
        }
    }

    private void thenNodesHaveAttributeEquals(final GraphModel model, final String string, final String string0) {
        for (Node n : model.getGraph().getNodes()) {
            assertEquals("test", n.getNodeData().getAttributes().getValue("newAttribute"));
        }
    }

    private String getExpectedResultFromResource(String nameResource) throws IOException {
        final InputStream expectedResultStream = this.getClass().getResource(nameResource).openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(expectedResultStream, "UTF-8"));
        String currentLine = br.readLine();
        StringBuffer expectedResult = new StringBuffer();

        while (currentLine != null) {
            expectedResult.append(currentLine + '\n');
            currentLine = br.readLine();
        }

        String er = expectedResult.toString();
        return er;
    }

    private List<String> buildPredecessorTable(Document doc1) {
        ArrayList<String> result = new ArrayList<String>();

        List<org.w3c.dom.Node> listNodes = new ArrayList<org.w3c.dom.Node>();
        addListNodes(listNodes, doc1.getChildNodes());
        Iterator<org.w3c.dom.Node> iterator = listNodes.iterator();
        while (iterator.hasNext()) {
            org.w3c.dom.Node currentNode = iterator.next();
            listNodes.remove(currentNode);
            NodeList children = currentNode.getChildNodes();
            String currentName = currentNode.getNodeName();
            for (int i = 0; i < children.getLength(); ++i) {
                result.add(children.item(i).getNodeName() + "=>" + currentName);
            }
            addListNodes(listNodes, children);
            iterator = listNodes.iterator();
        }
        Collections.sort(result);
        return result;
    }

    private void addListNodes(List<org.w3c.dom.Node> listNodes, NodeList childNodes) {
        for (int i = 0; i < childNodes.getLength(); ++i) {
            listNodes.add(childNodes.item(i));
        }
    }

    private List<String> buildPredecessorTable(String rdf) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document document = db.parse(new ByteArrayInputStream(rdf.getBytes()));
        List<String> result = buildPredecessorTable(document);
        return result;
    }
}
