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
import org.gephi.scripting.util.GyNamespace;
import org.openide.util.Lookup;
import org.python.core.PyObject;
import org.python.core.PySet;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyFilter extends PyObject {

    private GyNamespace namespace;
    private Query underlyingQuery;
    private PySet underlyingSet;

    public GyFilter(GyNamespace namespace, Query query) {
        this.namespace = namespace;
        this.underlyingQuery = query;
    }

    public Query getUnderlyingQuery() {
        return underlyingQuery;
    }

    public void setUnderlyingQuery(Query underlyingQuery) {
        this.underlyingQuery = underlyingQuery;
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
}
