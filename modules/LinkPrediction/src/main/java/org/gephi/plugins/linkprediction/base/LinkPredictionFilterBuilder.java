package org.gephi.plugins.linkprediction.base;

import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.util.lookup.ServiceProvider;

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

    public Category getCategory() {
        // TODO Create own category
        return FilterLibrary.EDGE;
    }

    public Icon getIcon() {
        return null;
    }

    public JPanel getPanel(Filter filter) {
        // TODO Implement UI
        return null;
    }

    public void destroy(Filter filter) {
    }
}
