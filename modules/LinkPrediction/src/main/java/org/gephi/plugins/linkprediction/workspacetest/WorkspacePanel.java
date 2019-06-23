package org.gephi.plugins.linkprediction.workspacetest;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;


public class WorkspacePanel extends javax.swing.JPanel {

    // UI elements
    private javax.swing.JComboBox workspaces;

    private Workspace[] allWS;


    public WorkspacePanel() {

        allWS = Lookup.getDefault().lookup(ProjectController.class).getCurrentProject()
                .getLookup().lookup(WorkspaceProvider.class).getWorkspaces();

        workspaces = new JComboBox();

        for (Workspace w : allWS ) {
            workspaces.addItem(w.getLookup().lookup(WorkspaceInformation.class).getName());
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(workspaces);


    }



}
