package org.gephi.plugins.linkprediction.base;

import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.FilterProperty;
import org.openide.util.Exceptions;

/**
 * Filter that removes all predicted edges that do not originate from the
 * corresponding algorithm.
 * <p>
 * This base class contains all filter-independent implementations. The
 * base class is extended by the implementations of the respective algorithms.
 *
 * @author Marco Romanutti
 * @see LinkPredictionFilterBuilder
 */
public abstract class LinkPredictionFilter implements ComplexFilter {
    /** Default number of displayed predicted edges **/
    public static final int EDGES_LIMIT_DEFAULT = 1;

    // Properties used in filter
    protected static FilterProperty[] filterProperties;
    // Number of displayed predicted edges
    protected int edgesLimit = EDGES_LIMIT_DEFAULT;

    /**
     * Get or creates singleton instance of properties.
     *
     * @return Filter properties
     */
    public FilterProperty[] getProperties() {
        if (filterProperties == null) {
            try {
                filterProperties = new FilterProperty[] { FilterProperty.createProperty(this, Integer.class, "edgesLimit") };
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return filterProperties;
    }

    public static FilterProperty[] getFilterProperties() {
        return filterProperties;
    }

    public static void setFilterProperties(FilterProperty[] filterProperties) {
        LinkPredictionFilter.filterProperties = filterProperties;
    }

    public int getEdgesLimit() {
        return edgesLimit;
    }

    public void setEdgesLimit(int edgesLimit) {
        this.edgesLimit = edgesLimit;
    }
}
