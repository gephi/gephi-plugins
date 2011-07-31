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
import org.gephi.scripting.util.GyNamespace;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PySet;
import org.python.core.PyString;
import org.python.core.PyTuple;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyNode extends PyObject {

    private GyNamespace namespace;
    private Node underlyingNode;
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
    public int degree;
    public PyList neighbors;

    public GyNode(GyNamespace namespace, Node node) {
        this.namespace = namespace;
        this.underlyingNode = node;
    }

    @Override
    public String toString() {
        return GyNamespace.NODE_PREFIX + Integer.toString(underlyingNode.getId());
    }

    public Node getNode() {
        return underlyingNode;
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        if (name.equals("color")) {
            int color = (Integer) value.__tojava__(Integer.class);
            float red = ((color >> 16) & 0xFF) / 255.0f;
            float green = ((color >> 8) & 0xFF) / 255.0f;
            float blue = (color & 0xFF) / 255.0f;
            underlyingNode.getNodeData().setColor(red, green, blue);
        } else if (name.equals("size")) {
            float size = (Float) value.__tojava__(Float.class);
            underlyingNode.getNodeData().setSize(size);
        } else if (name.equals("label")) {
            underlyingNode.getNodeData().setLabel(value.toString());
        } else if (name.equals("position")) {
            PyTuple tuple = (PyTuple) value;
            float x = (Float) tuple.__finditem__(0).__tojava__(Float.class);
            float y = (Float) tuple.__finditem__(1).__tojava__(Float.class);
            underlyingNode.getNodeData().setX(x);
            underlyingNode.getNodeData().setY(y);
        } else if (name.equals("x")) {
            float x = (Float) value.__tojava__(Float.class);
            underlyingNode.getNodeData().setX(x);
        } else if (name.equals("y")) {
            float y = (Float) value.__tojava__(Float.class);
            underlyingNode.getNodeData().setY(y);
        } else if (name.equals("fixed")) {
            boolean fixed = (Boolean) value.__tojava__(Boolean.class);
            underlyingNode.getNodeData().setFixed(fixed);
        } else if (name.equals("indegree")) {
            readonlyAttributeError(name);
        } else if (name.equals("outdegree")) {
            readonlyAttributeError(name);
        } else if (name.equals("degree")) {
            readonlyAttributeError(name);
        } else if (name.equals("neighbors")) {
            readonlyAttributeError(name);
        } else if (!name.startsWith("__")) {
            Object obj = null;

            // TODO: support conversions for other object types
            if (value instanceof PyString) {
                obj = (String) value.__tojava__(String.class);
            } else if (value instanceof PyBoolean) {
                obj = (Boolean) value.__tojava__(Boolean.class);
            } else if (value instanceof PyInteger) {
                obj = (Integer) value.__tojava__(Integer.class);
            } else if (value instanceof PyFloat) {
                obj = (Float) value.__tojava__(Float.class);
            }

            if (obj == null) {
                throw Py.AttributeError("Unsupported node attribute type '" + value.getType().getName() + "'");
            }

            underlyingNode.getNodeData().getAttributes().setValue(name, obj);
        } else {
            super.__setattr__(name, value);
        }
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        if (name.equals("color")) {
            int red = (int) Math.round(underlyingNode.getNodeData().r() * 255.0f);
            int green = (int) Math.round(underlyingNode.getNodeData().g() * 255.0f);
            int blue = (int) Math.round(underlyingNode.getNodeData().b() * 255.0f);
            return Py.java2py(new Integer((red << 16) + (green << 8) + blue));
        } else if (name.equals("size")) {
            return Py.java2py(new Float(underlyingNode.getNodeData().getSize()));
        } else if (name.equals("label")) {
            return Py.java2py(underlyingNode.getNodeData().getLabel());
        } else if (name.equals("position")) {
            float x = underlyingNode.getNodeData().x();
            float y = underlyingNode.getNodeData().y();
            return new PyTuple(new PyFloat(x), new PyFloat(y));
        } else if (name.equals("x")) {
            return new PyFloat(underlyingNode.getNodeData().x());
        } else if (name.equals("y")) {
            return new PyFloat(underlyingNode.getNodeData().y());
        } else if (name.equals("fixed")) {
            return new PyBoolean(underlyingNode.getNodeData().isFixed());
        } else if (name.equals("indegree")) {
            int indegree = namespace.getGraphModel().getDirectedGraph().getInDegree(underlyingNode);
            return new PyInteger(indegree);
        } else if (name.equals("outdegree")) {
            int outdegree = namespace.getGraphModel().getDirectedGraph().getOutDegree(underlyingNode);
            return new PyInteger(outdegree);
        } else if (name.equals("degree")) {
            int degree = namespace.getGraphModel().getDirectedGraph().getDegree(underlyingNode);
            return new PyInteger(degree);
        } else if (name.equals("neighbors")) {
            NodeIterable nodeIterable = namespace.getGraphModel().getGraph().getNeighbors(underlyingNode);
            PySet nodesSet = new PySet();

            for (NodeIterator nodeItr = nodeIterable.iterator(); nodeItr.hasNext();) {
                GyNode node = namespace.getGyNode(nodeItr.next().getId());
                nodesSet.add(node);
            }

            return nodesSet;
        } else if (!name.startsWith("__")) {
            Object obj = underlyingNode.getNodeData().getAttributes().getValue(name);
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
            PySet edgeSet = new PySet();
            Node target = ((GyNode) obj).getNode();
            Edge edge = namespace.getGraphModel().getMixedGraph().getEdge(underlyingNode, target);

            if (edge != null && edge.isDirected() && edge.getTarget().equals(target)) {
                edgeSet.add(namespace.getGyEdge(edge.getId()));
            }

            return edgeSet;
        } else if (obj instanceof PySet) {
            PySet edgeSet = new PySet();
            PySet nodeSet = (PySet) obj;

            for (Iterator iter = nodeSet.iterator(); iter.hasNext();) {
                PySet ret = (PySet) this.__rde__((PyObject) iter.next());
                edgeSet.__ior__(ret);
            }

            return edgeSet;
        }

        return null;
    }

    @Override
    public PyObject __lde__(PyObject obj) {
        if (obj instanceof GyNode || obj instanceof PySet) {
            return obj.__rde__(this);
        }

        return null;
    }

    @Override
    public PyObject __bde__(PyObject obj) {
        if (obj instanceof GyNode) {
            PySet edgeSet = new PySet();
            Node target = ((GyNode) obj).getNode();
            Edge edge = namespace.getGraphModel().getMixedGraph().getEdge(underlyingNode, target);

            if (edge != null && !edge.isDirected()) {
                edgeSet.add(namespace.getGyEdge(edge.getId()));
            } else {
                edge = namespace.getGraphModel().getMixedGraph().getEdge(target, underlyingNode);

                if (edge != null && !edge.isDirected()) {
                    edgeSet.add(namespace.getGyEdge(edge.getId()));
                }
            }

            return edgeSet;
        } else if (obj instanceof PySet) {
            PySet edgeSet = new PySet();
            PySet nodeSet = (PySet) obj;

            for (Iterator iter = nodeSet.iterator(); iter.hasNext();) {
                PySet ret = (PySet) this.__bde__((PyObject) iter.next());
                edgeSet.__ior__(ret);
            }

            return edgeSet;
        }

        return null;
    }

    @Override
    public PyObject __anye__(PyObject obj) {
        if (obj instanceof GyNode || obj instanceof PySet) {
            PySet edgeSet = new PySet();

            edgeSet.__ior__(this.__lde__(obj));
            edgeSet.__ior__(this.__rde__(obj));
            edgeSet.__ior__(this.__bde__(obj));

            return edgeSet;
        }

        return null;
    }
}
