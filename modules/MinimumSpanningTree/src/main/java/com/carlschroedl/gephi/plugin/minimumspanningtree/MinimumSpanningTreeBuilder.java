package com.carlschroedl.gephi.plugin.minimumspanningtree;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * See http://wiki.gephi.org/index.php/HowTo_write_a_metric#Create_StatisticsBuilder
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class MinimumSpanningTreeBuilder implements StatisticsBuilder {
    
    @Override
    public String getName() {
        return "Minimum Spanning Tree";
    }

    @Override
    public Statistics getStatistics() {
        return new MinimumSpanningTree();
    }
    
    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return MinimumSpanningTree.class;
    }

}
