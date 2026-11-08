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

import java.util.Iterator;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.scripting.util.GyNamespace;
import org.openide.util.Lookup;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PySet;

/**
 * This class represents a subgraph of the main graph or, more specifically,
 * wraps a <code>GraphView</code>.
 * 
 * Besides of wrapping a <code>GraphView</code> object, objects of this class
 * also store a <code>Query</code> object, which was the query used to build the
 * respective <code>GraphView</code>.
 * 
 * Note that the underlying <code>GraphView</code> object is instantiated by
 * this class' constructor. In other words, <code>GySubGraph</code> objects
 * are instantiated from the <code>Query</code> object.
 * 
 * If this <code>GySubGraph</code> represents the main graph (so it's a
 * <code>GyGraph</code> instance, actually), the underlying
 * <code>GraphView</code> and <code>Query</code> objects are <code>null</code>.
 *
 * @author Luiz Ribeiro
 */
public class GySubGraph extends PyObject {

    /** The namespace in which this object is inserted */
    protected GyNamespace namespace;
    /** The underlying <code>GraphView</code> object */
    protected GraphView underlyingGraphView;
    /** The query that was used to construct this subgraph */
    protected Query constructionQuery;

    /**
     * Constructor for the subgraph wrapper.
     * @param namespace     the namespace in which this object is inserted
     * @param query         the query that will be used to construct the subgraph
     */
    public GySubGraph(GyNamespace namespace, Query query) {
        this.namespace = namespace;
        this.constructionQuery = query;

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        if (this.constructionQuery != null) {
            this.underlyingGraphView = filterController.filter(this.constructionQuery);
        } else {
            this.underlyingGraphView = null;
        }
    }

    /**
     * Returns the corresponding <code>Graph</code> object of the underlying
     * <code>GraphView</code>.
     * @return an object for accessing the graph of the underlying subgraph
     */
    public Graph getUnderlyingGraph() {
        if (underlyingGraphView == null) {
            return namespace.getGraphModel().getGraph();
        } else {
            return namespace.getGraphModel().getGraph(underlyingGraphView);
        }
    }

    /**
     * Returns the underlying <code>GraphView</code> object.
     * @return the underlying <code>GraphView</code> object.
     */
    public GraphView getUnderlyingGraphView() {
        return this.underlyingGraphView;
    }

    /**
     * Returns the <code>GyFilter</code> that was used as a construction query
     * for this subgraph.
     * @return the <code>GyFilter</code> that was used as a construction query
     * for this subgraph.
     */
    public GyFilter getFilter() {
        return new GyFilter(namespace, constructionQuery);
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        if (name.equals("nodes")) {
            NodeIterable nodeIterable = getUnderlyingGraph().getNodes();
            PySet nodesSet = new PySet();

            for (Iterator nodeItr = nodeIterable.iterator(); nodeItr.hasNext();) {
                GyNode node = namespace.getGyNode((String) ((Node) nodeItr.next()).getId());
                nodesSet.add(node);
            }

            return nodesSet;
        } else if (name.equals("edges")) {
            EdgeIterable edgeIterable = getUnderlyingGraph().getEdges();
            PySet edgesSet = new PySet();

            for (Iterator edgeItr = edgeIterable.iterator(); edgeItr.hasNext();) {
                GyEdge edge = namespace.getGyEdge((String) ((Edge) edgeItr.next()).getId());
                edgesSet.add(edge);
            }

            return edgesSet;
        } else {
            return super.__findattr_ex__(name);
        }
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        if (name.equals("nodes")) {
            readonlyAttributeError(name);
        } else if (name.equals("edges")) {
            readonlyAttributeError(name);
        } else {
            super.__setattr__(name, value);
        }
    }

    /**
     * Filters this subgraph with the given filter.
     * @param filter    the filter that will be applied to this subgraph
     * @return          a new subgraph, that is this subgraph filtered with the given filter
     */
    public GySubGraph filter(GyFilter filter) {
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        Query query;

        if (constructionQuery != null) {
            // If we have a construction query for this subgraph, use it as a sub query
            query = filterController.createQuery(filter.getUnderlyingQuery().getFilter());
            filterController.setSubQuery(query, constructionQuery);
        } else {
            // If this is the main graph, just use the filter passed as argument
            query = filter.getUnderlyingQuery();
        }

        return new GySubGraph(namespace, query);
    }

    @Override
    public PyObject __dir__() {
        PyList list = new PyList();
        list.add("nodes");
        list.add("edges");
        
        return list;
    }
}
