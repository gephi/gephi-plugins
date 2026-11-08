package pl.edu.wat.wcy.gephi.plugin.clusteringcoefficient;

import org.gephi.graph.api.*;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

public class ClusteringCoefficientStatistic implements Statistics, LongTask {

    private String report = "";
    public static boolean cancel = false;
    private ProgressTicket progressTicket;

    @Override
    public void execute(GraphModel graphModel) {
        // The flag is shared and was never cleared, so after one cancellation no later run calculated anything
        cancel = false;
        if (ClusteringCoefficientUI.triangleMethod) {
            TriangleClusteringCoefficientAlgorithm tcc = new TriangleClusteringCoefficientAlgorithm(graphModel);
            report = tcc.calculate();
        } else {
            ClusteringCoefficientAlgorithm cc = new ClusteringCoefficientAlgorithm(graphModel);
            report = cc.calculate();
        }
    }

    @Override
    public String getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
