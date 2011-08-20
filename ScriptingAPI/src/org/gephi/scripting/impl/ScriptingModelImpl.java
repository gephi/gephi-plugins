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
package org.gephi.scripting.impl;

import org.gephi.project.api.Workspace;
import org.gephi.scripting.api.ScriptingModel;
import org.gephi.scripting.util.GyNamespace;
import org.python.core.PyStringMap;

/**
 * Default implementation of the scripting model.
 * 
 * This implementation only encapsulates the Python's local namespace for the
 * given workspace. It is responsible for instantiating the namespace upon
 * constructor's execution.
 *
 * @author Luiz Ribeiro
 */
public class ScriptingModelImpl implements ScriptingModel {

    /** Python's local namespace for the model's workspace */
    private PyStringMap localNamespace;
    /** The workspace to which this model is related to */
    private Workspace workspace;

    /**
     * Default constructor for the scripting model.
     * @param workspace the workspace to which this model is related to
     */
    public ScriptingModelImpl(Workspace workspace) {
        this.localNamespace = new GyNamespace(workspace);
        this.workspace = workspace;
    }

    @Override
    public final PyStringMap getLocalNamespace() {
        return localNamespace;
    }
}
