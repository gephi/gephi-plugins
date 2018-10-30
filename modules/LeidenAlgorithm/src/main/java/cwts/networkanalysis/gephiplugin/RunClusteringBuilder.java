package cwts.networkanalysis.gephiplugin;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Builder for {@link RunClustering}.
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class RunClusteringBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Leiden algorithm";
    }

    @Override
    public Statistics getStatistics() {
        return new RunClustering();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return RunClustering.class;
    }
}
