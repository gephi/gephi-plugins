package org.gephi.plugins.linkprediction.exceptions;

public class IllegalEdgeNumberException extends LinkPredictionException {
    /** Illegal iteration limit exception message **/
    public static final String EXCEPTION_MESSAGE = "No edges left! No edges will be removed from graph.";

    public IllegalEdgeNumberException() {
        super(EXCEPTION_MESSAGE);
    }
}


