/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */


package fr.inria.edelweiss.sparql.soap;

import fr.inria.edelweiss.sparql.SparqlDriverParameters;
import java.util.Properties;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class SparqlSoapEndPointDriverParameters extends SparqlDriverParameters {
    public static final String HOST_URL_TAG = "sparqlEndPointURL";
    private String url = "http://dbpedia.org/sparql";
    private SparqlSoapEndPointDriverParametersPanel panel;

    public SparqlSoapEndPointDriverParameters() {
        panel = new SparqlSoapEndPointDriverParametersPanel(this);
    }

    public void setUrl(String text) {
        url = text;
	setChanged();
	notifyObservers();
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void readProperties(Properties configuration) {
        setUrl(configuration.getProperty(SparqlSoapEndPointDriverParameters.HOST_URL_TAG));
        panel.reset();
    }

    @Override
    public void writeProperties(Properties p) {
        p.setProperty(SparqlSoapEndPointDriverParameters.HOST_URL_TAG, getUrl());
    }

}
