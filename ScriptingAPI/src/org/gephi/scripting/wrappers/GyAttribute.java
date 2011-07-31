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
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.operator.NOTBuilderEdge.NotOperatorEdge;
import org.gephi.filters.plugin.operator.NOTBuilderNode.NOTOperatorNode;
import org.gephi.filters.spi.Filter;
import org.gephi.scripting.util.GyNamespace;
import org.openide.util.Lookup;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 *
 * @author Luiz Ribeiro
 */
abstract class GyAttribute extends PyObject {

    protected GyNamespace namespace;

    GyAttribute(GyNamespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public abstract String toString();

    public abstract Class getAttributeType();

    public abstract boolean isNodeAttribute();

    protected abstract Query buildRangeQuery(Range range);

    protected abstract Query buildEqualsQuery(PyObject match);

    @Override
    public PyObject __gt__(PyObject obj) {
        Range filterRange;
        Query query;

        if (Number.class.isAssignableFrom(getAttributeType())) {
            // FIXME: lower bound should be an open interval
            Class columnType = getAttributeType();
            try {
                Number minValue = (Number) columnType.getDeclaredField("MIN_VALUE").get(null);
                Number maxValue = (Number) columnType.getDeclaredField("MAX_VALUE").get(null);
                Number lowerBound = (Number) obj.__tojava__(columnType);
                Number upperBound = maxValue;
                filterRange = new Range(lowerBound, upperBound, minValue, maxValue);
            } catch (Exception ex) {
                throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
            }
        } else {
            throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
        }

        query = buildRangeQuery(filterRange);

        return new GyFilter(namespace, query);
    }

    @Override
    public PyObject __ge__(PyObject obj) {
        Range filterRange;
        Query query;

        if (Number.class.isAssignableFrom(getAttributeType())) {
            Class columnType = getAttributeType();
            try {
                Number minValue = (Number) columnType.getDeclaredField("MIN_VALUE").get(null);
                Number maxValue = (Number) columnType.getDeclaredField("MAX_VALUE").get(null);
                Number lowerBound = (Number) obj.__tojava__(columnType);
                Number upperBound = maxValue;
                filterRange = new Range(lowerBound, upperBound, minValue, maxValue);
            } catch (Exception ex) {
                throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
            }
        } else {
            throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
        }

        query = buildRangeQuery(filterRange);

        return new GyFilter(namespace, query);
    }

    @Override
    public PyObject __lt__(PyObject obj) {
        Range filterRange;
        Query query;

        if (Number.class.isAssignableFrom(getAttributeType())) {
            // FIXME: upper bound should be an open interval
            Class columnType = getAttributeType();
            try {
                Number minValue = (Number) columnType.getDeclaredField("MIN_VALUE").get(null);
                Number maxValue = (Number) columnType.getDeclaredField("MAX_VALUE").get(null);
                Number lowerBound = minValue;
                Number upperBound = (Number) obj.__tojava__(columnType);
                filterRange = new Range(lowerBound, upperBound, minValue, maxValue);
            } catch (Exception ex) {
                throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
            }
        } else {
            throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
        }

        query = buildRangeQuery(filterRange);

        return new GyFilter(namespace, query);
    }

    @Override
    public PyObject __le__(PyObject obj) {
        Range filterRange;
        Query query;

        if (Number.class.isAssignableFrom(getAttributeType())) {
            Class columnType = getAttributeType();
            try {
                Number minValue = (Number) columnType.getDeclaredField("MIN_VALUE").get(null);
                Number maxValue = (Number) columnType.getDeclaredField("MAX_VALUE").get(null);
                Number lowerBound = minValue;
                Number upperBound = (Number) obj.__tojava__(columnType);
                filterRange = new Range(lowerBound, upperBound, minValue, maxValue);
            } catch (Exception ex) {
                throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
            }
        } else {
            throw Py.TypeError("unsupported operator for attribute type '" + getAttributeType() + "'");
        }

        query = buildRangeQuery(filterRange);

        return new GyFilter(namespace, query);
    }

    @Override
    public PyObject __eq__(PyObject obj) {
        Query query;

        query = buildEqualsQuery(obj);

        return new GyFilter(namespace, query);
    }

    @Override
    public PyObject __ne__(PyObject obj) {
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        Query equalsQuery = buildEqualsQuery(obj);
        Query notEqualsQuery;
        Filter notFilter;

        if (isNodeAttribute()) {
            notFilter = new NOTOperatorNode();
        } else {
            notFilter = new NotOperatorEdge();
        }

        notEqualsQuery = filterController.createQuery(notFilter);
        filterController.setSubQuery(notEqualsQuery, equalsQuery);

        return new GyFilter(namespace, notEqualsQuery);
    }
}
