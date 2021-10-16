package org.openuniversityofisrael.katzcentrality;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class KatzCentralityUI implements StatisticsUI {

    private final StatSettings settings = new StatSettings();
    private KatzCentrality statistic;
    private KatzCentralityPanel panel;

    @Override
    public JPanel getSettingsPanel() {
        this.panel = new KatzCentralityPanel();
        return this.panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.statistic = (KatzCentrality) statistics;
        if (this.panel != null) {
            this.settings.load(this.statistic);
            this.panel.setAlpha(this.statistic.getAlpha());
        }
    }

    @Override
    public void unsetup() {
        if (this.panel != null) {
            this.statistic.setAlpha(this.panel.getAlpha());
            this.settings.save(this.statistic);
        }
        this.panel = null;
        this.statistic = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return KatzCentrality.class;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Katz Centrality";
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

    private static class StatSettings {

        private double alpha = KatzCentrality.DEFAULT_ALPHA;

        private void save(KatzCentrality stat) {
            this.alpha = stat.getAlpha();
        }

        private void load(KatzCentrality stat) {
            stat.setAlpha(this.alpha);
        }
    }
}
