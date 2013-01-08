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

import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import org.gephi.desktop.neo4j.Neo4jMenuAction;
import org.gephi.neo4j.plugin.api.DebugTarget;
import org.gephi.neo4j.plugin.api.MutableNeo4jDelegateNodeDebugger;
import org.gephi.neo4j.plugin.api.Neo4jVisualDebugger;
import org.gephi.neo4j.plugin.api.NoMoreElementsException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Škurla
 */
public class DebugPanel extends javax.swing.JPanel {

    private final MutableNeo4jDelegateNodeDebugger neo4jDebugger;
    private final Neo4jVisualDebugger neo4jVisualDebugger =
            Lookup.getDefault().lookup(Neo4jVisualDebugger.class);

    public DebugPanel(MutableNeo4jDelegateNodeDebugger neo4jDebugger) {
        this.neo4jDebugger = neo4jDebugger;

        neo4jVisualDebugger.initialize();

        initComponents();
        initComponentsByDebuggerInfo();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetButtonGroup = new javax.swing.ButtonGroup();
        nextButton = new javax.swing.JButton();
        targetPanel = new javax.swing.JPanel();
        nodesRadioButton = new javax.swing.JRadioButton();
        pathsRadioButton = new javax.swing.JRadioButton();
        visualizationPanel = new javax.swing.JPanel();
        showNodesCheckBox = new javax.swing.JCheckBox();
        showRelationshipsCheckBox = new javax.swing.JCheckBox();
        relationshipsColorButton = new javax.swing.JButton();
        nodesColorButton = new javax.swing.JButton();

        nextButton.setText(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.nextButton.text")); // NOI18N
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        targetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.targetPanel.border.title"))); // NOI18N

        targetButtonGroup.add(nodesRadioButton);
        nodesRadioButton.setText(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.nodesRadioButton.text")); // NOI18N
        nodesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodesRadioButtonActionPerformed(evt);
            }
        });

