
package org.fernunihagen.fapra.girvannewman;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Andrej Sibirski
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class GirvanNewmanBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Girvan-Newman"; 
    }

    @Override
    public Statistics getStatistics() {
        return new GirvanNewman();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return GirvanNewman.class;
    }
    
}
