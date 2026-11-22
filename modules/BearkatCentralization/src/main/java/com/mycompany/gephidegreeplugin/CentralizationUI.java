package com.mycompany.gephidegreeplugin;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)

public class CentralizationUI implements StatisticsUI {

    private Centralization centralization;

    @Override
    public JPanel getSettingsPanel() {
        return null;
    }

    @Override
    public void setup(Statistics statistics) {
        this.centralization = (Centralization) statistics;
    }

    @Override
    public void unsetup() {
        this.centralization = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return Centralization.class;
    }

    @Override
    public String getValue() {
        return "Done";
    }

  @Override
public String getDisplayName() {
    return "Bearkat Centralization";
}

    @Override
    public String getShortDescription() {
        return "Measures weighted connections between nodes";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 999;
    }
}
