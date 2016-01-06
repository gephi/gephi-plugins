/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.soap;

import fr.inria.edelweiss.semantic.importer.SemanticWebImportParser;
import fr.inria.edelweiss.sparql.SparqlDriverFactoryTest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import static org.junit.Assert.*;
import org.junit.*;
import org.openide.util.Lookup;

/**
 *
 * @author edemairy
 */
public class SparqlSoapEndPointDriverTest {

    private static final Logger logger = Logger.getLogger(SparqlDriverFactoryTest.class.getName());
    final private String[] sparqlEndPointShortList = {
        "http://lod.openlinksw.com/sparql/",
        "http://www.semantic-systems-biology.org/biogateway/endpoint",
        "http://semantic.data.gov/sparql",
        "http://dbpedia.org/sparql",
        "http://dbpedia-live.openlinksw.com/sparql/"
    };
    final private String[] sparqlEndPointList = {
        "http://lod.openlinksw.com/sparql/",
        "http://www.semantic-systems-biology.org/biogateway/endpoint",
        "http://jena.hpl.hp.com:3040/backstage",
        "http://dbtune.org/bbc/peel/sparql",
        "http://dbtune.org/bbc/playcount/sparql",
        "http://www4.wiwiss.fu-berlin.de/dailymed/sparql",
        "http://semantic.data.gov/sparql",
        "http://data.gov.uk/sparql",
        "http://www4.wiwiss.fu-berlin.de/dblp/sparql",
        "http://dbpedia.org/sparql",
        "http://dbpedia-live.openlinksw.com/sparql/",
        "http://www4.wiwiss.fu-berlin.de/diseasome/sparql",
        "http://doapspace.org/sparql",
        "http://www4.wiwiss.fu-berlin.de/drugbank/sparql",
        "http://www.factforge.net/sparql",
        "http://openflydata.org/query/flyatlas_20080916",
        "http://openflydata.org/query/flyted_20081203",
        "http://spade.lbl.gov:2021/sparql",
        "http://spade.lbl.gov:2020/sparql",
        "http://dbtune.org/henry/sparql/",
        //        "http://hcls.deri.org/sparql/",
        "http://abdera.watson.ibm.com:8080/sparql",
        "http://dbtune.org/jamendo/sparql",
        "http://data.linkedct.org/sparql",
        "http://data.linkedmdb.org/sparql",
        "http://lobid.org/sparql/",
        "http://lod.openlinksw.com/sparql/",
        "http://dbtune.org/magnatune/sparql",
        "http://dbtune.org/musicbrainz/sparql",
        "http://rdf.myexperiment.org/sparql",
        "http://sparql.neurocommons.org/sparql?",
        "http://myopenlink.net:8890/sparql/",
        "http://demo.openlinksw.com/sparql/",
        "http://www4.wiwiss.fu-berlin.de/gutendata/sparql",
        "http://www.rdfabout.com/sparql",
        "http://revyu.com/sparql",
        "http://%2A.rkbexplorer.com/sparql/",
        "http://data.semanticweb.org/sparql",
        "http://sparql.semantic-web.at/sparql",
        "http://www.sparql.org/sparql",
        "http://www.wasab.dk/morten/2005/04/sparqlette/",
        "http://zbw.eu/beta/sparql",
        "http://www4.wiwiss.fu-berlin.de/is-group/sparql",
        "http://lsd.taxonconcept.org/sparql/",
        "http://uriburner.com/sparql/",
        "http://void.rkbexplorer.com/sparql/",
        "http://www.w3c.es/Prensa/sparql/",
        "http://dannyayers.com:8888/ontoworld/",
        "http://www4.wiwiss.fu-berlin.de/factbook/sparql",
        "http://api.talis.com/stores/periodicals/services/sparql"
    };
    private ProjectController projectController;
    private AttributeController attributeController;
    private AttributeModel attributeModel;
    private GraphController graphController;
    private GraphModel modelToFill;
    private Workspace workspace;

    /**
     * Test of sparqlQuery method, of class SparqlSoapEndPointDriver.
     */
    @Test
    public void testSparqlQuery() throws Exception {
        logger.info("testSparqlQuery");
        whenInitGephi();
        String request = "construct{?x ?r ?y} where { ?x ?r ?y} limit 100";
        SparqlSoapEndPointDriver instance = new SparqlSoapEndPointDriver();
        instance.getParameters().setUrl("http://fr.dbpedia.org/sparql/");//http://dbtune.org/bbc/programmes/sparql/");


        // \todo to factorize inanother place.
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);

