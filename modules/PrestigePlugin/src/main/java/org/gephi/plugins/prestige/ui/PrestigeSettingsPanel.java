/* 
 * Copyright (C) 2016 Michael Henninger <gephi@michihenninger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.plugins.prestige.ui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class PrestigeSettingsPanel extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger(PrestigeSettingsPanel.class.getName());
    public static final String DESCRIPTION_URL = "https://github.com/michihenninger/prestige-gephi-plugin/tree/prestige-plugin/modules";

    public PrestigeSettingsPanel() {
        initComponents();
        addMoreInformationActionListeners();
        setupGUI();
    }

    private void setupGUI() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel model = graphController.getGraphModel();
        if (!model.isDirected()) {
            LOG.info("Graph is undirected, will disable all options");
            // Disable all
            lblNetworkInfo.setText("Can not calculate Prestige metrics on undirected networks.");
            chkIndegree.setEnabled(false);
            chkIndegree.setSelected(false);
            chkDomain.setEnabled(false);
            chkDomain.setSelected(false);
            chkProximity.setEnabled(false);
            chkProximity.setSelected(false);
            chkRank.setEnabled(false);
            chkRank.setSelected(false);
            cmbRankAttributes.setEnabled(false);
            chkRankLogScale.setEnabled(false);
        } else {
            // Fetch supported datatypes for prominence attribute
            LOG.fine("Checking all available attributes for nummeric or boolean class type");
            Table nodeTable = model.getNodeTable();
            for (int i = 0; i < nodeTable.countColumns(); i++) {
                LOG.log(Level.FINE, "Checking column type for '{0}'", nodeTable.getColumn(i));
                Column col = nodeTable.getColumn(i);
                if (isSupported(col)) {
                    LOG.log(Level.FINE, "Type of column '{0}' is suported", col.getTitle());
                    cmbRankAttributes.addItem(col);
                }
            }

            if (cmbRankAttributes.getModel().getSize() == 0) {
                LOG.info("No nummeric or boolean attributes found, will disable rank prestige option");
                lblRankInfo.setText("No nummeric or boolean node attributs available.");
                chkRank.setSelected(false);
                chkRank.setEnabled(false);
                cmbRankAttributes.setEnabled(false);
                chkRankLogScale.setEnabled(false);
            }
        }
    }

    boolean isIndegree() {
        return chkIndegree.isSelected();
    }

    boolean isProximity() {
        return chkProximity.isSelected();
    }

    boolean isDomain() {
        return chkDomain.isSelected();
    }

    boolean isRank() {
        return chkRank.isSelected();
    }

    private boolean isSupported(Column col) {
        Class c = col.getTypeClass();
        return (c == Integer.class || c == Double.class || c == Boolean.class || c == Byte.class || c == Short.class || c == Long.class || c == BigDecimal.class || c == BigInteger.class || c == Float.class);
    }

    boolean isRankLogTransformation() {
        return chkRankLogScale.isSelected();
    }

    // Returns selected element or null if no selection. Hint. Check before calling, if <code>isRank</code> is true.
    public String getProminenceAttributeId() {
        return ((Column) cmbRankAttributes.getSelectedItem()).getId();
    }

    public String getDefaultIfNan() {
        return txtRankDefault.getText();
    }

    private void addMoreInformationActionListeners() {
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(DESCRIPTION_URL));
                    } catch (Exception ex) {
                        LOG.fine("Could not open Browser. Will show dialog");
                        showUrlDialog();
                    }
                } else {
                    showUrlDialog();
                }
            }

            private void showUrlDialog() {
                JOptionPane.showMessageDialog(PrestigeSettingsPanel.this, "For more information visit: " + DESCRIPTION_URL, "Detailed information", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        btnDomain.addActionListener(al);
        btnIndegree.addActionListener(al);
        btnProximity.addActionListener(al);
        btnRank.addActionListener(al);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jXHeader1 = new org.jdesktop.swingx.JXHeader();
        chkIndegree = new javax.swing.JCheckBox();
        chkDomain = new javax.swing.JCheckBox();
        chkProximity = new javax.swing.JCheckBox();
        chkRank = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbRankAttributes = new javax.swing.JComboBox<Column>();
        chkRankLogScale = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblIndegreeDesc = new javax.swing.JLabel();
        lblDomainDesc = new javax.swing.JLabel();
        lblProximityDesc = new javax.swing.JLabel();
        lblRankDesc = new javax.swing.JLabel();
        lblNetworkInfo = new javax.swing.JLabel();
        lblProximityInfo = new javax.swing.JLabel();
        lblDomainInfo = new javax.swing.JLabel();
        lblIndegreeInfo = new javax.swing.JLabel();
        lblRankInfo = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtRankDefault = new javax.swing.JTextField();
        btnIndegree = new javax.swing.JButton();
        btnDomain = new javax.swing.JButton();
        btnProximity = new javax.swing.JButton();
        btnRank = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();

        jLabel3.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel3.text")); // NOI18N

        jXHeader1.setDescription(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jXHeader1.description")); // NOI18N
        jXHeader1.setTitle(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jXHeader1.title")); // NOI18N

        chkIndegree.setSelected(true);
        chkIndegree.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.chkIndegree.text")); // NOI18N

        chkDomain.setSelected(true);
        chkDomain.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.chkDomain.text")); // NOI18N

        chkProximity.setSelected(true);
        chkProximity.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.chkProximity.text")); // NOI18N

        chkRank.setSelected(true);
        chkRank.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.chkRank.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel2.text")); // NOI18N

        cmbRankAttributes.setModel(new javax.swing.DefaultComboBoxModel<Column>());

        chkRankLogScale.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.chkRankLogScale.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel4.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel4.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel5.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel5.text")); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel6.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel6.text")); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel7.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel7.text")); // NOI18N

        lblIndegreeDesc.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblIndegreeDesc.text")); // NOI18N

        lblDomainDesc.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblDomainDesc.text")); // NOI18N

        lblProximityDesc.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblProximityDesc.text")); // NOI18N

        lblRankDesc.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblRankDesc.text")); // NOI18N

        lblNetworkInfo.setForeground(new java.awt.Color(255, 0, 0));
        lblNetworkInfo.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblNetworkInfo.text")); // NOI18N

        lblProximityInfo.setForeground(new java.awt.Color(255, 0, 0));
        lblProximityInfo.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblProximityInfo.text")); // NOI18N

        lblDomainInfo.setForeground(new java.awt.Color(255, 0, 0));
        lblDomainInfo.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblDomainInfo.text")); // NOI18N

        lblIndegreeInfo.setForeground(new java.awt.Color(255, 0, 0));
        lblIndegreeInfo.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblIndegreeInfo.text")); // NOI18N

        lblRankInfo.setForeground(new java.awt.Color(255, 0, 0));
        lblRankInfo.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.lblRankInfo.text")); // NOI18N

        jLabel12.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.jLabel12.text")); // NOI18N

        txtRankDefault.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.txtRankDefault.text")); // NOI18N

        btnIndegree.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.btnIndegree.text")); // NOI18N

        btnDomain.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.btnDomain.text")); // NOI18N

        btnProximity.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.btnProximity.text")); // NOI18N

        btnRank.setText(org.openide.util.NbBundle.getMessage(PrestigeSettingsPanel.class, "PrestigeSettingsPanel.btnRank.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jXHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblNetworkInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(chkRank, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(chkProximity, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                            .addComponent(chkDomain, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(chkIndegree, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRankInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblProximityInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblDomainInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblIndegreeInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbRankAttributes, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(131, 131, 131))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(chkRankLogScale, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtRankDefault, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblProximityDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblIndegreeDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblDomainDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRankDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnDomain)
                            .addComponent(btnIndegree)
                            .addComponent(btnProximity)
                            .addComponent(btnRank))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 801, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jXHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblNetworkInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkIndegree)
                    .addComponent(lblIndegreeInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkDomain)
                    .addComponent(lblDomainInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkProximity)
                    .addComponent(lblProximityInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkRank)
                    .addComponent(lblRankInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbRankAttributes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtRankDefault, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkRankLogScale)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIndegreeDesc)
                    .addComponent(jLabel4)
                    .addComponent(btnIndegree))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblDomainDesc)
                    .addComponent(btnDomain))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblProximityDesc)
                    .addComponent(btnProximity))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(lblRankDesc)
                    .addComponent(btnRank))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        chkRank.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chkRank.isSelected()) {
                    cmbRankAttributes.setEnabled(true);
                    chkRankLogScale.setEnabled(true);
                } else {
                    cmbRankAttributes.setEnabled(false);
                    chkRankLogScale.setEnabled(false);
                }
            }
        });
        KeyAdapter kl = new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0') && (c <= '9') ||
                    (c == KeyEvent.VK_BACK_SPACE) ||
                    (c == KeyEvent.VK_DELETE))) {
                getToolkit().beep();
                e.consume();
            }
        }
    };

    txtRankDefault.addKeyListener(kl);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDomain;
    private javax.swing.JButton btnIndegree;
    private javax.swing.JButton btnProximity;
    private javax.swing.JButton btnRank;
    private javax.swing.JCheckBox chkDomain;
    private javax.swing.JCheckBox chkIndegree;
    private javax.swing.JCheckBox chkProximity;
    private javax.swing.JCheckBox chkRank;
    private javax.swing.JCheckBox chkRankLogScale;
    private javax.swing.JComboBox<Column> cmbRankAttributes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private org.jdesktop.swingx.JXHeader jXHeader1;
    private javax.swing.JLabel lblDomainDesc;
    private javax.swing.JLabel lblDomainInfo;
    private javax.swing.JLabel lblIndegreeDesc;
    private javax.swing.JLabel lblIndegreeInfo;
    private javax.swing.JLabel lblNetworkInfo;
    private javax.swing.JLabel lblProximityDesc;
    private javax.swing.JLabel lblProximityInfo;
    private javax.swing.JLabel lblRankDesc;
    private javax.swing.JLabel lblRankInfo;
    private javax.swing.JTextField txtRankDefault;
    // End of variables declaration//GEN-END:variables

}
