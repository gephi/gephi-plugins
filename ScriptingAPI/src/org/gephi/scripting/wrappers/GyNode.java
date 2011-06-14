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

import java.util.Iterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.NodeIterator;
import org.gephi.scripting.util.GyEdgeSet;
import org.gephi.scripting.util.GyNamespace;
import org.gephi.scripting.util.GyNodeSet;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyNode extends PyObject {

    private GyNamespace namespace;
    private Node node;
    // Hack to get a few attributes into jythonconsole's auto-completion
    // TODO: get rid of this ugly hack (:
    public int color;
    public float size;
    public String label;
    public int position;
    public float x;
    public float y;
    public boolean fixed;
    public int indegree;
    public int outdegree;
    public int totaldegree;
    public PyList neighbors;

    public GyNode(GyNamespace namespace, Node node) {
        this.namespace = namespace;
        this.node = node;
    }

    @Override
    public String toString() {
        return GyNamespace.NODE_PREFIX + Integer.toString(node.getId());
    }

    public Node getNode() {
        return node;
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        if (name.equals("color")) {
            int color = (Integer) value.__tojava__(Integer.class);
            float red = ((color >> 16) & 0xFF) / 255.0f;
            float green = ((color >> 8) & 0xFF) / 255.0f;
            float blue = (color & 0xFF) / 255.0f;
            node.getNodeData().setColor(red, green, blue);
        } else if (name.equals("size")) {
            float size = (Float) value.__tojava__(Float.class);
            node.getNodeData().setSize(size);
        } else if (name.equals("label")) {
            node.getNodeData().setLabel(value.toString());
        } else if (name.equals("position")) {
            PyTuple tuple = (PyTuple) value;
            float x = (Float) tuple.__finditem__(0).__tojava__(Float.class);
            float y = (Float) tuple.__finditem__(1).__tojava__(Float.class);
            node.getNodeData().setX(x);
            node.getNodeData().setY(y);
        } else if (name.equals("x")) {
            float x = (Float) value.__tojava__(Float.class);
            node.getNodeData().setX(x);
        } else if (name.equals("y")) {
            float y = (Float) value.__tojava__(Float.class);
            node.getNodeData().setY(y);
        } else if (name.equals("fixed")) {
            boolean fixed = (Boolean) value.__tojava__(Boolean.class);
            node.getNodeData().setFixed(fixed);
        } else if (name.equals("indegree")) {
            readonlyAttributeError(name);
        } else if (name.equals("outdegree")) {
            readonlyAttributeError(name);
        } else if (name.equals("totaldegree")) {
            readonlyAttributeError(name);
        } else if (name.equals("neighbors")) {
            readonlyAttributeError(name);
        } else if (!name.startsWith("__")) {
            Object obj = null;

            // TODO: support conversions for other object types
            if (value instanceof PyString) {
                obj = (String) value.__tojava__(String.class);
            } else if (value instanceof PyInteger) {
                obj = (Integer) value.__tojava__(Integer.class);
            } else if (value instanceof PyFloat) {
                obj = (Float) value.__tojava__(Float.class);
            }

            if (obj == null) {
                throw Py.AttributeError("Unsupported node attribute type '" + value.getType().getName() + "'");
            }

            node.getNodeData().getAttributes().setValue(name, obj);
        } else {
            super.__setattr__(name, value);
        }
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        if (name.equals("color")) {
            int red = (int) Math.round(node.getNodeData().r() * 255.0f);
            int green = (int) Math.round(node.getNodeData().g() * 255.0f);
            int blue = (int) Math.round(node.getNodeData().b() * 255.0f);
            return Py.java2py(new Integer((red << 16) + (green << 8) + blue));
        } else if (name.equals("size")) {
            return Py.java2py(new Float(node.getNodeData().getSize()));
        } else if (name.equals("label")) {
            return Py.java2py(node.getNodeData().getLabel());
        } else if (name.equals("position")) {
            float x = node.getNodeData().x();
            float y = node.getNodeData().y();
            return new PyTuple(new PyFloat(x), new PyFloat(y));
        } else if (name.equals("x")) {
            return new PyFloat(node.getNodeData().x());
        } else if (name.equals("y")) {
            return new PyFloat(node.getNodeData().y());
        } else if (name.equals("fixed")) {
            return new PyBoolean(node.getNodeData().isFixed());
        } else if (name.equals("indegree")) {
            int indegree = namespace.getGraphModel().getDirectedGraph().getInDegree(node);
            return new PyInteger(indegree);
        } else if (name.equals("outdegree")) {
            int outdegree = namespace.getGraphModel().getDirectedGraph().getOutDegree(node);
            return new PyInteger(outdegree);
        } else if (name.equals("totaldegree")) {
            int totaldegree = namespace.getGraphModel().getDirectedGraph().getDegree(node);
            return new PyInteger(totaldegree);
        } else if (name.equals("neighbors")) {
            NodeIterable nodeIterable = namespace.getGraphModel().getGraph().getNeighbors(node);
            GyNodeSet nodesSet = new GyNodeSet();

            for (NodeIterator nodeItr = nodeIterable.iterator(); nodeItr.hasNext();) {
                GyNode node = namespace.getGyNode(nodeItr.next().getId());
                nodesSet.add(node);
            }

            return nodesSet;
        } else if (!name.startsWith("__")) {
            Object obj = node.getNodeData().getAttributes().getValue(name);
            // TODO: return null if there is no column with name
            if (obj == null) {
                return Py.None;
            }
            return Py.java2py(obj);
        } else {
            return super.__findattr_ex__(name);
        }
    }

    @Override
    public PyObject __rde__(PyObject obj) {
        if (obj instanceof GyNode) {
            GyEdgeSet edgeSet = new GyEdgeSet();
            Node target = ((GyNode) obj).getNode();
            Edge edge = namespace.getGraphModel().getMixedGraph().getEdge(node, target);

            if (edge != null && edge.isDirected() && edge.getTarget().equals(target)) {
                edgeSet.add(namespace.getGyEdge(edge.getId()));
            }

            return edgeSet;
        } else if (obj instanceof GyNodeSet) {
            GyEdgeSet edgeSet = new GyEdgeSet();
            GyNodeSet nodeSet = (GyNodeSet) obj;

            for (Iterator iter = nodeSet.iterator(); iter.hasNext();) {
                GyEdgeSet ret = (GyEdgeSet) this.__rde__((PyObject) iter.next());
                edgeSet.__ior__(ret);
            }

            return edgeSet;
        }

        return null;
    }

    @Override
    public PyObject __lde__(PyObject obj) {
        if (obj instanceof GyNode || obj instanceof GyNodeSet) {
            return obj.__rde__(this);
        }

        return null;
    }

    @Override
    public PyObject __bde__(PyObject obj) {
        if (obj instanceof GyNode) {
            GyEdgeSet edgeSet = new GyEdgeSet();
            Node target = ((GyNode) obj).getNode();
            Edge edge = namespace.getGraphModel().getMixedGraph().getEdge(node, target);

            if (edge != null && !edge.isDirected()) {
                edgeSet.add(namespace.getGyEdge(edge.getId()));
            } else {
                edge = namespace.getGraphModel().getMixedGraph().getEdge(target, node);

                if (edge != null && !edge.isDirected()) {
                    edgeSet.add(namespace.getGyEdge(edge.getId()));
                }
            }

            return edgeSet;
        } else if (obj instanceof GyNodeSet) {
            GyEdgeSet edgeSet = new GyEdgeSet();
            GyNodeSet nodeSet = (GyNodeSet) obj;

            for (Iterator iter = nodeSet.iterator(); iter.hasNext();) {
                GyEdgeSet ret = (GyEdgeSet) this.__bde__((PyObject) iter.next());
                edgeSet.__ior__(ret);
            }

            return edgeSet;
        }

        return null;
    }
}
