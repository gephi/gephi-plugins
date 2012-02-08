/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.datalab.plugin.manipulators.general.ui;

import java.util.ArrayList;
import javax.swing.JPanel;
import org.gephi.datalab.plugin.manipulators.general.AddEdgeToGraph;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 * UI for AddEdgeToGraph GeneralActionsManipulator
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class AddEdgeToGraphUI extends javax.swing.JPanel implements ManipulatorUI {

    private AddEdgeToGraph manipulator;
    private Node[] nodes, targetNodes;
    private MixedGraph graph;

    /** Creates new form AddEdgeToGraphUI */
    public AddEdgeToGraphUI() {
        initComponents();
    }

    public void setup(Manipulator m, DialogControls dialogControls) {
        this.manipulator = (AddEdgeToGraph) m;
        if (manipulator.isDirected()) {
            directedRadioButton.setSelected(true);
        } else {
            undirectedRadioButton.setSelected(true);
        }
        graph = Lookup.getDefault().lookup(GraphController.class).getModel().getMixedGraph();
        nodes = graph.getNodes().toArray();
        for (Node n : nodes) {
            sourceNodesComboBox.addItem(n.getId() + " - " + n.getNodeData().getLabel());
        }
        refreshAvailableTargetNodes();
    }

    public void unSetup() {
        manipulator.setDirected(directedRadioButton.isSelected());
        if (targetNodesComboBox.getSelectedIndex() != -1) {
            manipulator.setSource(nodes[sourceNodesComboBox.getSelectedIndex()]);
            manipulator.setTarget(targetNodes[targetNodesComboBox.getSelectedIndex()]);
        }
    }

    public String getDisplayName() {
        return manipulator.getName();
    }

    public JPanel getSettingsPanel() {
        return this;
    }

    public boolean isModal() {
        return true;
    }

    private void refreshAvailableTargetNodes() {
        if (nodes != null) {
            ArrayList<Node> availableTargetNodes = new ArrayList<Node>();
            Node sourceNode = nodes[sourceNodesComboBox.getSelectedIndex()];
            boolean createUndirected = undirectedRadioButton.isSelected();
            boolean canCreateEdge;
            for (Node n : nodes) {
                canCreateEdge = true;
                if (n != sourceNode) {//They are not the same node
                    for (Edge e : graph.getEdges(sourceNode).toArray()) {
                        if (createUndirected) {
                            if (e.getSource() == n || e.getTarget() == n) {//There is no edge with the source and target node:
                                canCreateEdge = false;
                            }
                        } else {
                            //There is no directed edge with that source and target, and there is no undirected edge with the 2 nodes:
                            if (e.isDirected()) {
                                if (e.getTarget() == n) {
                                    canCreateEdge = false;
                                }
                            } else if (e.getSource() == n || e.getTarget() == n) {
                                canCreateEdge = false;
                            }
                        }
                    }
                    if (canCreateEdge) {
                        availableTargetNodes.add(n);
                    }
                }
            }

            targetNodes = availableTargetNodes.toArray(new Node[0]);
            targetNodesComboBox.removeAllItems();
            for (Node n : targetNodes) {
                targetNodesComboBox.addItem(n.getId() + " - " + n.getNodeData().getLabel());
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        directedUndirectedRadioButtonGroup = new javax.swing.ButtonGroup();
        directedRadioButton = new javax.swing.JRadioButton();
        undirectedRadioButton = new javax.swing.JRadioButton();
        descriptionLabel = new javax.swing.JLabel();
        sourceNodesComboBox = new javax.swing.JComboBox();
        sourceNodeLabel = new javax.swing.JLabel();
        targetNodeLabel = new javax.swing.JLabel();
        targetNodesComboBox = new javax.swing.JComboBox();

        directedUndirectedRadioButtonGroup.add(directedRadioButton);
        directedRadioButton.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.directedRadioButton.text")); // NOI18N
        directedRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                directedRadioButtonItemStateChanged(evt);
            }
        });

        directedUndirectedRadioButtonGroup.add(undirectedRadioButton);
        undirectedRadioButton.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.undirectedRadioButton.text")); // NOI18N
        undirectedRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                undirectedRadioButtonItemStateChanged(evt);
            }
        });

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.descriptionLabel.text")); // NOI18N

        sourceNodesComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceNodesComboBoxItemStateChanged(evt);
            }
        });

        sourceNodeLabel.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.sourceNodeLabel.text")); // NOI18N

        targetNodeLabel.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.targetNodeLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(directedRadioButton)
                            .addComponent(sourceNodeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceNodesComboBox, 0, 204, Short.MAX_VALUE)
                            .addComponent(undirectedRadioButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetNodeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(targetNodesComboBox, 0, 204, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directedRadioButton)
                    .addComponent(undirectedRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceNodesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceNodeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetNodesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetNodeLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sourceNodesComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sourceNodesComboBoxItemStateChanged
        refreshAvailableTargetNodes();
    }//GEN-LAST:event_sourceNodesComboBoxItemStateChanged

    private void directedRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_directedRadioButtonItemStateChanged
        refreshAvailableTargetNodes();
    }//GEN-LAST:event_directedRadioButtonItemStateChanged

    private void undirectedRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_undirectedRadioButtonItemStateChanged
        refreshAvailableTargetNodes();
    }//GEN-LAST:event_undirectedRadioButtonItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JRadioButton directedRadioButton;
    private javax.swing.ButtonGroup directedUndirectedRadioButtonGroup;
    private javax.swing.JLabel sourceNodeLabel;
    private javax.swing.JComboBox sourceNodesComboBox;
    private javax.swing.JLabel targetNodeLabel;
    private javax.swing.JComboBox targetNodesComboBox;
    private javax.swing.JRadioButton undirectedRadioButton;
    // End of variables declaration//GEN-END:variables
}
