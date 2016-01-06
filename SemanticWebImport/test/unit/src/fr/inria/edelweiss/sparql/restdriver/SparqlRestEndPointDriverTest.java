/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.restdriver;

import fr.inria.edelweiss.sparql.SparqlDriver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 *
 * @author edemairy
 */
public class SparqlRestEndPointDriverTest {

    public SparqlRestEndPointDriverTest() {
    }

    @Test
    public void testConstruction() {
        SparqlRestEndPointDriver driver = new SparqlRestEndPointDriver();
        driver.init();
        assertNotNull(driver.getParameters());
    }

    @Test
    public void testLoadService() {
        Collection<? extends SparqlDriver> drivers = Lookup.getDefault().lookupAll(SparqlDriver.class);
        boolean foundDriver = false;
        SparqlRestEndPointDriver driverImpl = null;
        forDriver:
        for (SparqlDriver driver : drivers) {
            if (driver.getClass().getName().equals("fr.inria.edelweiss.sparql.restdriver.SparqlRestEndPointDriver")) {
                foundDriver = true;
                driverImpl = (SparqlRestEndPointDriver) driver;
                break forDriver;
            }
        }
        assertTrue(foundDriver);
        driverImpl.init();
        assertNotNull(driverImpl.getParameters());
    }

    @Test
    public void testParametersReadWrite() {
        SparqlRestEndPointDriver driver1 = new SparqlRestEndPointDriver();

        driver1.getParameters().setEndPointUrl("host1");
        driver1.getParameters().setQueryTagName("queryTag");
        driver1.getParameters().getRequestParameters().put("Autorization", "Basic Z2VwaGk6YW5yXzIwMDg= ");
        driver1.getParameters().getRequestParameters().put("X-User", "myLogin");
        driver1.getParameters().getRequestProperties().put("key1", " =%");
        driver1.getParameters().getRequestProperties().put("key2", "value2");


        Properties saveParameters = new Properties();
        driver1.getParameters().writeProperties(saveParameters);
        driver1 = null;

        SparqlRestEndPointDriver driver2 = new SparqlRestEndPointDriver();
        driver2.getParameters().readProperties(saveParameters);
        assertTrue(driver2.getParameters().getEndPointUrl().equals("host1"));
        assertTrue(driver2.getParameters().getQueryTagName().equals("queryTag"));
        assertTrue(driver2.getParameters().getRequestParameters().get("Autorization").equals("Basic Z2VwaGk6YW5yXzIwMDg= "));
        assertTrue(driver2.getParameters().getRequestParameters().get("X-User").equals("myLogin"));
        assertTrue(driver2.getParameters().getRequestProperties().get("key1").equals(" =%"));
        assertTrue(driver2.getParameters().getRequestProperties().get("key2").equals("value2"));

    }

    @Ignore("Must find an endpoint more stable than isicil (dbpedia ?)")
    @Test
    public void testAccessEndPoint() throws IOException, Exception {
        String expectedResult = makeString(ClassLoader.getSystemResourceAsStream("test_files/isicil_social_rest_expected_result.rdf"));
        SparqlRestEndPointDriver driver = new SparqlRestEndPointDriver();
        Properties configuration = new Properties();
        configuration.loadFromXML(ClassLoader.getSystemResourceAsStream("test_files/isicil_social_rest.xml"));
        driver.getParameters().readProperties(configuration);
        driver.init();
        String actualResult = driver.sparqlQuery("construct {?x ?r ?y} where {?x ?r ?y} limit 10");
        assertEquals(expectedResult, actualResult);
    }

    private String makeString(InputStream systemResourceAsStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(systemResourceAsStream));
        StringBuilder result = new StringBuilder();
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            result.append(currentLine).append('\n');
        }
        return result.toString();
    }
}
