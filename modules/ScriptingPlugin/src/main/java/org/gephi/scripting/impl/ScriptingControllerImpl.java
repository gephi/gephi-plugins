/*
Copyright 2008-2012 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
