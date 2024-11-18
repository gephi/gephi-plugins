/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */

package fr.inria.edelweiss.sparql.corese;

//~--- non-JDK imports --------------------------------------------------------

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.edelweiss.semantic.PluginProperties;
import fr.inria.edelweiss.sparql.SparqlDriver;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.openide.util.lookup.ServiceProvider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 */
@ServiceProvider(service = SparqlDriver.class)
public class CoreseDriver extends SparqlDriver<CoreseDriverParameters> {

    private static final Logger logger = Logger.getLogger(CoreseDriver.class.getName());
    private Load loader;
    private Graph graph;
    private QueryProcess queryProcess;
    private boolean ignoreBlankNodes = false;

    public CoreseDriver() {
        setParameters(new CoreseDriverParameters());
    }

    /**
     * Load the files given as parameters.
     *
     */
    @Override
    public void init() {
        graph = Graph.create();
        loader = Load.create( graph );
        this.queryProcess = QueryProcess.create( graph );

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

        Mappings results;
        try {
            results = queryProcess.query(request);
            ResultFormat resultRdf = ResultFormat.create(results, ResultFormat.RDF_XML_FORMAT);
            result.append(resultRdf);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return result.toString();
    }

    @Override
    public String[][] selectOnGraph(String request) {
        Mappings queryResults;
        try {
            queryResults = queryProcess.query(request);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        String[] variables = queryResults.getSelect().stream().map(Node::getKey).collect(Collectors.toList()).toArray(String[]::new);
        String[][] result = new String[queryResults.size()][variables.length];
        int counterResult = 0;
        for (Mapping queryResult : queryResults) {
            int variableNum = 0;
            for (String variable : variables) {
                result[counterResult][variableNum] = queryResult.getValue(variable).toString();
                ++variableNum;
            }
            ++counterResult;
        }
        return result;
    }

    private void loadUrl(final String url) throws IOException, LoadException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);

        try {
            int returnCode = client.executeMethod(method);
            logger.log(Level.INFO, "returned value when getting {0} = {1}", new Object[]{url, returnCode});
            if (returnCode != HttpStatus.SC_OK) {
                throw new IOException("An error occurred");
            } else {
                InputStream input = new ByteArrayInputStream(method.getResponseBodyAsString().getBytes());
                loader.parse(input);
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
                queryProcess.parse(fileName);
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

    private void load_resource(final String fileName) throws LoadException {
        final InputStream resource = this.getClass().getResourceAsStream(fileName);
        loader.parse(resource);
    }

    /**
     * Workaround because corese does not know which type is the file when loading inputstreams.
     *
     * @param fileName
     * @throws LoadException
     */
    private void load_resource_workaround(final String fileName) throws IOException, LoadException {
        int dotPos = fileName.lastIndexOf('.');
        var tempFile = File.createTempFile("corese_input", '.' + fileName.substring(dotPos + 1, fileName.length()));
        try (FileWriter outputTempFile = new FileWriter(tempFile);
             var resource = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)))
        ) {
            String currentLine;
            while ((currentLine = resource.readLine()) != null) {
                outputTempFile.write(currentLine + '\n');
            }
        }
        loader.parse(tempFile.getAbsolutePath());
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


}
