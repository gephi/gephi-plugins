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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.EdgeIterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;
import org.gephi.scripting.util.GyEdgeSet;
import org.gephi.scripting.util.GyNamespace;
import org.gephi.scripting.util.GyNodeSet;
import org.python.core.PyList;
import org.python.core.PyObject;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyGraph extends PyObject {

    private GyNamespace namespace;
    // Hack to get a few attributes into jythonconsole's auto-completion
    // TODO: get rid of this ugly hack (:
    public PyList nodes;
    public PyList edges;

    public GyGraph(GyNamespace namespace) {
        this.namespace = namespace;
    }

    public GyNode addNode(PyObject args[], String keywords[]) {
        GyNode ret;
        Node node;

        // Instantiates the new node and adds it to the graph
        node = namespace.getGraphModel().factory().newNode();
        namespace.getGraphModel().getGraph().addNode(node);
        ret = namespace.getGyNode(node.getId());

        // Sets the node attributes according to args, kwargs
        for (int i = 0; i < args.length; i++) {
            ret.__setattr__(keywords[i], args[i]);
        }

        return ret;
    }

    public GyEdge addDirectedEdge(GyNode source, GyNode target) {
        GyEdge ret = null;
        Edge edge;

        // Instantiates the new edge and adds it to the graph
        edge = namespace.getGraphModel().factory().newEdge(source.getNode(), target.getNode(), 1.0f, true);
        namespace.getGraphModel().getDirectedGraph().addEdge(edge);
        ret = namespace.getGyEdge(edge.getId());

        return ret;
    }

    public GyEdge addEdge(GyNode source, GyNode target) {
        return addUndirectedEdge(source, target);
    }

    public GyEdge addUndirectedEdge(GyNode source, GyNode target) {
        GyEdge ret = null;
        Edge edge;

        // Instantiates the new edge and adds it to the graph
        edge = namespace.getGraphModel().factory().newEdge(source.getNode(), target.getNode(), 1.0f, false);
        namespace.getGraphModel().getUndirectedGraph().addEdge(edge);
        ret = namespace.getGyEdge(edge.getId());

        return ret;
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        if (name.equals("nodes")) {
            NodeIterable nodeIterable = namespace.getGraphModel().getGraph().getNodes();
            GyNodeSet nodesSet = new GyNodeSet();

            for (NodeIterator nodeItr = nodeIterable.iterator(); nodeItr.hasNext();) {
                GyNode node = namespace.getGyNode(nodeItr.next().getId());
                nodesSet.add(node);
            }

            return nodesSet;
        } else if (name.equals("edges")) {
            EdgeIterable edgeIterable = namespace.getGraphModel().getGraph().getEdges();
            GyEdgeSet edgesSet = new GyEdgeSet();

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
}
