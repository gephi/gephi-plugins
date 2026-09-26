package org.gephi.plugins.linkprediction.evaluation;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

/**
 * UI used for {@link LinkPredictionEvaluation} evaluation.
 * <p>
 *
 * @author Marco Romanutti
 * @see LinkPredictionEvaluation
 */
@ServiceProvider(service = StatisticsUI.class) public class LinkPredictionEvaluationUI implements StatisticsUI {
    // Metrics to calculate
    private LinkPredictionEvaluation evaluation;

    // Panel to configure evaluation
    private LinkPredictionEvaluationPanel panel;

    @Override public JPanel getSettingsPanel() {
        panel = new LinkPredictionEvaluationPanel();
        return panel;
    }

    @Override public void setup(Statistics evaluation) {
        this.evaluation = (LinkPredictionEvaluation) evaluation;
        if (panel == null) {
            panel = new LinkPredictionEvaluationPanel();
        }
        panel.setEvaluation((LinkPredictionEvaluation) evaluation);
    }

    @Override public void unsetup() {
        this.panel = null;
        this.evaluation = null;
    }

    @Override public Class<? extends Statistics> getStatisticsClass() {
        return LinkPredictionEvaluation.class;
    }

    @Override public String getValue() {
        return "";
    }

    @Override public String getDisplayName() {
        return "Eval. Link Prediction Algorithms";
    }

    @Override public String getCategory() {
        return StatisticsUI.CATEGORY_EDGE_OVERVIEW;
    }

    @Override public int getPosition() {
        return 11000;
    }

    @Override public String getShortDescription() {
        return null;
    }

}

