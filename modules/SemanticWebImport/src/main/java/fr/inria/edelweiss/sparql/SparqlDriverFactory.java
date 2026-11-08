/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */

package fr.inria.edelweiss.sparql;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author edemairy
 */
@ServiceProvider(service=SparqlDriverFactory.class)
public class SparqlDriverFactory {
    static public SparqlDriver getDriver(String driverName) {
        SparqlDriver result = null;
        try {
            Class test = Class.forName(driverName);
            result = (SparqlDriver)test.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception caught when creating driverName(\""+driverName+"\"):" + e.getMessage() );
        }
        return result;
    }
}
