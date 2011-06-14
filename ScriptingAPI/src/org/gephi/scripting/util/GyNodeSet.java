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

import java.util.Arrays;
import java.util.Iterator;
import org.gephi.scripting.wrappers.GyNode;
import org.python.core.PyObject;
import org.python.core.PyType;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyNodeSet extends GySet {

    public static final PyType _TYPE = PyType.fromClass(GyNodeSet.class);

    public GyNodeSet() {
        this(_TYPE);
        _TYPE.setName("nodeset");
    }

    public GyNodeSet(PyType type) {
        super(type);
    }

    public GyNodeSet(PyObject args[]) {
        this();
        addAll(Arrays.asList(args));
    }
    
    @Override
    public boolean add(Object obj) {
        boolean added = false;

        if (obj instanceof GyNode) {
            added |= super.add(obj);
        } else if (obj instanceof GyNodeSet || (obj instanceof PyObject && ((PyObject) obj).isSequenceType())) {
            for (PyObject iter : ((PyObject) obj).asIterable()) {
                added |= add(iter);
            }
        }

        return added;
    }

    @Override
    public PyObject __rde__(PyObject obj) {
        if (obj instanceof GyNode || obj instanceof GyNodeSet) {
            GyEdgeSet edgeSet = new GyEdgeSet();

            for (Iterator iter = _set.iterator(); iter.hasNext();) {
                PyObject iterObj = (PyObject) iter.next();
                GyEdgeSet ret = (GyEdgeSet) iterObj.__rde__(obj);
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
        if (obj instanceof GyNode || obj instanceof GyNodeSet) {
            GyEdgeSet edgeSet = new GyEdgeSet();

            for (Iterator iter = _set.iterator(); iter.hasNext();) {
                PyObject iterObj = (PyObject) iter.next();
                GyEdgeSet ret = (GyEdgeSet) iterObj.__bde__(obj);
                edgeSet.__ior__(ret);
            }

            return edgeSet;
        }

        return null;
    }
}
