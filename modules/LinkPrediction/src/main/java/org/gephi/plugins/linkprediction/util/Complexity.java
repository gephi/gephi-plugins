package org.gephi.plugins.linkprediction.util;

/**
 * Big o complexitiy used to calculate runtime.
 *
 * @author Marco Romanutti
 * @see ..\base\LinkPredictionStatistics
 */
public enum Complexity {

    EXPONENTIAL("2^n"), QUADRATIC("n^2"), LINEAR("n"), LOGARITHMIC("log n"), CONSTANT("1");

    // Landau symbol
    private String order;

    private Complexity(String order) {
        this.order = "O( " + order + ")";
    }

    public String getOrder() {
        return order;
    }

}
