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
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.EdgeIterator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;
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
    // Hack to get a few attributes into jythonconsole's auto-completion
    // TODO: get rid of this ugly hack (:
    public PyList nodes;
    public PyList edges;

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

            for (NodeIterator nodeItr = nodeIterable.iterator(); nodeItr.hasNext();) {
                GyNode node = namespace.getGyNode(nodeItr.next().getId());
                nodesSet.add(node);
            }

            return nodesSet;
        } else if (name.equals("edges")) {
            EdgeIterable edgeIterable = getUnderlyingGraph().getEdges();
            PySet edgesSet = new PySet();

            for (EdgeIterator edgeItr = edgeIterable.iterator(); edgeItr.hasNext();) {
                GyEdge edge = namespace.getGyEdge(edgeItr.next().getId());
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
}
