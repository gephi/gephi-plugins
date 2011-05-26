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
import org.gephi.scripting.api.ScriptingController;
import org.gephi.scripting.api.ScriptingModel;
import org.openide.util.lookup.ServiceProvider;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Luiz Ribeiro
 */
@ServiceProvider(service = ScriptingController.class)
public class ScriptingControllerImpl implements ScriptingController {
    
    private ScriptingModelImpl currentModel;
    
    public ScriptingControllerImpl() {
        // TODO
    }

    @Override
    public ScriptingModel getModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ScriptingModel getModel(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PythonInterpreter getPythonInterpreter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
