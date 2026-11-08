package org.gephi.plugins.linkprediction.statistics;

import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatisticsBuilder;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
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
@ServiceProvider(service = StatisticsBuilder.class) public class PreferentialAttachmentStatisticsBuilder
        extends LinkPredictionStatisticsBuilder {
    /**
     * Name of the algorithm
     **/
    public static final String PREFERENTIAL_ATTACHMENT_NAME = "Preferential Attachment";

    @Override public String getName() {
        return PREFERENTIAL_ATTACHMENT_NAME;
    }

    @Override public Class<? extends Statistics> getStatisticsClass() {
        return PreferentialAttachmentStatistics.class;
    }

    @Override public Statistics getStatistics() {
        return new PreferentialAttachmentStatistics();
    }
}
