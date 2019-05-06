package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatistics;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;

public class LinkPredictionFilterPanel extends javax.swing.JPanel {

    private LinkPredictionFilter filter;
    private javax.swing.JSlider slider;

    private static Logger consoleLogger = LogManager.getLogger(LinkPredictionFilterPanel.class);

    public LinkPredictionFilterPanel(Filter filter) {
        this.filter = (LinkPredictionFilter) filter;

        this.slider = new JSlider(JSlider.HORIZONTAL);
        add(slider);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int edgesLimit = ((JSlider) e.getSource()).getValue();
                consoleLogger.debug("Filter changed to new limit " + edgesLimit);
                ((LinkPredictionFilter) filter).setEdgesLimit(edgesLimit);
            }
        });
    }
}

