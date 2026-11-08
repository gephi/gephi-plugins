package PS.gephi.plugins;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jonas Persson
 */
@ServiceProvider(service = StatisticsUI.class)
public class VectorStatisticsUI implements StatisticsUI{

    private VectorStatisticsPanel panel;
    
    @Override
    public JPanel getSettingsPanel() {
        return panel = new VectorStatisticsPanel();
    }

    @Override
    public void setup(Statistics ststcs) {
        VectorStatistics metric = (VectorStatistics) ststcs;
        metric.setLatAttribute(panel.getLatitudeColumnName());
        metric.setLonAttribute(panel.getLongitudeColumnName());
    }

    @Override
    public void unsetup() {
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return VectorStatistics.class;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Avg. edge length (km)";
    }

    @Override
    public String getShortDescription() {
        return "Measures the distance and direction of the edges by label type.";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_EDGE_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 800;
    }
}
