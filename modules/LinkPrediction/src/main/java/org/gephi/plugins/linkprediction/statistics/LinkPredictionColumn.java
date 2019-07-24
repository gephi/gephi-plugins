package org.gephi.plugins.linkprediction.statistics;

public enum LinkPredictionColumn {

    ADDED_IN_RUN("added_in_run"), LAST_VALUE("last_link_prediction_value"), LP_ALGORITHM("link_prediction_algorithm");

    private String value;

    public String getValue() {
        return value;
    }

    LinkPredictionColumn(String value) {
        this.value = value;
    }
}
