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

import org.gephi.project.api.Workspace;
import org.python.util.PythonInterpreter;

/**
 * Controller that manages the scripting models, one per workspace.
 * 
 * The <code>ScriptingController</code> is also responsible for instantiating
 * and managing the <code>PythonInterpreter</code> object. The
 * <code>PythonInterpreter</code> should be instantiated upon Controller's
 * construction.
 * 
 * The <code>ScriptingController</code> should also update the
 * <code>PythonInterpreter</code>'s current namespace accordingly whenever
 * the current workspace is changed, by implementing a
 * {@link org.gephi.project.api.WorkspaceListener}.
 *
 * @author Luiz Ribeiro
 * @see ScriptingModel
 */
public interface ScriptingController {

    /**
     * Returns the scripting model for the current workspace or
     * <code>null</code>, in case the project is empty.
     * @return          the current scripting model
     */
    public ScriptingModel getModel();

    /**
     * Returns the scripting model for a given <code>workspace</code>.
     * @param workspace the workspace whose model will be returned
     * @return          the <code>workspace</code>'s scripting model
     */
    public ScriptingModel getModel(Workspace workspace);

    /**
     * Returns the Python interpreter instance associated with the controller.
     * @return          the <code>PythonInterpreter</code> instance
     */
    public PythonInterpreter getPythonInterpreter();
}
