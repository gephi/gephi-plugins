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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.gephi.neo4j.plugin.api.ClassNotFulfillRequirementsException;
import org.gephi.neo4j.plugin.api.FileSystemClassLoader;
import org.gephi.neo4j.plugin.api.Neo4jDelegateNodeDebugger;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Škurla
 */
public class DebugFileChooserComponent extends javax.swing.JPanel implements PropertyChangeListener {

    private final JFileChooser fileChooser;

    public DebugFileChooserComponent(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        fileChooser.addPropertyChangeListener(this);

        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetButtonGroup = new javax.swing.ButtonGroup();
        targetPanel = new javax.swing.JPanel();
        nodesTargetRadioButton = new javax.swing.JRadioButton();
        pathsTargetRadioButton = new javax.swing.JRadioButton();
        visualizationPanel = new javax.swing.JPanel();
        relationshipsVisualizationCheckBox = new javax.swing.JCheckBox();
        nodesVisualizationCheckBox = new javax.swing.JCheckBox();

        targetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DebugFileChooserComponent.class, "DebugFileChooserComponent.targetPanel.border.title"))); // NOI18N

        targetButtonGroup.add(nodesTargetRadioButton);
        nodesTargetRadioButton.setText(org.openide.util.NbBundle.getMessage(DebugFileChooserComponent.class, "DebugFileChooserComponent.nodesTargetRadioButton.text")); // NOI18N
        nodesTargetRadioButton.setEnabled(false);

        targetButtonGroup.add(pathsTargetRadioButton);
        pathsTargetRadioButton.setText(org.openide.util.NbBundle.getMessage(DebugFileChooserComponent.class, "DebugFileChooserComponent.pathsTargetRadioButton.text")); // NOI18N
        pathsTargetRadioButton.setEnabled(false);

        javax.swing.GroupLayout targetPanelLayout = new javax.swing.GroupLayout(targetPanel);
        targetPanel.setLayout(targetPanelLayout);
        targetPanelLayout.setHorizontalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nodesTargetRadioButton)
                    .addComponent(pathsTargetRadioButton))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        targetPanelLayout.setVerticalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addComponent(nodesTargetRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pathsTargetRadioButton))
        );

        visualizationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DebugFileChooserComponent.class, "DebugFileChooserComponent.visualizationPanel.border.title"))); // NOI18N

        relationshipsVisualizationCheckBox.setText(org.openide.util.NbBundle.getMessage(DebugFileChooserComponent.class, "DebugFileChooserComponent.relationshipsVisualizationCheckBox.text")); // NOI18N
        relationshipsVisualizationCheckBox.setEnabled(false);

        nodesVisualizationCheckBox.setText(org.openide.util.NbBundle.getMessage(DebugFileChooserComponent.class, "DebugFileChooserComponent.nodesVisualizationCheckBox.text")); // NOI18N
        nodesVisualizationCheckBox.setEnabled(false);

        javax.swing.GroupLayout visualizationPanelLayout = new javax.swing.GroupLayout(visualizationPanel);
        visualizationPanel.setLayout(visualizationPanelLayout);
        visualizationPanelLayout.setHorizontalGroup(
            visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visualizationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(relationshipsVisualizationCheckBox)
                    .addComponent(nodesVisualizationCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        visualizationPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nodesVisualizationCheckBox, relationshipsVisualizationCheckBox});

        visualizationPanelLayout.setVerticalGroup(
            visualizationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visualizationPanelLayout.createSequentialGroup()
                .addComponent(nodesVisualizationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relationshipsVisualizationCheckBox))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(targetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(visualizationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(targetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(visualizationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton nodesTargetRadioButton;
    private javax.swing.JCheckBox nodesVisualizationCheckBox;
    private javax.swing.JRadioButton pathsTargetRadioButton;
    private javax.swing.JCheckBox relationshipsVisualizationCheckBox;
    private javax.swing.ButtonGroup targetButtonGroup;
    private javax.swing.JPanel targetPanel;
    private javax.swing.JPanel visualizationPanel;
    // End of variables declaration//GEN-END:variables

    private void updateComponents(Neo4jDelegateNodeDebugger neo4jDebugger) {
        switch (neo4jDebugger.getDebugTarget()) {
            case NODES:
                nodesTargetRadioButton.setSelected(true);
                break;

            case PATHS:
                pathsTargetRadioButton.setSelected(true);
                break;
        }

        nodesVisualizationCheckBox.setSelected(neo4jDebugger.isShowNodes());
        if (neo4jDebugger.getNodesColor() != null) {
            nodesVisualizationCheckBox.setForeground(neo4jDebugger.getNodesColor());
        }

        relationshipsVisualizationCheckBox.setSelected(neo4jDebugger.isShowRelationships());
        if (neo4jDebugger.getRelationshipsColor() != null) {
            relationshipsVisualizationCheckBox.setForeground(neo4jDebugger.getRelationshipsColor());
        }
    }

    private void resetComponents() {
        targetButtonGroup.clearSelection();

        nodesVisualizationCheckBox.setSelected(false);
        nodesVisualizationCheckBox.setForeground(Color.BLACK);

        relationshipsVisualizationCheckBox.setSelected(false);
        relationshipsVisualizationCheckBox.setForeground(Color.BLACK);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile == null) {
                resetComponents();
                return;
            }

            FileSystemClassLoader classLoader = Lookup.getDefault().lookup(FileSystemClassLoader.class);
            Class<?> debugClass = null;

            try {
                debugClass =
                        classLoader.loadClass(selectedFile,
                        true,
                        Neo4jDelegateNodeDebugger.class);
            } // do nothing, file is just not Neo4j debug file (all 4 catch clauses)
            catch (ClassNotFoundException cnfe) {
                resetComponents();
                return;
            } catch (NoClassDefFoundError ncdfe) {
                resetComponents();
                return;
            } catch (ClassNotFulfillRequirementsException cnfre) {
                resetComponents();
                return;
            } catch (IllegalArgumentException iae) {
                resetComponents();
                return;
            }

            Neo4jDelegateNodeDebugger neo4jDebugger = null;
            try {
                neo4jDebugger = (Neo4jDelegateNodeDebugger) debugClass.newInstance();
            } catch (IllegalAccessException iae) {
                // should never occur, public constructor is checked during the class loading process
                throw new AssertionError();
            } catch (InstantiationException ie) {
                // should never occur, loaded class must have public nonparam constructor
                throw new AssertionError();
            }

            updateComponents(neo4jDebugger);
        }
    }
}
