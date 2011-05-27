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

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.scripting.api.ScriptingController;
import org.gephi.scripting.api.ScriptingModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 *
 * @author Luiz Ribeiro
 */
@ServiceProvider(service = ScriptingController.class)
public class ScriptingControllerImpl implements ScriptingController {

    private ScriptingModelImpl currentModel;
    private PythonInterpreter pythonInterpreter;

    public ScriptingControllerImpl() {
        // Setup a WorkspaceListener
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
                workspace.add(new ScriptingModelImpl());
            }

            @Override
            public void select(Workspace workspace) {
                currentModel = (ScriptingModelImpl) getModel(workspace);

                // create a new namespace, if necessary
                if (currentModel == null) {
                    currentModel = new ScriptingModelImpl();
                    workspace.add(currentModel);
                }

                // Update the local namespace of the interpreter
                pythonInterpreter.setLocals(currentModel.getLocalNamespace());
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                currentModel = null;
            }
        });

        // Setup a model for the current workspace if needed
        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        if (currentWorkspace != null) {
            currentModel = (ScriptingModelImpl) getModel(currentWorkspace);
            if (currentModel == null) {
                currentModel = new ScriptingModelImpl();
                currentWorkspace.add(currentModel);
            }
        }

        // Setup the Controller's Python Interpreter
        PySystemState.initialize();
        pythonInterpreter = new PythonInterpreter();

        // Set the local namespace of the interpreter to current workspace's
        if (currentModel != null) {
            pythonInterpreter.setLocals(currentModel.getLocalNamespace());
        }
    }

    @Override
    public final ScriptingModel getModel() {
        return currentModel;
    }

    @Override
    public final ScriptingModel getModel(Workspace workspace) {
        return (ScriptingModel) workspace.getLookup().lookup(ScriptingModel.class);
    }

    @Override
    public final PythonInterpreter getPythonInterpreter() {
        return pythonInterpreter;
    }
}
