package Metric;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = StatisticsUI.class)
public class ClusteringCoefficientUI implements StatisticsUI {

    public JPanel getSettingsPanel() {
        return new JPanel();
    }

    public void setup(Statistics statistics) {

    }

    public void unsetup() {

    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ClusteringCoefficient.class;
    }

    public String getValue() {
        return null;
    }

    public String getDisplayName() {
        return "Clustering Coefficient";
    }

    public String getShortDescription() {
        return "Clustering Coefficient";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 800;
    }
}
