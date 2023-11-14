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
import org.gephi.filters.plugin.graph.DegreeRangeBuilder;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.filters.plugin.graph.InDegreeRangeBuilder;
import org.gephi.filters.plugin.graph.InDegreeRangeBuilder.InDegreeRangeFilter;
import org.gephi.filters.plugin.graph.OutDegreeRangeBuilder;
import org.gephi.filters.plugin.graph.OutDegreeRangeBuilder.OutDegreeRangeFilter;
import org.gephi.scripting.util.GyNamespace;
import org.openide.util.Lookup;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Wraps a topology attribute (i.e. degree, in degree or out degree), so that
 * it is exposed to the scripting language.
 * 
 * Objects of the type <code>GyAttributeTopology</code> are exposed to the
 * scripting language by <code>GyNamespace</code>. Once the user tries to access
 * a variable whose name matches an attribute column name, a new
 * <code>GyAttributeTopology</code> is instantiated and returned.
 *
 * @author Luiz Ribeiro
 */
public class GyAttributeTopology extends GyAttribute {

    /** The attribute type this object represents */
    protected Type topologyType;

    /**
     * An enumeration type that is used for representing one of the topology
     * attribute types that can <code>GyAttributeTopology</code> objects can
     * represent.
     */
    public static enum Type {

        /** Degree type */
        DEGREE,
        /** In degree type */
        IN_DEGREE,
        /** Out degree type */
        OUT_DEGREE
    }

    /**
     * Constructs a new <code>GyAttributeTopology</code> for representing
     * the given topologyy type.
     * 
     * @param namespace     namespace in which this attribute is inserted
     * @param topologyType  the topology type that the new object will represent
     */
    public GyAttributeTopology(GyNamespace namespace, Type topologyType) {
        super(namespace);
        this.topologyType = topologyType;
    }

    @Override
    public String toString() {
        String typeName;

        if (topologyType == Type.DEGREE) {
            typeName = "degree";
        } else if (topologyType == Type.IN_DEGREE) {
            typeName = "indegree";
        } else if (topologyType == Type.OUT_DEGREE) {
            typeName = "outdegree";
        } else {
            typeName = "UNKNOWN";
        }

        return "Node Attribute '" + typeName + "' (" + getAttributeType().getSimpleName() + ")";
    }

    @Override
    public Class getAttributeType() {
        return Integer.class;
    }

    @Override
    public boolean isNodeAttribute() {
        return true;
    }

    @Override
    protected Query buildRangeQuery(Range range) {
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        Query query;

        if (topologyType == Type.DEGREE) {
            DegreeRangeFilter degreeRangeFilter = new DegreeRangeBuilder.DegreeRangeFilter();
            query = filterController.createQuery(degreeRangeFilter);
            degreeRangeFilter.setRange(range);
        } else if (topologyType == Type.IN_DEGREE) {
            InDegreeRangeFilter inDegreeRangeFilter = new InDegreeRangeBuilder.InDegreeRangeFilter();
            query = filterController.createQuery(inDegreeRangeFilter);
            inDegreeRangeFilter.setRange(range);
        } else if (topologyType == Type.OUT_DEGREE) {
            OutDegreeRangeFilter outDegreeRangeFilter = new OutDegreeRangeBuilder.OutDegreeRangeFilter();
            query = filterController.createQuery(outDegreeRangeFilter);
            outDegreeRangeFilter.setRange(range);
        } else {
            // Shouldn't happen ever
            throw Py.TypeError("Unexpected error");
        }

        return query;
    }

    @Override
    protected Query buildEqualsQuery(PyObject match) {
        Integer intMatch = (Integer) match.__tojava__(Integer.class);
        return buildRangeQuery(new Range(intMatch, intMatch, Integer.MIN_VALUE, Integer.MAX_VALUE));
    }
}
