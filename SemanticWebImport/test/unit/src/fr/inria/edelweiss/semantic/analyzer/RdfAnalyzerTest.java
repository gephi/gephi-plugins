/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

//~--- non-JDK imports --------------------------------------------------------
import fr.inria.edelweiss.sparql.corese.CoreseDriver;
import fr.inria.edelweiss.sparql.SparqlDriver;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------
/**
 *
 * @author edemairy
 */
public class RdfAnalyzerTest {

    public RdfAnalyzerTest() {
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
     * Test of setSparqlEngine method, of class RdfAnalyzer.
     */
    @Test
    public void testSetSparqlEngine() {
        SparqlDriver driver = new CoreseDriver();
        RdfAnalyzer instance = new RdfAnalyzer(null, null,0);
        instance.setSparqlEngine(driver);
    }

    @Test
    public void testGetSparqlEngine() {
        SparqlDriver driver = new CoreseDriver();
        RdfAnalyzer instance = new RdfAnalyzer(null, null,0);
        SparqlDriver expResult = driver;

        instance.setSparqlEngine(driver);

        SparqlDriver result = instance.getSparqlEngine();

        assertEquals(expResult, result);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
