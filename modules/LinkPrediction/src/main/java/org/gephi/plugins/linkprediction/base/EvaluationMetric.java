package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
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

    // Console Logger
    private static Logger consoleLogger = LogManager.getLogger(EvaluationMetric.class);

    public EvaluationMetric(LinkPredictionStatistics statistic, Graph initial, Graph validation, Workspace initialWS, Workspace validationWS){
        this.statistic = statistic;
        this.initial = initial;
        this.validation = validation;
        this.initialWS = initialWS;
        this.validationWS = validationWS;
    }

    public void run() {

        /**
         * Duplicate Graph for calculation of chosen algorithm
         */
        if (consoleLogger.isDebugEnabled()) {
            consoleLogger.debug("Duplicate graph for link prediction");
        }
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project pr = pc.getCurrentProject();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);

        /**
         * Determines how many edges have to be calculated and then uses the chosen algorithm
         */
        Set<Edge> initialEdges = new HashSet<>(Arrays.asList(initial.getEdges().toArray()));
        Set<Edge> validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        Workspace ws = pc.newWorkspace(pr);
        if (consoleLogger.isDebugEnabled()) {
            consoleLogger.debug("Initial edges count: " + initialEdges.size());
            consoleLogger.debug("Validation edges count: " + validationEdges.size());
        }

        int diffEdgeCount = 0;
        GraphModel currentGraphModel = gc.getGraphModel(initialWS);
        if (initialEdges.size() > validationEdges.size()) {
            diffEdgeCount = initialEdges.size() - validationEdges.size();
            currentGraphModel = gc.getGraphModel(validationWS);

        }
        else if (initialEdges.size() < validationEdges.size()) {
            diffEdgeCount = validationEdges.size() - initialEdges.size();
            currentGraphModel = gc.getGraphModel(initialWS);
        }

        Graph current = currentGraphModel.getGraph();
        GraphModel trainedModel = gc.getGraphModel(ws);
        trainedModel.bridge().copyNodes(current.getNodes().toArray());
        pc.renameWorkspace(ws, getAlgorithmName());
        pc.openWorkspace(ws);

        trained = trainedModel.getGraph();
        Set<Edge> trainedEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));
        if (consoleLogger.isDebugEnabled()) {
            consoleLogger.debug("Trained edges count: " + trainedEdges.size());
        }
        for (int i = 0; i < diffEdgeCount; i++) {
            statistic.execute(trainedModel);
            if (consoleLogger.isDebugEnabled()) {
                consoleLogger.debug("Trained edges in iteration " + i + ": " + trainedEdges.size());
                consoleLogger.debug("Validation edges in iteration " + i + ": " + validationEdges.size());
            }

            // Calculate current accuracy of algorithm
            double currentResult = calculateCurrentResult(trainedEdges.size(), validationEdges.size());
            iterationResults.put(i, currentResult);
            if (consoleLogger.isDebugEnabled()) {
                consoleLogger.debug("Current result in iteration " + i + ": " + currentResult);
            }
        }

        // Calculate final accuracy of algorithm
        finalResult = calculateCurrentResult(trainedEdges.size(), validationEdges.size());
        if (consoleLogger.isDebugEnabled()) {
            consoleLogger.debug("Final result :" + finalResult);
        }
    }

    /**
     * Calculates the metric at current situation.
     *
     * @param trainedEdgesSize Number of edges of trained graph
     * @param validationEdgesSize Number of edges of validation graph
     * @return Metric result
     */
    private double calculateCurrentResult(int trainedEdgesSize, int validationEdgesSize) {
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
     * Calculates respective metric for link prediction algorithm.
     *
     * @return Metric value
     */
    public abstract double calculate(int addedEdges, Graph trained, Graph validation, LinkPredictionStatistics statistics);

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
