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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.AbstractTableModel;
import org.gephi.desktop.neo4j.ui.util.Neo4jUtils;
import org.gephi.neo4j.plugin.api.FilterDescription;
import org.gephi.neo4j.plugin.api.RelationshipDescription;
import org.gephi.neo4j.plugin.api.TraversalOrder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.netbeans.validation.api.ui.ValidationPanel;

/**
 *
 * @author Martin Škurla
 */
public class TraversalImportPanel extends javax.swing.JPanel {

    private final GraphDatabaseService graphDB;
    private final RelationshipsTableModel relationshipsTableModel;
    private int relationshipsSelectedRow;

    public TraversalImportPanel(GraphDatabaseService graphDB) {
        this.graphDB = graphDB;
        this.relationshipsTableModel = new RelationshipsTableModel();

        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        orderButtonGroup = new javax.swing.ButtonGroup();
        maxDepthButtonGroup = new javax.swing.ButtonGroup();
        startNodeButtonGroup = new javax.swing.ButtonGroup();
        relationshipsButtonGroup = new javax.swing.ButtonGroup();
        traversePanel = new javax.swing.JPanel();
        orderPanel = new javax.swing.JPanel();
        breadthFirstOrderRadioButton = new javax.swing.JRadioButton();
        depthFirstOrderRadioButton = new javax.swing.JRadioButton();
        maxDepthPanel = new javax.swing.JPanel();
        concreteMaxDepthRadioButton = new javax.swing.JRadioButton();
        endOfGraphMaxDepthRadioButton = new javax.swing.JRadioButton();
        maxDepthSpinner = new javax.swing.JSpinner();
        relationshipsPanel = new javax.swing.JPanel();
        relationshipTypeLabel = new javax.swing.JLabel();
        directionLabel = new javax.swing.JLabel();
        addRelationshipsButton = new javax.swing.JButton();
        removeRelationshipsButton = new javax.swing.JButton();
        relationshipsScrollPane = new javax.swing.JScrollPane();
        relationshipsTable = new javax.swing.JTable();
        relationshipTypeComboBox = new javax.swing.JComboBox();
        outcomingRelationshipsRadioButton = new javax.swing.JRadioButton();
        bothRelationshipsRadioButton = new javax.swing.JRadioButton();
        incomingRelationshipsRadioButton = new javax.swing.JRadioButton();
        startNodePanel = new javax.swing.JPanel();
        idStartNodeRadioButton = new javax.swing.JRadioButton();
        indexStartNodeRadioButton = new javax.swing.JRadioButton();
        indexValueStartNodeTextField = new javax.swing.JTextField();
        indexKeyStartNodeTextField = new javax.swing.JTextField();
        indexValueStartNodeLabel = new javax.swing.JLabel();
        idStartNodeTextField = new javax.swing.JTextField();
        filterPanel = new org.gephi.desktop.neo4j.ui.TraversalFilterPanel();

        traversePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.traversePanel.border.title"))); // NOI18N

        orderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.orderPanel.border.title"))); // NOI18N

        orderButtonGroup.add(breadthFirstOrderRadioButton);
        breadthFirstOrderRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.breadthFirstOrderRadioButton.text")); // NOI18N

        orderButtonGroup.add(depthFirstOrderRadioButton);
        depthFirstOrderRadioButton.setSelected(true);
        depthFirstOrderRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.depthFirstOrderRadioButton.text")); // NOI18N

        javax.swing.GroupLayout orderPanelLayout = new javax.swing.GroupLayout(orderPanel);
        orderPanel.setLayout(orderPanelLayout);
        orderPanelLayout.setHorizontalGroup(
            orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(orderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(depthFirstOrderRadioButton)
                    .addComponent(breadthFirstOrderRadioButton))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        orderPanelLayout.setVerticalGroup(
            orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(orderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(depthFirstOrderRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(breadthFirstOrderRadioButton)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        maxDepthPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.maxDepthPanel.border.title"))); // NOI18N

        maxDepthButtonGroup.add(concreteMaxDepthRadioButton);
        concreteMaxDepthRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.concreteMaxDepthRadioButton.text")); // NOI18N

        maxDepthButtonGroup.add(endOfGraphMaxDepthRadioButton);
        endOfGraphMaxDepthRadioButton.setSelected(true);
        endOfGraphMaxDepthRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.endOfGraphMaxDepthRadioButton.text")); // NOI18N

        javax.swing.GroupLayout maxDepthPanelLayout = new javax.swing.GroupLayout(maxDepthPanel);
        maxDepthPanel.setLayout(maxDepthPanelLayout);
        maxDepthPanelLayout.setHorizontalGroup(
            maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxDepthPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(maxDepthPanelLayout.createSequentialGroup()
                        .addComponent(concreteMaxDepthRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxDepthSpinner))
                    .addComponent(endOfGraphMaxDepthRadioButton))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        maxDepthPanelLayout.setVerticalGroup(
            maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxDepthPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(concreteMaxDepthRadioButton)
                    .addComponent(maxDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endOfGraphMaxDepthRadioButton)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        relationshipsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.relationshipsPanel.border.title"))); // NOI18N

        relationshipTypeLabel.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.relationshipTypeLabel.text")); // NOI18N

        directionLabel.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.directionLabel.text")); // NOI18N

        addRelationshipsButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.addRelationshipsButton.text")); // NOI18N
        addRelationshipsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRelationshipsButtonActionPerformed(evt);
            }
        });

        removeRelationshipsButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.removeRelationshipsButton.text")); // NOI18N
        removeRelationshipsButton.setEnabled(false);
        removeRelationshipsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRelationshipsButtonActionPerformed(evt);
            }
        });

