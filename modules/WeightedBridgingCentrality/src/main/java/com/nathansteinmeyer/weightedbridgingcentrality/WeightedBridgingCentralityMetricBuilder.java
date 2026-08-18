package com.nathansteinmeyer.weightedbridgingcentrality;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nathan Steinmeyer
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class WeightedBridgingCentralityMetricBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(WeightedBridgingCentralityMetricBuilder.class, "WeightedBridgingCentralityMetricBuilder.name");
    }

    public Statistics getStatistics() {
        return new WeightedBridgingCentralityMetric();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return WeightedBridgingCentralityMetric.class;
    }
}
