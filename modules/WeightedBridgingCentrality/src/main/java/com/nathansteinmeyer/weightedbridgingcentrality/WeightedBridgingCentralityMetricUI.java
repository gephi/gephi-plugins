package com.nathansteinmeyer.weightedbridgingcentrality;

import javax.swing.JPanel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nathan Steinmeyer
 */
@ServiceProvider(service = StatisticsUI.class)
public class WeightedBridgingCentralityMetricUI implements StatisticsUI {

    private final StatSettings settings = new StatSettings();
    
    private WeightedBridgingCentralityMetricPanel panel;
    private WeightedBridgingCentralityMetric metric;
    
    private boolean isDirected;
    private boolean isNormalized;

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }
    
    public void setNormalized(boolean isNormalized) {
        this.isNormalized = isNormalized;
    }

    public boolean isNormalized() {
        return isNormalized;
    }

    public JPanel getSettingsPanel() {
        panel = new WeightedBridgingCentralityMetricPanel();
        return panel;
    }

    public void setup(Statistics ststcs) {
        
        metric = (WeightedBridgingCentralityMetric) ststcs;
        
        if ( panel != null ) {
            
            metric.setNormalized( true ); // default
            
            panel.setDirected(metric.isDirected());
            panel.setNormalized(metric.isNormalized());
            
            GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            Graph graph = graphController.getGraphModel().getGraphVisible();
            panel.directedCheckBox( graph.isDirected() );
        }
    }

    public void unsetup() {
        if (panel != null) {
            metric.setDirected(panel.isDirected());
            metric.setNormalized(panel.isNormalized());
            settings.save(metric);
        }
        panel = null;
        metric = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return WeightedBridgingCentralityMetric.class;
    }

    /**
     * If your metric doesn't have a single result value, return null.
     */
    public String getValue() {
        return null; 
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WeightedBridgingCentralityMetricUI.class, "WeightedBridgingCentralityMetricUI.displayName");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(WeightedBridgingCentralityMetricUI.class, "WeightedBridgingCentralityMetricUI.shortDescription");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    /** 
     * The position control the order the metric front-end are displayed. 
     * Returns a value between 1 and 1000, that indicates the position. 
     * Less means upper.
     */
    public int getPosition() {
        return 200;
    }
    
    
    private static class StatSettings {

        private void save(WeightedBridgingCentralityMetric stat) {
            stat.isDirected();
            stat.isNormalized();
        }
    }

}
