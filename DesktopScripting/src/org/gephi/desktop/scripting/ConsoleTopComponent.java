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
package org.gephi.desktop.scripting;

import java.awt.Component;
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
import org.openide.util.Lookup;
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
@ConvertAsProperties(dtd = "-//org.gephi.desktop.scripting//Console//EN",
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
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
