package GenericParamForm;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;

import javax.swing.*;

public abstract class GenericUI<TParam extends Params<TStatistics>, TStatistics extends Statistics> implements StatisticsUI
{
    protected GenericPanel<TParam> panel;
    protected TStatistics statistics;

    protected abstract void CreatePanel();

    @Override
    public JPanel getSettingsPanel() {
        return panel;
    }

    @Override
    public void setup(Statistics statistics) {
        CreatePanel();
        this.statistics = (TStatistics) statistics;
    }

    @Override
    public void unsetup() {
        panel.getTParams().SetGeneratorParams(statistics);
        panel = null;
    }
}