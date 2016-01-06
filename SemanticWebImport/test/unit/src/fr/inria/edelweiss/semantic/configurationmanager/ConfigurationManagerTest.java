/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.configurationmanager;

import fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent;
import java.io.IOException;
import java.util.*;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class ConfigurationManagerTest {

    public ConfigurationManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testListFilesInJar() throws IOException {
        String[] expectedResult = {"BBC.xml", "DBPediaMovies.xml", "bio2rdf.xml", "Humans.xml", "DBPedia.xml"};
        ConfigurationManager configurationManager = new ConfigurationManager(null);
        ArrayList<String> listFiles = configurationManager.listFilesInJar(SemanticWebImportMainWindowTopComponent.class, "/fr/inria/edelweiss/semantic/default_configuration/");
        assertEquals(new HashSet(Arrays.asList(expectedResult)), new HashSet(listFiles));
    }

    @Test
    public void testLoadDefaultConfigurations() throws IOException {
        String[] expectedResult = {"DBPediaMovies", "DBPedia", "Humans", "Exemple - Bio2RDF SOAP services for GO SPARQL endpoint", "Example - BBC"};
        ConfigurationManager configurationManager = new ConfigurationManager(null);
        Set<Properties> defaultProperties = configurationManager.loadResourceConfigurations("/fr/inria/edelweiss/semantic/default_configuration/");
        Set<String> defaultPropertiesNames = new HashSet<String>();
        for (Properties p : defaultProperties) {
            defaultPropertiesNames.add(p.getProperty(ConfigurationManager.CONFIGURATION_NAME));
        }
        assertEquals(new HashSet(Arrays.asList(expectedResult)), defaultPropertiesNames);
    }
}
