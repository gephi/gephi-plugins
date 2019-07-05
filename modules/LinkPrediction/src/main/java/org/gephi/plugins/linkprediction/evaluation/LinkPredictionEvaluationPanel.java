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
    private javax.swing.JComboBox trainWorkspace;
    private javax.swing.JComboBox testWorkspace;

    private javax.swing.JLabel trainLabel;
    private javax.swing.JLabel testLabel;
    private javax.swing.JLabel statisticsLabel;

    private javax.swing.JCheckBox commonNeighbourCheckbox;
    private javax.swing.JCheckBox preferentialAttachmentCheckbox;

    private Map<String, Workspace> workspaces = new HashMap<>();

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionEvaluationPanel.class);


    public LinkPredictionEvaluationPanel() {
        trainLabel = new javax.swing.JLabel("Initial Workspace: ");
        testLabel = new javax.swing.JLabel("Extended Workspace: ");
        statisticsLabel = new javax.swing.JLabel("Algorithms to evaluate: ");

        Workspace[] allWorkspaces = Lookup.getDefault().lookup(ProjectController.class).getCurrentProject()
                .getLookup().lookup(WorkspaceProvider.class).getWorkspaces();

        trainWorkspace = new JComboBox();
        testWorkspace = new JComboBox();

        for (Workspace w : allWorkspaces) {
            String name = w.getLookup().lookup(WorkspaceInformation.class).getName();
            trainWorkspace.addItem(name);
            testWorkspace.addItem(name);

            workspaces.put(name, w);
        }

        commonNeighbourCheckbox = new javax.swing.JCheckBox(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        preferentialAttachmentCheckbox = new javax.swing.JCheckBox(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);

        setLayout(new GridLayout(5, 2));

        add(trainLabel);
        add(trainWorkspace);
        add(testLabel);
        add(testWorkspace);
        add(statisticsLabel);
        add(new JLabel()); // Empty placeholder
        add(commonNeighbourCheckbox);
        add(preferentialAttachmentCheckbox);

        commonNeighbourCheckbox.addItemListener(this);
        preferentialAttachmentCheckbox.addItemListener(this);

    }

    @Override public void itemStateChanged(ItemEvent e) {
        String trainItem = trainWorkspace.getSelectedItem().toString();
        String testItem = testWorkspace.getSelectedItem().toString();

        Workspace trainWS = workspaces.get(trainWorkspace.getSelectedItem().toString());
        Workspace testWS = workspaces.get(testWorkspace.getSelectedItem().toString());

        Graph train = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspaces.get(trainItem)).getUndirectedGraph();
        Graph test = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspaces.get(testItem)).getUndirectedGraph();

        this.evaluation.resetEvaluation();

        LinkPredictionAccuracy cnAccuracy = new LinkPredictionAccuracy(new CommonNeighboursStatistics(), train, test, trainWS, testWS);
        if (commonNeighbourCheckbox.isSelected()) {
            consoleLogger.debug("Add common neighbour to evaluations");
            this.evaluation.addEvaluation(cnAccuracy);
        } else if (!commonNeighbourCheckbox.isSelected()) {
            consoleLogger.debug("Remove common neighbour from evaluations");
            this.evaluation.removeEvaluation(cnAccuracy);
        }

        LinkPredictionAccuracy paAccuracy = new LinkPredictionAccuracy(new PreferentialAttachmentStatistics(), train, test, trainWS, testWS);
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
