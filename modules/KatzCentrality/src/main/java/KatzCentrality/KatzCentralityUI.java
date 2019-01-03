package KatzCentrality;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;
import javax.swing.*;

@ServiceProvider(service = StatisticsUI.class)
public class KatzCentralityUI implements StatisticsUI{

    private final StatSettings settings = new StatSettings();
    private KatzCentralityPanel panel;
    private KatzCentrality katz;


    @Override
    public JPanel getSettingsPanel() {
        panel = new KatzCentralityPanel();
        return panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.katz = (KatzCentrality) statistics;
        if (panel != null) {
            settings.load(katz);
            panel.setNumRuns(katz.getNumRuns());
            panel.setDirected(katz.isDirected());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            katz.setNumRuns(panel.getNumRuns());
            katz.setDirected(panel.isDirected());
            settings.save(katz);
        }
        panel = null;
        katz = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return KatzCentrality.class;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Katz centrality";
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 1;
    }


    private static class StatSettings {

        private int mNumRuns = 100;

        private void save(KatzCentrality stat) {
            this.mNumRuns = stat.getNumRuns();
        }

        private void load(KatzCentrality stat) {
            stat.setNumRuns(mNumRuns);
        }
    }
}
