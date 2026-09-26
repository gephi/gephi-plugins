/*
 Copyright Scott A. Hale, 2016
 * 
 
 Base on code from 
 Copyright 2008-2016 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 Portions Copyrighted 2011 Gephi Consortium.
 */
package uk.ac.ox.oii.sigmaexporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import uk.ac.ox.oii.sigmaexporter.model.ConfigFile;

public class SigmaSettingsPanel extends javax.swing.JPanel {

    //final String LAST_PATH = "SQLiteDatabaseSettingsPanel_Last_Path";
    private File path;
    private SigmaExporter exporter;

    /** Creates new form SQLiteDatabaseSettingsPanel */
    public SigmaSettingsPanel() {
        initComponents();

        browseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(pathTextField.getText());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //DialogFileFilter dialogFileFilter = new DialogFileFilter("SQLite files");
                //dialogFileFilter.addExtensions(new String[] {".sqlite"});
                //fileChooser.addChoosableFileFilter(dialogFileFilter);
                //fileChooser.setAcceptAllFileFilterUsed(false);
                int result = fileChooser.showSaveDialog(WindowManager.getDefault().getMainWindow());
                if (result == JFileChooser.APPROVE_OPTION) {
                    path = fileChooser.getSelectedFile();
                    pathTextField.setText(path.getAbsolutePath());
                }
            }
        });
    }

    public void setup(SigmaExporter exporter) {
        this.exporter = exporter;
        //path = new File(NbPreferences.forModule(SigmaSettingsPanel.class).get(LAST_PATH, System.getProperty("user.home")+"/sigma"));
        //pathTextField.setText(path.getAbsolutePath());
        
        //Checkbox for each node attribute (should it be included)
        /*javax.swing.JCheckBox b = new javax.swing.JCheckBox("b");
        a.setText("bbb");
        a.setVisible(true);
        //attributesPanel.add(a);
        attributesPanel.setViewportView(b);*/
       
        List<String> attributes = exporter.getNodeAttributes();
        
        for (String a : attributes) {
            ddGroupSelector.addItem(a);
            ddImageAttribute.addItem(a);
            //TODO: Also create check box for attributesPanel
            //javax.swing.JCheckBox cb = new javax.swing.JCheckBox();
            //cb.setText(a);
            //attributesPanel.add(cb);
        }
        attributesScrollPanel.setViewportView(attributesPanel);
        
        Preferences prefs = NbPreferences.forModule(SigmaSettingsPanel.class);
        pathTextField.setText(prefs.get("path", ""));
        txtNode.setText(prefs.get("legend.node", ""));
        txtEdge.setText(prefs.get("legend.edge",""));
        txtColor.setText(prefs.get("legend.color",""));
        cbSearch.setSelected(Boolean.valueOf(prefs.get("features.search","true")));
        ddHover.setSelectedItem(prefs.get("features.hoverBehavior", "None"));
        ddGroupSelector.setSelectedItem(prefs.get("features.groupSelectAttribute", "None"));
        ddImageAttribute.setSelectedItem(prefs.get("informationPanel.imageAttribute","None"));
        cbGroupEdges.setSelected(Boolean.valueOf(prefs.get("informationPanel.groupByEdgeDirection","false")));
        txtShort.setText(prefs.get("text.intro", ""));
        txtLong.setText(prefs.get("text.more", ""));
        txtTitle.setText(prefs.get("text.title", ""));        
        txtLogo.setText(prefs.get("logo.file", ""));        
        txtLink.setText(prefs.get("logo.link", ""));        
        txtAuthor.setText(prefs.get("logo.author", ""));
        cbRenumber.setSelected(Boolean.valueOf(prefs.get("renumber","true")));
    }

    public void unsetup(boolean update) {
        //HashMap<String,String> props = new HashMap<String,String>();
        Preferences props = NbPreferences.forModule(SigmaSettingsPanel.class);
        String path="";
        boolean renumber = false;
        if (update) {
            try {
                path = pathTextField.getText();
                renumber = cbRenumber.isSelected();
                props.put("path",path);
                props.put("renumber", String.valueOf(renumber));
                props.put("legend.node",txtNode.getText());
                props.put("legend.edge",txtEdge.getText());
                props.put("legend.color",txtColor.getText());
                props.put("features.search",String.valueOf(cbSearch.isSelected()));
                props.put("features.hoverBehavior",String.valueOf(ddHover.getSelectedItem()));
                props.put("features.groupSelectAttribute",String.valueOf(ddGroupSelector.getSelectedItem()));
                props.put("informationPanel.imageAttribute",String.valueOf(ddImageAttribute.getSelectedItem()));
                props.put("informationPanel.groupByEdgeDirection",String.valueOf(cbGroupEdges.isSelected()));
                props.put("text.intro",txtShort.getText());
                props.put("text.more",txtLong.getText());
                props.put("text.title",txtTitle.getText());
                props.put("logo.file",txtLogo.getText());
                props.put("logo.link",txtLink.getText());
                props.put("logo.author",txtAuthor.getText());
                
            } catch (Exception e) {
                Logger.getLogger(SigmaExporter.class.getName()).log(Level.SEVERE, null, e);
            }
            ConfigFile cfg = new ConfigFile();
            cfg.readFromPrefs(props);
            exporter.setConfigFile(cfg,path,renumber);
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

        jLabel1 = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtNode = new javax.swing.JTextField();
        txtEdge = new javax.swing.JTextField();
        txtColor = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        cbSearch = new javax.swing.JCheckBox();
        ddHover = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        attributesScrollPanel = new javax.swing.JScrollPane();
        attributesPanel = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtShort = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtLong = new javax.swing.JTextArea();
        txtLogo = new javax.swing.JTextField();
        txtLink = new javax.swing.JTextField();
        txtAuthor = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        ddGroupSelector = new javax.swing.JComboBox();
        cbGroupEdges = new javax.swing.JCheckBox();
        ddImageAttribute = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        txtTitle = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        cbRenumber = new javax.swing.JCheckBox();

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD, jLabel1.getFont().getSize()+3));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel1.text")); // NOI18N

        pathTextField.setText("\n"); // NOI18N

        browseButton.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel4.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel5.text")); // NOI18N

        txtNode.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtNode.text")); // NOI18N
        txtNode.setToolTipText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtNode.toolTipText")); // NOI18N

        txtEdge.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtEdge.text")); // NOI18N
        txtEdge.setToolTipText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtEdge.toolTipText")); // NOI18N

        txtColor.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtColor.text")); // NOI18N
        txtColor.setToolTipText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtColor.toolTipText")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel6.text")); // NOI18N

        jLabel8.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel8.text")); // NOI18N

        jLabel9.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel9.text")); // NOI18N
        jLabel9.setToolTipText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel9.toolTipText")); // NOI18N

        cbSearch.setSelected(true);
        cbSearch.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.cbSearch.text")); // NOI18N
        cbSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSearchActionPerformed(evt);
            }
        });

        ddHover.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None (Default)", "Dim" }));

        jLabel7.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel7.text")); // NOI18N

        jLabel10.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel10.text")); // NOI18N

        jLabel11.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel11.text")); // NOI18N

        jLabel12.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel12.text")); // NOI18N

        jLabel18.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel18.text")); // NOI18N

        javax.swing.GroupLayout attributesPanelLayout = new javax.swing.GroupLayout(attributesPanel);
        attributesPanel.setLayout(attributesPanelLayout);
        attributesPanelLayout.setHorizontalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesPanelLayout.createSequentialGroup()
                .addGap(81, 81, 81)
                .addComponent(jLabel18)
                .addContainerGap(443, Short.MAX_VALUE))
        );
        attributesPanelLayout.setVerticalGroup(
            attributesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesPanelLayout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addComponent(jLabel18)
                .addContainerGap(85, Short.MAX_VALUE))
        );

        attributesScrollPanel.setViewportView(attributesPanel);

        txtShort.setColumns(20);
        txtShort.setRows(5);
        jScrollPane2.setViewportView(txtShort);

        txtLong.setColumns(20);
        txtLong.setRows(5);
        jScrollPane3.setViewportView(txtLong);

        txtLogo.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtLogo.text")); // NOI18N

        txtLink.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtLink.text")); // NOI18N

        txtAuthor.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtAuthor.text")); // NOI18N

        jLabel13.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel13.text")); // NOI18N

        jLabel14.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel14.text")); // NOI18N

        jLabel15.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel15.text")); // NOI18N

        ddGroupSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "color" }));

        cbGroupEdges.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.cbGroupEdges.text")); // NOI18N

        ddImageAttribute.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None (Default)" }));

        jLabel16.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel16.text")); // NOI18N

        txtTitle.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.txtTitle.text")); // NOI18N

        jLabel17.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.jLabel17.text")); // NOI18N

        cbRenumber.setSelected(true);
        cbRenumber.setText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.cbRenumber.text")); // NOI18N
        cbRenumber.setToolTipText(org.openide.util.NbBundle.getMessage(SigmaSettingsPanel.class, "SigmaSettingsPanel.cbRenumber.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attributesScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jScrollPane2)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtEdge, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtColor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNode, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel2))
                        .addGap(41, 41, 41)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel10)
                                        .addComponent(jLabel11))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(txtLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtLink, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel12)
                                        .addComponent(jLabel17))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbSearch)
                                .addGap(33, 33, 33)
                                .addComponent(cbGroupEdges))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(26, 26, 26))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel16)
                                            .addComponent(jLabel15))
                                        .addGap(18, 18, 18)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ddHover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ddGroupSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ddImageAttribute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(97, 97, 97))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(pathTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel13)
                                    .addComponent(cbRenumber))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbSearch)
                            .addComponent(cbGroupEdges))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(ddHover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ddGroupSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ddImageAttribute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jLabel7))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(txtNode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(txtEdge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(txtColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel10)
                                            .addComponent(txtLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel11)
                                            .addComponent(txtLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel12))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(63, 63, 63)
                                        .addComponent(txtAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel17))))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addComponent(attributesScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbRenumber)
                .addGap(7, 7, 7))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbSearchActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributesPanel;
    private javax.swing.JScrollPane attributesScrollPanel;
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox cbGroupEdges;
    private javax.swing.JCheckBox cbRenumber;
    private javax.swing.JCheckBox cbSearch;
    private javax.swing.JComboBox ddGroupSelector;
    private javax.swing.JComboBox ddHover;
    private javax.swing.JComboBox ddImageAttribute;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JTextField txtAuthor;
    private javax.swing.JTextField txtColor;
    private javax.swing.JTextField txtEdge;
    private javax.swing.JTextField txtLink;
    private javax.swing.JTextField txtLogo;
    private javax.swing.JTextArea txtLong;
    private javax.swing.JTextField txtNode;
    private javax.swing.JTextArea txtShort;
    private javax.swing.JTextField txtTitle;
    // End of variables declaration//GEN-END:variables
}
