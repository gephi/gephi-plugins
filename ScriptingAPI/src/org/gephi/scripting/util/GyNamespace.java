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

import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.gephi.scripting.wrappers.GyNode;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyNamespace extends PyStringMap {

    public static final String NODE_PREFIX = "v";
    private GraphModel graphModel;

    public GyNamespace(GraphModel graphModel) {
        this.graphModel = graphModel;
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
            String id = key.substring(NODE_PREFIX.length());
            Node node = graphModel.getGraph().getNode(id);
            if (node != null) {
                ret = new GyNode(node);
            }
        }

        if (ret != null) {
            // Update the namespace binding, in case something was found
            super.__setitem__(key, ret);
        }

        return ret;
    }
}
