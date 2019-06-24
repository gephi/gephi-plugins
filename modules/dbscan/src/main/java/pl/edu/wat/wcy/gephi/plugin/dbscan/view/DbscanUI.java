package pl.edu.wat.wcy.gephi.plugin.dbscan.view;

import pl.edu.wat.wcy.gephi.plugin.dbscan.core.Dbscan;
import pl.edu.wat.wcy.gephi.plugin.dbscan.core.Labels;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = StatisticsUI.class)
public class DbscanUI implements StatisticsUI {

    private SettingsPanel settingsPanel;
    private Dbscan dbscan;

    @Override
    public JPanel getSettingsPanel() {
        settingsPanel = new SettingsPanel();
        return settingsPanel;
    }

    @Override
    public void setup(Statistics statistics) {
        if (statistics instanceof Dbscan){
            dbscan = (Dbscan) statistics;
        } else {
            throw new RuntimeException("Statistics is not an instance of DbscanUI.");
        }

        if(settingsPanel != null){
            settingsPanel.setNeighbors(dbscan.getNumberOfNeighbours());
            settingsPanel.setRadius(dbscan.getRadius());
        }
    }

    @Override
    public void unsetup() {
        if(settingsPanel != null && dbscan != null){
            dbscan.setNumberOfNeighbours(settingsPanel.getNeighbors());
            dbscan.setRadius(settingsPanel.getRadius());
        }
        settingsPanel = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return Dbscan.class;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return Labels.NAME;
    }

    @Override
    public String getShortDescription() {
        return Labels.SHORT_DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 1000;
    }
}
