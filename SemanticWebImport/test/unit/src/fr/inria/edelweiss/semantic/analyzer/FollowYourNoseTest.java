
package fr.inria.edelweiss.semantic.analyzer;

import java.io.InputStream;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;
import fr.inria.edelweiss.semantic.analyzer.HTTPRequestPoster;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ukhy
 */
public class FollowYourNoseTest {
    
    public FollowYourNoseTest() {
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
     * Test of run method, of class FollowYourNose.
     */
    @Test
    public void testRun() throws IOException, ParserConfigurationException, SAXException {
        final GraphModel model = whenCreateNewGraphModel();
        String endpoint = "http://dbpedia.org/resource/Antibes";
        String expectedResult = getExpectedResultFromResource("/test_files/Antibes.rdf");
        String actualResult;
        int count = 100;
      // FollowYourNose test = new FollowYourNose(model, endpoint);
        //test.run();
        //actualResult= test.getRDFResult();
        //assertEquals( buildPredecessorTable (expectedResult),buildPredecessorTable(actualResult));
        //assertEquals(expectedResult,actualResult);
      //  assertEquals(count,test.getStatement());
    }
    
    private String getExpectedResultFromResource(String nameResource) throws IOException {
        final InputStream expectedResultStream = this.getClass().getResource(nameResource).openStream();
        final BufferedReader br = new BufferedReader(new InputStreamReader(expectedResultStream , "UTF-8"));
        String line ;
        StringBuffer expectedResult = new StringBuffer();
        while ((line = br.readLine()) != null)
                {
                expectedResult.append(line);
                }
        br.close();
        String result = expectedResult.toString();
        return result;
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
