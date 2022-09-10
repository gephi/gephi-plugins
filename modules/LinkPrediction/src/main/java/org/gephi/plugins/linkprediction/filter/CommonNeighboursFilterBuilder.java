package org.gephi.plugins.linkprediction.filter;

import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilterBuilder;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatisticsBuilder;
import org.gephi.project.api.Workspace;
import org.openide.util.lookup.ServiceProvider;

/**
 * Filter builder for the {@link CommonNeighboursFilter} filter.
 *
 * @author Marco Romanutti
 * @see CommonNeighboursFilter
 */
@ServiceProvider(service = FilterBuilder.class) public class CommonNeighboursFilterBuilder
        extends LinkPredictionFilterBuilder {

    /**
     * Description of the common neighbours filter
     **/
    public static final String COMMON_NEIGHBOURS_DESC = "Predict n next link using common neighbours algorithm";

    @Override public String getName() {
        return CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME;
    }

    @Override public String getDescription() {
        return COMMON_NEIGHBOURS_DESC;
    }

    @Override public Filter getFilter(Workspace workspace) {
        return new CommonNeighboursFilter();
    }
}
