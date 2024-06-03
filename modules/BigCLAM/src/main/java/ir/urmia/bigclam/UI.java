
package ir.urmia.bigclam;

import javax.swing.JPanel;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class UI implements StatisticsUI {
    private Panel panel;
    private BigCLAM myMetric;

    @Override
    public JPanel getSettingsPanel() {
        panel = new Panel();
        return panel;
    }

    @Override
    public void setup(Statistics ststcs) {
        this.myMetric = (BigCLAM) ststcs;
        if (panel != null) {
            panel.setK(myMetric.getK());
            panel.setIterations(myMetric.getIterations());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            myMetric.setK(panel.getK());
            myMetric.setIterations(panel.getIterations());
        }
        panel = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return BigCLAM.class;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "BigCLAM";
    }

    @Override
    public String getShortDescription() {
        return "BigCLAM Algorithm";
    }

    @Override
    public String getCategory() {
        return CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 800;
    }

}
