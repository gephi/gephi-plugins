/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */


package fr.inria.edelweiss.sparql.corese;

import fr.inria.edelweiss.sparql.SparqlDriverParameters;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.DefaultListModel;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class CoreseDriverParameters extends SparqlDriverParameters {
    public static final String RDF_RESOURCE_LIST = "rdfResourceList";
    DefaultListModel rdfResourceListModel;

    public CoreseDriverParameters() {
        rdfResourceListModel = new DefaultListModel();
    }

    public String[] getRdfResources() {
        final Enumeration<String> list = (Enumeration<String>) rdfResourceListModel.elements();
        final ArrayList<String> resultList = new ArrayList<String>();
        while (list.hasMoreElements()) {
            resultList.add(list.nextElement());
        }
        String[] result = new String[resultList.size()];
        resultList.toArray(result);
        return result;
    }

    @Override
    public void readProperties(Properties properties) {
        String listResources = properties.getProperty(RDF_RESOURCE_LIST);
        String[] resources = listResources.split(";");
        addResources(resources);
    }

    @Override
    public void writeProperties(Properties properties) {
        String[] rdfResources = getRdfResources();
        String concatenatedList = "";
        for (String resource : rdfResources) {
            concatenatedList += resource;
            concatenatedList += ";";
        }
        properties.setProperty("rdfResourceList", concatenatedList);
    }

    void addResources(File[] selectedFiles) {
        for (File currentFile : selectedFiles) {
            addResource(currentFile.getPath());
        }
    }

    public final void addResources(final String[] resources) {
        for (String resource : resources) {
            addResource(resource);
        }
    }

    public final void addResource(final String resource) {
        rdfResourceListModel.addElement(resource);
    }

    DefaultListModel getRdfResourcesModel() {
        return rdfResourceListModel;
    }

}
