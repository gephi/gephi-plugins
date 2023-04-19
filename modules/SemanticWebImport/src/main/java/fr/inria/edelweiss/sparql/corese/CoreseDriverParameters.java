/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */


package fr.inria.edelweiss.sparql.corese;

import fr.inria.edelweiss.sparql.SparqlDriverParameters;

import javax.swing.DefaultListModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class CoreseDriverParameters extends SparqlDriverParameters {
    public static final String RDF_RESOURCE_LIST = "rdfResourceList";
    DefaultListModel<String> rdfResourceListModel;

    public CoreseDriverParameters() {
        rdfResourceListModel = new DefaultListModel<>();
    }

    public String[] getRdfResources() {
        final Enumeration<String> list = rdfResourceListModel.elements();
        final ArrayList<String> resultList = new ArrayList<>();
        while (list.hasMoreElements()) {
            resultList.add(list.nextElement());
        }
        var result = new String[resultList.size()];
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
        var rdfResources = Arrays.<String>asList(getRdfResources());
        var concatenatedListBuilder = new StringBuilder(50);
        for (String resource : rdfResources) {
            concatenatedListBuilder.append( resource );
            concatenatedListBuilder.append( ";" );
        }
        properties.setProperty(RDF_RESOURCE_LIST, concatenatedListBuilder.toString());
    }

    void addResources(File[] selectedFiles) {
        for (File currentFile : Arrays.<File>asList(selectedFiles)) {
            addResource(currentFile.getPath());
        }
    }

    public final void addResources(final String[] resources) {
        for (String resource : Arrays.<String>asList(resources)) {
            addResource(resource);
        }
    }

    public final void addResource(final String resource) {
        rdfResourceListModel.addElement(resource);
    }

    DefaultListModel<String> getRdfResourcesModel() {
        return rdfResourceListModel;
    }

}
