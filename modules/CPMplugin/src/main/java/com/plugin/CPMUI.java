package com.plugin;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

/**
 *
 * @author Mir Saman Tajbakhsh
 */
@ServiceProvider(service = StatisticsUI.class)
public class CPMUI implements StatisticsUI {

    private CPMPanel panel;
    private CPM myCliqueDetector;

    @Override
    public JPanel getSettingsPanel() {
        panel = new CPMPanel();
        return panel;
    }

    @Override
    public void setup(Statistics ststcs) {
        this.myCliqueDetector = (CPM) ststcs;
        if (panel != null) {
            panel.setK(myCliqueDetector.getK());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            myCliqueDetector.setK(panel.getK());
        }
        panel = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return CPM.class;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Clique Percolation Method";
    }

    @Override
    public String getShortDescription() {
        return "Clique Percolation Method implementaion in gephi";
    }

    @Override
    public String getCategory() {
        return CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 800;
    }

}
