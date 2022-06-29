package cz.cvut.fit.gephi.edgebetweenness;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class EdgeBetweennessBuilder implements StatisticsBuilder {

  @Override
  public String getName() {
    return NbBundle.getMessage(EdgeBetweennessBuilder.class, "EdgeBetweennessBuilder.name");
  }

  @Override
  public Statistics getStatistics() {
    return new EdgeBetweenness();
  }

  @Override
  public Class<? extends Statistics> getStatisticsClass() {
    return EdgeBetweenness.class;
  }
}
