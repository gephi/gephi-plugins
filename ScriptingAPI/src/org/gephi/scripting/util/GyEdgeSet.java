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
import org.python.core.PyObject;
import org.python.core.PyType;

/**
 *
 * @author Luiz Ribeiro
 */
public class GyEdgeSet extends GySet {

    public static final PyType _TYPE = PyType.fromClass(GyEdgeSet.class);

    public GyEdgeSet() {
        this(_TYPE);
        _TYPE.setName("edgeset");
    }

    public GyEdgeSet(PyType type) {
        super(type);
    }

    public GyEdgeSet(PyObject args[]) {
        this();
        addAll(Arrays.asList(args));
    }
}
