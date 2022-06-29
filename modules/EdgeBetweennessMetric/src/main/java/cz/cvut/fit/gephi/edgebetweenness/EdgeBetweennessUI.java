package cz.cvut.fit.gephi.edgebetweenness;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class EdgeBetweennessUI implements StatisticsUI {

  private EdgeBetweennessPanel panel;
  private EdgeBetweenness edgeBetweenness;

  @Override
  public JPanel getSettingsPanel() {
    panel = new EdgeBetweennessPanel();
    return panel;
  }

  @Override
  public void setup(Statistics ststcs) {
    this.edgeBetweenness = (EdgeBetweenness) ststcs;
    if (panel != null) {
      panel.setDirected(edgeBetweenness.getDirected());
      panel.doNormalize(edgeBetweenness.isNormalized());
    }
  }

  @Override
  public void unsetup() {
    if (panel != null) {
      edgeBetweenness.setDirected(panel.isDirected());
      edgeBetweenness.doNormalize(panel.isNormalized());
    }
    edgeBetweenness = null;
    panel = null;
  }

  @Override
  public Class<? extends Statistics> getStatisticsClass() {
    return EdgeBetweenness.class;
  }

  @Override
  public String getValue() {
    DecimalFormat df = new DecimalFormat("###.###");
    return "" + df.format(edgeBetweenness.getEdgeBetweenness());
  }

  @Override
  public String getDisplayName() {
    return org.openide.util.NbBundle.getMessage(getClass(), "EdgeBetweennessUI.name");
  }

  @Override
  public String getShortDescription() {
    return org.openide.util.NbBundle.getMessage(getClass(), "EdgeBetweennessUI.shortDescription");
  }

  @Override
  public String getCategory() {
    return StatisticsUI.CATEGORY_EDGE_OVERVIEW;
  }

  @Override
  public int getPosition() {
    return 200;
  }
}
