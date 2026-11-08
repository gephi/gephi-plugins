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
    private final GyNamespace namespace;
    /** The underlying query object */
    private Query underlyingQuery;

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
