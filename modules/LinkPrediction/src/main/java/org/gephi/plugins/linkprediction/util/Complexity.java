package org.gephi.plugins.linkprediction.util;

import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;

/**
 * Big o complexitiy used to calculate runtime.
 *
 * @author Marco Romanutti
 */
public enum Complexity {

    EXPONENTIAL("2^n"), QUADRATIC("n^2"), LINEAR("n"), LOGARITHMIC("log n"), CONSTANT("1");

    // Landau symbol
    private String order;

    Complexity(String order) {
        this.order = "O( " + order + ")";
    }

    /**
     * Gives an estimate of the assumed duration.
     *
     * @param iterationLimit Number of iterations
     * @param nodeCount      Number of nodes
     * @return If the calculation will takes a long time
     */
    public boolean longRuntimeExpected(long iterationLimit, long nodeCount) {
        switch (this) {
        case QUADRATIC:
            return (iterationLimit * nodeCount * nodeCount) > LinkPredictionStatistics.RUNTIME_THRESHOLD;
        case EXPONENTIAL:
            return (iterationLimit * Math.pow(2, nodeCount)) > LinkPredictionStatistics.RUNTIME_THRESHOLD;
        case LINEAR:
            return (iterationLimit * nodeCount) > LinkPredictionStatistics.RUNTIME_THRESHOLD;
        default:
            return false;
        }
    }

    /**
     * Gets the order in landau notation.
     *
     * @return Order in landau notation
     */
    public String getOrder() {
        return order;
    }

}
