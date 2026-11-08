package cwts.networkanalysis.gephiplugin;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * User interface for the {@link RunClustering}.
 */
@ServiceProvider(service = StatisticsUI.class)
public class RunClusteringUI implements StatisticsUI
{
    private final StatSettings settings = new StatSettings();

    private RunClustering statistic;
    private RunClusteringPanel panel;

    @Override
    public JPanel getSettingsPanel() {
        panel = new RunClusteringPanel();
        return panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.statistic = (RunClustering) statistics;
        if (panel != null)
        {
            settings.load(statistic);

            panel.setUseEdgeWeights(statistic.useEdgeWeights);
            panel.setUseRandomSeed(statistic.useRandomSeed);
            panel.setRandomSeed(statistic.randomSeed);
            panel.setNIterations(statistic.nIterations);
            panel.setNRestarts(statistic.nRestarts);
            panel.setResolution(statistic.resolution);
            panel.setAlgorithm(statistic.algorithm);
            panel.setQualityFunction(statistic.qualityFunction);
        }
    }

    @Override
    public void unsetup() {
        if (panel != null)
        {
            statistic.useEdgeWeights = panel.getUseEdgeWeights();
            statistic.useRandomSeed = panel.getUseRandomSeed();
            statistic.randomSeed = panel.getRandomSeed();
            statistic.nIterations = panel.getNIterations();
            statistic.nRestarts = panel.getNRestarts();
            statistic.resolution = panel.getResolution();
            statistic.algorithm = panel.getAlgorithm();
            statistic.qualityFunction = panel.getQualityFunction();

            settings.save(statistic);
        }
        statistic = null;
        panel = null;
    }


    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return RunClustering.class;
    }

    @Override
    public String getValue()
    {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(statistic.getQuality());
    }

    @Override
    public String getDisplayName() {
        return "Leiden algorithm";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 10000;
    }

    @Override
    public String getShortDescription() {
        return "Leiden algorithm";
    }

    private static class StatSettings {

        protected boolean useEdgeWeights = true;
        protected boolean useRandomSeed = false;
        protected int randomSeed = 0;
        protected int nIterations = 10;
        protected int nRestarts = 1;
        protected double resolution = 0.01;
        protected RunClustering.Algorithm algorithm = RunClustering.Algorithm.LEIDEN;
        protected RunClustering.QualityFunction qualityFunction = RunClustering.QualityFunction.CPM;

        private void save(RunClustering stat)
        {
            useEdgeWeights = stat.useEdgeWeights;
            useRandomSeed = stat.useRandomSeed;
            randomSeed = stat.randomSeed;
            nIterations = stat.nIterations;
            nRestarts = stat.nRestarts;
            resolution = stat.resolution;
            algorithm = stat.algorithm;
            qualityFunction = stat.qualityFunction;
        }

        private void load(RunClustering stat)
        {
            stat.useEdgeWeights = useEdgeWeights;
            stat.useRandomSeed = useRandomSeed;
            stat.randomSeed = randomSeed;
            stat.nIterations = nIterations;
            stat.nRestarts = nRestarts;
            stat.resolution = resolution;
            stat.algorithm = algorithm;
            stat.qualityFunction = qualityFunction;
        }
    }
}
