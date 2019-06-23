package org.gephi.plugins.linkprediction.evaluation;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceProvider;
import org.openide.util.Lookup;

import javax.swing.*;

public class LinkPredictionEvaluationPanel extends javax.swing.JPanel {

    // UI elements
    private javax.swing.JComboBox workspaces;

    private Workspace[] allWS;


    public LinkPredictionEvaluationPanel() {

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
