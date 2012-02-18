/*
Copyright 2008-2011 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.scripting.wrappers;

import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.operator.INTERSECTIONBuilder.IntersectionOperator;
import org.gephi.filters.plugin.operator.UNIONBuilder.UnionOperator;
import org.gephi.scripting.util.GyNamespace;
import org.openide.util.Lookup;
import org.python.core.PyObject;
import org.python.core.PySet;

/**
 * Wraps a <code>Query</code> object from the Filters API so that it can be
 * easily handled from the scripting language.
 * 
 * This class overrides the <code>__and__</code> and <code>__or__</code> methods
 * from the <code>PyObject</code> class for implementing the INTERSECTION and
 * UNION operators from the Filters API, respectively.
 *
 * @author Luiz Ribeiro
 */
public class GyFilter extends PyObject {

    /** The namespace in which this object is inserted */
    private GyNamespace namespace;
    /** The underlying query object */
    private Query underlyingQuery;
    private PySet underlyingSet;

    /**
     * Constructor for the filter wrapper.
     * @param namespace     the namespace in which this object is inserted
     * @param query         the query object that will be wrapped
     */
    public GyFilter(GyNamespace namespace, Query query) {
        this.namespace = namespace;
        this.underlyingQuery = query;
    }

    /**
     * Retrieves the underlying query object.
     * @return              the underlying query object
     */
    public Query getUnderlyingQuery() {
        return underlyingQuery;
    }

    /**
     * Sets a new query to be wrapped by this wrapper object.
     * @param query         the new underlying query object
     */
    public void setUnderlyingQuery(Query query) {
        this.underlyingQuery = query;
    }

    public PySet getUnderlyingSet() {
        return underlyingSet;
    }

    public void setUnderlyingSet(PySet underlyingSet) {
        this.underlyingSet = underlyingSet;
    }

    @Override
    public PyObject __and__(PyObject obj) {
        if (obj instanceof GyFilter) {
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            IntersectionOperator intersectionOperator = new IntersectionOperator();
            Query andQuery = filterController.createQuery(intersectionOperator);
            GyFilter otherFilter = (GyFilter) obj;

            filterController.setSubQuery(andQuery, underlyingQuery);
            filterController.setSubQuery(andQuery, otherFilter.underlyingQuery);

            return new GyFilter(namespace, andQuery);
        }

        return null;
    }

    @Override
    public PyObject __or__(PyObject obj) {
        if (obj instanceof GyFilter) {
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            UnionOperator unionOperator = new UnionOperator();
            Query orQuery = filterController.createQuery(unionOperator);
            GyFilter otherFilter = (GyFilter) obj;

            filterController.setSubQuery(orQuery, underlyingQuery);
            filterController.setSubQuery(orQuery, otherFilter.underlyingQuery);

            return new GyFilter(namespace, orQuery);
        }

        return null;
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        GyGraph graph = (GyGraph) this.namespace.__finditem__(GyNamespace.GRAPH_NAME);
        GySubGraph subGraph = graph.filter(this);

        PySet nodes = (PySet) subGraph.__findattr_ex__("nodes");
        PySet edges = (PySet) subGraph.__findattr_ex__("edges");

        nodes.__setattr__(name, value);
        edges.__setattr__(name, value);
    }
}
