package com.mycompany.gephidegreeplugin;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class CentralizationBuilder implements StatisticsBuilder {

   @Override
public String getName() {
    return "Bearkat Centralization";
}

    @Override
    public Statistics getStatistics() {
        return new Centralization();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return Centralization.class;
    }
}