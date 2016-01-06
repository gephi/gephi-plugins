/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql;

import org.openide.util.Lookup;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author edemairy
 */
public class SparqlDriverFactoryTest {

    /**
     * Test of getDriver method, of class SPARQLDriverFactory.
     */
    @Test
    public void testGetDriverOK() {
        String driverName = "fr.inria.edelweiss.sparql.corese.CoreseDriver";
        SparqlDriverFactory instance = Lookup.getDefault().lookup(SparqlDriverFactory.class);
        SparqlDriver expResult = null;
        SparqlDriver result = SparqlDriverFactory.getDriver(driverName);
        assertNotNull(result);
    }

    /**
     * Test of getDriver method, of class SPARQLDriverFactory.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetDriverKO() {
        String driverName = "noDriver";
        SparqlDriverFactory instance = new SparqlDriverFactory();
        SparqlDriver expResult = null;
        SparqlDriver result = SparqlDriverFactory.getDriver(driverName);
        assertEquals(expResult, result);
    }
}
