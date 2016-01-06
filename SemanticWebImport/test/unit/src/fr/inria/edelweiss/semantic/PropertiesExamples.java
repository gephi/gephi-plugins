// Error reading included file Templates/Classes/../Licenses/license-inria-short.txt
package fr.inria.edelweiss.semantic;

import static fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent.*;
import fr.inria.edelweiss.semantic.configurationmanager.ConfigurationManager;
import fr.inria.edelweiss.sparql.corese.CoreseDriver;
import fr.inria.edelweiss.sparql.corese.CoreseDriverParameters;
import fr.inria.edelweiss.sparql.soap.SparqlSoapEndPointDriver;
import fr.inria.edelweiss.sparql.soap.SparqlSoapEndPointDriverParameters;
import java.io.*;
import java.util.Properties;
import org.junit.Test;

/**
 *
 * @author edemairy
 */
public class PropertiesExamples {

    @Test
    public void propertiesUsage() throws FileNotFoundException, IOException {
        Properties bbcProperties = new Properties();
        bbcProperties.setProperty(CoreseDriverParameters.RDF_RESOURCE_LIST, "");
        bbcProperties.setProperty(ConfigurationManager.SPARQL_REQUEST, "construct{ \n  ?x ?r ?y  \n} where {\n  ?x ?r ?y  \n} \n limit 100");
        bbcProperties.setProperty(ConfigurationManager.DRIVER_NAME, "fr.inria.edelweiss.sparql.SparqlSoapEndPointDriver");
        bbcProperties.setProperty(SparqlSoapEndPointDriverParameters.HOST_URL_TAG, "http://lod.openlinksw.com/sparql");
        bbcProperties.setProperty(ConfigurationManager.CONFIGURATION_NAME, "BBC");

        storeInTempFile(bbcProperties);

        Properties moviesProperties = new Properties();
        moviesProperties.setProperty(CoreseDriverParameters.RDF_RESOURCE_LIST, "");
        moviesProperties.setProperty(ConfigurationManager.SPARQL_REQUEST,
                "prefix gephi:<http://gephi.org/>\n"
                + "CONSTRUCT{\n"
                + "  ?film gephi:label ?title .\n"
                + "  ?film gephi:category \"film\" .\n"
                + "  ?director gephi:category \"director\" .\n"
                + "  ?actor gephi:category \"actor\" .\n"
                + "  ?film <http://dbpedia.org/ontology/director> ?director .\n"
                + "  ?film <http://dbpedia.org/ontology/starring> ?actor\n"
                + "} WHERE {\n"
                + "  ?film rdfs:label ?title  .\n"
                + "  ?film <http://dbpedia.org/ontology/director> ?director . \n"
                + "  ?film <http://dbpedia.org/ontology/starring> ?actor \n"
                + "  FILTER(lang(?title)=\"en\")\n"
                + "} LIMIT 10000");
        moviesProperties.setProperty(ConfigurationManager.DRIVER_NAME, "fr.inria.edelweiss.sparql.SparqlSoapEndPointDriver");
        moviesProperties.setProperty(SparqlSoapEndPointDriverParameters.HOST_URL_TAG, "http://dbpedia-live.openlinksw.com/sparql");
        moviesProperties.setProperty(ConfigurationManager.CONFIGURATION_NAME, "DBPedia-Movies");

        storeInTempFile(moviesProperties);

        Properties defaultProperties = new Properties();
        defaultProperties.setProperty(CoreseDriverParameters.RDF_RESOURCE_LIST,
                "/fr/inria/edelweiss/examples/human_2007_09_11.rdf;"
                + "/fr/inria/edelweiss/examples/human_2007_09_11.rdfs;"
                + "/fr/inria/edelweiss/examples/human_2007_09_11.rul");
        defaultProperties.setProperty(ConfigurationManager.SPARQL_REQUEST, "construct{ \n  ?x ?r ?y \n} where {\n  ?x ?r ?y \n}");
        defaultProperties.setProperty(ConfigurationManager.DRIVER_NAME, "fr.inria.edelweiss.sparql.CoreseDriver");
        defaultProperties.setProperty(ConfigurationManager.CONFIGURATION_NAME, "humans");

        storeInTempFile(defaultProperties);

        Properties dbPediaProperties = new Properties();
        dbPediaProperties.setProperty(CoreseDriverParameters.RDF_RESOURCE_LIST, "");
        dbPediaProperties.setProperty(ConfigurationManager.SPARQL_REQUEST, "construct{ \n  ?x ?r ?y  \n} where {\n  ?x ?r ?y  \n} \n limit 100");
        dbPediaProperties.setProperty(ConfigurationManager.DRIVER_NAME, "fr.inria.edelweiss.sparql.SparqlSoapEndPointDriver");
        dbPediaProperties.setProperty(SparqlSoapEndPointDriverParameters.HOST_URL_TAG, "http://dbpedia.org/sparql");
        dbPediaProperties.setProperty(ConfigurationManager.CONFIGURATION_NAME, "DBPedia");

        storeInTempFile(dbPediaProperties);

    }

    private void storeInTempFile(Properties properties) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(File.createTempFile("propertiesTest", ".xml"));
        properties.storeToXML(os, ConfigurationManager.DRIVER_NAME, ConfigurationManager.DRIVER_NAME);
    }
}
