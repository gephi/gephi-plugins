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

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.scripting.util.GyNamespace;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyNode extends PyObject {

    private Graph graph;
    private Node node;
    // Hack to get color and size attributes into jythonconsole's auto-completion
    // TODO: get rid of this ugly hack (:
    public int color;
    public float size;

    public GyNode(Graph graph, Node node) {
        this.graph = graph;
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
        } else {
            return super.__findattr_ex__(name);
        }
    }
}
