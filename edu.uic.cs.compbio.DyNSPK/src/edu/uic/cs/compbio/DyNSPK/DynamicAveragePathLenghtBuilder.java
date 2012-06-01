/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.compbio.DyNSPK;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author zitterbewegung
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class DynamicAveragePathLenghtBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Dynamic Average Path Length";
    }

    @Override
    public Statistics getStatistics() {
        return new DynamicAveragePathLength();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return DynamicAveragePathLength.class;
    }
    
}
