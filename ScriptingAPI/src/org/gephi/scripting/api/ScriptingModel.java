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
package org.gephi.scripting.api;

import org.python.core.PyStringMap;

/**
 * Interface for storing Scripting API data.
 * 
 * Scripting models are responsible for instantiating and keeping a local
 * namespace for each workspace. Note that <code>ScriptingModel</code> are
 * instantiated by the <code>ScriptingController</code>, one per workspace.
 * 
 * Namespaces in Jython are essentially <code>String</code> to
 * <code>PyObject</code> maps, which represent bindings from variable bindings
 * to the actual objects. Namespaces implementations are always extended from
 * Jython's <code>PyStringMap</code> type.
 *
 * @author Luiz Ribeiro
 * @see ScriptingController
 */
public interface ScriptingModel {

    /**
     * Returns the local namespace associated to this scripting model.
     * @return          the local namespace
     */
    public PyStringMap getLocalNamespace();
}
