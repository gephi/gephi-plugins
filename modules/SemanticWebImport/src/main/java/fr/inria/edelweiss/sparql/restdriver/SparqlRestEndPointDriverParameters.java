/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.restdriver;

import fr.inria.edelweiss.sparql.RdfParser;
import fr.inria.edelweiss.sparql.SparqlDriverParameters;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class SparqlRestEndPointDriverParameters extends SparqlDriverParameters {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RdfParser.class.getName());
    public static final String HOST_URL_TAG = "restSparqlEndPointURL";
    public static final String QUERY_TAG = "restSparqlEndPointQuery";
    public static final String REQUEST_PROPERTIES = "restSparqlEndPointRequestProperties";
    public static final String REQUEST_PARAMETERS = "restSparqlEndPointRequestParameters";
    public static final String QUERY_TAGNAME_DEFAULT_VALUE = "query";
    private Map<String, String> requestParameters;
    private Map<String, String> requestProperties;
    String queryTagName = QUERY_TAGNAME_DEFAULT_VALUE;

    public SparqlRestEndPointDriverParameters() {
        requestParameters = new HashMap<String, String>();
        requestProperties = new HashMap<String, String>();
    }

    @Override
    public void readProperties(Properties configuration) {
        try {
            setEndPointUrl(configuration.getProperty(SparqlRestEndPointDriverParameters.HOST_URL_TAG));
            setQueryTagName(configuration.getProperty(SparqlRestEndPointDriverParameters.QUERY_TAG));
            setRequestProperties(readMapInProperties(SparqlRestEndPointDriverParameters.REQUEST_PROPERTIES, configuration));
            setRequestParameters(readMapInProperties(SparqlRestEndPointDriverParameters.REQUEST_PARAMETERS, configuration));
        } catch (UnsupportedEncodingException ex) {
            logger.severe(ex.getMessage());
        }
    }

    @Override
    public void writeProperties(Properties p) {
        try {
            p.setProperty(SparqlRestEndPointDriverParameters.HOST_URL_TAG, getEndPointUrl());
            p.setProperty(SparqlRestEndPointDriverParameters.QUERY_TAG, getQueryTagName());
            writeMapInProperties(getRequestProperties(), SparqlRestEndPointDriverParameters.REQUEST_PROPERTIES, p);
            writeMapInProperties(getRequestParameters(), SparqlRestEndPointDriverParameters.REQUEST_PARAMETERS, p);
        } catch (UnsupportedEncodingException ex) {
            logger.severe(ex.getMessage());
        }
    }

    private void writeMapInProperties(final Map<String, String> map, String nameEntry, Properties p) throws UnsupportedEncodingException {
        StringBuilder properties = new StringBuilder();
        for (String key : map.keySet()) {
            properties.append(URLEncoder.encode(key, "UTF-8"));
            properties.append(";");
            properties.append(URLEncoder.encode(map.get(key), "UTF-8"));
            properties.append(";");
        }
        p.setProperty(nameEntry, properties.toString());
    }

    private Map<String, String> readMapInProperties(String nameEntry, Properties p) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<String, String>();
        String property = p.getProperty(nameEntry);
        property = (property == null) ? "" : property;
        String[] properties = property.split(";");
        if (properties.length % 2 != 0) {
            logger.log(Level.WARNING, "The number of properties should be even, but is equal to {0}", properties.length);
        }
        for (int propertyNum = 0; propertyNum < properties.length / 2; ++propertyNum) {
            result.put(properties[propertyNum * 2], URLDecoder.decode(properties[propertyNum * 2 + 1], "UTF-8"));
        }
        return result;
    }
    String endPointUrl;

    public final String getEndPointUrl() {
        return endPointUrl;
    }

    public final void setEndPointUrl(final String newUrl) {
        this.endPointUrl = newUrl;
	setChanged();
	notifyObservers();
    }

    String makeRequest() {
        StringBuilder result = new StringBuilder();
        for (String key : requestParameters.keySet()) {
            String restValue = requestParameters.get(key);
            result.append("&").append(key).append("=").append(restValue);
        }
        return result.toString();
    }

    /**
     * @return the queryTagName
     */
    public String getQueryTagName() {
        return queryTagName;
    }

    /**
     * @param queryTagName the queryTagName to set
     */
    public void setQueryTagName(String queryTagName) {
        this.queryTagName = queryTagName;
	setChanged();
	notifyObservers();
    }

    public Map<String, String> getRequestProperties() {
        return requestProperties;
    }

    private void setRequestProperties(Map<String, String> readMapInProperties) {
        requestProperties = readMapInProperties;
	setChanged();
	notifyObservers();
    }

    public Map<String, String> getRequestParameters() {
        return requestParameters;
    }

    private void setRequestParameters(Map<String, String> readMapInProperties) {
        requestParameters = readMapInProperties;
	setChanged();
	notifyObservers();
    }
}
