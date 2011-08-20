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
 * Default implementation of the scripting controller.
 * 
 * This implementation manages the ScriptingModel instances, one for each
 * workspace, and also the PythonInterpreter instance. New models are created
 * as needed, as soon as a new workspace is selected. The interpreter, on the
 * other hand, is instantiated upon constructor's execution and, therefore,
 * there is only one PythonInterpreter instance for the entire application.
 *
 * @author Luiz Ribeiro
 */
@ServiceProvider(service = ScriptingController.class)
public class ScriptingControllerImpl implements ScriptingController {

    /** Model for the current workspace */
    private ScriptingModelImpl currentModel;
    /** The application's python interpreter */
    private PythonInterpreter pythonInterpreter;

    public ScriptingControllerImpl() {
        // Setup a WorkspaceListener
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                currentModel = (ScriptingModelImpl) getModel(workspace);

                // Update the local namespace of the interpreter
                pythonInterpreter.setLocals(currentModel.getLocalNamespace());
                ScriptingControllerImpl.this.preloadGlobals();
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

        // Load the model for the current workspace
        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        if (currentWorkspace != null) {
            currentModel = (ScriptingModelImpl) getModel(currentWorkspace);
        }

        // Setup the Controller's Python Interpreter
        PySystemState.initialize();
        pythonInterpreter = new PythonInterpreter();

        // Set the local namespace of the interpreter to current workspace's
        if (currentModel != null) {
            pythonInterpreter.setLocals(currentModel.getLocalNamespace());
            preloadGlobals();
        }
    }

    @Override
    public final ScriptingModel getModel() {
        return currentModel;
    }

    @Override
    public final synchronized ScriptingModel getModel(Workspace workspace) {
        ScriptingModel scriptingModel = (ScriptingModel) workspace.getLookup().lookup(ScriptingModel.class);

        // create a model for the workspace, if needed
        if (scriptingModel == null) {
            scriptingModel = new ScriptingModelImpl(workspace);
            workspace.add(scriptingModel);
        }

        return scriptingModel;
    }

    @Override
    public final PythonInterpreter getPythonInterpreter() {
        return pythonInterpreter;
    }

    /**
     * This function loads global functions into a newly created scripting
     * model.
     * 
     * This is called just after the instantiation of a new ScriptingModel, so
     * that the globals are loaded on the corresponding namespace.
     */
    private void preloadGlobals() {
        // FIXME: this should be called only once, just after loading up a newly
        // created ScriptingModel's namespace on the interpreter.
        pythonInterpreter.execfile(getClass().getResourceAsStream("/org/gephi/scripting/util/preload.py"));
    }
}
