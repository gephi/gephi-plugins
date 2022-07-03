package org.gephi.plugins.linkprediction.base;

import java.util.logging.Level;
import java.util.logging.Logger;
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

    // Long runtime validation
    public static final String HIGH_RUNTIME = "Possibly high runtime";
    private int nodeCount;

    // Input validation
    private boolean warningDisplayed = false;

    // Console logger
    private static Logger consoleLogger = Logger.getLogger(LinkPredictionStatisticsPanel.class.getName());

    /**
     * Creates new link prediction statistic panel.
     */
    public LinkPredictionStatisticsPanel() {
        consoleLogger.log(Level.FINE,"Initialize panel");

        // Get number of nodes
        nodeCount = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph().getNodeCount();
        if (consoleLogger.isLoggable(Level.FINE)) {
            consoleLogger.log(Level.FINE,"Graph contains " + nodeCount + "nodes");
        }

        // Initialize checkboxes
        commonNeighbourCheckbox = new javax.swing.JCheckBox(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME);
        preferentialAttachmentCheckbox = new javax.swing.JCheckBox(
                PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        iterationLabel = new javax.swing.JLabel("Iterations: ");

        // Initialize iteration number sections
        numberOfIterationsTextField = new javax.swing.JTextField("1");
        commonNeighbourWarning = new javax.swing.JLabel(" ");
        preferentialAttachmentWarning = new javax.swing.JLabel(" ");

        // Initialize algorithm section
        algorithms = new javax.swing.JLabel("Algorithms:");
        algorithms.setToolTipText("Currently only undirected, unweighted graphs are supported");
        Font f = algorithms.getFont();
        algorithms.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));

        // Set warning colour
        commonNeighbourWarning.setForeground(Color.red);
        preferentialAttachmentWarning.setForeground(Color.red);

        // Prevent textfield from expanding
        numberOfIterationsTextField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, numberOfIterationsTextField.getPreferredSize().height));

        // Set layout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add components
        add(algorithms);
        add(commonNeighbourCheckbox);
        add(commonNeighbourWarning);
        add(preferentialAttachmentCheckbox);
        add(preferentialAttachmentWarning);
        add(iterationLabel);
        add(numberOfIterationsTextField);

        // Ad listeners
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
            consoleLogger.log(Level.FINE,"Add common neighbour to macro");
            this.statistic.addStatistic(new CommonNeighboursStatistics());
        } else if (!commonNeighbourCheckbox.isSelected()) {
            consoleLogger.log(Level.FINE,"Remove common neighbour from macro");
            this.statistic.removeStatistic(new CommonNeighboursStatistics());
        }

        if (preferentialAttachmentCheckbox.isSelected()) {
            consoleLogger.log(Level.FINE,"Add preferential attachment to macro");
            this.statistic.addStatistic(new PreferentialAttachmentStatistics());
        } else if (!preferentialAttachmentCheckbox.isSelected()) {
            consoleLogger.log(Level.FINE,"Add preferential attachment from macro");
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
        // Default number of iteration
        int numberOfIterations = 1;
        try {
            numberOfIterations = Integer.valueOf(numberOfIterationsTextField.getText());
            warningDisplayed = false;
            consoleLogger.log(Level.FINE, () -> "Number of iteration changed to " + Integer.valueOf(numberOfIterationsTextField.getText()));
        } catch (NumberFormatException e) {
            if (!warningDisplayed) {
                // Display warning only once
                warningDisplayed = true;
                consoleLogger.log(Level.FINE,"Wrong number format entered!");
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
        consoleLogger.log(Level.FINE,"Set warning labels");

        // Preferential attachment
        LinkPredictionStatistics preferentialAttachment = statistic
                .getStatistic(PreferentialAttachmentStatistics.class);
        if (preferentialAttachment != null && PreferentialAttachmentStatistics.complexity
                .longRuntimeExpected(numberOfIterations, nodeCount)) {
            consoleLogger.log(Level.FINE,"Enable high runtime warning for preferential attachment");
            preferentialAttachmentWarning.setText(HIGH_RUNTIME);
        } else {
            consoleLogger.log(Level.FINE,"Disable high runtime warning for preferential attachment");
            preferentialAttachmentWarning.setText(" ");
        }

        // Common neighbour
        LinkPredictionStatistics commonNeighbour = statistic.getStatistic(CommonNeighboursStatistics.class);
        if (commonNeighbour != null && CommonNeighboursStatistics.complexity.longRuntimeExpected(numberOfIterations, nodeCount)) {
            consoleLogger.log(Level.FINE,"Enable high runtime warning for common neighbours");
            commonNeighbourWarning.setText(HIGH_RUNTIME);
        } else {
            commonNeighbourWarning.setText("");
            consoleLogger.log(Level.FINE,"Disable high runtime warning for common neighbours");
        }
    }

    public LinkPredictionMacro getStatistic() {
        return statistic;
    }

    public void setStatistic(LinkPredictionMacro statistic) {
        this.statistic = statistic;
    }
}
