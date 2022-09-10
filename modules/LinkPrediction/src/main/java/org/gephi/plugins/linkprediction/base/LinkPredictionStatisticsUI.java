package org.gephi.plugins.linkprediction.base;

import org.gephi.plugins.linkprediction.statistics.LinkPredictionMacro;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

/**
 * UI used for {@link LinkPredictionStatistics} statistics.
 * <p>
 *
 * @author Marco Romanutti
 * @see LinkPredictionStatistics
 */
@ServiceProvider(service = StatisticsUI.class)
public class LinkPredictionStatisticsUI implements StatisticsUI {
    // Algorithm used
    protected LinkPredictionStatistics statistic;
    // UI Panel
    protected LinkPredictionStatisticsPanel panel;


    @Override
    public JPanel getSettingsPanel() {
        if (panel == null) {
            panel = new LinkPredictionStatisticsPanel();
        }
        return panel;
    }

    @Override
    public void setup(Statistics statistic) {
        this.statistic = (LinkPredictionStatistics) statistic;
        if (panel == null) {
            panel = new LinkPredictionStatisticsPanel();
        }
        panel.setStatistic((LinkPredictionMacro) statistic);
    }

    @Override
    public void unsetup() {
        this.panel = null;
        this.statistic = null;
    }

    @Override public Class<? extends Statistics> getStatisticsClass() {
        return LinkPredictionMacro.class;
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
        return "Link Prediction Algorithm Selection";
    }


}
