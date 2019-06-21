package org.gephi.plugins.linkprediction.workspacetest;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = StatisticsUI.class)
public class WorkspaceUI implements StatisticsUI {

    private WorkspaceStatistics statistic;
    private WorkspacePanel panel;

    @Override
    public JPanel getSettingsPanel() {
        panel = new WorkspacePanel();
        return panel;
    }

    @Override
    public void setup(Statistics ststcs) {
        this.statistic = (WorkspaceStatistics) ststcs;
    }

    @Override
    public void unsetup() {
        this.panel = null;
        this.statistic = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return WorkspaceStatistics.class;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Workspace Overview";
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
        return null;
    }

}