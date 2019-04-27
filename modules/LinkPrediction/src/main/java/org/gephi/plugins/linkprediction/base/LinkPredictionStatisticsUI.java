package org.gephi.plugins.linkprediction.base;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;

import javax.swing.*;

public abstract class LinkPredictionStatisticsUI implements StatisticsUI {

    protected LinkPredictionStatistics statistic;
    protected LinkPredictionStatisticsPanel panel;

    @Override
    public JPanel getSettingsPanel() {
        panel = new LinkPredictionStatisticsPanel();
        return panel;
    }

    @Override
    public void setup(Statistics statistic) {
        this.statistic = (LinkPredictionStatistics) statistic;
    }

    @Override
    public void unsetup() {
        this.panel = null;
        this.statistic = null;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Link Prediction";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_EDGE_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 11000;
    }

    @Override
    public String getShortDescription() {
        return "Predicts next links in network";
    }


}
