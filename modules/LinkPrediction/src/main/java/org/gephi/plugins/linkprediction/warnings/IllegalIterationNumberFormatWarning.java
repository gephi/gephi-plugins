package org.gephi.plugins.linkprediction.warnings;

public class IllegalIterationNumberFormatWarning extends LinkPredictionWarning {
    /**
     * Illegal iteration limit exception message
     **/
    public static final String EXCEPTION_MESSAGE = "Wrong number format entered. Please use numerical values only";

    public IllegalIterationNumberFormatWarning() {
        super(EXCEPTION_MESSAGE);
    }
}


