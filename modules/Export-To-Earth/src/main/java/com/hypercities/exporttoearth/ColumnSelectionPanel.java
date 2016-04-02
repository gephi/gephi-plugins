/**
 * Copyright (c) 2012, David Shepard All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.hypercities.exporttoearth;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Column;
import org.openide.util.NbBundle;

/**
 * UI for attribute selection.
 * 
 * @author Dave Shepard
 */
public class ColumnSelectionPanel extends JPanel {
    
    private Column longitudeColumn;
    private Column latitudeColumn;
    private ArrayList<Column> columnsToExport = new ArrayList<Column>();

    private Map<String, Column> columnNames = new HashMap<String, Column>();
    private Map<JCheckBox, Column> checkBoxesToColumns = new HashMap<JCheckBox, Column>();


    private String getMessage(String resource) {
        return NbBundle.getMessage(ColumnSelectionPanel.class, resource);
    }

    private static final int MIN_EDGE_WIDTH = 1,
                          MAX_EDGE_WIDTH = 20,
                          MIN_NODE_RADIUS = 20,
            MAX_NODE_RADIUS = 100
    ;
    public static final int DEFAULT_EDGE_WIDTH = 2,
                         DEFAULT_NODE_RADIUS = (MAX_NODE_RADIUS - MIN_NODE_RADIUS) / 8 + MIN_NODE_RADIUS
            ;


    private final ActionListener longitudeColumnSelector = new ActionListener() {

        @Override
        /**
         * Expects ActionEvent.ActionCommand will be column name
         */
        public void actionPerformed(ActionEvent ae) {
            longitudeColumn = columnNames.get(ae.getActionCommand());
        }
    };

    private final ActionListener latitudeColumnSelector = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            latitudeColumn = columnNames.get(ae.getActionCommand());
        }
    };

    private final ItemListener columnSelectorResponder = new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent ie) {
            Object source = ie.getItemSelectable();
            for (Map.Entry<JCheckBox, Column> entry : checkBoxesToColumns.entrySet()) {
                Column column = entry.getValue();
                if (source == entry.getKey()) {
                    if (ie.getStateChange() == ItemEvent.DESELECTED) {
                        columnsToExport.remove(column);
                    } else {
                        if (!columnsToExport.contains(column)) {
                            columnsToExport.add(column);
                        }
                    } // end else
                    // break out of loop early
                    break;
                } // end if
            } // end for
        } // end itemStateChanged
    };

    private int maxEdgeWidth = DEFAULT_EDGE_WIDTH;
    private final ChangeListener maxEdgeWithSliderResponder = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent ce) {
            JSlider source = (JSlider)ce.getSource();
            if (!source.getValueIsAdjusting()) {
                maxEdgeWidth = (int)source.getValue();
            }
        }

    };

    private int maxNodeRadius = DEFAULT_NODE_RADIUS;
    private final ChangeListener maxNodeRadiusSliderResponder = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent ce) {
            JSlider source = (JSlider)ce.getSource();
            if (!source.getValueIsAdjusting()) {
                maxNodeRadius = (int)source.getValue();
            }
        }
        
    };

    /**
     * Create panel with specified columns.
     * 
     * @param allColumns
     * @param longitudeColumn Column to pre-select as the longitude column. May be null.
     * @param latitudeColumn Column to pre-select as the latitude column. May be null.
     */
    public ColumnSelectionPanel(Column[] allColumns, 
            Column longitudeColumn, Column latitudeColumn) {
        this.longitudeColumn = longitudeColumn;
        this.latitudeColumn = latitudeColumn;
        columnsToExport = new ArrayList<Column>();

        columnNames = new HashMap<String, Column>();
        checkBoxesToColumns = new HashMap<JCheckBox, Column>();

        JPanel columnSelectionPane = new JPanel();
        columnSelectionPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        // for each column, create a new label, checkbox, lat radio button and lon radio button
        GridLayout layout = new GridLayout(0, 3);
        columnSelectionPane.setLayout(layout);
        columnSelectionPane.add(new JLabel(getMessage("IncludeColumn")));
        columnSelectionPane.add(new JLabel(getMessage("Longitude")));
        columnSelectionPane.add(new JLabel(getMessage("Latitude")));

        ButtonGroup lonGroup = new ButtonGroup();
        ButtonGroup latGroup = new ButtonGroup();
        for (Column column : allColumns) {
            String title = column.getTitle();
            columnNames.put(title, column);
            
            JCheckBox checkbox = new JCheckBox(title);
            checkBoxesToColumns.put(checkbox, column);
            checkbox.addItemListener(columnSelectorResponder);
            if (column != longitudeColumn && column != latitudeColumn) {
                checkbox.setSelected(true);
            }
            columnSelectionPane.add(checkbox);

            JRadioButton lonButton = new JRadioButton();
            lonButton.setActionCommand(title);
            lonButton.addActionListener(longitudeColumnSelector);
            if (column == longitudeColumn) {
                lonButton.setSelected(true);
            }
            columnSelectionPane.add(lonButton);
            lonGroup.add(lonButton);

            JRadioButton latButton = new JRadioButton();
            latButton.addActionListener(latitudeColumnSelector);
            latButton.setActionCommand(title);
            if (column == latitudeColumn) {
                latButton.setSelected(true);
            }
            columnSelectionPane.add(latButton);
            latGroup.add(latButton);
        }
        add(columnSelectionPane);

        // Width and Edge sliders
        JPanel sliderPanel = new JPanel();
        BoxLayout sliderLayout = new BoxLayout(sliderPanel, BoxLayout.Y_AXIS);
        sliderPanel.setLayout(sliderLayout);
        sliderPanel.add(new JLabel(getMessage("MaxNodeRadius")));
        JSlider nodeRadius = new JSlider(JSlider.HORIZONTAL, MIN_NODE_RADIUS, MAX_NODE_RADIUS, maxNodeRadius);
        nodeRadius.addChangeListener(maxNodeRadiusSliderResponder);
        nodeRadius.setMajorTickSpacing(10);
        nodeRadius.setPaintTicks(true);
        nodeRadius.setPaintLabels(true);
        sliderPanel.add(nodeRadius);
        sliderPanel.add(new JLabel(getMessage("MaxEdgeWidth")));
        JSlider edgeWidth = new JSlider(JSlider.HORIZONTAL, MIN_EDGE_WIDTH, MAX_EDGE_WIDTH, maxEdgeWidth);
        edgeWidth.addChangeListener(maxEdgeWithSliderResponder);
        edgeWidth.setMajorTickSpacing(5);
        edgeWidth.setPaintTicks(true);
        edgeWidth.setPaintLabels(true);
        sliderPanel.add(edgeWidth);
        add(sliderPanel);
    }

    public Column getLongitudeColumn() {
        return longitudeColumn;
    }

    public Column getLatitudeColumn() {
        return latitudeColumn;
    }

    public Column[] getColumnsToExport() {
        return columnsToExport.toArray(new Column[columnsToExport.size()]);
    }

    int getMaxNodeRadius() {
        return maxNodeRadius;
    }

    int getMaxEdgeWidth() {
        return maxEdgeWidth;
    }
}
