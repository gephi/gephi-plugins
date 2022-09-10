package org.gephi.plugins.linkprediction.base;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.statistics.LinkPredictionColumn;
import org.gephi.plugins.linkprediction.util.Complexity;
import org.gephi.plugins.linkprediction.util.GraphUtils;
import org.gephi.statistics.spi.Statistics;

import java.util.*;
import java.util.stream.Collectors;

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

    // Graph to calculate predictions on
    protected Graph graph;
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
    private static final Logger consoleLogger = Logger.getLogger(LinkPredictionStatistics.class.getName());


    /**
     * Executes link prediction and adds edge with highest possibility.
     *
     * @param graphModel Graph on which edge will be added
     */
    @Override public void execute(GraphModel graphModel) {
        consoleLogger.log(Level.FINE,"Execution of link prediction started");

        // Look if the result column already exist and create it if needed
        consoleLogger.log(Level.FINE,"Initialize columns");
        Table edgeTable = graphModel.getEdgeTable();
        initializeColumns(edgeTable);

        // Get graph factory
        consoleLogger.log(Level.FINE,"Get factory");
        graph = graphModel.getGraph();
        GraphFactory factory = graphModel.factory();

        // Lock graph for writes
        consoleLogger.log(Level.FINE,"Lock graph");
        graph.writeLock();

        if (isInitialExecution()) {
            // Iterate on all nodes for first execution
            calculateAll(factory);

        } else {
            // Only change affected node for subsequent iterations
            recalculateAffected(factory);
        }

        // Add highest predicted edge to graph
        addHighestPredictedEdgeToGraph(factory, getAlgorithmName());

        // Unlock graph
        consoleLogger.log(Level.FINE,"Unlock graph");
        graph.writeUnlock();
    }

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
        colLastPrediction = edgeTable.getColumn(LinkPredictionColumn.LP_ALGORITHM.getName());
        consoleLogger.log(Level.FINE,"Initialize column " + LinkPredictionColumn.LP_ALGORITHM.getName());
        if (colLastPrediction == null) {
            colLastPrediction = edgeTable.addColumn(LinkPredictionColumn.LP_ALGORITHM.getName(), "Chosen Link Prediction Algorithm", String.class, "");
        }

        // Column containing info about iteration in which edge was added
        colAddedInRun = edgeTable.getColumn(LinkPredictionColumn.ADDED_IN_RUN.getName());
        consoleLogger.log(Level.FINE,"Initialize column " + LinkPredictionColumn.ADDED_IN_RUN.getName());
        if (colAddedInRun == null) {
            colAddedInRun = edgeTable.addColumn(LinkPredictionColumn.ADDED_IN_RUN.getName(), "Added in Run", Integer.class, 0);
        }

        // Column containing info about the calculated value
        colLastCalculatedValue = edgeTable.getColumn(LinkPredictionColumn.LAST_VALUE.getName());
        consoleLogger.log(Level.FINE,"Initialize column " + LinkPredictionColumn.LAST_VALUE.getName());
        if (colLastCalculatedValue == null) {
            colLastCalculatedValue = edgeTable.addColumn(LinkPredictionColumn.LAST_VALUE.getName(), "Last Link Prediction Value", Integer.class, 0);
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
        consoleLogger.log(Level.FINE,"Get current max iteration");
        return Arrays.stream(graph.getEdges().toArray())
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
        int lastIteration = Arrays.stream(graph.getEdges().toArray())
                .filter(edge -> edge.getAttribute(colLastPrediction).toString().equals(algorithm))
                .map(edge -> (int) edge.getAttribute(colAddedInRun)).max(Comparator.comparing(Integer::valueOf))
                .orElse(0);
        consoleLogger.log(Level.FINE, () -> "Number of last iteration: " + lastIteration);

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
     * @param a       Center node
     */
    protected abstract void recalculateProbability(GraphFactory factory, Node a);

    /**
     * Checks if undirected edge between node a and b exists.
     *
     * @param a     Source/target node
     * @param b     Source/target node
     * @return Whether edge already does not exist already
     */
    protected boolean isNewEdge(Node a, Node b, String algorithm) {
        // Get edges between a and b
        consoleLogger.log(Level.FINE, () -> "Check if edge exists already");
        // FIXME graph.getEdges returns always null
        List<Edge> existingEdges = GraphUtils.getEdges(graph, a, b);

        // Retain only edges with respective algorithm
        existingEdges.removeIf(
                edge -> !edge.getAttribute(colLastPrediction).equals(algorithm) && !edge.getAttribute(colLastPrediction)
                        .equals(""));

        // Count number of edges
        long numberOfExistingEdges = existingEdges.size();
        consoleLogger.log(Level.FINE, () -> "Size of existing edges: " + numberOfExistingEdges);

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
        consoleLogger.log(Level.FINE, () -> "Save edge: " + a.getLabel() + ", " + b.getLabel() + ", " + value);

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
        consoleLogger.log(Level.FINE,
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
        consoleLogger.log(Level.FINE,"Get prediction probability");

        // Loop through calculated values
        LinkPredictionProbability predictionProbability = null;
        for (LinkPredictionProbability p : probabilities) {
            if ((p.getNodeSource().equals(a) && p.getNodeTarget().equals(b)) || (p.getNodeSource().equals(b)
                    && p.getNodeTarget().equals(a))) {
                consoleLogger.log(Level.FINE, () -> "Probability is " + p);
                predictionProbability = p;
            }
        }

        return predictionProbability;
    }

    /**
     * Iterates over all nodes twice to initially calculate prediction values.
     *
     * @param factory Factory to create new edges
     */
    protected abstract void calculateAll(GraphFactory factory);

    /**
     * Recalculates link prediction probability for nodes, affected by last prediction.
     *
     * @param factory Factory to create new edge
     */
    protected void recalculateAffected(GraphFactory factory) {
        // Recalculate only affected nodes
        consoleLogger.log(Level.FINE,"Subsequent calculation");
        // Remove last added element from queue
        highestPrediction = getHighestPrediction();
        queue.remove(highestPrediction);

        // Recalculate for affected nodes
        Node a = lastPrediction.getSource();
        Node b = lastPrediction.getTarget();
        recalculateProbability(factory, a);
        recalculateProbability(factory, b);
    }

    /**
     * Adds highest predicted edge to graph.
     *
     * @param factory Factory to create edge
     */
    protected void addHighestPredictedEdgeToGraph(GraphFactory factory, String algorithm) {
        // Get highest predicted value
        highestPrediction = getHighestPrediction();
        consoleLogger.log(Level.FINE, () -> "Highest predicted value is " + highestPrediction);

        final Edge max;
        if (highestPrediction != null) {
            // Create corresponding edge
            max = factory.newEdge(highestPrediction.getNodeSource(), highestPrediction.getNodeTarget(), false);

            // Add edge to graph
            int iteration = getNextIteration(graph, algorithm);
            max.setAttribute(colAddedInRun, iteration);
            max.setAttribute(colLastPrediction, algorithm);
            max.setAttribute(colLastCalculatedValue, highestPrediction.getPredictionValue());
            consoleLogger.log(Level.FINE, () -> "Add highest predicted edge: " + max);

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
        return queue.isEmpty() && lastPrediction == null;
    }

    /**
     * Retrieve neighbours for node a from graph
     *
     * @param n Node for which neighbours will be searched
     * @return Neighbours, that were added by algorithm or already have been there initially
     */
    protected ArrayList<Node> getNeighbours(Node n) {
        consoleLogger.log(Level.FINE,"Get relevant neighbours");

        // Get all neighbours
        ArrayList<Node> neighbours = Arrays.stream(graph.getNeighbors(n).toArray())
            .distinct().collect(Collectors.toCollection(ArrayList::new));

        // Filter neighbours with edges from
        // same algorithm or that initially existed
        return neighbours.stream()
            .filter(r -> GraphUtils.getEdges(graph, n, r).stream()
                .anyMatch(e -> e.getAttribute(colLastPrediction).equals(getAlgorithmName()) || e
                    .getAttribute(colLastPrediction).equals("")))
            .distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Statistic class used to store link prediction information for handling priority queue.
     */
    public static class LinkPredictionProbability implements Comparable<LinkPredictionProbability> {

        private final Node nodeSource;
        private final Node nodeTarget;
        private Integer predictionValue;

        public LinkPredictionProbability(Node nodeSource, Node nodeTarget, int predictionValue) {
            this.nodeSource = nodeSource;
            this.nodeTarget = nodeTarget;
            this.predictionValue = predictionValue;
        }

        /**
         * Compares two probability objects.
         *
         * @param o Compared instances
         * @return Comparison value based on prediction value
         */
        @Override public int compareTo(LinkPredictionProbability o) {
            return this.getPredictionValue().compareTo(o.getPredictionValue());
        }

        /**
         * Verifies if two prediction probabilities are equal.
         *
         * @param o Other statistic
         * @return Evaluation result
         */
        @Override public boolean equals(Object o) {
            if (o instanceof LinkPredictionProbability) {
                // Object is from same class
                LinkPredictionProbability probability = (LinkPredictionProbability) o;
                // Object has same source node and target node
                return (this.getNodeSource().equals(probability.getNodeSource()) && this.getNodeTarget()
                        .equals(probability.getNodeTarget())) || (
                        this.getNodeTarget().equals(probability.getNodeSource()) && this.getNodeSource()
                                .equals(probability.getNodeTarget()));
            } else {
                return false;
            }
        }

        /**
         * Generates hash code out of prediction value.
         *
         * @return Hash code
         */
        @Override public int hashCode() {
            return this.getPredictionValue().hashCode();
        }

        /**
         * Gets predicted probability.
         *
         * @return Link prediction value
         */
        public Integer getPredictionValue() {
            return predictionValue;
        }

        /**
         * Sets predicted probability.
         *
         * @param predictionValue Link prediction value
         */
        public void setPredictionValue(int predictionValue) {
            this.predictionValue = predictionValue;
        }

        /**
         * Gets source node.
         *
         * @return Source node
         */
        public Node getNodeSource() {
            return nodeSource;
        }

        /**
         * Gets target node.
         *
         * @return target node
         */
        public Node getNodeTarget() {
            return nodeTarget;
        }
    }
}
