package nl.liacs.boundingdiameters;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class DiameterMetricBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "BoundingDiameter";
    }

    @Override
    public Statistics getStatistics() {
        return new DiameterMetric();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return DiameterMetric.class;
    }
    
}
