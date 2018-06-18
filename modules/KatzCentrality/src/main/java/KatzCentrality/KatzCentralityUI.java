package KatzCentrality;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;
import javax.swing.*;

@ServiceProvider(service = StatisticsUI.class)
public class KatzCentralityUI implements StatisticsUI{

    private KatzCentralityPanel panel;
    private KatzCentrality statistic;

    @Override
    public JPanel getSettingsPanel() {
        panel = new KatzCentralityPanel();
        return panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.statistic = (KatzCentrality) statistics;
//        if (panel != null) {
//            settings.load(statistic);
//            panel.setNumRuns(statistic.getNumRuns());
//            panel.setDirected(statistic.isDirected());
//        }
    }

    @Override
    public void unsetup() {
        this.statistic = null;
//        if (panel != null) {
//            statistic.setNumRuns(panel.getNumRuns());
//            statistic.setDirected(panel.isDirected());
//            settings.save(statistic);
//        }
//        panel = null;
//        statistic = null;
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
}
