package org.openuniversityofisrael.katzcentrality;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class KatzCentralityBuilder implements StatisticsBuilder {
    public String getName() {
        return "Calculate KatzCentrality";
    }

    public Statistics getStatistics() {
        return new KatzCentrality();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return KatzCentrality.class;
    }
}
