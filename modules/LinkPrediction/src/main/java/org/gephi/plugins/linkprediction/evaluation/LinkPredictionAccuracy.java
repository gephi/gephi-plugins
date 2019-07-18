package org.gephi.plugins.linkprediction.evaluation;

import org.apache.logging.log4j.Level;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.project.api.Workspace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Calculates link prediction accuracy as percentage of correct predicted links.
 * Detailed information and example can be found in README.
 *
 * @see <a href="<a href="https://www.codecogs.com/eqnedit.php?latex=Acc&space;=&space;|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;\&space;/&space;\&space;|E_v|&space;*&space;100" target="_blank"><img src="https://latex.codecogs.com/gif.latex?Acc&space;=&space;|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;\&space;/&space;\&space;|E_v|&space;*&space;100" title="Acc = | E_i \ \cap \ E_v| \ / \ |E_v| * 100" /></a>">Formula</a>
 */
public class LinkPredictionAccuracy extends EvaluationMetric {

    public LinkPredictionAccuracy(LinkPredictionStatistics statistic, Graph initial, Graph validation, Workspace initialWS, Workspace validationWS) {
        super(statistic, initial, validation, initialWS, validationWS);
    }

    /**
     * Calculates accuracy as percentage of correct predicted edges compared to total predicted edges.
     *
     * @param addedEdges Number of edges to add
     * @param trained    Graph on that links predictions are added
     * @param validation Validation graph
     * @param statistics Algorithm used
     * @return Accuracy in percent, rounded to two decimal places
     */
    @Override
    public double calculate(int addedEdges, Graph trained, Graph validation, LinkPredictionStatistics statistics) {
        consoleLogger.debug("Calculate accuracy");

        Set<Edge> trainedEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));

        // Remove edges from other algorithms and
        // edges that initially existed
        consoleLogger.debug("Remove irrelevant edges");
        trainedEdges.removeIf(e -> !e.getAttribute(LinkPredictionStatistics.LP_ALGORITHM).equals(statistics.getAlgorithmName()));

        // Get edges that are in both sets
        consoleLogger.debug("Get congruent edges");
        Set<Edge> diff = trainedEdges;
        diff.removeIf(e -> !validation.isAdjacent(validation.getNode(e.getSource().getId()), validation.getNode(e.getTarget().getId())));

        // Get original accuracy
        double accuracy = ((double) diff.size() / (double) addedEdges) * 100;

        // Round to two decimals
        double rounded = Math.round(accuracy * 100.0) / 100.0;
        consoleLogger.log(Level.DEBUG, () -> "Accuracy for " + statistic.getAlgorithmName() + " is " + rounded);

        return rounded;
    }
}
