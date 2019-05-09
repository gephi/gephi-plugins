package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.plugins.linkprediction.statistics.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class LinkPredictionStatisticsPanel extends javax.swing.JPanel implements ItemListener, ActionListener {

    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionStatisticsPanel.class);

    private LinkPredictionMacro statistic;
    private javax.swing.JCheckBox commonNeighbourCheckbox;
    private javax.swing.JCheckBox preferentialAttachmentCheckbox;
    private javax.swing.JTextField numberOfIterationsTextField;

    public LinkPredictionStatisticsPanel() {
        commonNeighbourCheckbox = new javax.swing.JCheckBox(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        preferentialAttachmentCheckbox = new javax.swing.JCheckBox(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        this.numberOfIterationsTextField = new javax.swing.JTextField("1");

        add(commonNeighbourCheckbox);
        add(preferentialAttachmentCheckbox);
        add(numberOfIterationsTextField);

        commonNeighbourCheckbox.addItemListener(this);
        preferentialAttachmentCheckbox.addItemListener(this);
        numberOfIterationsTextField.addActionListener(this);
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

    @Override public void actionPerformed(ActionEvent e) {
        int numberOfIterations = Integer.valueOf(numberOfIterationsTextField.getText());
        statistic.setIterationLimit(numberOfIterations);
    }

    public LinkPredictionMacro getStatistic() {
        return statistic;
    }

    public void setStatistic(LinkPredictionMacro statistic) {
        this.statistic = statistic;
    }
}
