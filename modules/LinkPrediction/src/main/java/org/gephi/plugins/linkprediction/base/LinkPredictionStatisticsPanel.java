package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.plugins.linkprediction.statistics.*;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.event.*;

public class LinkPredictionStatisticsPanel extends javax.swing.JPanel implements ItemListener, KeyListener {

    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionStatisticsPanel.class);

    private LinkPredictionMacro statistic;
    private javax.swing.JCheckBox commonNeighbourCheckbox;
    private javax.swing.JCheckBox preferentialAttachmentCheckbox;
    private javax.swing.JTextField numberOfIterationsTextField;
    private javax.swing.JLabel commonNeighbourWarning;
    private javax.swing.JLabel preferentialAttachmentWarning;
    private javax.swing.JLabel iterationLabel;

    private int noOfNodes;
    private double runtimeCommonNeighbours;
    private double runtimePreferentialAttachment;

    private final double THRESHOLD_N2 = 1000000;

    private final String HIGH_RUNTIME = "High runtime value";

    public LinkPredictionStatisticsPanel() {
        noOfNodes = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph().getNodeCount();

        commonNeighbourCheckbox = new javax.swing.JCheckBox(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        preferentialAttachmentCheckbox = new javax.swing.JCheckBox(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        numberOfIterationsTextField = new javax.swing.JTextField("1");
        commonNeighbourWarning = new javax.swing.JLabel(" ");
        preferentialAttachmentWarning = new javax.swing.JLabel(" ");
        iterationLabel = new javax.swing.JLabel("Iterations: ");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(commonNeighbourCheckbox);
        add(commonNeighbourWarning);
        add(preferentialAttachmentCheckbox);
        add(preferentialAttachmentWarning);
        add(iterationLabel);
        add(numberOfIterationsTextField);

        commonNeighbourCheckbox.addItemListener(this);
        preferentialAttachmentCheckbox.addItemListener(this);
        numberOfIterationsTextField.addKeyListener(this);
    }

    public void itemStateChanged(ItemEvent e) {
        if (commonNeighbourCheckbox.isSelected()){
            this.statistic.addStatistic(new CommonNeighboursStatistics());
        } else if (!commonNeighbourCheckbox.isSelected()) {
            this.statistic.removeStatistic(new CommonNeighboursStatistics());
        }

        if (preferentialAttachmentCheckbox.isSelected()){
            this.statistic.addStatistic(new PreferentialAttachmentStatistics());
        } else if (!preferentialAttachmentCheckbox.isSelected()) {
            this.statistic.removeStatistic(new PreferentialAttachmentStatistics());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        updateIterationLimit();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        updateIterationLimit();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        updateIterationLimit();
    }

    private void updateIterationLimit() {
        int numberOfIterations = 1;
        try {
            numberOfIterations = Integer.valueOf(numberOfIterationsTextField.getText());
        }
        catch(NumberFormatException e) {
            numberOfIterationsTextField.setText("1");
            numberOfIterations = Integer.valueOf(numberOfIterationsTextField.getText());
        }

        statistic.setIterationLimit(numberOfIterations);
        getRuntime(numberOfIterations);
        setLabels();
    }

    private void getRuntime(int noOfIterations) {
        // O(N^2)
        runtimeCommonNeighbours =  noOfIterations * noOfNodes * noOfNodes;
        // O(N^2)
        runtimePreferentialAttachment = noOfIterations * noOfNodes * noOfNodes;
    }

    private void setLabels() {
        if (runtimePreferentialAttachment > THRESHOLD_N2) {
            preferentialAttachmentWarning.setText(HIGH_RUNTIME);
        } else {
            preferentialAttachmentWarning.setText("");
        }

        if (runtimeCommonNeighbours > THRESHOLD_N2) {
            commonNeighbourWarning.setText(HIGH_RUNTIME);
        } else {
            commonNeighbourWarning.setText("");
        }
    }

    public LinkPredictionMacro getStatistic() {
        return statistic;
    }

    public void setStatistic(LinkPredictionMacro statistic) {
        this.statistic = statistic;
    }
}
