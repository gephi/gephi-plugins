package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.plugins.linkprediction.statistics.*;
import org.gephi.plugins.linkprediction.warnings.IllegalIterationNumberFormatWarning;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.event.*;

/**
 * Statistics panel which will be used with {@link LinkPredictionStatistics} statistics.
 * <p>
 * This panel will be use to specify link prediction algorithm and number of iterations.
 *
 * @author Marco Romanutti
 * @see LinkPredictionStatistics
 */
public class LinkPredictionStatisticsPanel extends javax.swing.JPanel implements ItemListener, KeyListener {

    // UI elements
    private LinkPredictionMacro statistic;
    private javax.swing.JCheckBox commonNeighbourCheckbox;
    private javax.swing.JCheckBox preferentialAttachmentCheckbox;
    private javax.swing.JTextField numberOfIterationsTextField;
    private javax.swing.JLabel commonNeighbourWarning;
    private javax.swing.JLabel preferentialAttachmentWarning;
    private javax.swing.JLabel iterationLabel;

    // Long runtime verification
    public static final double THRESHOLD_N2 = 1000000;
    public static final String HIGH_RUNTIME = "High runtime value";
    private int noOfNodes;
    private double runtimeCommonNeighbours;
    private double runtimePreferentialAttachment;

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionStatisticsPanel.class);

    public LinkPredictionStatisticsPanel() {
        consoleLogger.debug("Initialize panel");

        noOfNodes = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph().getNodeCount();
        consoleLogger.debug("Graph contains " + noOfNodes + "nodes");

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

    /**
     * Actualizes lists with link predictions algorithms which shall be calculated.
     *
     * @param e Event
     */
    public void itemStateChanged(ItemEvent e) {
        if (commonNeighbourCheckbox.isSelected()){
            consoleLogger.debug("Add common neighbour to macro");
            this.statistic.addStatistic(new CommonNeighboursStatistics());
        } else if (!commonNeighbourCheckbox.isSelected()) {
            consoleLogger.debug("Remove common neighbour from macro");
            this.statistic.removeStatistic(new CommonNeighboursStatistics());
        }

        if (preferentialAttachmentCheckbox.isSelected()){
            consoleLogger.debug("Add preferential attachment to macro");
            this.statistic.addStatistic(new PreferentialAttachmentStatistics());
        } else if (!preferentialAttachmentCheckbox.isSelected()) {
            consoleLogger.debug("Add preferential attachment from macro");
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

    /**
     * Updates number of iterations in which prediction will be applied.
     */
    private void updateIterationLimit() {
        int numberOfIterations = 1;
        try {
            numberOfIterations = Integer.valueOf(numberOfIterationsTextField.getText());
            consoleLogger.debug("Number of iteration changed to " + numberOfIterations);

        }
        catch(NumberFormatException e) {
            consoleLogger.debug("Wrong number format entered!");
            new IllegalIterationNumberFormatWarning();
        }

        statistic.setIterationLimit(numberOfIterations);
        getRuntime(numberOfIterations);
        setLabels();
    }

    /**
     * Calculates runtime based on number of iterations.
     *
     * @param noOfIterations Number of iterations
     */
    private void getRuntime(int noOfIterations) {
        // O(N^2)
        runtimeCommonNeighbours = (double) noOfIterations * noOfNodes * noOfNodes;
        // O(N^2)
        runtimePreferentialAttachment = (double) noOfIterations * noOfNodes * noOfNodes;
    }

    /**
     * Sets warning labels in case of high runtime.
     */
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
