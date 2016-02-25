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

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
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
public class AttributeColumnSelectionPanel extends JPanel {
    
    // Fields that will be exposed.
    private Column longitudeColumn;
    private Column latitudeColumn;
    private ArrayList<Column> columnsToExport = new ArrayList<Column>();

    private Map<String, Column> columnNames = new HashMap<String, Column>();
    private Map<JCheckBox, Column> checkBoxesToColumns = new HashMap<JCheckBox, Column>();


    private ActionListener longitudeColumnSelector = new ActionListener() {

        @Override
        /**
         * Expects ActionEvent.ActionCommand will be column name
         */
        public void actionPerformed(ActionEvent ae) {
            longitudeColumn = columnNames.get(ae.getActionCommand());
        }
    };

    private ActionListener latitudeColumnSelector = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            latitudeColumn = columnNames.get(ae.getActionCommand());
        }
    };

    private ItemListener columnSelectorResponder = new ItemListener() {

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

    private static BundleAccessor bundle = BundleAccessor.forClass(AttributeColumnSelectionPanel.class);

    private int maxEdgeWidth = 10;
    private ChangeListener maxEdgeWithSliderResponder = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent ce) {
            JSlider source = (JSlider)ce.getSource();
            if (!source.getValueIsAdjusting()) {
                maxEdgeWidth = (int)source.getValue();
            }
        }

    };

    private int maxNodeRadius = 80;
    private ChangeListener maxNodeRadiusSliderResponder = new ChangeListener() {

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
    public AttributeColumnSelectionPanel(Column[] allColumns, 
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
//        setLayout(layout);
        columnSelectionPane.setLayout(layout);
        //add(new JLabel("Column name"));
//        columnSelectionPane.add(new JLabel("Include column?"));
        columnSelectionPane.add(new JLabel(bundle.get("IncludeColumn")));
//        columnSelectionPane.add(new JLabel("Longitude"));
        columnSelectionPane.add(new JLabel(bundle.get("Longitude")));
//        columnSelectionPane.add(new JLabel("Latitude"));
        columnSelectionPane.add(new JLabel(bundle.get("Latitude")));

        for (Column column : allColumns) {
            String title = column.getTitle();
            columnNames.put(title, column);
            JLabel label = new JLabel(title);
            //add(label);
            
            JCheckBox checkbox = new JCheckBox(title);
            checkBoxesToColumns.put(checkbox, column);
            checkbox.addItemListener(columnSelectorResponder);
            if (column != longitudeColumn && column != latitudeColumn) {
                checkbox.setSelected(true);
            }
            columnSelectionPane.add(checkbox);

            JRadioButton lonButton = new JRadioButton();
            lonButton.addActionListener(longitudeColumnSelector);
            if (column == longitudeColumn) {
                lonButton.setSelected(true);
            }
            columnSelectionPane.add(lonButton);

            JRadioButton latButton = new JRadioButton();
            latButton.addActionListener(latitudeColumnSelector);
            latButton.setActionCommand(title);
            if (column == latitudeColumn) {
                latButton.setSelected(true);
            }
            columnSelectionPane.add(latButton);

        }
        add(columnSelectionPane);
        // checkboxes are stored in a hash of objects, item to column name

        // Width and Edge sliders
        JPanel sliderPanel = new JPanel();
        BoxLayout sliderLayout = new BoxLayout(sliderPanel, BoxLayout.Y_AXIS);
        sliderPanel.setLayout(sliderLayout);
//        sliderPanel.add(new JLabel("Max node radius"));
        sliderPanel.add(new JLabel(bundle.get("MaxNodeRadius")));
        JSlider nodeRadius = new JSlider(JSlider.HORIZONTAL, 20, 100, maxNodeRadius);
        nodeRadius.addChangeListener(maxNodeRadiusSliderResponder);
        nodeRadius.setMajorTickSpacing(10);
        nodeRadius.setPaintTicks(true);
        nodeRadius.setPaintLabels(true);
        sliderPanel.add(nodeRadius);
//        sliderPanel.add(new JLabel("Max edge width"));
        sliderPanel.add(new JLabel(bundle.get("MaxNodeRadius")));
        JSlider edgeWidth = new JSlider(JSlider.HORIZONTAL, 1, 20, maxEdgeWidth);
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
