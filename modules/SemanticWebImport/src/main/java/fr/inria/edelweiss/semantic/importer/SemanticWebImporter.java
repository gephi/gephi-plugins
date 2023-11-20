/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.importer;

import fr.inria.edelweiss.sparql.corese.CoreseDriver;
import java.io.Reader;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft.Factory;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Where the import job is done.
 * <ul>
 * <li> receive a Reader to obtain the data to import, \sa setReader;
 * <li> returns a container, \sa getContainer;
 * <li> update a progress bar, \sa setProgressTicket;
 * <li> fill a log file, \sa getReport.
 * </ul>
 * @author edemairy
 */
public class SemanticWebImporter implements FileImporter, LongTask {

    private static final Logger LOGGER = Logger.getLogger(SemanticWebImporter.class.getName());
    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    private String sparqlRequest;
    private SemanticWebImportParser rdfParser;
    private final CoreseDriver sparqlDriver;

    public SemanticWebImporter() {
        sparqlDriver = new CoreseDriver();
    }

    @Override
    public final boolean execute(final ContainerLoader loader) {
        this.container = loader;
        this.report = new Report();

        progressTicket.setDisplayName("SemanticWebImporter");
        progressTicket.start();

        LOGGER.info("Beginning the import");
        final SemanticWebImportParser.RequestParameters requestParameters =
                new SemanticWebImportParser.RequestParameters(getSparqlRequest());
        rdfParser = new SemanticWebImportParser(requestParameters);

        LOGGER.info("Starting the RDF importer for Gephi");

        rdfParser.populateRDFGraph(sparqlDriver, new Properties(), new NullLongTaskListener());
        try {
            rdfParser.waitEndpopulateRDFGraph();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            LOGGER.log(Level.WARNING, "Interrupted!", ex);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Finished the import");

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace dataWorkspace = pc.getCurrentWorkspace();
        GraphController currentGraphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel model = currentGraphController.getGraphModel(dataWorkspace);

        Factory draftFactory = container.factory();
        for (Node currentNode : model.getGraph().getNodes()) {
            String nodeDraftLabel = currentNode.getLabel();
            NodeDraft nodeDraft = draftFactory.newNodeDraft(nodeDraftLabel);
            nodeDraft.setLabel(nodeDraftLabel);
            LOGGER.log(Level.INFO, "Adding new node: {0}", nodeDraftLabel);
            loader.addNode(nodeDraft);
        }

        for (Edge currentEdge: model.getGraph().getEdges()) {
            EdgeDraft edgeDraft = draftFactory.newEdgeDraft();
            edgeDraft.setLabel(currentEdge.getLabel());

            String nodeDraftSourceLabel = currentEdge.getSource().getLabel();
            NodeDraft nodeDraftSource = loader.getNode(nodeDraftSourceLabel);

            String nodeDraftTargetLabel = currentEdge.getTarget().getLabel();
            NodeDraft nodeDraftTarget = loader.getNode(nodeDraftTargetLabel);

            edgeDraft.setSource(nodeDraftSource);
            edgeDraft.setTarget(nodeDraftTarget);
            LOGGER.log(Level.INFO, "Adding edge from {0} to {1}", new Object[]{nodeDraftSourceLabel, nodeDraftTargetLabel});
            loader.addEdge(edgeDraft);
        }

        progressTicket.finish();
        return !cancel;
    }

    @Override
    public final ContainerLoader getContainer() {
        return container;
    }

    @Override
    public final Report getReport() {
        return report;
    }

    @Override
    public final boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public final void setProgressTicket(final ProgressTicket newProgressTicket) {
        this.progressTicket = newProgressTicket;
    }

    @Override
    public final void setReader(Reader reader) {
        this.reader = reader;
    }

    public final String getSparqlRequest() {
        return sparqlRequest;
    }

    public final void setSparqlRequest(final String newSparqlRequest) {
        this.sparqlRequest = newSparqlRequest;
    }

    public final CoreseDriver getDriver() {
        return sparqlDriver;
    }

    void setResources(List<String> resourceList) {
        sparqlDriver.getParameters().addResources( resourceList.toArray(new String[]{}) );
    }
}
