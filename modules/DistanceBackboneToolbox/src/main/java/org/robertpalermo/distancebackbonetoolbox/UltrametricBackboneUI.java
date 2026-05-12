package org.robertpalermo.distancebackbonetoolbox;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class UltrametricBackboneUI implements StatisticsUI {

    private UltrametricBackbone ultrametricBackbone;

    @Override
    public JPanel getSettingsPanel() {
        return null; 
    }

    @Override
    public void setup(Statistics statistics) {
        this.ultrametricBackbone = (UltrametricBackbone) statistics;
    }

    @Override
    public void unsetup() {
        // Nothing to do
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return UltrametricBackbone.class;
    }

    @Override
    public String getDisplayName() {
        return "Ultrametric Backbone";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW; 
    }

    @Override
    public int getPosition() {
        return 1000; 
    }

    @Override
    public String getShortDescription() {
        return "Computes and filters the network using the ultrametric backbone method.";
    }
    
    @Override
    public String getValue() {
        return null; 
    }
}