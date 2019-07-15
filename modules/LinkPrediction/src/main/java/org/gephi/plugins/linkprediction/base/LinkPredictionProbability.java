package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

public class LinkPredictionProbability implements Comparable<LinkPredictionProbability> {

    private Node nodeSource;
    private Node nodeTarget;
    private Integer predictionValue;

    public LinkPredictionProbability(Node nodeSource, Node nodeTarget, int predictionValue) {
        this.nodeSource = nodeSource;
        this.nodeTarget = nodeTarget;
        this.predictionValue = predictionValue;
    }

    @Override
    public int compareTo(LinkPredictionProbability o) {
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