        pc.renameWorkspace(pc.getCurrentWorkspace(), "RDF Request Graph");
        Workspace dataWorkspace = pc.getCurrentWorkspace();

        GraphModel model = getCurrentGraphModel();
        instance.sparqlQuery(request);
    }

    /**
     * Test of setUrl method, of class SparqlSoapEndPointDriver.
     */
    @Test
    public void testSetGetURL() {
        logger.info("testSetGetURL");
        String text = "myURL";
        SparqlSoapEndPointDriver instance = new SparqlSoapEndPointDriver();
        instance.getParameters().setUrl(text);
        String result = instance.getParameters().getUrl();
        assertEquals(text, result);
    }

    /**
     * Test whether the driver can access to the short list of SPARQL endpoints.
     */
    @Ignore("Negociation has to be implemented.")
    @Test(timeout = 3000000)
    public void testEndPoints() throws Exception {
        logger.info("testEndPointShort");
        int nbSuccesful = 0;
        for (String sparqlEndPointUrl : sparqlEndPointList) {
            whenInitGephi();
            //callOneEndPoint()
            String request = "construct{?x ?r ?y} where { ?x ?r ?y} limit 100";
            SparqlSoapEndPointDriver instance = new SparqlSoapEndPointDriver();
            instance.getParameters().setUrl(sparqlEndPointUrl);
            try {
                logger.info("Calling " + sparqlEndPointUrl + " ");
                instance.sparqlQuery(request);
                SemanticWebImportParser rdfParser = new SemanticWebImportParser();
                rdfParser.getParameters().setRdfRequest(request);
                rdfParser.populateRDFGraph(instance, new Properties(), null);
                rdfParser.waitEndpopulateRDFGraph();
                int nodeCount = getCurrentGraphModel().getGraph().getNodeCount();
                int edgeCount = getCurrentGraphModel().getGraph().getEdgeCount();
                logger.info("resulting graph: #nodes = " + nodeCount + " #edges = " + edgeCount);
                if ((nodeCount > 0) && (edgeCount > 0)) {
                    nbSuccesful++;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "KO: An exception occured: {0}", e.getMessage());
            }
        }
        logger.log(Level.INFO, "OK: {0}, KO: {1}, Percentage of succesful connections: {2}%",
                new Object[]{nbSuccesful, sparqlEndPointList.length, (100.0 * nbSuccesful / sparqlEndPointList.length)});
        assertTrue(nbSuccesful > sparqlEndPointList.length / 2);
    }

    private void whenInitGephi() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeModel = attributeController.getModel();
        graphController = Lookup.getDefault().lookup(GraphController.class);
        modelToFill = graphController.getModel();
        workspace = projectController.getCurrentWorkspace();
    }

    private GraphModel getCurrentGraphModel() {
        final GraphController currentGraphController = Lookup.getDefault().lookup(GraphController.class);
        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        return currentGraphController.getModel(currentWorkspace);
    }

    @Test
    public void sparqlConnexionExample() throws UnsupportedEncodingException, MalformedURLException, IOException {
        String url = "http://dbpedia.inria.fr/sparql";
        String sparqlQuery = "construct { ?x ?r ?y } where { ?x ?r ?y } limit 50";
        String restQuery = "query" + "=" + URLEncoder.encode(sparqlQuery, "UTF-8") ;
        byte[] msgAsBytes = restQuery.getBytes();

        URL queryURL = new URL(url);
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);
        urlConn.setUseCaches(false);
       // urlConn.setRequestProperty("Content-Type", "application/x-www-url-form-urlencoded");
        urlConn.setRequestProperty("Accept", "application/rdf+xml");

        OutputStream oStream = urlConn.getOutputStream();
        logger.log(Level.INFO, "{0} executing request: {1}", new Object[]{getClass().getName(), restQuery});
        oStream.write(msgAsBytes);
        oStream.flush();

        int responseCode = urlConn.getResponseCode();
        String responseMessage = urlConn.getResponseMessage();
        logger.log(Level.INFO, "responseCode = {0}, message = {1}", new Object[]{responseCode, responseMessage});
        if (responseCode != HttpURLConnection.HTTP_OK) {
            BufferedReader err = new BufferedReader(new InputStreamReader(urlConn.getErrorStream()));
            String currentLine = null;
            while ( (currentLine = err.readLine()) != null ) {
                System.out.println(currentLine);
            }
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

        // Write to temp file

        String currentLine;
        logger.log(Level.INFO, "Result request:");
        while ((currentLine = in.readLine()) != null) {
            System.out.println(currentLine);
        }

        in.close();
        oStream.close();

    }
}
