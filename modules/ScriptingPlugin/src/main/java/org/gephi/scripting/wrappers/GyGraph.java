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

//import org.gephi.data.attributes.api.AttributeColumn;
//import org.gephi.data.attributes.api.AttributeModel;

import org.gephi.graph.api.GraphModel; // new import
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.scripting.util.GyNamespace;
import org.python.core.Py;
import org.python.core.PyDictionary;
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
        
        //Default size not too small:
        node.setSize(10);
        //Set random position to the node:
        node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
        node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
        
        namespace.getGraphModel().getGraph().addNode(node);
        ret = namespace.getGyNode((String) node.getId());

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
        Edge edge;

        // Instantiates the new edge and adds it to the graph
        edge = namespace.getGraphModel().factory().newEdge(source.getNode(), target.getNode(), (int) 1.0f, true);
        namespace.getGraphModel().getDirectedGraph().addEdge(edge);
        GyEdge ret = namespace.getGyEdge((String) edge.getId());

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
        Edge edge;

        // Instantiates the new edge and adds it to the graph
        edge = namespace.getGraphModel().factory().newEdge(source.getNode(), target.getNode(), (int) 1.0f, false);
        namespace.getGraphModel().getUndirectedGraph().addEdge(edge);
        GyEdge ret = namespace.getGyEdge((String) edge.getId());

        return ret;
    }

    /**
     * Returns true if the graph is directed by default.
     * This value is an indicator of the current state and it means that so far
     * all edges are directed in the graph.
     * @return          true if the graph is only directed or false otherwise
     */
    public boolean isDirected() {
        return namespace.getGraphModel().isDirected();
    }

    /**
     * Returns true if the graph is undirected by default.
     * This value is an indicator of the current state and it means that so far
     * all edges are undirected in the graph.
     * @return          true if the graph is only undirected or false otherwise
     */
    public boolean isUndirected() {
        return namespace.getGraphModel().isUndirected();
    }

    /**
     * Returns true if the graph is mixed by default.
     * This value is an indicator of the current state and it means that
     * directed and undirected edges has been added to the graph. When it
     * returns true, isDirected() and isUndirected() methods always returns
     * false.
     * @return          true if the graph is mixed or false otherwise
     */
    public boolean isMixed() {
        return namespace.getGraphModel().isMixed();
    }

    public PyDictionary getNodeAttributes() {
        GraphModel graphModel = namespace.getGraphModel();
        PyDictionary dict = new PyDictionary();
        for (Column column : graphModel.getNodeTable().toArray()) {
            dict.put(Py.java2py(column.getTitle()), new GyAttributeColumn(namespace, column));
        }

        return dict;
    }

    public PyDictionary getEdgeAttributes() {
        GraphModel graphModel = namespace.getGraphModel();
        PyDictionary dict = new PyDictionary();
        for (Column column : graphModel.getEdgeTable().toArray()) {
            dict.put(Py.java2py(column.getTitle()), new GyAttributeColumn(namespace, column));
        }

        return dict;
    }
}
