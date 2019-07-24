package org.gephi.plugins.linkprediction.warnings;

/**
 * Warning class, used to expose illegal iteration limit to the user.
 */
public class IllegalIterationLimitWarning extends LinkPredictionWarning {
    /** Illegal iteration limit exception message */
    public static final String EXCEPTION_MESSAGE = "No prediction found! Calculate prediction first using respective statistics.";

    public IllegalIterationLimitWarning() {
        super(EXCEPTION_MESSAGE);
    }
}


