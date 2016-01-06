/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic;

import fr.inria.edelweiss.semantic.PluginProperties;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author edemairy
 */
public class PluginPropertiesTest {

    public PluginPropertiesTest() {
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
     * Test of values method, of class PluginProperties.
     */
    @Test
    public void testSetProperty() {
        Properties properties = new Properties();
        properties.setProperty(PluginProperties.IGNORE_BLANK_PROPERTIES.getValue(), Boolean.toString(true));
        assertEquals("true", properties.getProperty(PluginProperties.IGNORE_BLANK_PROPERTIES.getValue()));
        assertEquals("true", properties.getProperty(PluginProperties.IGNORE_BLANK_PROPERTIES.getValue(), "true"));
        assertEquals(true, Boolean.parseBoolean(properties.getProperty(PluginProperties.IGNORE_BLANK_PROPERTIES.getValue(), "true")));
    }
}
