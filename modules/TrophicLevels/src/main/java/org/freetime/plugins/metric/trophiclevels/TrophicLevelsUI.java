/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freetime.plugins.metric.trophiclevels;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wouter
 */
@ServiceProvider(service = StatisticsUI.class)
public class TrophicLevelsUI implements StatisticsUI {
    private TrophicLevels statistic;
    private TrophicLevelsPanel panel;
        
    @Override    
    public JPanel getSettingsPanel() {
        panel = new TrophicLevelsPanel();
        return panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.statistic = (TrophicLevels) statistics;
    }

    @Override
    public void unsetup() {
        panel = null;
        statistic = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return TrophicLevels.class;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "TrophicLevels";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
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
