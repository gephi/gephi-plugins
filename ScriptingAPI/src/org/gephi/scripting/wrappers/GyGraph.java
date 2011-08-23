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
import org.gephi.graph.api.Node;
import org.gephi.scripting.util.GyNamespace;
import org.python.core.PyObject;

/**
 * A specialization of <code>GySubGraph</code> that allows creating nodes and
 * edges.
 * 
 * This class is only used for representing the main graph on the scripting
 * language.
 * 
 * @author Luiz Ribeiro
 * @see GySubGraph
 */
public class GyGraph extends GySubGraph {

    public GyGraph(GyNamespace namespace) {
        super(namespace, null);
    }

    /**
     * Creates a new node and add it to the graph.
     * 
     * This method supports *args and **kwargs. Because of that, the user can
     * make calls like <code>g.addNode(color = red, size = 100)</code>, which
     * allows the user to set many attributes as soon as the node is created.
     * 
     * For more information on how *args and **kwargs work on Jython, refer to
     * the Jython FAQ.
     * 
     * @param args      values for setting nodes' attributes
     * @param keywords  keywords for setting nodes' attributes
     * @return          the newly created node
     * @see <a href="http://www.jython.org/archive/22/userfaq.html#supporting-args-and-kw-in-java-methods">Jython FAQ</a>
     */
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

    /**
     * Creates a new directed edge and adds it to the graph.
     * @param source    the source node of the edge
     * @param target    the target node of the edge
     * @return          the newly created edge
     */
    public GyEdge addDirectedEdge(GyNode source, GyNode target) {
        GyEdge ret = null;
        Edge edge;

        // Instantiates the new edge and adds it to the graph
        edge = namespace.getGraphModel().factory().newEdge(source.getNode(), target.getNode(), 1.0f, true);
        namespace.getGraphModel().getDirectedGraph().addEdge(edge);
        ret = namespace.getGyEdge(edge.getId());

        return ret;
    }

    /**
     * An alias to the <code>addUndirectedEdge</code>, for compatibility with
     * GUESS.
     * @param source    the source node of the edge
     * @param target    the target node of the edge
     * @return          the newly created edge
     * @see #addUndirectedEdge(org.gephi.scripting.wrappers.GyNode, org.gephi.scripting.wrappers.GyNode) 
     */
    public GyEdge addEdge(GyNode source, GyNode target) {
        return addUndirectedEdge(source, target);
    }

    /**
     * Creates a new undirected edge and adds it to the graph.
     * @param source    the source node of the edge
     * @param target    the target node of the edge
     * @return          the newly created edge
     */
    public GyEdge addUndirectedEdge(GyNode source, GyNode target) {
        GyEdge ret = null;
        Edge edge;

        // Instantiates the new edge and adds it to the graph
        edge = namespace.getGraphModel().factory().newEdge(source.getNode(), target.getNode(), 1.0f, false);
        namespace.getGraphModel().getUndirectedGraph().addEdge(edge);
        ret = namespace.getGyEdge(edge.getId());

        return ret;
    }
}
