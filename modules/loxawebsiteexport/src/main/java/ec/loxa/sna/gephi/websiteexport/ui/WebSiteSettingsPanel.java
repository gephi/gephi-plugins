/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WebSiteSettingsPanel.java
 *
 * Created on 27-abr-2011, 19:01:05
 */
package ec.loxa.sna.gephi.websiteexport.ui;

import ec.loxa.sna.gephi.websiteexport.WebSiteExporter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.*;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author jorgaf
 */
public class WebSiteSettingsPanel extends javax.swing.JPanel {

    final String LAST_PATH = "WebSiteExporterUI_Last_Path";
    private WebSiteExporter wsExporter;
    private File path;
    private boolean append = false;

    /**
     * Creates new form WebSiteSettingsPanel
     */
    public WebSiteSettingsPanel() {
        initComponents();
        loadWorkSpaceNames();

        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(txtPath.getText());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
                if (result == JFileChooser.APPROVE_OPTION) {
                    path = fileChooser.getSelectedFile();
                    txtPath.setText(path.getAbsolutePath());
                    append = false;
                }
            }
        });

        btnAppend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                String nom[] = {"index.html", "estadisticas.json"};
                TreeSet files = new TreeSet();
                files.addAll(Arrays.asList(nom));

                ArrayList<String> temp = new ArrayList<String>();
                StringBuilder sb = new StringBuilder();
                boolean miss = false;

                JFileChooser fChooser = new JFileChooser(txtPath.getText());
                fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int r = fChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
                if (r == JFileChooser.APPROVE_OPTION) {
                    Iterator i = files.iterator();
                    while (i.hasNext())
                        temp.add(fChooser.getSelectedFile().getPath()+File.separator+i.next());
                    
                    ListIterator<String> t = temp.listIterator();
                    while (t.hasNext()) {
                        String tMsg= t.next();
                        File fTemp = new File(tMsg);
                        if (!fTemp.exists()){                            
                            sb.append(fTemp.getName()).append(", ");
                            miss = true;
                        }
                    }

                    if (miss) {
                        //JOptionPane.showMessageDialog(null, "files missing: "+sb.deleteCharAt(sb.length()-2));
                        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                    NbBundle.getMessage(WebSiteExporterUI.class, "WebSiteExporterUI.Error.FilesNoExistsDescription"),
                                    NbBundle.getMessage(WebSiteExporterUI.class, "WebSiteExporterUI.Error.FilesNoExistsTitle"),
                                    JOptionPane.ERROR_MESSAGE);
                    } else {
                        txtPath.setText(fChooser.getSelectedFile().getPath());
                        append = true;
                    }
                }
            }
        });
    }

    public void setup(WebSiteExporter exporter) {
        this.wsExporter = exporter;


        path = new File(NbPreferences.forModule(WebSiteExporterUI.class).get(LAST_PATH, System.getProperty("user.home")));
        txtPath.setText(path.getAbsolutePath());

        chbAttributes.setSelected(wsExporter.isExportAttributes());
        chbColors.setSelected(wsExporter.isExportColors());
        chbDynamic.setSelected(wsExporter.isExportDynamic());
        chbPosition.setSelected(wsExporter.isExportPosition());
        chbSize.setSelected(wsExporter.isExportSize());
    }

    public void unsetup(boolean update) {
        if (update) {
            try {
                path = new File(txtPath.getText());
            } catch (Exception e) {
            }
            NbPreferences.forModule(WebSiteExporterUI.class).put(LAST_PATH, path.getAbsolutePath());
            wsExporter.setPath(path);
            wsExporter.setExportAttributes(chbAttributes.isSelected());
            wsExporter.setExportColors(chbColors.isSelected());
            wsExporter.setExportDynamic(chbDynamic.isSelected());
            wsExporter.setExportPosition(chbPosition.isSelected());
            wsExporter.setExportSize(chbSize.isSelected());
            wsExporter.setAppend(append);
            if (rbnPGD.isSelected()) {
                wsExporter.setTheme("paintViewer");
            }else if (rbnHide.isSelected()) {
                wsExporter.setTheme("hide");
            }else if (rbnFishEye.isSelected()){
                wsExporter.setTheme("fishEye");
            }
        }
    }

    public static ValidationPanel createValidationPanel(WebSiteSettingsPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(innerPanel.txtPath, StringValidators.FILE_MUST_BE_DIRECTORY);

        return validationPanel;
    }

    private void loadWorkSpaceNames() {
        Project project = Lookup.getDefault().lookup(ProjectController.class).
                getCurrentProject();
        WorkspaceProvider workspaceProvider =
                project.getLookup().lookup(WorkspaceProvider.class);
        WorkspaceInformation workspaceInfortion;
        Workspace workspace;

        ArrayList<String> namesSelected = new ArrayList<String>();
        GraphModel graphModel;

        for (int i = 0; i < workspaceProvider.getWorkspaces().length; i++) {
            workspace = workspaceProvider.getWorkspaces()[i];

            workspaceInfortion = workspace.getLookup().lookup(WorkspaceInformation.class);
            graphModel = workspace.getLookup().lookup(GraphModel.class);
            if (graphModel.getGraphVisible().getNodeCount() > 0 || graphModel.getGraphVisible().getEdgeCount() > 0) {
                namesSelected.add(workspaceInfortion.getName());
            }
        }
        final String[] names;
        String[] namesAux = new String[namesSelected.size()];
        names = namesSelected.toArray(namesAux);

        lstWorkspaces.setModel(new AbstractListModel() {
            String[] strings = names;

            @Override
            public int getSize() {
                return strings.length;
            }

            @Override
            public Object getElementAt(int index) {

                return strings[index];
            }
        });

        if (names.length == 1) {
            lstWorkspaces.setSelectedIndex(0);
            lstWorkspaces.setEnabled(false);
        }
    }

    public String[] getSelectedWorkspaces() {
        String[] selectedWorkspaces = new String[lstWorkspaces.getSelectedValuesList().size()];
        for (int i = 0; i < lstWorkspaces.getSelectedValuesList().size(); i++) {
            selectedWorkspaces[i] = lstWorkspaces.getSelectedValuesList().get(i).toString().replace(" ", "");
        }
        return selectedWorkspaces;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btngGraphTheme = new javax.swing.ButtonGroup();
        header = new org.jdesktop.swingx.JXHeader();
        pnlWSettings = new javax.swing.JPanel();
        lblPath = new javax.swing.JLabel();
        txtPath = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        lblWorkspace = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWorkspaces = new javax.swing.JList();
        btnAppend = new javax.swing.JButton();
        pnlGEXFSettings = new javax.swing.JPanel();
        chbAttributes = new javax.swing.JCheckBox();
        chbColors = new javax.swing.JCheckBox();
        chbDynamic = new javax.swing.JCheckBox();
        chbPosition = new javax.swing.JCheckBox();
        chbSize = new javax.swing.JCheckBox();
        pnlGraphTheme = new javax.swing.JPanel();
        rbnPGD = new javax.swing.JRadioButton();
        rbnHide = new javax.swing.JRadioButton();
        rbnFishEye = new javax.swing.JRadioButton();

        header.setDescription(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.header.description")); // NOI18N
        header.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ec/loxa/sna/gephi/websiteexport/files/loxa.png"))); // NOI18N
        header.setTitle(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.header.title")); // NOI18N

        pnlWSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.pnlWSettings.border.title"))); // NOI18N

        lblPath.setLabelFor(txtPath);
        lblPath.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.lblPath.text")); // NOI18N

        btnBrowse.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.btnBrowse.text")); // NOI18N

        lblWorkspace.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.lblWorkspace.text")); // NOI18N

        jScrollPane1.setViewportView(lstWorkspaces);

        btnAppend.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.btnAppend.text")); // NOI18N

        org.jdesktop.layout.GroupLayout pnlWSettingsLayout = new org.jdesktop.layout.GroupLayout(pnlWSettings);
        pnlWSettings.setLayout(pnlWSettingsLayout);
        pnlWSettingsLayout.setHorizontalGroup(
            pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlWSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(pnlWSettingsLayout.createSequentialGroup()
                        .add(lblPath)
                        .add(18, 18, 18)
                        .add(txtPath))
                    .add(pnlWSettingsLayout.createSequentialGroup()
                        .add(lblWorkspace)
                        .add(18, 18, 18)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 422, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(18, 18, 18)
                .add(pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btnBrowse)
                    .add(btnAppend))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlWSettingsLayout.setVerticalGroup(
            pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlWSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPath)
                    .add(txtPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnBrowse))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlWSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblWorkspace)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnAppend))
                .addContainerGap())
        );

        btnAppend.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.btnAppend.text")); // NOI18N

        pnlGEXFSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.pnlGEXFSettings.border.title"))); // NOI18N

        chbAttributes.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbAttributes.text")); // NOI18N

        chbColors.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbColors.text")); // NOI18N

        chbDynamic.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbDynamic.text")); // NOI18N

        chbPosition.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbPosition.text")); // NOI18N

        chbSize.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.chbSize.text")); // NOI18N

        org.jdesktop.layout.GroupLayout pnlGEXFSettingsLayout = new org.jdesktop.layout.GroupLayout(pnlGEXFSettings);
        pnlGEXFSettings.setLayout(pnlGEXFSettingsLayout);
        pnlGEXFSettingsLayout.setHorizontalGroup(
            pnlGEXFSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlGEXFSettingsLayout.createSequentialGroup()
                .add(97, 97, 97)
                .add(pnlGEXFSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(chbSize)
                    .add(chbPosition)
                    .add(chbDynamic)
                    .add(chbColors)
                    .add(chbAttributes))
                .addContainerGap(120, Short.MAX_VALUE))
        );
        pnlGEXFSettingsLayout.setVerticalGroup(
            pnlGEXFSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlGEXFSettingsLayout.createSequentialGroup()
                .add(chbAttributes)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbColors)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbDynamic)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbPosition)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chbSize)
                .add(0, 0, Short.MAX_VALUE))
        );

        pnlGraphTheme.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.pnlGraphTheme.border.title"))); // NOI18N
        pnlGraphTheme.setToolTipText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.pnlGraphTheme.toolTipText")); // NOI18N

        btngGraphTheme.add(rbnPGD);
        rbnPGD.setSelected(true);
        rbnPGD.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.rbnPGD.text")); // NOI18N

        btngGraphTheme.add(rbnHide);
        rbnHide.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.rbnHide.text")); // NOI18N

        btngGraphTheme.add(rbnFishEye);
        rbnFishEye.setText(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.rbnFishEye.text")); // NOI18N

        org.jdesktop.layout.GroupLayout pnlGraphThemeLayout = new org.jdesktop.layout.GroupLayout(pnlGraphTheme);
        pnlGraphTheme.setLayout(pnlGraphThemeLayout);
        pnlGraphThemeLayout.setHorizontalGroup(
            pnlGraphThemeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlGraphThemeLayout.createSequentialGroup()
                .add(14, 14, 14)
                .add(pnlGraphThemeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rbnPGD)
                    .add(rbnHide)
                    .add(rbnFishEye))
                .addContainerGap(180, Short.MAX_VALUE))
        );
        pnlGraphThemeLayout.setVerticalGroup(
            pnlGraphThemeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlGraphThemeLayout.createSequentialGroup()
                .add(21, 21, 21)
                .add(rbnPGD)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(rbnHide)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(rbnFishEye)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        rbnPGD.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.rbnPGD.text")); // NOI18N
        rbnHide.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.rbnHide.text")); // NOI18N
        rbnFishEye.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.rbnFishEye.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(header, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlWSettings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(pnlGEXFSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(pnlGraphTheme, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(header, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(pnlWSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(pnlGEXFSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pnlGraphTheme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlGraphTheme.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WebSiteSettingsPanel.class, "WebSiteSettingsPanel.pnlGraphTheme.border.title")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAppend;
    private javax.swing.JButton btnBrowse;
    private javax.swing.ButtonGroup btngGraphTheme;
    private javax.swing.JCheckBox chbAttributes;
    private javax.swing.JCheckBox chbColors;
    private javax.swing.JCheckBox chbDynamic;
    private javax.swing.JCheckBox chbPosition;
    private javax.swing.JCheckBox chbSize;
    private org.jdesktop.swingx.JXHeader header;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPath;
    private javax.swing.JLabel lblWorkspace;
    private javax.swing.JList lstWorkspaces;
    private javax.swing.JPanel pnlGEXFSettings;
    private javax.swing.JPanel pnlGraphTheme;
    private javax.swing.JPanel pnlWSettings;
    private javax.swing.JRadioButton rbnFishEye;
    private javax.swing.JRadioButton rbnHide;
    private javax.swing.JRadioButton rbnPGD;
    private javax.swing.JTextField txtPath;
    // End of variables declaration//GEN-END:variables
}
