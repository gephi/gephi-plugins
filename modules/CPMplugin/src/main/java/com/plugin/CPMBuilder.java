package com.plugin;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider (service = StatisticsBuilder.class)
public class CPMBuilder implements org.gephi.statistics.spi.StatisticsBuilder {

    @Override
    public String getName() {
        return "Clique Percolation Method";
    }
    
    @Override
    public Statistics getStatistics() {
        return new CPM();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return CPM.class;
    }
    
}
