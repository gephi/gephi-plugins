package org.gephi.plugins.linkprediction.base;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = StatisticsUI.class)
public class LinkPredictionStatisticsUI implements StatisticsUI {

    private LinkPredictionStatistics statistic;
    private LinkPredictionStatisticsPanel panel;

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
    public Class<? extends Statistics> getStatisticsClass() {
        return LinkPredictionStatistics.class;
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
