/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql;

import fr.inria.edelweiss.semantic.configurationmanager.ConfigurationManager;
import fr.inria.edelweiss.semantic.utils.FilesUtils;
import java.util.Observer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for applying a SPARQL request on data which can be local or remote.
 */
public abstract class SparqlDriver<P extends SparqlDriverParameters> implements SparqlRequester{

    private static final Logger logger = Logger.getLogger(SparqlDriver.class.getName());
    private P parameters;

    public P getParameters() {
        return parameters;
    }

    public void setParameters(final P parameters) {
        this.parameters = parameters;
    }

    public abstract void init();

    public void setPluginProperties(Properties properties) {
        this.pluginProperties = properties;
    }

    public String getDisplayName() {
        return getClass().getName();
    }
    protected Properties pluginProperties = new Properties();

    /**
     * Use a configuration to: <li> \item \item <\li>
     */
    public static SparqlDriver createFromConfigurationFile(String fileName) {
        Properties currentProperties = FilesUtils.readProperties(fileName);
        String driverName = currentProperties.getProperty(ConfigurationManager.DRIVER_NAME);
        SparqlDriver result = null;
        try {
            result = (SparqlDriver) ClassLoader.getSystemClassLoader().loadClass(driverName).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (result == null) {
            logger.log(Level.INFO, "No SPARQL driver found with name: \"{0}\"", driverName);
            return null;
        }
        result.getParameters().readProperties(currentProperties);
        return result;
    }
}
