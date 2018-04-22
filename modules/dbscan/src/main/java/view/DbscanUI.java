package view;

import core.Dbscan;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            Logger.getGlobal().log(Level.WARNING, "Statistics is not an instance of DbscanUI.");
            return;
        }

        if(settingsPanel != null){

        }
    }

    @Override
    public void unsetup() {
        if(settingsPanel != null && dbscan != null){

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
        return "DBSCAN";
    }

    @Override
    public String getShortDescription() {
        return "Short desc";
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
