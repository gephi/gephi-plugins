/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.configurationmanager;

import fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent;
import fr.inria.edelweiss.semantic.utils.FilesUtils;
import fr.inria.edelweiss.sparql.SparqlDriver;
import fr.inria.edelweiss.sparql.SparqlDriverParameters;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.openide.util.Exceptions;

/**
 *
 * @author edemairy
 */
public class ConfigurationManager {

    static String lastDirectorySaveConfiguration = System.getenv("HOME") + "/.semanticwebimport/";
    public final static String DRIVER_NAME = "SparqlDriver";
    public final static String CONFIGURATION_NAME = "Name";
    public final static String SPARQL_REQUEST = "SparqlRequest";
    public final static String PYTHON_PRE = "PythonPre";
    public final static String PYTHON_POST = "PythonPost";
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
    private Map<String, Properties> listProperties = null;
    private Properties currentProperties = new Properties();
    private final Component parent;

    public ConfigurationManager(Component newParent) {
        parent = newParent;
    }

    /**
     * @return the listProperties
     */
    public Map<String, Properties> getListProperties() {
        return listProperties;
    }

    /**
     * @param listProperties the listProperties to set
     */
    public void setListProperties(Map<String, Properties> listProperties) {
        this.listProperties = listProperties;
    }

    public void setCurrentProperties(String configurationName) {
        currentProperties = listProperties.get(configurationName);
    }

    public Properties getCurrentProperties() {
        return currentProperties;
    }

    public void saveConfigurationActionPerformed(SparqlDriver<SparqlDriverParameters> sparqlDriver, String configurationName, String sparqlRequest, String pythonPre, String pythonPost) {
        OutputStream os = null;
        try {
            File newFile = FilesUtils.selectFile(JFileChooser.SAVE_DIALOG, "Saving configuration \"" + configurationName + "\"", "Save", lastDirectorySaveConfiguration, parent, ".xml", "Configuration file");
            if (newFile == null) {
                return;
            }
            os = new java.io.FileOutputStream(newFile);
            if (os == null) {
                throw new IllegalArgumentException("Impossible to create file.");
            }
            try {
                Properties properties = new Properties(currentProperties);

                sparqlDriver.getParameters().writeProperties(properties);
                properties.setProperty(CONFIGURATION_NAME, configurationName);
                properties.setProperty(DRIVER_NAME, sparqlDriver.getClass().getName());
                properties.setProperty(SPARQL_REQUEST, sparqlRequest);
                properties.setProperty(PYTHON_POST, pythonPost);
                properties.setProperty(PYTHON_PRE, pythonPre);
                properties.storeToXML(os, "Properties to save.");

                os.close();

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public Properties loadConfigurationActionPerformed(ActionEvent evt) {
        File newFile = FilesUtils.selectFile(JFileChooser.OPEN_DIALOG, "Loading configuration", "Load", lastDirectorySaveConfiguration, parent, ".xml", "Configuration file");
        if (newFile == null) {
            return new Properties();
        }
        InputStream is = null;
        try {
            is = new java.io.FileInputStream(newFile);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (is == null) {
            throw new IllegalArgumentException("Impossible to open the file.");
        }
        Properties result = new Properties();
        try {
            result.loadFromXML(is);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "exception occurred when loading the file: {0}", ex.getMessage());
        }
        return result;
    }

    /**
     *
     * @param directoryPath Path to the directory inside the jar of this class
     * where to find the configuration files.
     * @return A set of properties, each object containing a configuration for
     * the plugin.
     */
    public Set<Properties> loadResourceConfigurations(final String directoryPath) {
        Set<Properties> result = new HashSet<Properties>();
        ArrayList<String> paths = listFilesInJar(ConfigurationManager.class, directoryPath);
        for (String path : paths) {
            String fileName = directoryPath + path;
            try {
                result.add(loadResourceConfiguration(fileName));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Exception occured when attempting to read as a set of properties the resource file: {0}", fileName);
            }
        }
        return result;
    }

    public Properties loadResourceConfiguration(final String fileName) throws IOException {
        Properties newProperties = new Properties();
        InputStream is = SemanticWebImportMainWindowTopComponent.class.getResourceAsStream(fileName);
        newProperties.loadFromXML(is);
        is.close();
        return newProperties;
    }

    /**
     * List the files inside a jar in a given directory.
     *
     * @param classJar The jar searched is the jar containing the class
     * classJar.
     * @param path Path to list inside the jar.
     * @return A list of short file names (i.e. removing the string "path").
     */
    static protected ArrayList<String> listFilesInJar(java.lang.Class classJar, String path) {
        ArrayList<String> result = new ArrayList<String>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL dirURL = classLoader.getResource(removeFirstSlash(path));
        logger.log(Level.INFO, " dirURL = {0}, protocol = {1}", new Object[]{dirURL.getPath(), dirURL.getProtocol()});
        try {
            if (dirURL.getProtocol().equals("jar")) {
                String jarPath = dirURL.getPath().substring("file:".length(), dirURL.getPath().indexOf("!"));
                String jarName = URLDecoder.decode(jarPath, "UTF-8");
                result = listFilesInJar(jarName, path);
            } else if (dirURL.getProtocol().equals("file")) {
                File folder = new File(dirURL.getFile());
                File[] contenuti = folder.listFiles();
                String entryName;
                for (File actual : contenuti) {
                    entryName = actual.getName();
                    entryName = entryName.substring(0, entryName.lastIndexOf('.'));
                    result.add(entryName);
                }

            } else {
                logger.log(Level.SEVERE, "Unsupported kind of file: the path {0} is of type: {1}.", new Object[]{path, dirURL.getProtocol()});
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Exception when attempting to read the default configurations: {0}.", ex.getMessage());
        }

        return result;
    }

    protected static ArrayList<String> listFilesInJar(String jarName, String path) throws IOException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        ArrayList<String> result = new ArrayList<String>();
        try (JarFile jar = new JarFile(jarName)) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var name = entries.nextElement().getName();
                logger.log(Level.INFO, "{0}", name);
                if (name.startsWith(path)) { //filter according to the path
                    var entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    if (!entry.isEmpty()) {
                        result.add(entry);
                    }
                }
            }
        }
        return result;
    }

    private static String removeFirstSlash(String path) {
        if (path.charAt(0) == '/') {
            return path.substring(1);
        } else {
            return path;
        }
    }
}
