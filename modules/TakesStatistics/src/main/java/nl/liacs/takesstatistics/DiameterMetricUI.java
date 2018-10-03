package nl.liacs.takesstatistics;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class DiameterMetricUI implements StatisticsUI {
    private DiameterMetricPanel panel;
    private DiameterMetric metric;

    @Override
    public JPanel getSettingsPanel() {
        panel = new DiameterMetricPanel();
        return (JPanel) panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.metric = (DiameterMetric) statistics;
        if (panel != null) {
            panel.setDirected(metric.isDirected());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            metric.setDirected(panel.isDirected());
        }
        panel = null;
        metric = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return DiameterMetric.class;
    }

    @Override
    public String getValue() {
        return (metric != null) ? (metric.getDiameter() + " / " + metric.getRadius()) : "";
    }

    @Override
    public String getDisplayName() {
        return "Diameter & Radius";
    }

    @Override
    public String getShortDescription() {
        return "Compute diameter and radius, using Frank Takes` algorithm";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 900;
    }
    
}
