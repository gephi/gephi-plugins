/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PS.gephi.plugins;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jonas Persson
 */

@ServiceProvider(service = StatisticsBuilder.class)
public class VectorStatisticsBuilder implements StatisticsBuilder {

    public String getName() {
        return "Vector calculations";
    }

    public Statistics getStatistics() {
        return new VectorStatistics();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return VectorStatistics.class;
    }
    
}

  
