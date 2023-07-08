package GenericParamForm;

import org.gephi.statistics.spi.Statistics;

import javax.swing.*;
import java.util.List;

public abstract class Params<TStatistics extends Statistics> extends JPanel {

    protected abstract String ShortDescription();
    protected abstract List<String> Descritpion();
    public abstract void SetGeneratorParams(TStatistics generator);
}
