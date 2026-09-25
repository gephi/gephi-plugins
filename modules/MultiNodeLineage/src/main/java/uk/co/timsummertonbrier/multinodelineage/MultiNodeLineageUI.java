
package uk.co.timsummertonbrier.multinodelineage;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class MultiNodeLineageUI implements StatisticsUI {
    
    private MultiNodeLineage statistics;
    private MultiNodeLineagePanel panel;

    @Override
    public JPanel getSettingsPanel() {
        panel = new MultiNodeLineagePanel();
        return panel;
    }

    @Override
    public void setup(Statistics ststcs) {
        statistics = (MultiNodeLineage) ststcs;
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            statistics.setOriginNodeIds(panel.getOriginNodeIds());
        }
        statistics = null;
        panel = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return MultiNodeLineage.class;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Multi Node Lineage";
    }

    @Override
    public String getShortDescription() {
        return "Identifies the ancestors and descendants of a set of nodes.";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 9999;
    }
    
}
