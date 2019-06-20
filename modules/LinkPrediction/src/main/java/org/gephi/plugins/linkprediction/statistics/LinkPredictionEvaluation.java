package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Macro class that triggers the evaluation calculation for all selected algorithms.
 */
public class LinkPredictionEvaluation extends LinkPredictionStatistics {
    // Number of edge prediction iterations
    private int iterationLimit = ITERATION_LIMIT_DEFAULT;
    // Test graph
    // TODO Add UI element to set path to file
    private String file = "sna_test.gexf";
    // List of link prediction evaluations
    private Map<LinkPredictionStatistics, EvaluationMetric> evaluations = new HashMap<>();

    /**
     * Calcualtes link predictions and metrics for all evaluations.
     *
     * @param graphModel Model to add evaluations
     */
    public void execute(final GraphModel graphModel) {

        Graph train = graphModel.getUndirectedGraph();
        Graph test = loadTestGraph(file);
        int i = 0;
        // TODO Verify if statistics should also be calculated over this UI or if link prediction have to be run via link prediction macro in advance
        while (i < iterationLimit) {
            evaluations.keySet().stream().forEach(statistic -> {
                statistic.execute(graphModel);
                evaluations.get(statistic).run(train, test, statistic);});
            i++;
        }
    }

    /**
     * Add link prediction statistic class if no already exists in list.
     *
     * @param statistic Statistic to add
     */
    public void addStatistic(LinkPredictionStatistics statistic) {
        if (!evaluations.keySet().contains(statistic)) {
            evaluations.put(statistic, new LinkPredictionAccuracy());
        }
    }

    /**
     * Removes evaluation from list.
     *
     * @param evaluation Statistic to remove
     */
    public void removeStatistic(LinkPredictionStatistics evaluation) {
        if (evaluations.keySet().contains(evaluation)) {
            evaluations.remove(evaluation);
        }
    }

    public int getIterationLimit() {
        return iterationLimit;
    }

    public void setIterationLimit(int iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    public Map<LinkPredictionStatistics, EvaluationMetric> getEvaluations() {
        return evaluations;
    }

    /**
     * Get specific link prediction algorithm from evaluations list
     * @param statistic Class of searched statistic
     * @return LinkPredictionStatistic
     */
    public EvaluationMetric getEvaluation(Class statistic) {
        return evaluations.get(statistic);
    }

    public void setEvaluation(Map<LinkPredictionStatistics, EvaluationMetric> evaluations) {
        this.evaluations.putAll(evaluations);
    }

    /**
     * Loads test graph from file into new workspace
     *
     * @param filename Relative path and filename
     * @return graph
     */
    public Graph loadTestGraph(String filename) {
        // Create new workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.newWorkspace(pc.getCurrentProject());

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file
        Container container;
        try {
            File file = new File(getClass().getResource(filename).toURI());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);   //Force UNDIRECTED
            container.getLoader().setAllowAutoNode(false);  //Don't create missing nodes
        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO Warning popup instead
            return Lookup.getDefault().lookup(Graph.class);
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        // Get graph
        Graph test = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace).getUndirectedGraph();

        return test;

    }
}
