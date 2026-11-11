package org.robertpalermo.distancebackbonetoolbox;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class MetricBackboneBuilder implements StatisticsBuilder {

    public MetricBackboneBuilder() {
        System.out.println(">>> MetricBackboneBuilder loaded");
    }
    
    @Override
    public Statistics getStatistics() {
        return new MetricBackbone();
    }

    @Override
    public String getName() {
        return "Metric Backbone";
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return MetricBackbone.class;
    }
}
