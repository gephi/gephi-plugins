package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
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
     */
    public static final int ITERATION_LIMIT_DEFAULT = 1;
    /**
     * Long runtime threshold, warning if value is reached
     */
    public static final long RUNTIME_THRESHOLD = 1000000;

    /* Column containing info when edge got added */
    public static final String ADDED_IN_RUN = "added_in_run";
    /* Column containing info which value the prediction algorithm calculated */
    public static final String LAST_VALUE = "last_link_prediction_value";
    /* Column containing info which prediction algorithm was used */
    public static final String LP_ALGORITHM = "link_prediction_algorithm";

    // Columns for data labour
    protected static Column colLastPrediction;
    protected static Column colAddedInRun;
    protected static Column colLastCalculatedValue;

    // Big o complexity of algorithm
    protected static Complexity complexity;
    // Get highest Prediction + save the Edge that was changed last to save processing time during re-calculation
    protected PriorityQueue<LinkPredictionProbability> pQ = new PriorityQueue<>(Collections.reverseOrder());
    // Predicted value
    protected List<LinkPredictionProbability> lpProb = new ArrayList<>();
    // Last predicted edge
    protected Edge changedInLastRun;
    // Highest prediction
    protected LinkPredictionProbability highestValueObject;

    // Console Logger
    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionStatistics.class);



    /**
     * Initializes the columns used in link prediction.
     *
     * @param edgeTable Table on which columns will be added
     */
    public static void initializeColumns(Table edgeTable) {
        colLastPrediction = edgeTable.getColumn(LP_ALGORITHM);
        consoleLogger.debug("Intialize column " + LP_ALGORITHM);
        if (colLastPrediction == null) {
            colLastPrediction = edgeTable.addColumn(LP_ALGORITHM, "Chosen Link Prediction Algorithm", String.class, "");
        }

        colAddedInRun = edgeTable.getColumn(ADDED_IN_RUN);
        consoleLogger.debug("Intialize column " + ADDED_IN_RUN);
        if (colAddedInRun == null) {
            colAddedInRun = edgeTable.addColumn(ADDED_IN_RUN, "Added in Run", Integer.class, 0);
        }

        colLastCalculatedValue = edgeTable.getColumn(LAST_VALUE);
        consoleLogger.debug("Intialize column " + LAST_VALUE);
        if (colLastCalculatedValue == null) {
            colLastCalculatedValue = edgeTable.addColumn(LAST_VALUE, "Last Link Prediction Value", Integer.class, 0);
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
        consoleLogger.debug("Get current max iteration");
        return Arrays.asList(graph.getEdges().toArray()).stream()
                .filter(edge -> edge.getAttribute(colLastPrediction).toString().equals(algorithm))
                .map(edge -> (int) edge.getAttribute(colAddedInRun)).max(Comparator.comparing(Integer::valueOf))
                .orElse(0);
    }

    /**
     * Gets the name of the respective algorithm.
     *
     * @return Algorithm name
     */
    public abstract String getAlgorithmName();

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
     * @param iterationLimit Number of iterations
     * @param nodeCount      Number of nodes
     * @return If the calculation will takes a long time
     */
    public boolean longRuntimeExpected(long iterationLimit, long nodeCount) {
        switch (complexity) {
            case QUADRATIC:
                consoleLogger.debug("Verify runtime for exponential complexity");
                return (iterationLimit * nodeCount * nodeCount) > RUNTIME_THRESHOLD;
            default:
                // TODO Implement other complexities
                return false;
        }
    }

    /**
     * Gets the column "last prediction".
     *
     * @return Column "last prediction"
     */
    public static Column getColLastPrediction() {
        return colLastPrediction;
    }

    /**
     * Sets the columns "last prediction".
     *
     * @param colLastPrediction Column "last prediction"
     */
    public static void setColLastPrediction(Column colLastPrediction) {
        LinkPredictionStatistics.colLastPrediction = colLastPrediction;
    }

    /**
     * Gets the columns "added in run".
     *
     * @return Column "added in run"
     */
    public static Column getColAddedInRun() {
        return colAddedInRun;
    }

    /**
     * Sets the column "added in run".
     *
     * @param colAddedInRun Column "added in run"
     */
    public static void setColAddedInRun(Column colAddedInRun) {
        LinkPredictionStatistics.colAddedInRun = colAddedInRun;
    }

    /**
     * Gets the column "last calculated value".
     *
     * @return Column "last calculated value"
     */
    public static Column getColLastCalculatedValue() {
        return colLastCalculatedValue;
    }

    /**
     * Sets the column "last calculated value".
     *
     * @param colLastCalculatedValue Column "last calculated value"
     */
    public static void setColLastCalculatedValue(Column colLastCalculatedValue) {
        LinkPredictionStatistics.colLastCalculatedValue = colLastCalculatedValue;
    }

    /**
     * Gets the complexity of the algorithm.
     *
     * @return Algorithms complexity
     */
    public static Complexity getComplexity() {
        return complexity;
    }

    /**
     * Sets the complexity of the algorithm.
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
    /*public Edge getHighestPrediction() {
        return predictions.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }*/

    public LinkPredictionProbability getHighestPrediction() {
        return pQ.peek();
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
                .filter(edge -> edge.getAttribute(colLastPrediction).toString().equals(algorithm))
                .map(edge -> (int) edge.getAttribute(colAddedInRun)).max(Comparator.comparing(Integer::valueOf))
                .orElse(0);
        consoleLogger.debug("Number of last iteration: " + lastIteration);

        return lastIteration + 1;
    }

    /**
     * Verifies if two statistics are equal.
     *
     * @param o Other statistic
     * @return Evaluation result
     */
    @Override public boolean equals(Object o) {
        if (o != null) {
            return o.getClass() == this.getClass();
        } else {
            return false;
        }
    }

    /**
     * Generates hash code out of class.
     *
     * @return Hash code
     */
    @Override public int hashCode() {
        return this.getClass().hashCode();
    }


    /**
     * Add egde
     */
    public void addNewEdge(GraphFactory factory, LinkPredictionProbability lpObject, Node a, Node b, int highestValue) {
        // Add new edge to calculation map
        Edge newEdge = factory.newEdge(a, b, false);
        newEdge.setAttribute(colLastCalculatedValue, highestValue);

        if (consoleLogger.isDebugEnabled()) {
            consoleLogger.debug("Add new edge: " + a.getLabel() + ", " + b.getLabel() + ", " + highestValue);
        }

        if (lpObject != null) {
            // Set new Values
            pQ.remove(lpObject);
            lpObject.setPredictionValue(highestValue);
            pQ.add(lpObject);
        } else {
            LinkPredictionProbability lpE = new LinkPredictionProbability(newEdge.getSource(), newEdge.getTarget(), highestValue);
            lpProb.add(lpE);
            pQ.add(lpE);
        }
    }

    public LinkPredictionProbability getLPObject(Node a, Node b) {

        LinkPredictionProbability lpObject = null;
        for (LinkPredictionProbability lpp : lpProb) {
            if ((lpp.getNodeSource().equals(a) && lpp.getNodeTarget().equals(b)) ||
                    (lpp.getNodeSource().equals(b) && lpp.getNodeTarget().equals(a))) {
                lpObject = lpp;
            }
        }

        if (lpObject != null) {
            lpProb.remove(lpObject);
        }

        return lpObject;
    }
}
