package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.util.*;

/**
 * Calculates the metric to evaluate the quality of a link prediction algorithm.
 *
 * @author Marco Romanutti
 */
public abstract class EvaluationMetric {
    /**
     * Initial graph
     */
    protected final Graph initial;
    /**
     * Trained graph
     *
     */
    protected Graph trained;
    /**
     * Validation graph
     *
     */
    protected final Graph validation;
    /**
     * Validation Workspace
     */
    protected final Workspace validationWS;
    /**
     * Initial Workspace
     */
    protected final Workspace initialWS;
    /**
     * Algorithm to evaluate
     */
    protected final LinkPredictionStatistics statistic;
    /**
     * Calculated results per iteration
     */
    protected Map<Integer, Double> iterationResults = new HashMap<>();
    /**
     * Final results after all iterations
     */
    protected double finalResult;

    /**
     * Number of edges to predict
     */
    protected int diffEdgeCount;

    // Console Logger
    private static Logger consoleLogger = LogManager.getLogger(EvaluationMetric.class);

    public EvaluationMetric(LinkPredictionStatistics statistic, Graph initial, Graph validation, Workspace initialWS, Workspace validationWS){
        this.statistic = statistic;
        this.initial = initial;
        this.validation = validation;
        this.initialWS = initialWS;
        this.validationWS = validationWS;
    }

    /**
     * Calculates respective metric for link prediction algorithm.
     *
     * @return Metric value
     */
    public abstract double calculate(int addedEdges, Graph trained, Graph validation, LinkPredictionStatistics statistics);

    /**
     * Runs the calculation of prediction and evaluates initial and validation graph.
     */
    public void run() {
        consoleLogger.debug("Calcualte evaluation metric");

        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project pr = pc.getCurrentProject();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Table edgeTable = gc.getGraphModel().getEdgeTable();
        LinkPredictionStatistics.initializeColumns(edgeTable);

        // Determine how many edges have to be calculated and then uses the chosen algorithm
        Set<Edge> initialEdges = new HashSet<>(Arrays.asList(initial.getEdges().toArray()));
        consoleLogger.debug("Initial edges count: " + initialEdges.size());
        Set<Edge> validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        consoleLogger.debug("Validation edges count: " + validationEdges.size());

        // Duplicate workspace in order to add edges
        Workspace ws = pc.newWorkspace(pr);

        // Determines current graph model and number of edges to predict
        GraphModel currentGraphModel = determineCurrentGraphModel(gc, initialEdges, validationEdges);

        // Duplicate nodes from current to new workspace
        Graph current = currentGraphModel.getGraph();
        GraphModel trainedModel = gc.getGraphModel(ws);
        trainedModel.bridge().copyNodes(current.getNodes().toArray());
        pc.renameWorkspace(ws, getAlgorithmName());
        pc.openWorkspace(ws);

        // Predict links and save metric per iteration
        trained = trainedModel.getGraph();
        Set<Edge> trainedEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));
        consoleLogger.debug("Trained edges count: " + trainedEdges.size());
        predictLinks(validationEdges, trainedModel, trainedEdges);

        // Calculate final accuracy of algorithm
        finalResult = calculateCurrentResult(trainedEdges.size(), validationEdges.size());
        consoleLogger.debug("Final result :" + finalResult);
    }

    /**
     * Processes link prediction and calculation of current metric iteratively.
     *
     * @param validationEdges Edges of validation graph
     * @param trainedModel Models to predict links on
     * @param trainedEdges Edges of trained graph
     */
    private void predictLinks(Set<Edge> validationEdges, GraphModel trainedModel, Set<Edge> trainedEdges) {
        consoleLogger.debug("Predict links");
        for (int i = 0; i < diffEdgeCount; i++) {
            statistic.execute(trainedModel);
            consoleLogger.debug("Trained edges in iteration " + i + ": " + trainedEdges.size());
            consoleLogger.debug("Validation edges in iteration " + i + ": " + validationEdges.size());

            // Calculate current accuracy of algorithm
            double currentResult = calculateCurrentResult(trainedEdges.size(), validationEdges.size());
            iterationResults.put(i, currentResult);
            consoleLogger.debug("Current result in iteration " + i + ": " + currentResult);
        }
    }

    /**
     * Determines number of edges to predict and current graph model.
     *
     * @param gc Current graph controller
     * @param initialEdges Set of edges of initial graph
     * @param validationEdges Set of edges from validation graph
     * @return Current graph model
     */
    private GraphModel determineCurrentGraphModel(GraphController gc, Set<Edge> initialEdges,
            Set<Edge> validationEdges) {
        consoleLogger.debug("Determine current graph model");
        GraphModel currentGraphModel;

        if (initialEdges.size() > validationEdges.size()) {
            diffEdgeCount = initialEdges.size() - validationEdges.size();
            currentGraphModel = gc.getGraphModel(validationWS);
            consoleLogger.debug("Initial graph is bigger than validation graph");
        }
        else {
            diffEdgeCount = validationEdges.size() - initialEdges.size();
            currentGraphModel = gc.getGraphModel(initialWS);
            consoleLogger.debug("Validation graph is bigger than initial graph");
        }
        return currentGraphModel;
    }

    /**
     * Calculates the metric at current situation.
     *
     * @param trainedEdgesSize Number of edges of trained graph
     * @param validationEdgesSize Number of edges of validation graph
     * @return Metric result
     */
    private double calculateCurrentResult(int trainedEdgesSize, int validationEdgesSize) {
        consoleLogger.debug("Calculate current result");
        double currentResult;
        int addedEdges = validationEdgesSize - trainedEdgesSize;

        if (trainedEdgesSize > validationEdgesSize) {
            currentResult = calculate(addedEdges, validation, trained, statistic);
        } else if (trainedEdgesSize < validationEdgesSize) {
            currentResult = calculate(addedEdges, trained, validation, statistic);
        } else {
            currentResult = calculate(addedEdges, trained, validation, statistic);
        }

        return currentResult;
    }

    /**
     * Get caluclated evaluation results per iteration.
     *
     * @return Calculated metric values per iteration.
     */
    public Map<Integer, Double> getIterationResults() {
        return iterationResults;
    }

    /**
     * Get caluclated final evaluation result.
     *
     * @return Calculated final metric value.
     */
    public double getFinalResult() {
        return finalResult;
    }

    /**
     * Gets number of edges to predict.
     *
     * @return Number of edges to predict
     */
    public int getDiffEdgeCount() {
        return diffEdgeCount;
    }

    public String getAlgorithmName() {
        return statistic.getAlgorithmName();
    }

    public LinkPredictionStatistics getStatistic() {
        return statistic;
    }
    /**
     * Evlauates if evaluation metric has the same underlying statistic algorithm.
     *
     * @param o Object to compare
     * @return Equality of two evaluation metrics
     */
    @Override public boolean equals(Object o) {
        if (!(o instanceof EvaluationMetric))
            return false;
        if (((EvaluationMetric) o).statistic.getClass().equals(this.getClass()))
            return true;
        else
            return false;
    }

    /**
     * Hashes class from statistic algorithm.
     *
     * @return Hashed statistic class
     */
    @Override
    public int hashCode() {
        return Objects.hash(statistic.getClass());
    }
}
