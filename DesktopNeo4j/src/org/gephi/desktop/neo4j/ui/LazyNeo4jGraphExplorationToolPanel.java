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


/**
 *
 * @author Martin Škurla
 */
public class LazyNeo4jGraphExplorationToolPanel extends javax.swing.JPanel {

    public LazyNeo4jGraphExplorationToolPanel() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        depthLabel = new javax.swing.JLabel();
        depthSpinner = new javax.swing.JSpinner();
        automaticLayoutCheckBox = new javax.swing.JCheckBox();

        depthLabel.setFont(new java.awt.Font("Tahoma", 0, 10));
        depthLabel.setText(org.openide.util.NbBundle.getMessage(LazyNeo4jGraphExplorationToolPanel.class, "LazyNeo4jGraphExplorationToolPanel.depthLabel.text")); // NOI18N

        depthSpinner.setFont(new java.awt.Font("Tahoma", 0, 10));
        depthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 1));

        automaticLayoutCheckBox.setText(org.openide.util.NbBundle.getMessage(LazyNeo4jGraphExplorationToolPanel.class, "LazyNeo4jGraphExplorationToolPanel.automaticLayoutCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(depthLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(depthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(automaticLayoutCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(depthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(depthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(automaticLayoutCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox automaticLayoutCheckBox;
    private javax.swing.JLabel depthLabel;
    private javax.swing.JSpinner depthSpinner;
    // End of variables declaration//GEN-END:variables

    public int getDepth() {
        return (Integer) depthSpinner.getValue();
    }

    public boolean isAutomaticLayoutOn() {
        return automaticLayoutCheckBox.isSelected();
    }
}
