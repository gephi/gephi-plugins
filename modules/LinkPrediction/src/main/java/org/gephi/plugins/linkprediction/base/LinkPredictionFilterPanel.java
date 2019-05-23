package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.filters.spi.Filter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Table;
import org.gephi.plugins.linkprediction.exceptions.IllegalIterationLimitException;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.initializeColumns;

/**
 * Filter panel which will be used with {@link LinkPredictionFilter} filter.
 * <p>
 * This base factory class will be used in all specific link prediction filter
 * implementations.
 *
 * @author Marco Romanutti
 * @see LinkPredictionFilter
 */
public class LinkPredictionFilterPanel extends javax.swing.JPanel {

    /** Filter on which panel will set its values */
    private LinkPredictionFilter filter;
    /** Slider to adapt number uf shown edges */
    private javax.swing.JSlider slider;
    /** Label which shows current chosen value of shown edges */
    private javax.swing.JLabel current;

    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionFilterPanel.class);

    public LinkPredictionFilterPanel(Filter filter) {
        this.filter = (LinkPredictionFilter) filter;
        //Init project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = pc.getCurrentWorkspace();

        // Get max value from edges table
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        consoleLogger.debug("Initialize columns");
        Table edgeTable = graph.getModel().getEdgeTable();
        initializeColumns(edgeTable);

        int maxIteration = LinkPredictionStatistics.getMaxIteration(graph, filter.getName());
        // Stats have to be executed first
        if (maxIteration == 0) {
            new IllegalIterationLimitException();
        }

        // Set layout
        setLayout(new GridLayout(2, 1));

        // Add slider
        JPanel topPanel = new JPanel(new BorderLayout());
        // Set init value
        int initValue = maxIteration / 2;
        this.slider = new JSlider(JSlider.HORIZONTAL, 0, maxIteration, initValue);
        topPanel.add(slider);

        // Add current slider value
        JPanel bottomPanel = new JPanel(new BorderLayout());
        // Set init value
        ((LinkPredictionFilter) filter).setEdgesLimit(initValue);
        this.current = new JLabel(String.valueOf(initValue));
        bottomPanel.add(current);

        add(topPanel);
        add(bottomPanel);

        // Add listener
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int edgesLimit = ((JSlider) e.getSource()).getValue();
                consoleLogger.debug("Filter changed to new limit " + edgesLimit);

                // Set current displayed value
                current.setText(String.valueOf(edgesLimit));
                // Set filter value
                ((LinkPredictionFilter) filter).setEdgesLimit(edgesLimit);

            }
        });
    }
}

