package org.gephi.plugins.linkprediction.filter;

import org.gephi.plugins.linkprediction.base.LinkPredictionFilter;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder;

/**
 * Filter that limits the displayed edges to the number specified calculated
 * using the preferential attachment algorithm.
 *
 * @author Marco Romanutti
 * @see PreferentialAttachmentFilterBuilder
 */
public class PreferentialAttachmentFilter extends LinkPredictionFilter {

    @Override public String getName() {
        return PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;
    }

}
