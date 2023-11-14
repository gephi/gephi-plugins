/*
Copyright 2008-2012 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
 * Abstract class for other classes that wrap graph attributes.
 * 
 * Classes that inherit from this should implement the following methods:
 * <code>toString</code>, <code>getAttributeType</code>,
 * <code>isNodeAttribute</code>, <code>buildRangeQuery</code> and
 * <code>buildEqualsQuery</code>.
 * 
 * Note that this class already implements the comparison operators (e.g.
 * <code>__gt__</code>, <code>__lt__</code>, etc). The implemented comparison
 * operators build <code>Range</code> objects according to the parameter
 * passed to them and it is <code>buildRangeQuery</code>'s job to build
 * a query with the newly created <code>Range</code> object.
 *
 * @author Luiz Ribeiro
 */
abstract class GyAttribute extends PyObject {

    /** The namespace in which this attribute is inserted */
    protected GyNamespace namespace;

    /**
     * Default constructor.
     * @param namespace     the namespace in which this attribute is inserted
     */
    GyAttribute(GyNamespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public abstract String toString();

    /**
     * Should return the class of the data that is contained in this attribute
     * column.
     * @return              class of the attribute column's data
     */
    public abstract Class getAttributeType();

    /**
     * Should indicate if this attribute is related to nodes. If this is an edge
     * attribute, the function returns false.
     * @return              true if it's related to nodes, false if not
     */
    public abstract boolean isNodeAttribute();

    /**
     * Should build a range query for this attribute, with the given range.
     * @param range         range for building the query
     * @return              a <code>Query</code> containing the range query
     */
    protected abstract Query buildRangeQuery(Range range);

    /**
     * Should build an equals query for this attribute, looking for matches
     * with the given object.
     * @param match         value to match for
     * @return              a <code>Query</code> containing the equals query
     */
    protected abstract Query buildEqualsQuery(PyObject match);

    @Override
    public PyObject __gt__(PyObject obj) {
        Range filterRange;
        Query query;

        if (Number.class.isAssignableFrom(getAttributeType())) {
            Class columnType = getAttributeType();
            try {
                Number lowerBound = (Number) obj.__tojava__(columnType);
                Number upperBound = (Number) columnType.getDeclaredField("MAX_VALUE").get(null);
                filterRange = new Range(lowerBound, upperBound, false, true);
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
            Class columnType = getAttributeType();
            try {
                Number lowerBound = (Number) columnType.getDeclaredField("MIN_VALUE").get(null);
                Number upperBound = (Number) obj.__tojava__(columnType);
                filterRange = new Range(lowerBound, upperBound, true, false);
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
