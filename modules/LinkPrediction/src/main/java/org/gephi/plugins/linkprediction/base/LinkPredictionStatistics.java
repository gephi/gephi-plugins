package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.statistics.LinkPredictionColumn;
import org.gephi.plugins.linkprediction.util.Complexity;
import org.gephi.plugins.linkprediction.util.GraphUtils;
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
    /** Default number of iteration used to predict next edges */
    public static final int ITERATION_LIMIT_DEFAULT = 1;
    /** Long runtime threshold, warning if value is reached */
    public static final long RUNTIME_THRESHOLD = 1000000;

    // Columns for data labour
    protected static Column colLastPrediction;
    protected static Column colAddedInRun;
    protected static Column colLastCalculatedValue;

    // Big o complexity of algorithm
    protected static Complexity complexity;
    // Queue of predictions, highest first
    protected PriorityQueue<LinkPredictionProbability> queue = new PriorityQueue<>(Collections.reverseOrder());
    // Prediction probabilities
    protected List<LinkPredictionProbability> probabilities = new ArrayList<>();
    // Last predicted edge
    protected Edge lastPrediction;
    // Highest prediction
    protected LinkPredictionProbability highestPrediction;

    // Console Logger
    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionStatistics.class);


    /**
     * Gets the name of the respective algorithm.
     *
     * @return Algorithm name
     */
    public abstract String getAlgorithmName();

    /**
     * Initializes the columns used in link prediction.
     *
     * @param edgeTable Table on which columns will be added
     */
    public static void initializeColumns(Table edgeTable) {
        // Column containing info about last prediction algorithm
        colLastPrediction = edgeTable.getColumn(LinkPredictionColumn.LP_ALGORITHM.getValue());
        consoleLogger.debug("Initialize column " + LinkPredictionColumn.LP_ALGORITHM.getValue());
        if (colLastPrediction == null) {
            colLastPrediction = edgeTable.addColumn(LinkPredictionColumn.LP_ALGORITHM.getValue(), "Chosen Link Prediction Algorithm", String.class, "");
        }

        // Column containing info about iteration in which edge was added
        colAddedInRun = edgeTable.getColumn(LinkPredictionColumn.ADDED_IN_RUN.getValue());
        consoleLogger.debug("Initialize column " + LinkPredictionColumn.ADDED_IN_RUN.getValue());
        if (colAddedInRun == null) {
            colAddedInRun = edgeTable.addColumn(LinkPredictionColumn.ADDED_IN_RUN.getValue(), "Added in Run", Integer.class, 0);
        }

        // Column containing info about the calculated value
        colLastCalculatedValue = edgeTable.getColumn(LinkPredictionColumn.LAST_VALUE.getValue());
        consoleLogger.debug("Intialize column " + LinkPredictionColumn.LAST_VALUE.getValue());
        if (colLastCalculatedValue == null) {
            colLastCalculatedValue = edgeTable.addColumn(LinkPredictionColumn.LAST_VALUE.getValue(), "Last Link Prediction Value", Integer.class, 0);
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
     * Gets the column "last prediction".
     *
     * @return Column "last prediction"
     */
    public static Column getColLastPrediction() {
        return colLastPrediction;
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
     * Gets the column "last calculated value".
     *
     * @return Column "last calculated value"
     */
    public static Column getColLastCalculatedValue() {
        return colLastCalculatedValue;
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
     * Gets the edge to add, with the highest calculated prediction.
     *
     * @return Edge to add to the network
     */
    public LinkPredictionProbability getHighestPrediction() {
        return queue.peek();
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
     * Recalculates the link prediction probability for neighbours of affected nodes.
     *
     * @param factory Factory to create new edges
     * @param graph   Graph to add predictions on
     * @param a       Center node
     */
    protected abstract void recalculateProbability(GraphFactory factory, Graph graph, Node a);

    /**
     * Checks if undirected edge between node a and b exists.
     *
     * @param graph Graph used for lookup
     * @param a     Source/target node
     * @param b     Source/target node
     * @return Whether edge already does not exist already
     */
    protected boolean isNewEdge(Graph graph, Node a, Node b, String algorithm) {
        // Get edges between a and b
        consoleLogger.log(Level.DEBUG, () -> "Check if edge exists already");
        // FIXME graph.getEdges returns always null
        List<Edge> existingEdges = GraphUtils.getEdges(graph, a, b);

        // Retain only edges with respective algorithm
        existingEdges.removeIf(
                edge -> !edge.getAttribute(colLastPrediction).equals(algorithm) && !edge.getAttribute(colLastPrediction)
                        .equals(""));

        // Count number of edges
        long numberOfExistingEdges = existingEdges.size();
        consoleLogger.log(Level.DEBUG, () -> "Size of existing edges: " + numberOfExistingEdges);

        return numberOfExistingEdges == 0;
    }

    /**
     * Saves calculated values in temporary data structures for further iterations.
     *
     * @param factory Factory to create new edge
     * @param a       Node a
     * @param b       Node b
     * @param value   Calculated prediction value
     */
    protected void saveCalculatedValue(GraphFactory factory, Node a, Node b, int value) {
        // Create new edge
        Edge newEdge = factory.newEdge(a, b, false);
        newEdge.setAttribute(colLastCalculatedValue, value);
        consoleLogger.log(Level.DEBUG, () -> "Save edge: " + a.getLabel() + ", " + b.getLabel() + ", " + value);

        // Add edge to temporary helper data structures
        LinkPredictionProbability predictionProbability = new LinkPredictionProbability(newEdge.getSource(),
                newEdge.getTarget(), value);
        queue.add(predictionProbability);
        probabilities.add(predictionProbability);
    }

    /**
     * Updates calculated values in temporary data structures for further iterations.
     *
     * @param factory Factory to create new edge
     * @param a       Node a
     * @param b       Node b
     * @param value   Calculated prediction value
     */
    protected void updateCalculatedValue(GraphFactory factory, Node a, Node b, int value) {
        // Get calculated prediction probability
        LinkPredictionProbability predictionProbability = getPredictionProbability(a, b);

        // Create edge with new calculated value
        Edge newEdge = factory.newEdge(a, b, false);
        newEdge.setAttribute(colLastCalculatedValue, value);
        consoleLogger.log(Level.DEBUG,
                () -> "Temporarily add new edge: " + a.getLabel() + ", " + b.getLabel() + ", " + value);

        if (predictionProbability != null) {
            // Update values
            queue.remove(predictionProbability);
            predictionProbability.setPredictionValue(value);
            queue.add(predictionProbability);
        } else {
            // Create probability object
            LinkPredictionProbability newProbability = new LinkPredictionProbability(newEdge.getSource(),
                    newEdge.getTarget(), value);
            probabilities.add(newProbability);
            queue.add(newProbability);
        }
    }

    /**
     * Gets already calculated prediction probability.
     *
     * @param a Node a
     * @param b Node b
     * @return Probability of edge between node a and b
     */
    protected LinkPredictionProbability getPredictionProbability(Node a, Node b) {
        consoleLogger.debug("Get prediction probability");

        // Loop through calculated values
        LinkPredictionProbability predictionProbability = null;
        for (LinkPredictionProbability p : probabilities) {
            if ((p.getNodeSource().equals(a) && p.getNodeTarget().equals(b)) || (p.getNodeSource().equals(b)
                    && p.getNodeTarget().equals(a))) {
                consoleLogger.log(Level.DEBUG, () -> "Probability is " + p);
                predictionProbability = p;
            }
        }

        return predictionProbability;
    }

    /**
     * Recalculates link prediction probability for nodes, affected by last prediction.
     *
     * @param graph   Graph on which calculation is based on
     * @param factory Factory to create new edge
     */
    protected void recalculateAffectedNodes(Graph graph, GraphFactory factory) {
        // Recalculate only affected nodes
        consoleLogger.debug("Subsequent calculation");
        // Remove last added element from queue
        highestPrediction = getHighestPrediction();
        queue.remove(highestPrediction);

        // Recalculate for affected nodes
        Node a = lastPrediction.getSource();
        Node b = lastPrediction.getTarget();
        recalculateProbability(factory, graph, a);
        recalculateProbability(factory, graph, b);
    }

    /**
     * Adds highest predicted edge to graph.
     *
     * @param graph   Graph to add edge
     * @param factory Factory to create edge
     */
    protected void addHighestPredictedEdgeToGraph(Graph graph, GraphFactory factory, String algorithm) {
        // Get highest predicted value
        highestPrediction = getHighestPrediction();
        consoleLogger.log(Level.DEBUG, () -> "Highest predicted value is " + highestPrediction);

        final Edge max;
        if (highestPrediction != null) {
            // Create corresponding edge
            max = factory.newEdge(highestPrediction.getNodeSource(), highestPrediction.getNodeTarget(), false);

            // Add edge to graph
            int iteration = getNextIteration(graph, algorithm);
            max.setAttribute(colAddedInRun, iteration);
            max.setAttribute(colLastPrediction, algorithm);
            max.setAttribute(colLastCalculatedValue, highestPrediction.getPredictionValue());
            consoleLogger.log(Level.DEBUG, () -> "Add highest predicted edge: " + max);

            graph.addEdge(max);
            lastPrediction = max;
        }
    }

    /**
     * Checks if execute is executed the first time.
     *
     * @return If initial execution
     */
    protected boolean isInitialExecution() {
        return queue.size() == 0 && lastPrediction == null;
    }

    /**
     * Statistic class used to store link prediction information for handling priority queue.
     */
    public static class LinkPredictionProbability implements Comparable<LinkPredictionProbability> {

        private Node nodeSource;
        private Node nodeTarget;
        private Integer predictionValue;

        public LinkPredictionProbability(Node nodeSource, Node nodeTarget, int predictionValue) {
            this.nodeSource = nodeSource;
            this.nodeTarget = nodeTarget;
            this.predictionValue = predictionValue;
        }

        @Override public int compareTo(LinkPredictionProbability o) {
            return this.getPredictionValue().compareTo(o.getPredictionValue());
        }

        public Integer getPredictionValue() {
            return predictionValue;
        }

        public void setPredictionValue(int predictionValue) {
            this.predictionValue = predictionValue;
        }

        public Node getNodeSource() {
            return nodeSource;
        }

        public Node getNodeTarget() {
            return nodeTarget;
        }
    }
}
