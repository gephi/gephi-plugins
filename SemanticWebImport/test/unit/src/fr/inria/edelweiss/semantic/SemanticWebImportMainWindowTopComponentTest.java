/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic;

//~--- non-JDK imports --------------------------------------------------------
//~--- JDK imports ------------------------------------------------------------
import fr.inria.edelweiss.semantic.configurationmanager.ConfigurationManager;
import fr.inria.edelweiss.semantic.tests.Utils;
import fr.inria.edelweiss.sparql.GephiUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.ImporterGEXF;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.junit.*;
import static org.junit.Assert.*;
import org.openide.util.Lookup;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class SemanticWebImportMainWindowTopComponentTest {

    private String HUMANS_CLASSES[] = {
        "http://www.inria.fr/2007/09/11/humans.rdfs#Animal", "http://www.inria.fr/2007/09/11/humans.rdfs#Female",
        "http://www.inria.fr/2007/09/11/humans.rdfs#Lecturer", "http://www.inria.fr/2007/09/11/humans.rdfs#Male",
        "http://www.inria.fr/2007/09/11/humans.rdfs#Man", "http://www.inria.fr/2007/09/11/humans.rdfs#Person",
        "http://www.inria.fr/2007/09/11/humans.rdfs#Researcher", "http://www.inria.fr/2007/09/11/humans.rdfs#Woman"
    };
    private String HUMANS_INDIVIDUALS[] = {
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Alice",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Catherine",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#David",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Eve",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Flora",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Gaston",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Harry",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Jennifer",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#John",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Karl",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Laura",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Lucas",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Mark",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Pierre",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#Sophie",
        "http://www.inria.fr/2007/09/11/humans.rdfs-instances#William"
    };
    private String HUMANS_PROPERTIES[] = {
        "http://www.inria.fr/2007/09/11/humans.rdfs#age", "http://www.inria.fr/2007/09/11/humans.rdfs#hasAncestor",
        "http://www.inria.fr/2007/09/11/humans.rdfs#hasBrother", "http://www.inria.fr/2007/09/11/humans.rdfs#hasChild",
        "http://www.inria.fr/2007/09/11/humans.rdfs#hasFather", "http://www.inria.fr/2007/09/11/humans.rdfs#hasFriend",
        "http://www.inria.fr/2007/09/11/humans.rdfs#hasMother", "http://www.inria.fr/2007/09/11/humans.rdfs#hasParent",
        "http://www.inria.fr/2007/09/11/humans.rdfs#hasSister", "http://www.inria.fr/2007/09/11/humans.rdfs#hasSpouse",
        "http://www.inria.fr/2007/09/11/humans.rdfs#shirtsize", "http://www.inria.fr/2007/09/11/humans.rdfs#shoesize",
        "http://www.inria.fr/2007/09/11/humans.rdfs#trouserssize"
    };
    private Container container;
    private GraphController graphController;
    private ImportController importController;
    private ProjectController projectController;
    private Workspace workspace;

    public SemanticWebImportMainWindowTopComponentTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private Utils utils;

    @Before
    public void setUp() {
        utils = new Utils();
        utils.initGephi();
    }

    @After
    public void tearDown() {
    }

    /**
     * @todo Should be removed and set in an independant test, as an example to show how to test gephi functionalities from junit.
     */
    @Test
    public void useGephiFromTests() throws FileNotFoundException, IOException, URISyntaxException {
        int i = -1;

        i = org.openide.util.Utilities.getOperatingSystem();
        readGraphFile("/test_files/hello_world.gexf");
        applyRandomAlgorithm();
        writeFile("result", ".svg");
    }

    private void readGraphFile(String fileName) throws FileNotFoundException, URISyntaxException {
        importController = Lookup.getDefault().lookup(ImportController.class);

        InputStream inputStream = getClass().getResourceAsStream(fileName);
        ImporterGEXF importer = new ImporterGEXF();
        container = importController.importFile(inputStream, importer);
        importController.process(container, new DefaultProcessor(), workspace);
    }

    private static void applyRandomAlgorithm() {
        //todo Not terminated.
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
    }

    private void writeFile(String prefix, String suffix) throws IOException {
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        File tempFile = File.createTempFile(prefix, suffix);
        System.err.println("path=" + tempFile.getAbsolutePath());
        ec.exportFile(tempFile);
    }

    @Test
    public void testCreateGraphs() throws IOException, InterruptedException {
        SemanticWebImportMainWindowTopComponent topComponent = new SemanticWebImportMainWindowTopComponent();
        topComponent.setConfigurationAction("Humans");

        Workspace workspaces[] = utils.whenCreatingGraphs(topComponent);

        assertEquals("there should be 1 worskpace in the project", 1, workspaces.length);
        thenWorkspaceNameEquals("Workspace 0", workspaces[0]);
        thenWorkspaceMustContainIds(HUMANS_INDIVIDUALS, workspaces[0]);
        thenWorkspaceMustContainIds(HUMANS_CLASSES, workspaces[0]);
        thenWorkspaceMustContainIds(HUMANS_PROPERTIES, workspaces[0]);
        thenNodesHaveNamespaceAttribute(HUMANS_CLASSES, workspaces[0], "http://www.inria.fr/2007/09/11/humans.rdfs");
        thenNoJunkDataBug12488(workspaces[0]);
    }

    @Test
    public void testMovieDBConfiguration() throws IOException, InterruptedException {
        SemanticWebImportMainWindowTopComponent topComponent = new SemanticWebImportMainWindowTopComponent();
        topComponent.setConfigurationAction("DBPediaMovies");
        Workspace workspaces[] = utils.whenCreatingGraphs(topComponent);

        assertEquals("there should be 1 worskpace in the project", 1, workspaces.length);
        thenWorkspaceNameEquals("Workspace 0", workspaces[0]);
        thenNoJunkDataBug12488(workspaces[0]);
    }

    @Test
    public void testInitConfigurations() throws IOException {
        String[] expectedResult = {"Example - BBC", "DBPediaMovies", "DBPedia", "Humans"};
        SemanticWebImportMainWindowTopComponent topComponent = new SemanticWebImportMainWindowTopComponent();
        Collection<Properties> properties = topComponent.getConfigurationManager().getListProperties().values();
        Set<String> defaultPropertiesNames = new HashSet<String>();
        for (Properties p : properties) {
            defaultPropertiesNames.add(p.getProperty(ConfigurationManager.CONFIGURATION_NAME));
        }
        assertTrue(defaultPropertiesNames.containsAll(Arrays.asList(expectedResult)));
    }

    private void thenWorkspaceNameEquals(final String expectedWorkspaceName, final Workspace workspace) {
        WorkspaceInformation information = workspace.getLookup().lookup(WorkspaceInformation.class);

        assertEquals("workspace should be named \"" + expectedWorkspaceName + "\"", expectedWorkspaceName,
                information.getName());
    }

    private void thenWorkspaceMustContainIds(final String ids[], final Workspace workspace) {
        for (String id : ids) {
            assertNotNull("node " + id + " not found.", getNode(id));
        }
    }

    private void thenNodesHaveNamespaceAttribute(final String ids[], final Workspace workspace,
            final String namespace) {
        for (String id : ids) {
            Node node = getNode(id);

            assertEquals(namespace, node.getNodeData().getAttributes().getValue("namespace"));
        }
    }

    private void thenNoJunkDataBug12488(final Workspace workspace) {
        final String JUNK_BUG_12488[] = {};
        for (String id : JUNK_BUG_12488) {
            Node node = getNode(id);
            assertNull(node);
        }
    }

    private Node getNode(final String id) {
        GephiUtils utils = new GephiUtils(GephiUtils.getCurrentGraph().getGraphModel());
        return utils.findNode(id);
    }
}
