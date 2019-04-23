package org.gephi.plugins.linkprediction.base;

import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.Statistics;

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

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return LinkPredictionStatistics.class;
    }

}
