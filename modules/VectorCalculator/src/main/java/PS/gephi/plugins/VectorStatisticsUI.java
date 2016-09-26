/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PS.gephi.plugins;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jonas
 */

@ServiceProvider(service = StatisticsUI.class)
public class VectorStatisticsUI implements StatisticsUI{

    private VectorStatisticsPanel _panel;
    private VectorStatistics _metric;
    
    public JPanel getSettingsPanel() {
        _panel = new VectorStatisticsPanel();
        return _panel;
    }

    public void setup(Statistics ststcs) {
        this._metric = (VectorStatistics) ststcs;
        this._metric.lat = this._panel.getLatitudeColumnName();
        this._metric.lon = this._panel.getLongitudeColumnName();
    }

    public void unsetup() {
        //_panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return VectorStatistics.class;
    }

    public String getValue() {
        return null;
    }

    public String getDisplayName() {
        return "Avg. edge length (km)";
    }

    public String getShortDescription() {
        return "Measures the distance and direction of the edges by label type.";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_EDGE_OVERVIEW;
    }

    public int getPosition() {
        return 800;
    }
    
    
}
