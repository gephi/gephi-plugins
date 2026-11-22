
package uk.co.timsummertonbrier.multinodelineage;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class MultiNodeLineageBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Multi Node Lineage";
    }

    @Override
    public Statistics getStatistics() {
        return new MultiNodeLineage();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return MultiNodeLineage.class;
    }
    
   
    
}