        relationshipsTable.setModel(relationshipsTableModel);
        relationshipsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        relationshipsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                relationshipsTableMouseClicked(evt);
            }
        });
        relationshipsScrollPane.setViewportView(relationshipsTable);

        relationshipTypeComboBox.setModel(new DefaultComboBoxModel(Neo4jUtils.relationshipTypeNames(graphDB)));

        relationshipsButtonGroup.add(outcomingRelationshipsRadioButton);
        outcomingRelationshipsRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.outcomingRelationshipsRadioButton.text")); // NOI18N

        relationshipsButtonGroup.add(bothRelationshipsRadioButton);
        bothRelationshipsRadioButton.setSelected(true);
        bothRelationshipsRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.bothRelationshipsRadioButton.text")); // NOI18N

        relationshipsButtonGroup.add(incomingRelationshipsRadioButton);
        incomingRelationshipsRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.incomingRelationshipsRadioButton.text")); // NOI18N

        javax.swing.GroupLayout relationshipsPanelLayout = new javax.swing.GroupLayout(relationshipsPanel);
        relationshipsPanel.setLayout(relationshipsPanelLayout);
        relationshipsPanelLayout.setHorizontalGroup(
            relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(directionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(relationshipTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bothRelationshipsRadioButton)
                    .addComponent(outcomingRelationshipsRadioButton)
                    .addComponent(incomingRelationshipsRadioButton)
                    .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(relationshipTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(removeRelationshipsButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addRelationshipsButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(relationshipsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addContainerGap())
        );

        relationshipsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addRelationshipsButton, removeRelationshipsButton});

        relationshipsPanelLayout.setVerticalGroup(
            relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(relationshipsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, relationshipsPanelLayout.createSequentialGroup()
                        .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(directionLabel)
                                    .addComponent(incomingRelationshipsRadioButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(outcomingRelationshipsRadioButton)
                                .addGap(25, 25, 25)
                                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(relationshipTypeLabel)
                                    .addComponent(relationshipTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addComponent(bothRelationshipsRadioButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addRelationshipsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeRelationshipsButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        startNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.startNodePanel.border.title"))); // NOI18N

        startNodeButtonGroup.add(idStartNodeRadioButton);
        idStartNodeRadioButton.setSelected(true);
        idStartNodeRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.idStartNodeRadioButton.text")); // NOI18N
        idStartNodeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idStartNodeRadioButtonActionPerformed(evt);
            }
        });

        startNodeButtonGroup.add(indexStartNodeRadioButton);
        indexStartNodeRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.indexStartNodeRadioButton.text")); // NOI18N
        indexStartNodeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexStartNodeRadioButtonActionPerformed(evt);
            }
        });

        indexValueStartNodeTextField.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalPanel.index value.text")); // NOI18N
        indexValueStartNodeTextField.setEnabled(false);
        indexValueStartNodeTextField.setName("index value"); // NOI18N

        indexKeyStartNodeTextField.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalPanel.index key.text")); // NOI18N
        indexKeyStartNodeTextField.setEnabled(false);
        indexKeyStartNodeTextField.setName("index key"); // NOI18N

        indexValueStartNodeLabel.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalImportPanel.indexValueStartNodeLabel.text")); // NOI18N

        idStartNodeTextField.setText(org.openide.util.NbBundle.getMessage(TraversalImportPanel.class, "TraversalPanel.node id.text")); // NOI18N
        idStartNodeTextField.setName("node id"); // NOI18N

        javax.swing.GroupLayout startNodePanelLayout = new javax.swing.GroupLayout(startNodePanel);
        startNodePanel.setLayout(startNodePanelLayout);
        startNodePanelLayout.setHorizontalGroup(
            startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(startNodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(idStartNodeRadioButton)
                    .addGroup(startNodePanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(indexValueStartNodeLabel))
                    .addComponent(indexStartNodeRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(indexValueStartNodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(indexKeyStartNodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(idStartNodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
                .addContainerGap())
        );
        startNodePanelLayout.setVerticalGroup(
            startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(startNodePanelLayout.createSequentialGroup()
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(startNodePanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(idStartNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(idStartNodeRadioButton)))
                .addGap(3, 3, 3)
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(startNodePanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(indexKeyStartNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(indexStartNodeRadioButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(indexValueStartNodeLabel)
                    .addComponent(indexValueStartNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        filterPanel.setPreferredSize(new java.awt.Dimension(513, 187));

        javax.swing.GroupLayout traversePanelLayout = new javax.swing.GroupLayout(traversePanel);
        traversePanel.setLayout(traversePanelLayout);
        traversePanelLayout.setHorizontalGroup(
            traversePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(traversePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(relationshipsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, traversePanelLayout.createSequentialGroup()
                    .addComponent(startNodePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(orderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(maxDepthPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        traversePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {maxDepthPanel, orderPanel});

        traversePanelLayout.setVerticalGroup(
            traversePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(traversePanelLayout.createSequentialGroup()
                .addGroup(traversePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(maxDepthPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(orderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startNodePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relationshipsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        traversePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {maxDepthPanel, orderPanel, startNodePanel});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(traversePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(traversePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void idStartNodeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idStartNodeRadioButtonActionPerformed
        idStartNodeTextField.setEnabled(true);
        indexKeyStartNodeTextField.setEnabled(false);
        indexValueStartNodeTextField.setEnabled(false);
    }//GEN-LAST:event_idStartNodeRadioButtonActionPerformed

    private void indexStartNodeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexStartNodeRadioButtonActionPerformed
        idStartNodeTextField.setEnabled(false);
        indexKeyStartNodeTextField.setEnabled(true);
        indexValueStartNodeTextField.setEnabled(true);
    }//GEN-LAST:event_indexStartNodeRadioButtonActionPerformed

    private void addRelationshipsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRelationshipsButtonActionPerformed
        String relationshipType = (String) relationshipTypeComboBox.getSelectedItem();

        String direction = null;
        for (Enumeration<AbstractButton> e = relationshipsButtonGroup.getElements(); e.hasMoreElements();) {
            AbstractButton button = e.nextElement();
            if (button.isSelected()) {
                direction = button.getText();
                break;
            }
        }

        relationshipsTableModel.addData(relationshipType, direction);
    }//GEN-LAST:event_addRelationshipsButtonActionPerformed

    private void removeRelationshipsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRelationshipsButtonActionPerformed
        removeRelationshipsButton.setEnabled(false);

        relationshipsTable.removeRowSelectionInterval(relationshipsSelectedRow, relationshipsSelectedRow);
        relationshipsTableModel.removeData(relationshipsSelectedRow);
    }//GEN-LAST:event_removeRelationshipsButtonActionPerformed

    private void relationshipsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_relationshipsTableMouseClicked
        removeRelationshipsButton.setEnabled(true);
        this.relationshipsSelectedRow = relationshipsTable.getSelectedRow();
    }//GEN-LAST:event_relationshipsTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRelationshipsButton;
    private javax.swing.JRadioButton bothRelationshipsRadioButton;
    private javax.swing.JRadioButton breadthFirstOrderRadioButton;
    private javax.swing.JRadioButton concreteMaxDepthRadioButton;
    private javax.swing.JRadioButton depthFirstOrderRadioButton;
    private javax.swing.JLabel directionLabel;
    private javax.swing.JRadioButton endOfGraphMaxDepthRadioButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JRadioButton idStartNodeRadioButton;
    private javax.swing.JTextField idStartNodeTextField;
    private javax.swing.JRadioButton incomingRelationshipsRadioButton;
    private javax.swing.JTextField indexKeyStartNodeTextField;
    private javax.swing.JRadioButton indexStartNodeRadioButton;
    private javax.swing.JLabel indexValueStartNodeLabel;
    private javax.swing.JTextField indexValueStartNodeTextField;
    private javax.swing.ButtonGroup maxDepthButtonGroup;
    private javax.swing.JPanel maxDepthPanel;
    private javax.swing.JSpinner maxDepthSpinner;
    private javax.swing.ButtonGroup orderButtonGroup;
    private javax.swing.JPanel orderPanel;
    private javax.swing.JRadioButton outcomingRelationshipsRadioButton;
    private javax.swing.JComboBox relationshipTypeComboBox;
    private javax.swing.JLabel relationshipTypeLabel;
    private javax.swing.ButtonGroup relationshipsButtonGroup;
    private javax.swing.JPanel relationshipsPanel;
    private javax.swing.JScrollPane relationshipsScrollPane;
    private javax.swing.JTable relationshipsTable;
    private javax.swing.JButton removeRelationshipsButton;
    private javax.swing.ButtonGroup startNodeButtonGroup;
    private javax.swing.JPanel startNodePanel;
    private javax.swing.JPanel traversePanel;
    // End of variables declaration//GEN-END:variables

    public int getMaxDepth() {
        return endOfGraphMaxDepthRadioButton.isSelected() ? Integer.MAX_VALUE
                : (Integer) maxDepthSpinner.getValue();
    }

    public TraversalOrder getOrder() {
        return depthFirstOrderRadioButton.isSelected() ? TraversalOrder.DEPTH_FIRST
                : TraversalOrder.BREADTH_FIRST;
    }

    public long getStartNodeId() {
        if (idStartNodeRadioButton.isSelected()) {
            return Integer.parseInt(idStartNodeTextField.getText().trim());
        } else {
            String key = indexKeyStartNodeTextField.getText().trim();
            String value = indexValueStartNodeTextField.getText().trim();

            for(String index : graphDB.index().nodeIndexNames()) {
                Node n =graphDB.index().forNodes(index).get(key, value).getSingle();
                if(n!=null) {
                    return n.getId();
                }
            }

           return -1;
        }
    }

    public Collection<RelationshipDescription> getRelationshipDescriptions() {
        List<RelationshipDescription> relationshipDescriptions = new LinkedList<RelationshipDescription>();

        for (String[] data : relationshipsTableModel.data) {
            relationshipDescriptions.add(new RelationshipDescription(DynamicRelationshipType.withName(data[0]),
                    Direction.valueOf(data[1].toUpperCase())));
        }

        return relationshipDescriptions;
    }

    public Collection<FilterDescription> getFilterDescriptions() {
        return ((TraversalFilterPanel) filterPanel).getFilterDescriptions();
    }

    public boolean isMatchCaseEnabled() {
        return ((TraversalFilterPanel) filterPanel).isMatchCaseEnabled();
    }

    public boolean isRestrictModeEnabled() {
        return ((TraversalFilterPanel) filterPanel).isRestrictModeEnabled();
    }

    public ValidationPanel createValidationPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);
//        ValidationGroup group = validationPanel.getValidationGroup();

        //Validators
//        group.add(this.idStartNodeTextField, ValidationStrategy.ON_FOCUS_LOSS,       new NodeIdValidator());
//        group.add(this.idStartNodeTextField, ValidationStrategy.ON_CHANGE_OR_ACTION, new NodeIdValidator());

        return validationPanel;
    }
//TODO >>> finish validation
//    private class NodeIdValidator implements Validator<String>{
//        private int counter = 0;
//
//        @Override
//        public boolean validate(Problems problems, String string, String value) {
//            System.out.println("focus lost: " + focusLost);
////            if (idStartNodeRadioButton.isSelected()) {
//             if (!focusLost) {
//                int nodeId;
//
//                try {
//                    nodeId = Integer.parseInt(value);
//                }
//                catch (NumberFormatException nfe) {
//                    problems.add("not number...");
//                    return false;
//                }
//
//                try {
//                    graphDB.getNodeById(nodeId);
//                }
//                catch (NotFoundException nfe) {
//                    problems.add("Node with id '" + nodeId + "' doesn't exist");
//                    return false;
//                }
//            }
//
//            if (focusLost) {
//                counter++;
//
//                if (counter == 2) {
//                    counter = 0;
//                    focusLost = false;
//                }
//            }
//
//            return true;
//        }
//    }

    private static class RelationshipsTableModel extends AbstractTableModel {

        @SuppressWarnings("rawtypes")
        private final Class[] columnTypes = {String.class, String.class};
        private final String[] columnNames = {"Relationship type", "Direction"};
        private final List<String[]> data;

        RelationshipsTableModel() {
            data = new ArrayList<String[]>();
        }

        public void addData(String relationshipType, String direction) {
            data.add(new String[]{relationshipType, direction});

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
