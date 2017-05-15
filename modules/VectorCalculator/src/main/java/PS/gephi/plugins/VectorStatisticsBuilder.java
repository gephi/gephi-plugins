package PS.gephi.plugins;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jonas Persson
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class VectorStatisticsBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Vector calculations";
    }

    @Override
    public Statistics getStatistics() {
        return new VectorStatistics();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return VectorStatistics.class;
    }
}

  
