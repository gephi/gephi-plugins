/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.importer;

import fr.inria.edelweiss.semantic.LayoutExampleAbstractPostProcessor;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.openide.util.Lookup;

import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.edelweiss.semantic.PluginProperties;
import fr.inria.edelweiss.semantic.analyzer.RdfAnalyzer;
import fr.inria.edelweiss.sparql.GephiUtils;
import fr.inria.edelweiss.sparql.SparqlDriver;


/**
 * Responsible for making the request and building the resulting graph
 * asynchronously. The purpose of this class is to: <ol> <li>use a SPARQL driver
 * to obtain a RDF/XML result;</li> <li>populate the graph by extracting in the
 * result the nodes and the edges.</li> </ol>
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class SemanticWebImportParser implements LongTaskListener {

    private static final Logger logger = Logger.getLogger(SemanticWebImportParser.class.getName());
    private final Semaphore waitEndPopulate = new Semaphore(0);
    LongTaskExecutor executor = new LongTaskExecutor(true);
    private ProjectController pc;
    private Graph rdfGraph;
    private RequestParameters parameters;
    private RdfAnalyzer analyzer;
    private SparqlDriver driver;
    private LongTaskListener listener; // listener waiting for the end of the population task.

    public SemanticWebImportParser() {
        this(new RequestParameters(""));
    }

    public SemanticWebImportParser(RequestParameters requests) {
        this.parameters = requests;
    }

    /**
     * Asynchronous operation that fills the current workspace.
     *
     * @param driverUsed SPARQL request driver.
     * @param properties Parameters to use.
     * @sa waitEndpopulateRDFGraph
     */
    public final void populateRDFGraph(SparqlDriver<?> driverUsed, Properties properties, LongTaskListener listener) {
        this.listener = listener;
        boolean resetWorkspace = Boolean.parseBoolean(properties.getProperty(PluginProperties.RESET_WORKSPACE.getValue(), "false"));
        boolean postProcessing = Boolean.parseBoolean(properties.getProperty(PluginProperties.POST_PROCESSING.getValue(), "false"));
        String saveResultName = properties.getProperty(PluginProperties.SAVE_SPARQL_RESULT.getValue(), "");
        int fynLevel = Integer.parseInt(properties.getProperty(PluginProperties.FYN_LEVEL.getValue(), "0"));

        logger.log(Level.INFO, "resetWorkspace = {0}", resetWorkspace);
        logger.log(Level.INFO, "postProcessing = {0}", postProcessing);

        this.driver = driverUsed;
        this.driver.setPluginProperties(properties);

        GephiUtils.createProjectIfEmpty();
        GephiUtils.createWorkspaceIfEmpty();

        pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace dataWorkspace = pc.getCurrentWorkspace();
        GraphModel model = getCurrentGraphModel(dataWorkspace);
        // @TODO how to reset the graph
        if (resetWorkspace) {
            if (model != null) {
                model.getGraph().clear();
                logger.log(Level.INFO, "workspace reset done");
            }
        }
        Table nodeTable = model.getNodeTable();
        if (!nodeTable.hasColumn("namespace")) {
            nodeTable.addColumn("namespace", String.class);
        }

        analyzer = initAnalyzer(model, parameters.getRdfRequest(), fynLevel);
        analyzer.setSaveResult(saveResultName);
        if (postProcessing) {
            LayoutExampleAbstractPostProcessor postProcessor = new LayoutExampleAbstractPostProcessor();
            analyzer.setPostProcessing(postProcessor);
        }
        executor.setLongTaskListener(this);
        executor.execute(analyzer, analyzer, "Importing Semantic Graph", new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });

    }

    @Override
    public void taskFinished(LongTask lt) {
        if (waitEndPopulate.availablePermits() < 1) {
            waitEndPopulate.release();
        }
        listener.taskFinished(lt);
    }

    public final void waitEndpopulateRDFGraph() throws InterruptedException {
        waitEndPopulate.acquire();
    }

    private RdfAnalyzer initAnalyzer(GraphModel model, String rdfRequest, int fynLevel) {
        driver.init();
        RdfAnalyzer localAnalyzer = new RdfAnalyzer(model, rdfRequest, fynLevel);
        localAnalyzer.setSparqlEngine(driver);
        return localAnalyzer;
    }

    public RequestParameters getParameters() {
        return parameters;
    }

    public void setParameters(RequestParameters newParameters) {
        this.parameters = newParameters;
    }

    private GraphModel getCurrentGraphModel(final Workspace currentWorkspace) {
        final GraphController currentGraphController = Lookup.getDefault().lookup(GraphController.class);
        return currentGraphController.getGraphModel(currentWorkspace);
    }

    /**
     * @return the rdfGraph
     */
    public Graph getRdfGraph() {
        return this.rdfGraph;
    }

    public String getLastRdfResult() {
        return analyzer.getSparqlRequestResult();
    }

    static public class RequestParameters {

        private String rdfRequest;

        public RequestParameters(String rdfRequest) {
            setRdfRequest(rdfRequest);
        }

        public String getRdfRequest() {
            return this.rdfRequest;
        }

        public final void setRdfRequest(String nodeRdfRequest) {
            this.rdfRequest = nodeRdfRequest;
        }
    }
}
