
package ir.urmia.bigclam;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class Builder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "BigCLAM";
    }

    @Override
    public Statistics getStatistics() {
        return new BigCLAM();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return BigCLAM.class;
    }

}
