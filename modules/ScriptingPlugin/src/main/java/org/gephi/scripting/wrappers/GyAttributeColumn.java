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

import org.gephi.filters.api.FilterController; //new import
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.attribute.AttributeEqualBuilder;
import org.gephi.filters.plugin.attribute.AttributeRangeBuilder.AttributeRangeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.scripting.util.GyNamespace; //new import
import org.openide.util.Lookup;  // new import
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Wraps an attribute column, so that it is exposed to the scripting language.
 *
 * Objects of the type <code>GyAttributeColumn</code> are exposed to the scripting language by <code>GyNamespace</code>. Once the user tries to access a variable whose name matches an attribute column
 * name, a new <code>GyAttributeColumn</code> is instantiated and returned.
 *
 * @author Luiz Ribeiro
 */
public class GyAttributeColumn extends GyAttribute {

    /**
     * The underlying attribute column
     */
    private final Column underlyingAttributeColumn;

    /**
     * Constructor for the attribute column wrapper.
     *
     * @param namespace the namespace in which this wrapper is inserted
     * @param attributeColumn the underlying attribute column
     * @param isNodeColumn
     */
    public GyAttributeColumn(GyNamespace namespace, Column attributeColumn) {
        super(namespace);
        this.underlyingAttributeColumn = attributeColumn;
    }

    @Override
    public String toString() {
        if (isNodeAttribute()) {
            return "Node Attribute '" + underlyingAttributeColumn.getId() + "' (" + underlyingAttributeColumn.getTypeClass() + ")";
        } else {
            return "Edge Attribute '" + underlyingAttributeColumn.getId() + "' (" + underlyingAttributeColumn.getTypeClass() + ")";
        }
    }

    public Column getUnderlyingAttributeColumn() {
        return this.underlyingAttributeColumn;
    }

    @Override
    public Class getAttributeType() {
        return underlyingAttributeColumn.getTypeClass();
    }

    @Override
    public boolean isNodeAttribute() {
        return AttributeUtils.isNodeColumn(underlyingAttributeColumn);
    }

    @Override
    protected Query buildRangeQuery(Range range) {
        if (!underlyingAttributeColumn.isNumber() || underlyingAttributeColumn.isArray() || underlyingAttributeColumn.isDynamic()) {
            throw Py.TypeError("unsupported operator for attribute type '" + underlyingAttributeColumn.getTypeClass() + "'");
        }

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        AttributeRangeFilter rangeFilter;
        if (isNodeAttribute()) {
            rangeFilter = new AttributeRangeFilter.Node(underlyingAttributeColumn);
        } else {
            rangeFilter = new AttributeRangeFilter.Edge(underlyingAttributeColumn);
        }

        rangeFilter.setRange(range);
        return filterController.createQuery(rangeFilter);
    }

    @Override
    protected Query buildEqualsQuery(PyObject match) {
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        Filter attributeEqualFilter;
        Query query;

        if (underlyingAttributeColumn.isArray() || underlyingAttributeColumn.isDynamic()) {
            throw Py.TypeError("unsupported operator for attribute type '" + underlyingAttributeColumn.getTypeClass() + "'");
        }

        if (underlyingAttributeColumn.isNumber()) {
            AttributeEqualBuilder.EqualNumberFilter filter;

            if (isNodeAttribute()) {
                filter = new AttributeEqualBuilder.EqualNumberFilter.Node(underlyingAttributeColumn);
            } else {
                filter = new AttributeEqualBuilder.EqualNumberFilter.Edge(underlyingAttributeColumn);
            }

            attributeEqualFilter = filter;
            query = filterController.createQuery(attributeEqualFilter);

            filter.setMatch((Number) match.__tojava__(underlyingAttributeColumn.getTypeClass()));
        } else if (AttributeUtils.isStringType(underlyingAttributeColumn.getTypeClass())) {
            AttributeEqualBuilder.EqualStringFilter filter;

            if (isNodeAttribute()) {
                filter = new AttributeEqualBuilder.EqualStringFilter.Node(underlyingAttributeColumn);
            } else {
                filter = new AttributeEqualBuilder.EqualStringFilter.Edge(underlyingAttributeColumn);
            }

            attributeEqualFilter = filter;
            query = filterController.createQuery(attributeEqualFilter);

            filter.setColumn(underlyingAttributeColumn);
            filter.setPattern(match.toString());
        } else if (AttributeUtils.isBooleanType(underlyingAttributeColumn.getTypeClass())) {
            AttributeEqualBuilder.EqualBooleanFilter filter;

            if (isNodeAttribute()) {
                filter = new AttributeEqualBuilder.EqualBooleanFilter.Node(underlyingAttributeColumn);
            } else {
                filter = new AttributeEqualBuilder.EqualBooleanFilter.Edge(underlyingAttributeColumn);
            }

            query = filterController.createQuery(filter);

            filter.setColumn(underlyingAttributeColumn);
            filter.setMatch((Boolean) match.__tojava__(Boolean.class));
        } else {
            throw Py.TypeError("unsupported operator for attribute type '" + underlyingAttributeColumn.getTypeClass() + "'");
        }

        return query;
    }
}
