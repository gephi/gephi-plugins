package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.statistics.LinkPredictionPreferentialAttachment;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.lookup.ServiceProvider;

/**
 * Statistic builder for the {@link LinkPredictionStatistics} statistic.
 * <p>
 * This base factory class configures how the statistics should be integrated. The
 * base class is extended by a concrete builder implementation of the respective
 * algorithms.
 *
 * @author Saskia Schueler
 * @see LinkPredictionStatistics
 */
public abstract class LinkPredictionStatisticsBuilder implements StatisticsBuilder {

    public LinkPredictionStatisticsBuilder() {
    }

    @Override
    public String getName() {
        return "Link Prediction";
    }

    @Override
    public Statistics getStatistics() {
        return new LinkPredictionPreferentialAttachment();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return LinkPredictionStatistics.class;
    }

}
