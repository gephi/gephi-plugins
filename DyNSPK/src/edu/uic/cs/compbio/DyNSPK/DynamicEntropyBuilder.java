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
public class DynamicEntropyBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Graph Distance";
    }

    @Override
    public Statistics getStatistics() {
        return new DynamicEntropy();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return DynamicEntropy.class;
    }
    
}
