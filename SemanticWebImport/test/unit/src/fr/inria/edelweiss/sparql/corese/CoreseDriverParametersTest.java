/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.corese;

import fr.inria.edelweiss.sparql.corese.CoreseDriver;
import fr.inria.edelweiss.sparql.SparqlDriverParameters;

import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author edemairy
 */
public class CoreseDriverParametersTest {

    final String[] VALUES = {"stringValue0", "stringValue1", "stringValue2"};

    public CoreseDriverParametersTest() {
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

    @Test
    public void testParametersAddResources() {
        CoreseDriver driver = new CoreseDriver();
        CoreseDriverParameters parameters = driver.getParameters();
        parameters.addResources(VALUES);
        for (String value : VALUES) {
            assert (Arrays.asList(parameters.getRdfResources()).contains(value));
        }
    }
}
