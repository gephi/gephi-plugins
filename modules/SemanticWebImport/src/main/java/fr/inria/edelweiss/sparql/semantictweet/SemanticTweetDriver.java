/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.semantictweet;

import fr.inria.edelweiss.sparql.SparqlDriver;
import fr.inria.edelweiss.sparql.corese.CoreseDriver;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * \todo Rewrite the iteration using Corese instead of an ad-hoc recognition.
 *
 * @author edemairy
 */
@ServiceProvider(service = SparqlDriver.class)
public class SemanticTweetDriver extends SparqlDriver<SemanticTweetDriverParameters> {

    private static final Logger logger = Logger.getLogger(SemanticTweetDriver.class.getName());

    public SemanticTweetDriver() {
        setParameters( new SemanticTweetDriverParameters() );
    }

    @Override
    public void init() {
    }

    /**
     * Breadth-first exploration of the semantictweet graph.
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public String sparqlQuery(String request) {
        LinkedList<String> listNodes = new LinkedList<String>();
        listNodes.addFirst(getParameters().getRoot());
        HashSet<String> alreadySeenNodes = new HashSet<String>();
        alreadySeenNodes.add(getParameters().getRoot());

        ArrayList<File> tempFiles = new ArrayList<File>();
        // \todo to be removed after initial tests.
        int cptNodes = 0;
        ProgressTicketProvider progressProvider = Lookup.getDefault().lookup(ProgressTicketProvider.class);
        ProgressTicket progressTicket = progressProvider.createTicket("task", null);
        Progress.setDisplayName(progressTicket, "Task running...");
        Progress.start(progressTicket, getParameters().getMaxNodes() + 1);
        while ((!listNodes.isEmpty()) && (cptNodes < getParameters().getMaxNodes())) {
            Progress.progress(progressTicket, cptNodes);
            ++cptNodes;
            String currentNode = listNodes.removeLast();
            Progress.setDisplayName(progressTicket, "Reading node: " + currentNode);

            String rdfCurrentNode = getRdf(makeAllNode(currentNode));
            logger.log(Level.FINE, "{0} returned the following rdf: \n{1}", new Object[]{currentNode, rdfCurrentNode});

            File newTempFile;
            try {
                newTempFile = File.createTempFile("semanticTweetDriver", ".rdf");

                FileWriter fw = new FileWriter(newTempFile);
                fw.write(rdfCurrentNode);
                newTempFile.deleteOnExit();
                tempFiles.add(newTempFile);
                fw.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            ArrayList<String> listNewNodes = getChilds(rdfCurrentNode);
            listNodes.addAll(0, listNewNodes);
        }
        Progress.progress(progressTicket, cptNodes);
        Progress.setDisplayName(progressTicket, "Processing the request with Corese.");

        CoreseDriver corese = new CoreseDriver();
        for (File currentFile : tempFiles) {
            corese.getParameters().addResource(currentFile.getPath());
        }
        corese.init();
        String result = corese.sparqlQuery(request);
        Progress.finish(progressTicket);
        return result;
    }

    private String getRdf(String currentNode) {
        try {
            URL queryUrl = new URL(currentNode);
            HttpURLConnection urlConn = (HttpURLConnection) queryUrl.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setDoOutput(true);
            urlConn.setRequestProperty("Accept", "application/rdf+xml");

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                result.append(currentLine).append('\n');
            }
            return result.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public ArrayList<String> getChilds(String rdfCurrentNode) {
        ArrayList<String> result = new ArrayList<String>();
        RdfIterator it = new RdfIterator(new ByteArrayInputStream(rdfCurrentNode.getBytes()));
        while (it.hasNext()) {
            Edge currentEdge = it.next();
            if (isIndividual(currentEdge.getSource()) && isIndividual(currentEdge.getDestination())
                    && isKnows(currentEdge.getId())) {
                result.add(removeMeSuffix(currentEdge.getDestination()));
            }
        }
        return result;
    }

    private String removeMeSuffix(String destination) {
        String result = destination.replaceAll("#me", "");
        return result;
    }

    private boolean isIndividual(final String id) {
        return id.contains("#me");
    }

    private boolean isKnows(final String id) {
        return id.contains("http://xmlns.com/foaf/0.1/knows");
    }

    private String makeAllNode(final String currentNode) {
        return currentNode + "/all";
    }

    @Override
    public String[][] selectOnGraph(String request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDisplayName() {
        return "Remote - SemanticTweet Crawler";
    }

    @Override
    public void update(Observable o, Object arg) {
    }
}
