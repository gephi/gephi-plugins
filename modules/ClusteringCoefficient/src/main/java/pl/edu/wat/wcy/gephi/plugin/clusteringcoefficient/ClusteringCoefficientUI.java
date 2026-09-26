package pl.edu.wat.wcy.gephi.plugin.clusteringcoefficient;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = StatisticsUI.class)
public class ClusteringCoefficientUI implements StatisticsUI {

    public static boolean triangleMethod = false;

    private JRadioButton btn1,btn2;

    public JPanel getSettingsPanel() {
        JPanel jPanel = new JPanel();
        ButtonGroup group = new ButtonGroup();
        btn1 = new JRadioButton("Basic method ");btn1.setSelected(true);
        btn2 = new JRadioButton("Triangle Method ");
        group.add(btn1 );
        group.add(btn2 );

        jPanel.add(btn1);
        jPanel.add(btn2);

        return jPanel;
    }

    public void setup(Statistics statistics) {

        if(btn2.isSelected()){
            triangleMethod = true;
        }if(btn1.isSelected()){
            triangleMethod = false;
        }
    }

    public void unsetup() {

    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ClusteringCoefficientStatistic.class;
    }

    public String getValue() {
        return null;
    }

    public String getDisplayName() {
        return "Clustering Coefficient";
    }

    public String getShortDescription() {
        return "Clustering Coefficient";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 800;
    }
}
