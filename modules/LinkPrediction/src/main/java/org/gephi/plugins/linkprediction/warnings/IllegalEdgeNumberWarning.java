package org.gephi.plugins.linkprediction.warnings;

/**
 * Warning class, used to expose wrong number of edge to the user.
 */
public class IllegalEdgeNumberWarning extends LinkPredictionWarning {
    /** Illegal iteration limit exception message */
    public static final String EXCEPTION_MESSAGE = "No edges left! No edges will be removed from graph.";

    public IllegalEdgeNumberWarning() {
        super(EXCEPTION_MESSAGE);
    }
}


