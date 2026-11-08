/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author edemairy
 */

@ServiceProvider(service=StatisticsBuilder.class)
public class SemanticWebStatisticsBuilder implements StatisticsBuilder {

    private SemanticWebStatistics statistics;

    @Override
    public String getName() {
        return SemanticWebStatisticsBuilder.class.getSimpleName();
    }

    @Override
    public Statistics getStatistics() {
        this.statistics = new SemanticWebStatistics();
        return statistics;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return SemanticWebStatistics.class;
    }

}
