package org.gephi.plugins.linkprediction.exceptions;

public class IllegalIterationLimitException extends LinkPredictionException {
    /** Illegal iteration limit exception message **/
    public static final String EXCEPTION_MESSAGE = "No prediction found! Calculate prediction first using respective statistics.";

    public IllegalIterationLimitException() {
        super(EXCEPTION_MESSAGE);
    }
}


