package AverageGlobalConnectionEfficiency;

import GenericParamForm.GenericUI;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import java.text.DecimalFormat;

@ServiceProvider(service = StatisticsUI.class)
public class AverageGlobalConnectionEfficiencyUI extends GenericUI<AverageGlobalConnectionEfficiencyParam, AverageGlobalConnectionEfficiency> {
    @Override
    protected void CreatePanel() {

    }


    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return AverageGlobalConnectionEfficiency.class;
    }

    @Override
    public String getValue() {
        var f = new DecimalFormat("#0.0000");
        return "" + f.format(statistics.getAvgGCE());
    }

    @Override
    public String getDisplayName() {
        return "Average Global Connection Efficiency";
    }

    @Override
    public String getShortDescription() {
        return "Average Global Connection Efficiency";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 3;
    }
}
