/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.neo4j.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.AbstractTableModel;
import org.gephi.neo4j.plugin.api.FilterDescription;
import org.gephi.neo4j.plugin.api.FilterOperator;
import org.netbeans.validation.api.ui.ValidationPanel;

/**
 *
 * @author Martin Škurla
 */
public class TraversalFilterPanel extends javax.swing.JPanel {

    private final FilterTableModel filterTableModel;
    private int filterSelectedRow;

    public TraversalFilterPanel() {
        filterTableModel = new FilterTableModel();
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterScrollPane = new javax.swing.JScrollPane();
        filterTable = new javax.swing.JTable();
        propertyKeyLabel = new javax.swing.JLabel();
        propertyKeyFilterTextField = new javax.swing.JTextField();
        propertyValueLabel = new javax.swing.JLabel();
        propertyValueFilterTextField = new javax.swing.JTextField();
        operatorLabel = new javax.swing.JLabel();
        operatorComboBox = new javax.swing.JComboBox();
        addFilterButton = new javax.swing.JButton();
        removeFilterButton = new javax.swing.JButton();
        restrictModeCheckBox = new javax.swing.JCheckBox();
        matchCaseCheckBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.border.title"))); // NOI18N
        setPreferredSize(new java.awt.Dimension(513, 187));

        filterTable.setModel(filterTableModel);
        filterTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                filterTableMouseClicked(evt);
            }
        });
        filterScrollPane.setViewportView(filterTable);

        propertyKeyLabel.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.propertyKeyLabel.text")); // NOI18N

        propertyKeyFilterTextField.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.propertyKeyFilterTextField.text")); // NOI18N

        propertyValueLabel.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.propertyValueLabel.text")); // NOI18N

        propertyValueFilterTextField.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.propertyValueFilterTextField.text")); // NOI18N

        operatorLabel.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.operatorLabel.text")); // NOI18N

        operatorComboBox.setModel(new DefaultComboBoxModel(FilterOperator.getTextRepresentations()));

        addFilterButton.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.addFilterButton.text")); // NOI18N
        addFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFilterButtonActionPerformed(evt);
            }
        });

        removeFilterButton.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.removeFilterButton.text")); // NOI18N
        removeFilterButton.setEnabled(false);
        removeFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFilterButtonActionPerformed(evt);
            }
        });

        restrictModeCheckBox.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.restrictModeCheckBox.text")); // NOI18N

        matchCaseCheckBox.setText(org.openide.util.NbBundle.getMessage(TraversalFilterPanel.class, "TraversalFilterPanel.matchCaseCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(propertyKeyLabel)
                            .addComponent(operatorLabel)
                            .addComponent(propertyValueLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(addFilterButton)
                            .addComponent(propertyKeyFilterTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .addComponent(propertyValueFilterTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .addComponent(operatorComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 118, Short.MAX_VALUE)))
                    .addComponent(removeFilterButton))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(filterScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(restrictModeCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(matchCaseCheckBox)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addFilterButton, removeFilterButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(filterScrollPane, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(propertyKeyLabel)
                            .addComponent(propertyKeyFilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(propertyValueLabel)
                            .addComponent(propertyValueFilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(operatorLabel)
                            .addComponent(operatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addFilterButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFilterButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(matchCaseCheckBox)
                    .addComponent(restrictModeCheckBox)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFilterButtonActionPerformed
        filterTableModel.addData(propertyKeyFilterTextField.getText().trim(),
                (String) operatorComboBox.getSelectedItem(),
                propertyValueFilterTextField.getText().trim());
}//GEN-LAST:event_addFilterButtonActionPerformed

    private void removeFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFilterButtonActionPerformed
        removeFilterButton.setEnabled(false);

        filterTable.removeRowSelectionInterval(filterSelectedRow, filterSelectedRow);
        filterTableModel.removeData(filterSelectedRow);
}//GEN-LAST:event_removeFilterButtonActionPerformed

    private void filterTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterTableMouseClicked
        removeFilterButton.setEnabled(true);
        this.filterSelectedRow = filterTable.getSelectedRow();
}//GEN-LAST:event_filterTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFilterButton;
    private javax.swing.JScrollPane filterScrollPane;
    private javax.swing.JTable filterTable;
    private javax.swing.JCheckBox matchCaseCheckBox;
    private javax.swing.JComboBox operatorComboBox;
    private javax.swing.JLabel operatorLabel;
    private javax.swing.JTextField propertyKeyFilterTextField;
    private javax.swing.JLabel propertyKeyLabel;
    private javax.swing.JTextField propertyValueFilterTextField;
    private javax.swing.JLabel propertyValueLabel;
    private javax.swing.JButton removeFilterButton;
    private javax.swing.JCheckBox restrictModeCheckBox;
    // End of variables declaration//GEN-END:variables

    public Collection<FilterDescription> getFilterDescriptions() {
        List<FilterDescription> filterDescriptions = new LinkedList<FilterDescription>();

        for (String[] data : filterTableModel.data) {
            filterDescriptions.add(new FilterDescription(data[0], FilterOperator.fromTextRepresentation(data[1]), data[2]));
        }

        return filterDescriptions;
    }

    public boolean isMatchCaseEnabled() {
        return matchCaseCheckBox.isSelected();
    }

    public boolean isRestrictModeEnabled() {
        return restrictModeCheckBox.isSelected();
    }

    public ValidationPanel createValidationPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        return validationPanel;
    }

    private static class FilterTableModel extends AbstractTableModel {

        @SuppressWarnings("rawtypes")
        private final Class[] columnTypes = {String.class, String.class, String.class};
        private final String[] columnNames = {"Property key", "Operator", "Property value"};
        private final List<String[]> data;

        FilterTableModel() {
            data = new ArrayList<String[]>();
        }

        public void addData(String key, String operator, String value) {
            data.add(new String[]{key, operator, value});

            fireTableDataChanged();
        }

        public void removeData(int index) {
            data.remove(index);

            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnTypes.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex)[columnIndex];
        }
    }
}
