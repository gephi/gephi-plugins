package cwts.networkanalysis;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * User interface for the {@link RunClustering}.
 */
@ServiceProvider(service = StatisticsUI.class)
public class RunClusteringUI implements StatisticsUI {

    private RunClustering statistic;

    @Override
    public JPanel getSettingsPanel() {
        return null;
    }

    @Override
    public void setup(Statistics ststcs) {
        this.statistic = (RunClustering) ststcs;
    }

    @Override
    public void unsetup() {
        this.statistic = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return RunClustering.class;
    }

    @Override
    public String getValue() {
        if (statistic != null) {
            return "" + statistic.getQuality();
        }
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Leiden algorithm";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 10000;
    }

    @Override
    public String getShortDescription() {
        return "Leiden algorithm";
    }
}
