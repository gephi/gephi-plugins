package org.gephi.plugins.linkprediction.evaluation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.plugins.linkprediction.statistics.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceProvider;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

public class LinkPredictionEvaluationPanel extends javax.swing.JPanel implements ItemListener {

    //
    private LinkPredictionEvaluation evaluation;

    // UI elements
    private javax.swing.JComboBox initialWorkspace;
    private javax.swing.JComboBox validationWorkspace;

    private javax.swing.JLabel initialLabel;
    private javax.swing.JLabel validationLabel;
    private javax.swing.JLabel statisticsLabel;

    private javax.swing.JCheckBox commonNeighbourCheckbox;
    private javax.swing.JCheckBox preferentialAttachmentCheckbox;

    private Map<String, Workspace> workspaces = new HashMap<>();

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionEvaluationPanel.class);


    public LinkPredictionEvaluationPanel() {
        initialLabel = new javax.swing.JLabel("Initial Workspace: ");
        validationLabel = new javax.swing.JLabel("Validation Workspace: ");
        statisticsLabel = new javax.swing.JLabel("Algorithms to evaluate: ");

        Workspace[] allWorkspaces = Lookup.getDefault().lookup(ProjectController.class).getCurrentProject()
                .getLookup().lookup(WorkspaceProvider.class).getWorkspaces();

        initialWorkspace = new JComboBox();
        validationWorkspace = new JComboBox();

        for (Workspace w : allWorkspaces) {
            String name = w.getLookup().lookup(WorkspaceInformation.class).getName();
            initialWorkspace.addItem(name);
            validationWorkspace.addItem(name);

            workspaces.put(name, w);
        }

        commonNeighbourCheckbox = new javax.swing.JCheckBox(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        preferentialAttachmentCheckbox = new javax.swing.JCheckBox(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);

        setLayout(new GridLayout(5, 2));

        add(initialLabel);
        add(initialWorkspace);
        add(validationLabel);
        add(validationWorkspace);
        add(statisticsLabel);
        add(new JLabel()); // Empty placeholder
        add(commonNeighbourCheckbox);
        add(preferentialAttachmentCheckbox);

        commonNeighbourCheckbox.addItemListener(this);
        preferentialAttachmentCheckbox.addItemListener(this);

    }

    @Override public void itemStateChanged(ItemEvent e) {
        String initialItem = initialWorkspace.getSelectedItem().toString();
        String validationItem = validationWorkspace.getSelectedItem().toString();

        Workspace initialWS = workspaces.get(initialWorkspace.getSelectedItem().toString());
        Workspace validationWS = workspaces.get(validationWorkspace.getSelectedItem().toString());

        Graph initial = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspaces.get(initialItem)).getUndirectedGraph();
        Graph validation = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspaces.get(validationItem)).getUndirectedGraph();

        LinkPredictionAccuracy cnAccuracy = new LinkPredictionAccuracy(new CommonNeighboursStatistics(), initial, validation, initialWS, validationWS);
        if (commonNeighbourCheckbox.isSelected()) {
            consoleLogger.debug("Add common neighbour to evaluations");
            this.evaluation.addEvaluation(cnAccuracy);
        } else if (!commonNeighbourCheckbox.isSelected()) {
            consoleLogger.debug("Remove common neighbour from evaluations");
            this.evaluation.removeEvaluation(cnAccuracy);
        }

        LinkPredictionAccuracy paAccuracy = new LinkPredictionAccuracy(new PreferentialAttachmentStatistics(), initial, validation, initialWS, validationWS);
        if (preferentialAttachmentCheckbox.isSelected()) {
            consoleLogger.debug("Add preferential attachment to evaluations");
            this.evaluation.addEvaluation(paAccuracy);
        } else if (!preferentialAttachmentCheckbox.isSelected()) {
            consoleLogger.debug("Add preferential attachment from evaluations");
            this.evaluation.removeEvaluation(paAccuracy);
        }
    }

    public void setEvaluation(LinkPredictionEvaluation evaluation) {
        this.evaluation = evaluation;
    }

}
