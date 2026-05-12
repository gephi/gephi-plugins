package ia.ppgco.facom.ufu.br.gephi.plugin.bridgingcoefficient;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Getúlio de Morais Pereira
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class BridgingCentralityMetricBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(BridgingCentralityMetricBuilder.class, "BridgingCentralityMetricBuilder.name");
    }

    public Statistics getStatistics() {
        return new BridgingCentralityMetric();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return BridgingCentralityMetric.class;
    }
}
