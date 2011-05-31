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
package org.gephi.scripting.util;

import java.util.regex.Pattern;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.scripting.wrappers.GyEdge;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.gephi.scripting.wrappers.GyNode;
import org.python.core.Py;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyNamespace extends PyStringMap {

    public static final String NODE_PREFIX = "v";
    public static final String EDGE_PREFIX = "e";
    private GraphModel graphModel;

    public GyNamespace(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    @Override
    public void __setitem__(String key, PyObject value) {
        // The user shouldn't be able to set any of the variables that match
        // the regular expressions NODE_PREFIX[0-9]+ or EDGE_PREFIX[0-9]+

        if (key.startsWith(NODE_PREFIX)) {
            // Checks if key matches a node reserved variable name
            String id = key.substring(NODE_PREFIX.length());
            if (Pattern.compile("[0-9]+").matcher(id).matches()) {
                throw Py.NameError(key + " is a reserved variable name.");
            }
        } else if (key.startsWith(EDGE_PREFIX)) {
            // Checks if key matches an edge reserved variable name
            String id = key.substring(EDGE_PREFIX.length());
            if (Pattern.compile("[0-9]+").matcher(id).matches()) {
                throw Py.NameError(key + " is a reserved variable name.");
            }
        }

        // If everything ok, set the binding on the namespace
        super.__setitem__(key, value);
    }

    @Override
    public void __delitem__(String key) {
        // If the user tries to delete a node's binding from the namespace, it
        // will delete the node from the graph accordingly

        PyObject object = __finditem__(key);
        if (object instanceof GyNode) {
            GyNode node = (GyNode) object;
            graphModel.getGraph().removeNode(node.getNode());
        } else if (object instanceof GyEdge) {
            GyEdge node = (GyEdge) object;
            graphModel.getGraph().removeEdge(node.getEdge());
        }

        // Effectively delete the binding from the namespace
        super.__delitem__(key);
    }

    @Override
    public PyObject __finditem__(String key) {
        PyObject ret = super.__finditem__(key);

        if (ret != null) {
            // Object is already on the namespace
            return ret;
        }

        // Got a namespace lookup failure

        if (key.startsWith(NODE_PREFIX)) {
            // Check if it is a node
            String strId = key.substring(EDGE_PREFIX.length());
            if (Pattern.compile("[1-9][0-9]*").matcher(strId).matches()) {
                int id = Integer.parseInt(strId);
                Graph graph = graphModel.getGraph();
                Node node = graph.getNode(id);
                if (node != null) {
                    ret = new GyNode(graph, node);
                }
            }
        } else if (key.startsWith(EDGE_PREFIX)) {
            // Check if it is an edge
            String strId = key.substring(EDGE_PREFIX.length());
            if (Pattern.compile("[1-9][0-9]*").matcher(strId).matches()) {
                int id = Integer.parseInt(strId);
                Graph graph = graphModel.getGraph();
                Edge edge = graph.getEdge(id);
                if (edge != null) {
                    ret = new GyEdge(graph, edge);
                }
            }
        }

        if (ret != null) {
            // Update the namespace binding, in case something was found
            super.__setitem__(key, ret);
        }

        return ret;
    }
}
