package org.robertpalermo.distancebackbonetoolbox;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class UltrametricBackboneBuilder implements StatisticsBuilder {

    public UltrametricBackboneBuilder() {
        System.out.println(">>> UltrametricBackboneBuilder loaded");
    }
    
    @Override
    public Statistics getStatistics() {
        return new UltrametricBackbone();
    }

    @Override
    public String getName() {
        return "Ultrametric Backbone";
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return UltrametricBackbone.class;
    }
}