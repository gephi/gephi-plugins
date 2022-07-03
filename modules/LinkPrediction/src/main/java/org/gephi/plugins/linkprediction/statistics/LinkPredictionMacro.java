package org.gephi.plugins.linkprediction.statistics;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

import java.util.ArrayList;
import java.util.List;

import static org.gephi.plugins.linkprediction.statistics.LinkPredictionMacroBuilder.LINK_PREDICTION_NAME;

/**
 * Macro class that triggers the calculations for all selected algorithms.
 */
public class LinkPredictionMacro extends LinkPredictionStatistics {
    // Number of edge prediction iterations
    private int iterationLimit = ITERATION_LIMIT_DEFAULT;
    // List of link prediction statistics
    private List<LinkPredictionStatistics> statistics = new ArrayList<>();

    // Console logger
    private static final Logger consoleLogger = Logger.getLogger(LinkPredictionMacro.class.getName());

    // Serial uid
    private static final long serialVersionUID = 6605122051350231817L;


    /**
     * Gets the name of the respective algorithm.
     *
     * @return Algorithm name
     */
    @Override public String getAlgorithmName() {
        return LINK_PREDICTION_NAME;
    }

    /**
     * Calculates link predictions on all statistics.
     *
     * @param graphModel Model to add statistics
     */
    @Override public void execute(final GraphModel graphModel) {
        // Clear predictions
        clearPredictions();

        // Execute predictions on different algorithms
        int i = 0;
        while (i < iterationLimit) {
            statistics.stream().forEach(statistic -> statistic.execute(graphModel));
            i++;
        }
    }

    /**
     * Add link prediction statistic class if no already exists in list.
     *
     * @param statistic Statistic to add
     */
    public void addStatistic(LinkPredictionStatistics statistic) {
        if (!statistics.contains(statistic)) {
            consoleLogger.log(Level.FINE, () -> "Add algorithm " + statistic.getAlgorithmName());
            statistics.add(statistic);
        }
    }

    /**
     * Removes statistic from list.
     *
     * @param statistic Statistic to remove
     */
    public void removeStatistic(LinkPredictionStatistics statistic) {
        if (statistics.contains(statistic)) {
            consoleLogger.log(Level.FINE, () -> "Remove algorithm " + statistic.getAlgorithmName());
            statistics.remove(statistic);
        }
    }

    /**
     * Sets the maximal number of prediction, that will be added.
     *
     * @param iterationLimit Maximal number of iterations
     */
    public void setIterationLimit(int iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    /**
     * Gets the list of the added algorithms.
     *
     * @return List of added algorithms
     */
    public List<LinkPredictionStatistics> getStatistics() {
        return statistics;
    }

    /**
     * Gets specific link prediction algorithm from statistics list
     *
     * @param statistic Class of searched statistic
     * @return LinkPredictionStatistic
     */
    public LinkPredictionStatistics getStatistic(Class statistic) {
        return statistics.stream().filter(s -> s.getClass().equals(statistic)).findFirst().orElse(null);
    }

    /**
     * Set algorithms to use in link prediction.
     *
     * @param statistics List of statistics
     */
    public void setStatistics(List<LinkPredictionStatistics> statistics) {
        this.statistics = statistics;
    }

    /**
     Iterates over all nodes twice to initially calculate prediction values.

     * @param factory Factory to create new edge
     * @throws UnsupportedOperationException as not possible in macro execution
     */
    @Override protected void calculateAll(GraphFactory factory) {
        throw new UnsupportedOperationException();
    }

    /**
     * Recalculates link prediction probability for nodes, affected by last prediction.
     *
     * @param factory Factory to create new edge
     * @throws UnsupportedOperationException as not possible in macro execution
     */
    @Override protected void recalculateProbability(GraphFactory factory, Node a) {
        throw new UnsupportedOperationException();
    }

    /**
     * Clears predictions on internal calculation stack.
     */
    private void clearPredictions() {
        consoleLogger.log(Level.FINE,"Clear predictions");
        queue = null;
        probabilities = null;
        lastPrediction = null;
    }
}
