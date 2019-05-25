package org.gephi.plugins.linkprediction.filter;

import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilterBuilder;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder;
import org.gephi.project.api.Workspace;
import org.openide.util.lookup.ServiceProvider;

/**
 * Filter builder for the {@link PreferentialAttachmentFilter} filter.
 *
 * @author Marco Romanutti
 * @see PreferentialAttachmentFilter
 */
@ServiceProvider(service = FilterBuilder.class) public class PreferentialAttachmentFilterBuilder
        extends LinkPredictionFilterBuilder {

    /**
     * Description of the common neighbours filter
     **/
    public static final String PREFERENTIAL_ATTACHMENT_DESC = "Predict n next link using preferential attachment algorithm";

    @Override public String getName() {
        return PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;
    }

    @Override public String getDescription() {
        return PREFERENTIAL_ATTACHMENT_DESC;
    }

    @Override public Filter getFilter(Workspace workspace) {
        return new PreferentialAttachmentFilter();
    }
}
