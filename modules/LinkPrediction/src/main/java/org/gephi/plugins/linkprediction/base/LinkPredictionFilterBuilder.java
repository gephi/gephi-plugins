package org.gephi.plugins.linkprediction.base;

import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;

import javax.swing.*;

/**
 * Filter builder for the {@link LinkPredictionFilter} filter.
 * <p>
 * This base factory class configures how the filter should be integrated. The
 * base class is extended by a concrete builder implementation of the respective
 * algorithms.
 *
 * @author Marco Romanutti
 * @see LinkPredictionFilter
 */
public abstract class LinkPredictionFilterBuilder implements FilterBuilder {
    /** Name of the newly added filter category */
    public static final String LINK_PREDICTION_CATEGORY = "Link prediction";


    /**
     * Category in which filter will be shown in UI.
     *
     * @return Filter category
     */
    public Category getCategory() {
        return new Category(LINK_PREDICTION_CATEGORY);
    }

    /**
     * Gets icon.
     *
     * @return icon for filter
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Gets filter panel.
     *
     * @param filter Filter to be applied
     * @return Filter panel to modify filter
     */
    public JPanel getPanel(Filter filter) {
        return new LinkPredictionFilterPanel(filter);
    }

    /**
     * Destroys current filter.
     *
     * @param filter Filter to destroy
     */
    public void destroy(Filter filter) {
    }
}
