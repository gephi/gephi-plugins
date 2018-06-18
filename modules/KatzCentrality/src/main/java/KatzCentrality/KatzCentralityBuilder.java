package KatzCentrality;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class KatzCentralityBuilder implements StatisticsBuilder{
    @Override
    public String getName() {
        return "Katz centrality";
    }

    @Override
    public Statistics getStatistics() {
        return new KatzCentrality();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return KatzCentrality.class;
    }
}
