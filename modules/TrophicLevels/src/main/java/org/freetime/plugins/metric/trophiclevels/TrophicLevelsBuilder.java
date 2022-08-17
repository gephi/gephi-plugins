/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freetime.plugins.metric.trophiclevels;

/**
 *
 * @author wouter
 */

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class TrophicLevelsBuilder implements StatisticsBuilder {
    
    @Override
    public String getName() {
        return "Trophic Levels";
    }
    
    @Override
    public Statistics getStatistics() {
        return new TrophicLevels();
    }
    
    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return TrophicLevels.class;
    }
}
