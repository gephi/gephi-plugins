package pl.edu.wat.wcy.gephi.plugin.clusteringcoefficient;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class ClusteringCoefficientBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Clustering Coefficient";
    }

    @Override
    public Statistics getStatistics() {
        return new ClusteringCoefficientStatistic();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return ClusteringCoefficientStatistic.class;
    }
}
