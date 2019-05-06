package org.gephi.plugins.linkprediction.base;

import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;

public class LinkPredictionFilterPanel extends javax.swing.JPanel {

    private LinkPredictionFilter filter;
    private javax.swing.JSlider slider;

    public LinkPredictionFilterPanel(Filter filter) {
        this.filter = (LinkPredictionFilter) filter;

        this.slider = new JSlider(JSlider.HORIZONTAL);
        add(slider);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int edgesLimit = ((JSlider) e.getSource()).getValue();
                ((LinkPredictionFilter) filter).setEdgesLimit(edgesLimit);
            }
        });
    }
}

