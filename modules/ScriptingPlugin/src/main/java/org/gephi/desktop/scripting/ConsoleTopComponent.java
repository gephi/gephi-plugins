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
package org.gephi.desktop.scripting;

import java.awt.Component;
import java.io.File;
import javax.swing.JPanel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.scripting.api.ScriptingController;
import org.gephi.scripting.api.ScriptingModel;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Top component which displays the scripting console.
 * 
 * Note that this class is responsible for instantiating jythonconsole from the
 * PythonInterpreter that is referenced from the ScriptingController. Each
 * workspace has it's own jythonconsole instance.
 * 
 * @author Luiz Ribeiro
 */
@ConvertAsProperties(dtd = "-//org.gephi.ScriptingPlugin//Console//EN",
autostore = false)
@TopComponent.Description(preferredID = "ConsoleTopComponent",
iconBase = "org/gephi/desktop/scripting/resources/icon.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = true, roles = {"overview", "datalab"})
@ActionID(category = "Window", id = "org.gephi.desktop.scripting.ConsoleTopComponent")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ConsoleAction",
preferredID = "ConsoleTopComponent")
public final class ConsoleTopComponent extends TopComponent {

    public ConsoleTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ConsoleTopComponent.class, "CTL_ConsoleTopComponent"));
        //setToolTipText(NbBundle.getMessage(ConsoleTopComponent.class, "HINT_ConsoleTopComponent"));

        // Setup a WorkspaceListener for listening to Workspace selection events.
        // Whenever a Workspace is selected, the JScrollPane is updated with
        // the corresponding jythonconsole.
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                updateCurrentConsole(workspace);
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                jScrollPane1.setViewportView(new JPanel());
            }
        });

        // Setup a jythonconsole for the current workspace, if there is one
        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        if (currentWorkspace != null) {
            updateCurrentConsole(currentWorkspace);
        }
    }

    /**
     * This method is called from within the component to change the
     * jythonconsole instance that is being currently used.
     * @param workspace the workspace that owns the new jythonconsole
     */
    private void updateCurrentConsole(Workspace workspace) {
        PyObject jythonConsole = workspace.getLookup().lookup(PyObject.class);

        if (jythonConsole == null) {
            // This workspace doesn't have an associated jythonconsole yet, create it
            jythonConsole = newJythonConsole(workspace);
            workspace.add(jythonConsole);
        }

        // Show the right jythonconsole on the scroll pane
        Component jythonConsoleComponent = (Component) jythonConsole.__getattr__("text_pane").__tojava__(Component.class);
        jScrollPane1.setViewportView(jythonConsoleComponent);

        // Hack to redirect sys.stdout to the current workspace's jythonconsole
        ScriptingController scriptingController = Lookup.getDefault().lookup(ScriptingController.class);
        PythonInterpreter pythonInterpreter = scriptingController.getPythonInterpreter();
        pythonInterpreter.getSystemState().__setattr__("stdout", jythonConsole.__getattr__("stdout"));
    }

    /**
     * Instantiates a new jythonconsole for the given workspace.
     * @param workspace the workspace that owns the new jythonconsole
     * @return a jythonconsole instance
     */
    private PyObject newJythonConsole(Workspace workspace) {
        ScriptingController scriptingController = Lookup.getDefault().lookup(ScriptingController.class);
        PythonInterpreter pyi = scriptingController.getPythonInterpreter();
        ScriptingModel scriptingModel = scriptingController.getModel(workspace);

        // put internal path for jythonconsole into the Jython's PATH variable
        File pyDir = InstalledFileLocator.getDefault().locate("py", "org.gephi.scripting.plugin", false);
        pyi.getSystemState().path.append(Py.newString(pyDir.getAbsolutePath()));

        // put path for custom user libraries into Jython's PATH
        pyi.getSystemState().path.append(Py.newString(System.getProperty("netbeans.user") + File.separatorChar + "scripts"));

        // instantiate a new jythonconsole
        pyi.exec("from jythonconsole.console import Console");
        PyObject jythonConsoleClass = pyi.get("Console");
        PyObject console = jythonConsoleClass.__call__(scriptingModel.getLocalNamespace());

        // Stores a reference to the console's stdout redirector into an attribute
        // (used later for redirecting stdout to the correct jythonconsole)
        console.__setattr__("stdout", pyi.getSystemState().__getattr__("stdout"));

        return console;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}
