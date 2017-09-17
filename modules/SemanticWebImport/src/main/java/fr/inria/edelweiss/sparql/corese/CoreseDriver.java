/*
 * Copyright (c) 2011, INRIA All rights reserved.
 */
package fr.inria.edelweiss.sparql.corese;

//~--- non-JDK imports --------------------------------------------------------
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.QueryResults;
import fr.inria.edelweiss.semantic.PluginProperties;
import fr.inria.edelweiss.sparql.SparqlDriver;
import java.io.*;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.openide.util.lookup.ServiceProvider;

/**
 */
@ServiceProvider(service = SparqlDriver.class)
public class CoreseDriver extends SparqlDriver<CoreseDriverParameters> {

    private static final Logger logger = Logger.getLogger(CoreseDriver.class.getName());
    private IEngine wrapped;
    private boolean ignoreBlankNodes = false;

    public CoreseDriver() {
        setParameters(new CoreseDriverParameters());
    }

    /**
     * Load the files given as parameters.
     *
     * @throws new value
     */
    @Override
    public void init() {
        final EngineFactory engineFactory = new EngineFactory();
        this.wrapped = engineFactory.newInstance();

        for (String resourceName : getParameters().getRdfResources()) {
            logger.log(Level.INFO, "loading {0}", resourceName);
            loadFile(resourceName);
        }
        ignoreBlankNodes = Boolean.parseBoolean(pluginProperties.getProperty(PluginProperties.IGNORE_BLANK_PROPERTIES.getValue(), "false"));
    }

    /**
     *
     */
    @Override
    public String sparqlQuery(final String request) {
        StringBuilder result = new StringBuilder();

        QueryResults results;
        try {
            results = (QueryResults) wrapped.SPARQLQuery(request);
            result.append(results.toCoreseResult());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return result.toString();
    }

    @Override
    public String[][] selectOnGraph(String request) {
        QueryResults queryResults;
        try {
            queryResults = (QueryResults) wrapped.SPARQLQuery(request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        String[] variables = queryResults.getVariables();
        String[][] result = new String[queryResults.size()][variables.length];
        int counterResult = 0;
        for (IResult queryResult : queryResults) {
            int variableNum = 0;
            for (String variable : variables) {
                result[counterResult][variableNum] = queryResult.getStringValue(variable);
                ++variableNum;
            }
            ++counterResult;
        }
        return result;
    }

    private void loadUrl(final String url) throws IOException, EngineException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);

        try {
            int returnCode = client.executeMethod(method);
            logger.log(Level.INFO, "returned value when getting {0} = {1}", new Object[]{url, returnCode});
            if (returnCode != HttpStatus.SC_OK) {
                throw new IOException("An error occurred");
            } else {
                InputStream input = new ByteArrayInputStream(method.getResponseBodyAsString().getBytes());
                wrapped.load(input, url);
            }
        } finally {
            method.releaseConnection();
        }
    }

    private void loadFile(final String fileName) {
        try {
            if (fileName.startsWith("http://")) {
                loadUrl(fileName);
            } else if (checkFileExist(fileName)) {
                wrapped.load(fileName);
            } else if (checkResourceExist(fileName)) {
                load_resource_workaround(fileName);
            } else {
                throw new IllegalArgumentException("No URL, file or resource with name:" + fileName);
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception in loadFile:{0}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void load_resource(final String fileName) throws EngineException {
        final InputStream resource = this.getClass().getResourceAsStream(fileName);
        wrapped.load(resource, "file:/" + fileName);
    }

    /**
     * Workaround because corese does not know which type is the file when loading inputstreams.
     *
     * @param fileName
     * @throws EngineException
     */
    private void load_resource_workaround(final String fileName) throws EngineException, IOException {
        int dotPos = fileName.lastIndexOf('.');
        File tempFile = File.createTempFile("corese_input", '.' + fileName.substring(dotPos + 1, fileName.length()));
        FileWriter outputTempFile = new FileWriter(tempFile);
        final BufferedReader resource = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)));
        String currentLine;
        while ((currentLine = resource.readLine()) != null) {
            outputTempFile.write(currentLine + '\n');
        }
        resource.close();
        outputTempFile.close();
        wrapped.load(tempFile.getAbsolutePath());
    }

    private boolean checkFileExist(final String fileName) {
        final File checkFile = new File(fileName);

        if (fileName.isEmpty() || checkFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkResourceExist(final String fileName) {
        InputStream resource = this.getClass().getResourceAsStream(fileName);
        return (resource != null);
    }

    @Override
    public String getDisplayName() {
        return "Local Driver - Corese";
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
