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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.ui.utils.BusyUtils;
import org.gephi.ui.utils.BusyUtils.BusyLabel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

final class HierarchyTopComponent extends TopComponent {

    private static HierarchyTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "HierarchyTopComponent";

    //Dendrogram
    private Dendrogram dendrogram;

    private HierarchyTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(HierarchyTopComponent.class, "CTL_HierarchyTopComponent"));
//        setToolTipText(NbBundle.getMessage(HierarchyTopComponent.class, "HINT_HierarchyTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        initToolbar();
        dendrogram = new Dendrogram();
    }

    private void initToolbar() {
        refreshButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        levelLimitCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int lvl = levelLimitCombo.getSelectedIndex();
                if (lvl != dendrogram.getMaxHeight()) {
                    dendrogram.setMaxHeight(lvl);
                    refresh();
                }
            }
        });
    }

    private void refreshLevelLimit(HierarchicalGraph graph) {
        int h = graph.getHeight();
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NbBundle.getMessage(HierarchyTopComponent.class, "HierarchyTopComponent.bar.levelmax"));
        String levelStr = NbBundle.getMessage(HierarchyTopComponent.class, "HierarchyTopComponent.bar.level");
        for (int i = 1; i <= h; i++) {
            comboBoxModel.addElement(levelStr + " " + i);
        }
        levelLimitCombo.setModel(comboBoxModel);
        levelLimitCombo.setSelectedIndex(Math.min(h, dendrogram.getMaxHeight()));
    }

    public void refresh() {
        final GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        if (model != null) {
            Thread thread = new Thread(new Runnable() {

                public void run() {
                    BusyLabel busyLabel = BusyUtils.createCenteredBusyLabel(centerScrollPane, NbBundle.getMessage(HierarchyTopComponent.class, "HierarchyTopComponent.busyLabel.text"), dendrogram);
                    busyLabel.setBusy(true);
                    final HierarchicalGraph graph = model.getHierarchicalGraph();
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            refreshLevelLimit(graph);
                        }
                    });
                    dendrogram.refresh(graph);
                    busyLabel.setBusy(false);
                }
            }, "Dendrogram refresh");
            thread.start();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolPanel = new javax.swing.JPanel();
        labelLevelLimit = new javax.swing.JLabel();
        levelLimitCombo = new javax.swing.JComboBox();
        refreshButton = new javax.swing.JButton();
        centerScrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        toolPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labelLevelLimit, org.openide.util.NbBundle.getMessage(HierarchyTopComponent.class, "HierarchyTopComponent.labelLevelLimit.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 0, 0);
        toolPanel.add(labelLevelLimit, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        toolPanel.add(levelLimitCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(HierarchyTopComponent.class, "HierarchyTopComponent.refreshButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 4);
        toolPanel.add(refreshButton, gridBagConstraints);

        add(toolPanel, java.awt.BorderLayout.PAGE_START);

        centerScrollPane.setBorder(null);
        centerScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        centerScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(centerScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane centerScrollPane;
    private javax.swing.JLabel labelLevelLimit;
    private javax.swing.JComboBox levelLimitCombo;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel toolPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized HierarchyTopComponent getDefault() {
        if (instance == null) {
            instance = new HierarchyTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HierarchyTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HierarchyTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HierarchyTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof HierarchyTopComponent) {
            return (HierarchyTopComponent) win;
        }
        Logger.getLogger(HierarchyTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return HierarchyTopComponent.getDefault();
        }
    }
}
