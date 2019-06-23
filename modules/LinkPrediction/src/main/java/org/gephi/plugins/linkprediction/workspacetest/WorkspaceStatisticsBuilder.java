package org.gephi.plugins.linkprediction.workspacetest;

import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatisticsBuilder;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsBuilder.class)
public class WorkspaceStatisticsBuilder implements StatisticsBuilder {

    public WorkspaceStatisticsBuilder() {
    }

    @Override
    public String getName() {
        return "Link Prediction";
    }

    @Override
    public Statistics getStatistics() {
        return new WorkspaceStatistics();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return WorkspaceStatistics.class;
    }

}
