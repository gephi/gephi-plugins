package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Table;
import org.gephi.plugins.linkprediction.util.Complexity;
import org.gephi.statistics.spi.Statistics;

import java.util.*;

/**
 * Statistic that predicts the next edge based on different link-prediction
 * algorithm implementations.
 * <p>
 * Per iteration a new edge is added to the existing network. After every
 * iteration, a new situation is assumed and the next edge is calculated
 * on the basis of this new, expanded network.
 * <p>
 * The number of iterations can be limited using the UI.
 *
 * @author Saskia Schueler
 * @see LinkPredictionStatisticsBuilder
 */
public abstract class LinkPredictionStatistics implements Statistics {
    /**
     * Default number of iteration used to predict next edges
     **/
    public static final int ITERATION_LIMIT_DEFAULT = 1;

    /* Column names for data labour */
    public static final String ADDED_IN_RUN = "added_in_run";
    public static final String LAST_VALUE = "last_link_prediction_value";
    public static final String LP_ALGORITHM = "link_prediction_algorithm";

    /* Columns for data labour */
    protected static Column ColLastPrediction;
    protected static Column ColAddedInRun;
    protected static Column ColLastCalculatedValue;

    // Big o complexity of algorithm
    protected Complexity complexity;
    // Holds the calculated prediction values
    protected Map<Edge, Integer> predictions = new HashMap<>();

    /**
     * Initializes the columns used in link prediction.
     *
     * @param edgeTable Table on which columns will be added
     */
    public static void initializeColumns(Table edgeTable) {
        ColLastPrediction = edgeTable.getColumn(LP_ALGORITHM);
        if (ColLastPrediction == null) {
            ColLastPrediction = edgeTable.addColumn(LP_ALGORITHM, "Chosen Link Prediction Algorithm", String.class, "");
        }

        ColAddedInRun = edgeTable.getColumn(ADDED_IN_RUN);
        if (ColAddedInRun == null) {
            ColAddedInRun = edgeTable.addColumn(ADDED_IN_RUN, "Added in Run", Integer.class, 0);
        }

        ColLastCalculatedValue = edgeTable.getColumn(LAST_VALUE);
        if (ColLastCalculatedValue == null) {
            ColLastCalculatedValue = edgeTable.addColumn(LAST_VALUE, "Last Link Prediction Value", Integer.class, 0);
        }
    }

    /**
     * Gets the number of the highest added iteration per algorithm.
     *
     * @param graph     Graph currently working on
     * @param algorithm Used algorithm
     * @return Number of highest iteration
     */
    public static int getMaxIteration(Graph graph, String algorithm) {
        int lastIteration = Arrays.asList(graph.getEdges().toArray()).stream()
                .filter(edge -> edge.getAttribute(ColLastPrediction).toString().equals(algorithm))
                .map(edge -> (int) edge.getAttribute(ColAddedInRun)).max(Comparator.comparing(Integer::valueOf)).orElse(0);
        return lastIteration;
    }

    /**
     * Generates a report after link prediction calculation has finished.
     *
     * @return HTML report
     */
    public String getReport() {
        //This is the HTML report shown when execution ends.
        //One could add a distribution histogram for instance
        return "<HTML> <BODY> <h1>Link Prediction</h1> " + "<hr>"
                + "<br> No global results to show. Check Data Laboratory for results" + "<br />" + "</BODY></HTML>";
    }

    /**
     * Gives an estimate of the assumed duration.
     *
     * @return If the calculation will takes a long time
     */
    public boolean longRuntimeExpected() {
        // TODO Implement
        return false;
    }

    /**
     * Gets the column "last prediction".
     *
     * @return Column "last prediction"
     */
    public static Column getColLastPrediction() {
        return ColLastPrediction;
    }

    /**
     * Sets the columns "last prediction".
     *
     * @param colLastPrediction Column "last prediction"
     */
    public static void setColLastPrediction(Column colLastPrediction) {
        LinkPredictionStatistics.ColLastPrediction = colLastPrediction;
    }

    /**
     * Gets the columns "added in run".
     *
     * @return Column "added in run"
     */
    public static Column getColAddedInRun() {
        return ColAddedInRun;
    }

    /** Sets the column "added in run".
     *
     * @param colAddedInRun Column "added in run"
     */
    public static void setColAddedInRun(Column colAddedInRun) {
        LinkPredictionStatistics.ColAddedInRun = colAddedInRun;
    }

    /**
     * Gets the column "last calculated value".
     *
     * @return Column "last calculated value"
     */
    public static Column getColLastCalculatedValue() {
        return ColLastCalculatedValue;
    }

    /**
     * Sets the column "last calculated value".
     *
     * @param colLastCalculatedValue Column "last calculated value"
     */
    public static void setColLastCalculatedValue(Column colLastCalculatedValue) {
        LinkPredictionStatistics.ColLastCalculatedValue = colLastCalculatedValue;
    }

    /**
     * Gets the complexity of the algorithm.
     *
     * @return Algorithms complexity
     */
    public Complexity getComplexity() {
        return complexity;
    }

    /** Sets the complexity of the algorithm.
     *
     * @param complexity Algorithms complexity
     */
    public void setComplexity(Complexity complexity) {
        this.complexity = complexity;
    }

    /**
     * Gets the edge to add, with the highest calculated prediction.
     *
     * @return Edge to add to the network
     */
    public Edge getHighestPrediction() {
        return predictions.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    /**
     * Gets the number of the next iteration per algorithm.
     *
     * @param graph     Graph currently working on
     * @param algorithm Used algorithm
     * @return Number of next iteration
     */
    public int getNextIteration(Graph graph, String algorithm) {
        int lastIteration = Arrays.asList(graph.getEdges().toArray()).stream()
                .filter(edge -> edge.getAttribute(ColLastPrediction).toString().equals(algorithm))
                .map(edge -> (int) edge.getAttribute(ColAddedInRun)).max(Comparator.comparing(Integer::valueOf)).orElse(0);
        return lastIteration + 1;
    }

    /**
     * Verifies if two statistics are equal.
     *
     * @param o Other statistic
     * @return Evaluation result
     */
    @Override
    public boolean equals(Object o){
        if (o != null) {
            return o.getClass() == this.getClass();
        } else
            return false;
    }

    /**
     * Generates hash code out of class.
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }


}
