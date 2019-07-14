package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.GraphController;
import org.gephi.plugins.linkprediction.statistics.*;
import org.gephi.plugins.linkprediction.warnings.IllegalIterationNumberFormatWarning;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
    private javax.swing.JLabel algorithms;

    // Long runtime verification
    public static final String HIGH_RUNTIME = "Possibly high runtime";
    private int noOfNodes;

    // Input validation
    private boolean warningDisplayed = false;

    // Console logger
    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionStatisticsPanel.class);

    /**
     * Creates new link prediction statistic panel.
     */
    public LinkPredictionStatisticsPanel() {
        consoleLogger.debug("Initialize panel");

        noOfNodes = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph().getNodeCount();
        if (consoleLogger.isDebugEnabled()) {
            consoleLogger.debug("Graph contains " + noOfNodes + "nodes");
        }

        commonNeighbourCheckbox = new javax.swing.JCheckBox(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        preferentialAttachmentCheckbox = new javax.swing.JCheckBox(
                PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        iterationLabel = new javax.swing.JLabel("Iterations: ");

        numberOfIterationsTextField = new javax.swing.JTextField("1");
        commonNeighbourWarning = new javax.swing.JLabel(" ");
        preferentialAttachmentWarning = new javax.swing.JLabel(" ");

        algorithms = new javax.swing.JLabel("Algorithms:");
        algorithms.setToolTipText("Currently only undirected, unweighted graphs are supported");
        Font f = algorithms.getFont();
        algorithms.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));

        commonNeighbourWarning.setForeground(Color.red);
        preferentialAttachmentWarning.setForeground(Color.red);

        // Prevent textfield from expanding
        numberOfIterationsTextField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, numberOfIterationsTextField.getPreferredSize().height));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(algorithms);
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
        if (commonNeighbourCheckbox.isSelected()) {
            consoleLogger.debug("Add common neighbour to macro");
            this.statistic.addStatistic(new CommonNeighboursStatistics());
        } else if (!commonNeighbourCheckbox.isSelected()) {
            consoleLogger.debug("Remove common neighbour from macro");
            this.statistic.removeStatistic(new CommonNeighboursStatistics());
        }

        if (preferentialAttachmentCheckbox.isSelected()) {
            consoleLogger.debug("Add preferential attachment to macro");
            this.statistic.addStatistic(new PreferentialAttachmentStatistics());
        } else if (!preferentialAttachmentCheckbox.isSelected()) {
            consoleLogger.debug("Add preferential attachment from macro");
            this.statistic.removeStatistic(new PreferentialAttachmentStatistics());
        }
    }

    @Override public void keyTyped(KeyEvent e) {
        updateIterationLimit();
    }

    @Override public void keyPressed(KeyEvent e) {
        updateIterationLimit();
    }

    @Override public void keyReleased(KeyEvent e) {
        updateIterationLimit();
    }

    /**
     * Updates number of iterations in which prediction will be applied.
     */
    private void updateIterationLimit() {
        int numberOfIterations = 1;
        try {
            numberOfIterations = Integer.valueOf(numberOfIterationsTextField.getText());
            warningDisplayed = false;
            consoleLogger.debug("Number of iteration changed to " + numberOfIterations);
        } catch (NumberFormatException e) {
            if (!warningDisplayed) {
                warningDisplayed = true;
                consoleLogger.debug("Wrong number format entered!");
                new IllegalIterationNumberFormatWarning();
            }
        }

        statistic.setIterationLimit(numberOfIterations);
        setWarnings(numberOfIterations);
    }

    /**
     * Sets warning labels in case of high runtime.
     */
    private void setWarnings(int numberOfIterations) {
        consoleLogger.debug("Set warning labels");
        LinkPredictionStatistics preferentialAttachment = statistic
                .getStatistic(PreferentialAttachmentStatistics.class);

        if (preferentialAttachment != null && preferentialAttachment
                .longRuntimeExpected(numberOfIterations, noOfNodes)) {
            consoleLogger.debug("Enable high runtime warning for preferential attachment");
            preferentialAttachmentWarning.setText(HIGH_RUNTIME);
        } else {
            consoleLogger.debug("Disable high runtime warning for preferential attachment");
            preferentialAttachmentWarning.setText(" ");
        }

        LinkPredictionStatistics commonNeighbour = statistic.getStatistic(CommonNeighboursStatistics.class);
        if (commonNeighbour != null && commonNeighbour.longRuntimeExpected(numberOfIterations, noOfNodes)) {
            consoleLogger.debug("Enable high runtime warning for common neighbours");
            commonNeighbourWarning.setText(HIGH_RUNTIME);
        } else {
            commonNeighbourWarning.setText("");
            consoleLogger.debug("Disable high runtime warning for common neighbours");
        }
    }

    public LinkPredictionMacro getStatistic() {
        return statistic;
    }

    public void setStatistic(LinkPredictionMacro statistic) {
        this.statistic = statistic;
    }
}
