/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */


package fr.inria.edelweiss.sparql;

import java.util.Observable;
import java.util.Properties;

/**
 * Responsible for the management of parameters.
 */
public abstract class SparqlDriverParameters extends Observable {

    /**
     * Define how to retrieve the configuration of the driver from a configuration.
     */
    public abstract void readProperties(Properties configuration);

    /**
     * Define how to store the parameters into a configuration file.
     */
    public abstract void writeProperties(Properties p);

    /**
     * returns the name of the class responsible for GUI management of the parameters.
     */
    public String getPanelClassName() {
        String simpleClassName = this.getClass().getSimpleName();
        String className = this.getClass().getCanonicalName();
        int lastIndex = className.lastIndexOf('.');
        String packageName = className.substring(0, lastIndex);
        return packageName + "." + simpleClassName + "Panel";
    }

}
