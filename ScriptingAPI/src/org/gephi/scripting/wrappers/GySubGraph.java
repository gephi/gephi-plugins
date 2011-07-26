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
 *
 * @author Luiz Ribeiro
 */
public class GySubGraph extends PyObject {

    protected GyNamespace namespace;
    protected GraphView underlyingGraphView;
    // Hack to get a few attributes into jythonconsole's auto-completion
    // TODO: get rid of this ugly hack (:
    public PyList nodes;
    public PyList edges;

    public GySubGraph(GyNamespace namespace, GraphView graphView) {
        this.namespace = namespace;
        this.underlyingGraphView = graphView;
    }

    public Graph getUnderlyingGraph() {
        if (underlyingGraphView == null) {
            return namespace.getGraphModel().getGraph();
        } else {
            return namespace.getGraphModel().getGraph(underlyingGraphView);
        }
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

    public GySubGraph filter(GyFilter filter) {
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        GraphView graphView = filterController.filter(filter.getUnderlyingQuery());
        return new GySubGraph(namespace, graphView);
    }
}