        targetButtonGroup.add(pathsRadioButton);
        pathsRadioButton.setText(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.pathsRadioButton.text")); // NOI18N
        pathsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathsRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout targetPanelLayout = new javax.swing.GroupLayout(targetPanel);
        targetPanel.setLayout(targetPanelLayout);
        targetPanelLayout.setHorizontalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nodesRadioButton)
                    .addComponent(pathsRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        targetPanelLayout.setVerticalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addComponent(nodesRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pathsRadioButton)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        visualizationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.visualizationPanel.border.title"))); // NOI18N

        showNodesCheckBox.setText(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.showNodesCheckBox.text")); // NOI18N
        showNodesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNodesCheckBoxActionPerformed(evt);
            }
        });

        showRelationshipsCheckBox.setText(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.showRelationshipsCheckBox.text")); // NOI18N
        showRelationshipsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showRelationshipsCheckBoxActionPerformed(evt);
            }
        });

        relationshipsColorButton.setText(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.relationshipsColorButton.text")); // NOI18N
        relationshipsColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relationshipsColorButtonActionPerformed(evt);
            }
        });

        nodesColorButton.setText(org.openide.util.NbBundle.getMessage(DebugPanel.class, "DebugPanel.nodesColorButton.text")); // NOI18N
        nodesColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodesColorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout visualizationPanelLayout = new javax.swing.GroupLayout(visualizationPanel);
        visualizationPanel.setLayout(visualizationPanelLayout);
        visualizationPanelLayout.setHorizontalGroup(
            visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visualizationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(showRelationshipsCheckBox)
                    .addComponent(showNodesCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(nodesColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(relationshipsColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        visualizationPanelLayout.setVerticalGroup(
            visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visualizationPanelLayout.createSequentialGroup()
                .addGroup(visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showNodesCheckBox)
                    .addComponent(nodesColorButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showRelationshipsCheckBox)
                    .addComponent(relationshipsColorButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(targetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(visualizationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(286, Short.MAX_VALUE)
                .addComponent(nextButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(targetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(visualizationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void initComponentsByDebuggerInfo() {
        switch (neo4jDebugger.getDebugTarget()) {
            case NODES:
                nodesRadioButton.setSelected(true);
                break;

            case PATHS:
                pathsRadioButton.setSelected(true);
                break;
        }

        setNodesColor(neo4jDebugger.getNodesColor());
        setRelationshipsColor(neo4jDebugger.getRelationshipsColor());

        showNodesCheckBox.setSelected(neo4jDebugger.isShowNodes());
        nodesColorButton.setEnabled(neo4jDebugger.isShowNodes());

        showRelationshipsCheckBox.setSelected(neo4jDebugger.isShowRelationships());
        relationshipsColorButton.setEnabled(neo4jDebugger.isShowRelationships());
    }

    private void setNodesColor(Color nodesColor) {
        neo4jDebugger.setNodesColor(nodesColor);
        nodesColorButton.setBackground(nodesColor);
    }

    private void setRelationshipsColor(Color relationshipsColor) {
        neo4jDebugger.setRelationshipsColor(relationshipsColor);
        relationshipsColorButton.setBackground(relationshipsColor);
    }

    private void nodesColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodesColorButtonActionPerformed
        String colorDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_NodesColorChooserDialogTitle");
        JColorChooser nodesColorChooser = new JColorChooser(neo4jDebugger.getNodesColor());

        DialogDescriptor colorDialog = new DialogDescriptor(nodesColorChooser, colorDialogTitle);
        int dialogResult = (Integer) DialogDisplayer.getDefault().notify(colorDialog);

        if (dialogResult == JOptionPane.OK_OPTION) {
            setNodesColor(nodesColorChooser.getColor());

            neo4jVisualDebugger.update(neo4jDebugger);
        }
    }//GEN-LAST:event_nodesColorButtonActionPerformed

    private void relationshipsColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relationshipsColorButtonActionPerformed
        String colorDialogTitle = NbBundle.getMessage(Neo4jMenuAction.class, "CTL_Neo4j_RelationshipsColorChooserDialogTitle");
        JColorChooser relationshipsColorChooser = new JColorChooser(neo4jDebugger.getRelationshipsColor());

        DialogDescriptor colorDialog = new DialogDescriptor(relationshipsColorChooser, colorDialogTitle);
        int dialogResult = (Integer) DialogDisplayer.getDefault().notify(colorDialog);

        if (dialogResult == JOptionPane.OK_OPTION) {
            setRelationshipsColor(relationshipsColorChooser.getColor());

            neo4jVisualDebugger.update(neo4jDebugger);
        }
    }//GEN-LAST:event_relationshipsColorButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        try {
            neo4jVisualDebugger.nextStep(neo4jDebugger);
        } catch (NoMoreElementsException nmee) {
            nextButton.setEnabled(false);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void showNodesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNodesCheckBoxActionPerformed
        nodesColorButton.setEnabled(showNodesCheckBox.isSelected());

        neo4jDebugger.setShowNodes(showNodesCheckBox.isSelected());

        neo4jVisualDebugger.update(neo4jDebugger);
    }//GEN-LAST:event_showNodesCheckBoxActionPerformed

    private void showRelationshipsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showRelationshipsCheckBoxActionPerformed
        relationshipsColorButton.setEnabled(showRelationshipsCheckBox.isSelected());

        neo4jDebugger.setShowRelationships(showRelationshipsCheckBox.isSelected());

        neo4jVisualDebugger.update(neo4jDebugger);
    }//GEN-LAST:event_showRelationshipsCheckBoxActionPerformed

    private void nodesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodesRadioButtonActionPerformed
        showRelationshipsCheckBox.setEnabled(false);
        relationshipsColorButton.setEnabled(false);

        neo4jDebugger.setDebugTarget(DebugTarget.NODES);

        neo4jVisualDebugger.update(neo4jDebugger);
    }//GEN-LAST:event_nodesRadioButtonActionPerformed

    private void pathsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathsRadioButtonActionPerformed
        showRelationshipsCheckBox.setEnabled(true);

        if (showRelationshipsCheckBox.isSelected()) {
            relationshipsColorButton.setEnabled(true);
        }

        neo4jDebugger.setDebugTarget(DebugTarget.PATHS);

        neo4jVisualDebugger.update(neo4jDebugger);
    }//GEN-LAST:event_pathsRadioButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton nextButton;
    private javax.swing.JButton nodesColorButton;
    private javax.swing.JRadioButton nodesRadioButton;
    private javax.swing.JRadioButton pathsRadioButton;
    private javax.swing.JButton relationshipsColorButton;
    private javax.swing.JCheckBox showNodesCheckBox;
    private javax.swing.JCheckBox showRelationshipsCheckBox;
    private javax.swing.ButtonGroup targetButtonGroup;
    private javax.swing.JPanel targetPanel;
    private javax.swing.JPanel visualizationPanel;
    // End of variables declaration//GEN-END:variables
}
