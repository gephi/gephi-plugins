package org.gephi.plugins.linkprediction.evaluation;

import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatisticsBuilder;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Statistic evaluation for the evaluation of the accuracy of {@link LinkPredictionStatistics} statistic.
 * <p>
 * This base factory class configures how the statistics should be integrated. The
 * base class is extended by a concrete builder implementation of the respective
 * algorithms.
 *
 * @author Marco Romanutti
 * @see LinkPredictionEvaluation
 */
@ServiceProvider(service = StatisticsBuilder.class) public class LinkPredictionEvaluationBuilder
        extends LinkPredictionStatisticsBuilder {
    /**
     * Name of the algorithm
     **/
    public static final String LINK_PREDICTION_NAME = "Link Prediction Evaluation";

    @Override public String getName() {
        return LINK_PREDICTION_NAME;
    }

    @Override public Class<? extends Statistics> getStatisticsClass() {
        return LinkPredictionEvaluation.class;
    }

    @Override public Statistics getStatistics() {
        return new LinkPredictionEvaluation();
    }
}
