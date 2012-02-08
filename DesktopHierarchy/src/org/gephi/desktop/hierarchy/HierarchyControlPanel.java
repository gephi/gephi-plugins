/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.desktop.hierarchy;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphSettings;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.jdesktop.swingx.JXHyperlink;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyControlPanel extends javax.swing.JPanel {

    public HierarchyControlPanel() {
        initComponents();
        initEvents();
        showTreeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void initEvents() {
        autoMetaEdgeCheckbox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
                boolean sel = autoMetaEdgeCheckbox.isSelected();
                model.settings().putClientProperty(GraphSettings.AUTO_META_EDGES, sel);
                sumRadio.setEnabled(sel);
                avgRadio.setEnabled(sel);
                labelWeight.setEnabled(sel);
            }
        });

        showTreeLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                TopComponent tc = WindowManager.getDefault().findTopComponent("HierarchyTopComponent");
                if (tc != null) {
                    tc.open();
                    tc.requestActive();
                    HierarchyTopComponent hierarchyTopComponent = (HierarchyTopComponent) tc;
                    hierarchyTopComponent.refresh();
                }
            }
        });

        metaEdgeInfoLabel.addMouseListener(new MouseAdapter() {

            RichTooltip richTooltip;

            @Override
            public void mouseEntered(MouseEvent e) {
                String description = NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.info.description");
                String title = NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.info.title");
                richTooltip = new RichTooltip(title, description);
                richTooltip.showTooltip(metaEdgeInfoLabel);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (richTooltip != null) {
                    richTooltip.hideTooltip();
                    richTooltip = null;
                }
            }
        });

        metaWeightInfoLabel.addMouseListener(new MouseAdapter() {

            RichTooltip richTooltip;

            @Override
            public void mouseEntered(MouseEvent e) {
                String description = NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.weightinfo.description");
                String title = NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.weightinfo.title");
                richTooltip = new RichTooltip(title, description);
                richTooltip.showTooltip(metaWeightInfoLabel);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (richTooltip != null) {
                    richTooltip.hideTooltip();
                    richTooltip = null;
                }
            }
        });

        ActionListener radioListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
                GraphSettings settings = model.settings();
                settings.putClientProperty(GraphSettings.METAEDGE_BUILDER, e.getActionCommand());
            }
        };
        sumRadio.setActionCommand("sum");
        avgRadio.setActionCommand("average");
        sumRadio.addActionListener(radioListener);
        avgRadio.addActionListener(radioListener);
    }

    public void setup() {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        HierarchicalGraph graph = model.getHierarchicalGraphVisible();
        initLevelsLinks(graph);

        //Init status
        GraphSettings settings = model.settings();
        boolean enabled = (Boolean) settings.getClientProperty(GraphSettings.AUTO_META_EDGES);
        autoMetaEdgeCheckbox.setSelected(enabled);

        sumRadio.setEnabled(enabled);
        avgRadio.setEnabled(enabled);
        labelWeight.setEnabled(enabled);

        //Weight
        String builder = (String) settings.getClientProperty(GraphSettings.METAEDGE_BUILDER);
        if (builder.equalsIgnoreCase("sum")) {
            sumRadio.setSelected(true);
        } else if (builder.equalsIgnoreCase("average")) {
            avgRadio.setSelected(true);
        } else {
            sumRadio.setEnabled(false);
            avgRadio.setEnabled(false);
        }

        //Stats
        heightLabel.setText("" + (graph.getHeight() + 1));
    }

    private void initLevelsLinks(HierarchicalGraph graph) {

        levelViewPanel.removeAll();
        String levelStr = NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.linkLevel");
        String nodesStr = NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.linkLevel.nodes");
        String leavesStr = NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.linkLevel.leaves");

        int[] levelSize;
        graph.readLock();
        int height = graph.getHeight();
        levelSize = new int[height + 1];
        for (int i = 0; i < height + 1; i++) {
            levelSize[i] = graph.getLevelSize(i);
        }
        graph.readUnlock();

        //Level links    
        for (int i = 0; i < levelSize.length; i++) {

            JXHyperlink link = new JXHyperlink();
            link.setClickedColor(new java.awt.Color(0, 51, 255));
            link.setText(levelStr + " " + i + " (" + levelSize[i] + " " + nodesStr + ")");
            link.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
            final int lvl = i;
            link.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
                    HierarchicalGraph graph = model.getHierarchicalGraphVisible();
                    graph.resetViewToLevel(lvl);
                }
            });
            GridBagConstraints gdc = new GridBagConstraints(0, i, 1, 1, 1, 0, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 0, 0), 0, 0);
            levelViewPanel.add(link, gdc);
        }

        //Leaves
        JXHyperlink link = new JXHyperlink();
        link.setClickedColor(new java.awt.Color(0, 51, 255));
        link.setText(leavesStr);
        link.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        link.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
                HierarchicalGraph graph = model.getHierarchicalGraphVisible();
                graph.resetViewToLeaves();
            }
        });
        GridBagConstraints gdc = new GridBagConstraints(0, height + 1, 1, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 0, 0), 0, 0);
        levelViewPanel.add(link, gdc);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        weightGroup = new javax.swing.ButtonGroup();
        showTreeLabel = new javax.swing.JLabel();
        labelHeight = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        separator1 = new javax.swing.JSeparator();
        settingsPanel = new javax.swing.JPanel();
        labelAuto = new javax.swing.JLabel();
        metaEdgeInfoLabel = new javax.swing.JLabel();
        autoMetaEdgeCheckbox = new javax.swing.JCheckBox();
        labelWeight = new javax.swing.JLabel();
        sumRadio = new javax.swing.JRadioButton();
        avgRadio = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        metaWeightInfoLabel = new javax.swing.JLabel();
        labelView = new javax.swing.JLabel();
        levelViewPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();

        setPreferredSize(new java.awt.Dimension(214, 300));

        showTreeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/hierarchy/resources/tree.png"))); // NOI18N
        showTreeLabel.setText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.showTreeLabel.text")); // NOI18N
        showTreeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.showTreeLabel.toolTipText")); // NOI18N
        showTreeLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        labelHeight.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelHeight.setText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.labelHeight.text")); // NOI18N

        heightLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        heightLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        heightLabel.setText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.heightLabel.text")); // NOI18N
        heightLabel.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.heightLabel.toolTipText")); // NOI18N

        settingsPanel.setLayout(new java.awt.GridBagLayout());

        labelAuto.setText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.labelAuto.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        settingsPanel.add(labelAuto, gridBagConstraints);

        metaEdgeInfoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/hierarchy/resources/information-small.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        settingsPanel.add(metaEdgeInfoLabel, gridBagConstraints);

        autoMetaEdgeCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.1;
        settingsPanel.add(autoMetaEdgeCheckbox, gridBagConstraints);

        labelWeight.setText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.labelWeight.text")); // NOI18N
        labelWeight.setToolTipText(""); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        settingsPanel.add(labelWeight, gridBagConstraints);

        weightGroup.add(sumRadio);
        sumRadio.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.sumRadio.toolTipText")); // NOI18N
        sumRadio.setLabel(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.sumRadio.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        settingsPanel.add(sumRadio, gridBagConstraints);

        weightGroup.add(avgRadio);
        avgRadio.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.avgRadio.toolTipText")); // NOI18N
        avgRadio.setLabel(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.avgRadio.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        settingsPanel.add(avgRadio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        settingsPanel.add(jLabel2, gridBagConstraints);

        metaWeightInfoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/hierarchy/resources/information-small.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 5);
        settingsPanel.add(metaWeightInfoLabel, gridBagConstraints);

        labelView.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelView.setText(org.openide.util.NbBundle.getMessage(HierarchyControlPanel.class, "HierarchyControlPanel.labelView.text")); // NOI18N

        levelViewPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(showTreeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(separator1, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(labelHeight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(heightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelView)
                .addContainerGap(179, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(levelViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(showTreeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelHeight)
                    .addComponent(heightLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelView)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(levelViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoMetaEdgeCheckbox;
    private javax.swing.JRadioButton avgRadio;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelAuto;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelView;
    private javax.swing.JLabel labelWeight;
    private javax.swing.JPanel levelViewPanel;
    private javax.swing.JLabel metaEdgeInfoLabel;
    private javax.swing.JLabel metaWeightInfoLabel;
    private javax.swing.JSeparator separator1;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JLabel showTreeLabel;
    private javax.swing.JRadioButton sumRadio;
    private javax.swing.ButtonGroup weightGroup;
    // End of variables declaration//GEN-END:variables
}
