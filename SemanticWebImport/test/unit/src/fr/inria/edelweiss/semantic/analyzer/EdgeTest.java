/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author edemairy
 */
public class EdgeTest {

    public EdgeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testConstructor() {
        Edge edge = new Edge("string1", "string2", "string3");
        assertEquals("string1", edge.getSource());
        assertEquals("string2", edge.getId());
        assertEquals("string3", edge.getDestination());
    }

    @Test
    public void testGetSet() {
        Edge edge = new Edge("","","");
        edge.setDestination("string3");
        edge.setSource("string1");
        edge.setId("string2");
        assertEquals("string1", edge.getSource());
        assertEquals("string2", edge.getId());
        assertEquals("string3", edge.getDestination());
    }
}
