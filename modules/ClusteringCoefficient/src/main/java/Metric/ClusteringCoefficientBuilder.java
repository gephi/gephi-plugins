package Metric;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class ClusteringCoefficientBuilder implements StatisticsBuilder {
    public String getName() {
        return "Clustering Coefficient";
    }

    public Statistics getStatistics() {
        return new ClusteringCoefficient();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ClusteringCoefficient.class;
    }
}
