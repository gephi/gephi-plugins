/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

import java.io.InputStream;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author edemairy
 */
public class RdfIteratorTest {

    private InputStream shortFile;
    private static final Logger logger = Logger.getLogger(RdfIteratorTest.class.getName());

    public RdfIteratorTest() {
        shortFile = RdfIteratorTest.class.getResourceAsStream("/test_files/inriaSemanticTweetShort.rdf");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testRoughIteration() {
        RdfIterator iterator = new RdfIterator(shortFile);
        int nbElements = 0;
        while (iterator.hasNext()) {
            Edge newEdge = iterator.next();
            ++nbElements;
        }
        assertEquals(16, nbElements);
    }
}
