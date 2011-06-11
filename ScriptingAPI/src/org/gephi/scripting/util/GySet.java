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
import org.python.core.PyObject;
import org.python.core.PySet;
import org.python.core.PyType;

/**
 *
 * @author Luiz Ribeiro
 */
public class GySet extends PySet {

    public static final PyType _TYPE = PyType.fromClass(GySet.class);

    public GySet() {
        this(_TYPE);
    }

    public GySet(PyType type) {
        super(type);
    }

    public GySet(PyObject args[]) {
        this();
        addAll(Arrays.asList(args));
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        return super.__findattr_ex__(name);
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        for (Iterator<PyObject> iter = _set.iterator(); iter.hasNext();) {
            PyObject object = iter.next();
            object.__setattr__(name, value);
        }
    }
}
