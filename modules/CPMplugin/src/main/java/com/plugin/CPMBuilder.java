package com.plugin;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mir Saman Tajbakhsh
 */
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
