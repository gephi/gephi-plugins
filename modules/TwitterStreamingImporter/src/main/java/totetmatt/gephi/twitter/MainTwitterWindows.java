package totetmatt.gephi.twitter;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.WorkspaceListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import totetmatt.gephi.twitter.networklogic.Networklogic;
import totetmatt.gephi.twitter.networklogic.utils.Language;
import totetmatt.gephi.twitter.networklogic.utils.TrackLocation;
import twitter4j.JSONException;

@ConvertAsProperties(dtd = "-//org.gephi.plugins.example.panel//Simple//EN",
        autostore = false)
@TopComponent.Description(preferredID = "MainTwitterWindows",
        iconBase = "totetmatt/gephi/twitter/resources/twitterlogo.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "org.gephi.plugins.twitter.panel.MainTwitterWindows")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SimpleAction",
        preferredID = "MainTwitterWindows")
public final class MainTwitterWindows extends TopComponent {

    private final TwitterStreamer streamer;
    private final DefaultListModel wordTrackingListModel = new DefaultListModel();
    private final DefaultTableModel userTrackingTableModel;
    private final ProjectController projectController;
    private final DefaultTableModel locationsTrackingTableModel;
    private final DefaultComboBoxModel languageComboBoxModel = new DefaultComboBoxModel();
    private final DefaultListModel languageTrackingListModel = new DefaultListModel();
    private int idWorkspace = -1;

    public MainTwitterWindows() {
        initComponents();
        setName(NbBundle.getMessage(MainTwitterWindows.class, "CTL_SimpleTopComponent"));
        setToolTipText(NbBundle.getMessage(MainTwitterWindows.class, "HINT_SimpleTopComponent"));
        List<Networklogic> networks = new ArrayList((Collection<Networklogic>) Lookup.getDefault().lookupAll(Networklogic.class));
        Collections.sort(networks) ;
        
        streamer = Lookup.getDefault().lookup(TwitterStreamer.class);

        DefaultComboBoxModel c = new DefaultComboBoxModel();
        for (Networklogic nl : networks) {
            c.addElement(nl);
        }
        
        for (Language l : Language.ALL) {
            languageComboBoxModel.addElement(l);
        }
        
        network_logic_combo.setModel(c);

        userTrackingTableModel = (DefaultTableModel) ut_list_table.getModel();
        locationsTrackingTableModel = (DefaultTableModel) lt_list_table.getModel();
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        checkPluginEnabling();

        projectController.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(org.gephi.project.api.Workspace wrkspc) {
                checkPluginEnabling();
            }

            @Override
            public void select(org.gephi.project.api.Workspace wrkspc) {
                checkPluginEnabling();
            }

            @Override
            public void unselect(org.gephi.project.api.Workspace wrkspc) {
                checkPluginEnabling();
            }

            @Override
            public void close(org.gephi.project.api.Workspace wrkspc) {
                checkPluginEnabling();
                if (idWorkspace == wrkspc.getId()) {
                    stopStreamer();
                }
            }

            @Override
            public void disable() {
                checkPluginEnabling();
            }

        });

    }

    private boolean isProjectWorkspaceOk() {
        return projectController.getCurrentProject() != null
                && projectController.getCurrentWorkspace() != null;
    }

    private void checkPluginEnabling() {
        if (isProjectWorkspaceOk()) {
            connect_toggleButton.setEnabled(true);
            warning_new_project_label.setVisible(false);                 
        } else {
            connect_toggleButton.setEnabled(false);
            warning_new_project_label.setVisible(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPasswordField1 = new javax.swing.JPasswordField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        connect_toggleButton = new javax.swing.JToggleButton();
        tracking_tab_panel = new javax.swing.JTabbedPane();
        wt_panel = new javax.swing.JPanel();
        wt_add_textfield = new javax.swing.JTextField();
        wt_add_button = new javax.swing.JButton();
        wt_word_list_scrollpane = new javax.swing.JScrollPane();
        wt_word_list = new javax.swing.JList<>();
        wt_word_list.setModel(wordTrackingListModel);
        wt_delete_button = new javax.swing.JButton();
        ut_panel = new javax.swing.JPanel();
        ut_list_scrollpane = new javax.swing.JScrollPane();
        ut_list_table = new javax.swing.JTable();
        ut_add_button = new javax.swing.JButton();
        ut_add_textfield = new javax.swing.JTextField();
        ut_delete_button = new javax.swing.JButton();
        ut_add_from_list_button = new javax.swing.JButton();
        ut_add_from_list_user_textfield = new javax.swing.JTextField();
        ut_add_from_list_listname_textfield = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lt_panel = new javax.swing.JPanel();
        lt_list_scrollpane = new javax.swing.JScrollPane();
        lt_list_table = new javax.swing.JTable();
        lt_add_button = new javax.swing.JButton();
        lt_add_sw_lat_textfield = new javax.swing.JTextField();
        lt_add_sw_long_textfield = new javax.swing.JTextField();
        lt_add_ne_lat_textfield = new javax.swing.JTextField();
        lt_add_ne_long_textfield = new javax.swing.JTextField();
        lt_add_name_textfield = new javax.swing.JTextField();
        lt_delete_button = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lang_panel = new javax.swing.JPanel();
        language_combo_box = new javax.swing.JComboBox<>();
        language_add_button = new javax.swing.JButton();
        language_delete_button = new javax.swing.JButton();
        wt_word_list_scrollpane1 = new javax.swing.JScrollPane();
        wt_lang_list = new javax.swing.JList<>();
        wt_lang_list.setModel(languageTrackingListModel);
        network_logic_combo = new javax.swing.JComboBox<>();
        network_logic_label = new javax.swing.JLabel();
        load_tracking_button = new javax.swing.JButton();
        save_tracking_button = new javax.swing.JButton();
        crendential_button = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        credential_goto_twitter_button = new javax.swing.JButton();
        warning_new_project_label = new javax.swing.JLabel();
        random_sample_chk = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();

        jPasswordField1.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jPasswordField1.text")); // NOI18N

        setAutoscrolls(true);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setAutoscrolls(true);

        jPanel1.setAutoscrolls(true);
        jPanel1.setName(""); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(connect_toggleButton, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.connect_toggleButton.text")); // NOI18N
        connect_toggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connect_toggleButtonActionPerformed(evt);
            }
        });

        wt_add_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.wt_add_textfield.text")); // NOI18N
        wt_add_textfield.setToolTipText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.wt_add_textfield.toolTipText")); // NOI18N
        wt_add_textfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                wt_add_textfieldKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(wt_add_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.wt_add_button.text")); // NOI18N
        wt_add_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wt_add_buttonActionPerformed(evt);
            }
        });

        wt_word_list.setToolTipText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.wt_word_list.toolTipText")); // NOI18N
        wt_word_list_scrollpane.setViewportView(wt_word_list);

        org.openide.awt.Mnemonics.setLocalizedText(wt_delete_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.wt_delete_button.text")); // NOI18N
        wt_delete_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wt_delete_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout wt_panelLayout = new javax.swing.GroupLayout(wt_panel);
        wt_panel.setLayout(wt_panelLayout);
        wt_panelLayout.setHorizontalGroup(
            wt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wt_panelLayout.createSequentialGroup()
                .addGroup(wt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(wt_add_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(wt_delete_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(wt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wt_add_textfield)
                    .addComponent(wt_word_list_scrollpane))
                .addContainerGap())
        );
        wt_panelLayout.setVerticalGroup(
            wt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, wt_panelLayout.createSequentialGroup()
                .addGroup(wt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wt_add_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wt_add_button))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(wt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(wt_panelLayout.createSequentialGroup()
                        .addComponent(wt_word_list_scrollpane)
                        .addGap(11, 11, 11))
                    .addGroup(wt_panelLayout.createSequentialGroup()
                        .addComponent(wt_delete_button)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        tracking_tab_panel.addTab(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.wt_panel.TabConstraints.tabTitle_1"), wt_panel); // NOI18N

        ut_list_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Screen Name", "Id"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Long.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        ut_list_table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ut_list_scrollpane.setViewportView(ut_list_table);

        org.openide.awt.Mnemonics.setLocalizedText(ut_add_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.ut_add_button.text")); // NOI18N
        ut_add_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ut_add_buttonActionPerformed(evt);
            }
        });

        ut_add_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.ut_add_textfield.text")); // NOI18N
        ut_add_textfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ut_add_textfieldKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ut_delete_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.ut_delete_button.text")); // NOI18N
        ut_delete_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ut_delete_buttonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ut_add_from_list_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.ut_add_from_list_button.text")); // NOI18N
        ut_add_from_list_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ut_add_from_list_buttonActionPerformed(evt);
            }
        });

        ut_add_from_list_user_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.ut_add_from_list_user_textfield.text")); // NOI18N

        ut_add_from_list_listname_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.ut_add_from_list_listname_textfield.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout ut_panelLayout = new javax.swing.GroupLayout(ut_panel);
        ut_panel.setLayout(ut_panelLayout);
        ut_panelLayout.setHorizontalGroup(
            ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ut_panelLayout.createSequentialGroup()
                .addGroup(ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ut_delete_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ut_add_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ut_add_from_list_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ut_panelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ut_add_from_list_user_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ut_add_from_list_listname_textfield, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                    .addComponent(ut_add_textfield)
                    .addComponent(ut_list_scrollpane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        ut_panelLayout.setVerticalGroup(
            ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ut_panelLayout.createSequentialGroup()
                .addGroup(ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ut_add_button)
                    .addComponent(ut_add_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ut_add_from_list_button)
                        .addComponent(jLabel3))
                    .addGroup(ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ut_add_from_list_user_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ut_add_from_list_listname_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ut_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ut_delete_button)
                    .addComponent(ut_list_scrollpane, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tracking_tab_panel.addTab(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.ut_panel.TabConstraints.tabTitle_1"), ut_panel); // NOI18N

        lt_list_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Location", "Coord"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Long.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        lt_list_table.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lt_list_scrollpane.setViewportView(lt_list_table);

        org.openide.awt.Mnemonics.setLocalizedText(lt_add_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_add_button.text")); // NOI18N
        lt_add_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lt_add_buttonActionPerformed(evt);
            }
        });

        lt_add_sw_lat_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_add_sw_lat_textfield.text")); // NOI18N
        lt_add_sw_lat_textfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lt_add_sw_lat_textfieldKeyReleased(evt);
            }
        });

        lt_add_sw_long_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_add_sw_long_textfield.text")); // NOI18N
        lt_add_sw_long_textfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lt_add_sw_long_textfieldKeyReleased(evt);
            }
        });

        lt_add_ne_lat_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_add_ne_lat_textfield.text")); // NOI18N
        lt_add_ne_lat_textfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lt_add_ne_lat_textfieldKeyReleased(evt);
            }
        });

        lt_add_ne_long_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_add_ne_long_textfield.text")); // NOI18N
        lt_add_ne_long_textfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lt_add_ne_long_textfieldKeyReleased(evt);
            }
        });

        lt_add_name_textfield.setText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_add_name_textfield.text")); // NOI18N
        lt_add_name_textfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lt_add_name_textfieldKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lt_delete_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_delete_button.text")); // NOI18N
        lt_delete_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lt_delete_buttonActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel8.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel9.text")); // NOI18N

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel10.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel13.text")); // NOI18N

        javax.swing.GroupLayout lt_panelLayout = new javax.swing.GroupLayout(lt_panel);
        lt_panel.setLayout(lt_panelLayout);
        lt_panelLayout.setHorizontalGroup(
            lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lt_panelLayout.createSequentialGroup()
                .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lt_delete_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lt_add_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lt_list_scrollpane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(lt_panelLayout.createSequentialGroup()
                        .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(lt_panelLayout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lt_add_sw_lat_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lt_add_sw_long_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(lt_panelLayout.createSequentialGroup()
                                .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(lt_panelLayout.createSequentialGroup()
                                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                        .addGap(1, 1, 1)
                                        .addComponent(jLabel11))
                                    .addComponent(jLabel13))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(lt_panelLayout.createSequentialGroup()
                                        .addComponent(lt_add_ne_lat_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lt_add_ne_long_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lt_add_name_textfield))))
                        .addGap(18, 18, 18)))
                .addContainerGap())
        );
        lt_panelLayout.setVerticalGroup(
            lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lt_panelLayout.createSequentialGroup()
                .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(lt_panelLayout.createSequentialGroup()
                        .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lt_add_sw_lat_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(lt_add_sw_long_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lt_add_ne_lat_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(lt_add_ne_long_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(lt_add_name_textfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lt_add_button, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(lt_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lt_list_scrollpane, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lt_delete_button))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tracking_tab_panel.addTab(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lt_panel.TabConstraints.tabTitle"), lt_panel); // NOI18N

        language_combo_box.setModel(languageComboBoxModel);

        org.openide.awt.Mnemonics.setLocalizedText(language_add_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.language_add_button.text")); // NOI18N
        language_add_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                language_add_buttonMouseClicked(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(language_delete_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.language_delete_button.text")); // NOI18N
        language_delete_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                language_delete_buttonMouseClicked(evt);
            }
        });

        wt_lang_list.setToolTipText(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.wt_lang_list.toolTipText")); // NOI18N
        wt_word_list_scrollpane1.setViewportView(wt_lang_list);

        javax.swing.GroupLayout lang_panelLayout = new javax.swing.GroupLayout(lang_panel);
        lang_panel.setLayout(lang_panelLayout);
        lang_panelLayout.setHorizontalGroup(
            lang_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lang_panelLayout.createSequentialGroup()
                .addGroup(lang_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(language_add_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(language_delete_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lang_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(language_combo_box, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(wt_word_list_scrollpane1))
                .addContainerGap())
        );
        lang_panelLayout.setVerticalGroup(
            lang_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lang_panelLayout.createSequentialGroup()
                .addGroup(lang_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(language_add_button)
                    .addComponent(language_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(lang_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(language_delete_button)
                    .addComponent(wt_word_list_scrollpane1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );

        tracking_tab_panel.addTab(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.lang_panel.TabConstraints.tabTitle"), lang_panel); // NOI18N

        network_logic_combo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(network_logic_label, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.network_logic_label.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(load_tracking_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.load_tracking_button.text")); // NOI18N
        load_tracking_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                load_tracking_buttonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(save_tracking_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.save_tracking_button.text")); // NOI18N
        save_tracking_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_tracking_buttonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(crendential_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.crendential_button.text")); // NOI18N
        crendential_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crendential_buttonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(credential_goto_twitter_button, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.credential_goto_twitter_button.text")); // NOI18N
        credential_goto_twitter_button.setActionCommand(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.credential_goto_twitter_button.actionCommand")); // NOI18N
        credential_goto_twitter_button.setBorder(null);
        credential_goto_twitter_button.setBorderPainted(false);
        credential_goto_twitter_button.setContentAreaFilled(false);
        credential_goto_twitter_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                credential_goto_documentation_buttonActionPerformed(evt);
            }
        });

        warning_new_project_label.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        warning_new_project_label.setForeground(new java.awt.Color(102, 0, 0));
        warning_new_project_label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(warning_new_project_label, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.warning_new_project_label.text")); // NOI18N

        random_sample_chk.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(random_sample_chk, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.random_sample_chk.text")); // NOI18N
        random_sample_chk.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                random_sample_chkItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(random_sample_chk)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(network_logic_label)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(network_logic_combo, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(connect_toggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(warning_new_project_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(tracking_tab_panel, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(load_tracking_button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(save_tracking_button))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(credential_goto_twitter_button, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(crendential_button, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(crendential_button))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(credential_goto_twitter_button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(save_tracking_button)
                    .addComponent(load_tracking_button))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tracking_tab_panel, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(random_sample_chk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(network_logic_label)
                    .addComponent(network_logic_combo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(warning_new_project_label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(connect_toggleButton)
                .addContainerGap())
        );

        tracking_tab_panel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.tracking_tab_panel.AccessibleContext.accessibleName")); // NOI18N

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 556, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void random_sample_chkItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_random_sample_chkItemStateChanged
        // TODO add your handling code here:

        if(random_sample_chk.isSelected()){

            streamer.setRandomSample(true);
            for(Component c : wt_panel.getComponents()){
                c.setEnabled(false);
            }
            for(Component c : ut_panel.getComponents()){
                c.setEnabled(false);
            }
            for(Component c : lt_panel.getComponents()){
                c.setEnabled(false);
            }
        } else {
            streamer.setRandomSample(false);
            for(Component c : wt_panel.getComponents()){
                c.setEnabled(true);
            }
            for(Component c : ut_panel.getComponents()){
                c.setEnabled(true);
            }
            for(Component c : lt_panel.getComponents()){
                c.setEnabled(true);
            }

        }
    }//GEN-LAST:event_random_sample_chkItemStateChanged

    private void credential_goto_documentation_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_credential_goto_documentation_buttonActionPerformed
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("https://seinecle.github.io/gephi-tutorials/generated-html/twitter-streaming-importer-en.html"));
            } catch (IOException e) {
                Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, e);
            } catch (URISyntaxException ex) {
                Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, new Exception("Desktop method isn't supported"));
        }
    }//GEN-LAST:event_credential_goto_documentation_buttonActionPerformed

    private void crendential_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crendential_buttonActionPerformed
        TwitterCredentialDialog dg = new TwitterCredentialDialog(null, true);
        dg.setCredentialProperty(streamer.getCredentialProperty());
        dg.setVisible(true);// TODO add your handling code here:
        streamer.setCredentialProperty(dg.getCredentialProperty());
        streamer.getCredentialProperty().save();
    }//GEN-LAST:event_crendential_buttonActionPerformed

    private void save_tracking_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_tracking_buttonActionPerformed
        final JFileChooser fc = new JFileChooser();

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.file_chooser.format"), "json", "application/json");
        fc.setFileFilter(filter);
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                streamer.saveTracking(file);
            } catch (JSONException ex) {
                Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_save_tracking_buttonActionPerformed

    private void load_tracking_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_load_tracking_buttonActionPerformed
        final JFileChooser fc = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter(org.openide.util.NbBundle.getMessage(MainTwitterWindows.class, "MainTwitterWindows.file_chooser.format"), "json", "application/json");
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                streamer.loadTracking(file);
            } catch (IOException ex) {
                Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(MainTwitterWindows.class.getName()).log(Level.SEVERE, null, ex);
            }
            refreshTracking();
        }
    }//GEN-LAST:event_load_tracking_buttonActionPerformed

    private void language_delete_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_language_delete_buttonMouseClicked
        streamer.getLanguageFilter().removeAll(wt_lang_list.getSelectedValuesList());
        refreshLanguageList();
    }//GEN-LAST:event_language_delete_buttonMouseClicked

    private void language_add_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_language_add_buttonMouseClicked
        Language selected = (Language)language_combo_box.getSelectedItem();
        streamer.addLanguage(selected);
        refreshLanguageList();
    }//GEN-LAST:event_language_add_buttonMouseClicked

    private void lt_delete_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lt_delete_buttonActionPerformed
        for (int row : lt_list_table.getSelectedRows()) {
            streamer.getLocationTracking().remove(locationsTrackingTableModel.getValueAt(row, 0));
        }
        this.refreshLocationList();
    }//GEN-LAST:event_lt_delete_buttonActionPerformed

    private void lt_add_name_textfieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lt_add_name_textfieldKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_lt_add_name_textfieldKeyReleased

    private void lt_add_ne_long_textfieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lt_add_ne_long_textfieldKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_lt_add_ne_long_textfieldKeyReleased

    private void lt_add_ne_lat_textfieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lt_add_ne_lat_textfieldKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_lt_add_ne_lat_textfieldKeyReleased

    private void lt_add_sw_long_textfieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lt_add_sw_long_textfieldKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_lt_add_sw_long_textfieldKeyReleased

    private void lt_add_sw_lat_textfieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lt_add_sw_lat_textfieldKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_lt_add_sw_lat_textfieldKeyReleased

    private void lt_add_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lt_add_buttonActionPerformed
        addLocation();
    }//GEN-LAST:event_lt_add_buttonActionPerformed

    private void ut_add_from_list_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ut_add_from_list_buttonActionPerformed
        if ( !this.ut_add_from_list_user_textfield.getText().isEmpty() && !this.ut_add_from_list_listname_textfield.getText().isEmpty()) {
            addFromList();
        }
    }//GEN-LAST:event_ut_add_from_list_buttonActionPerformed

    private void ut_delete_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ut_delete_buttonActionPerformed
        for (int row : ut_list_table.getSelectedRows()) {
            streamer.getUserTracking().remove(userTrackingTableModel.getValueAt(row, 0));
        }
        refreshUserList();
    }//GEN-LAST:event_ut_delete_buttonActionPerformed

    private void ut_add_textfieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ut_add_textfieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addUser();
        }
    }//GEN-LAST:event_ut_add_textfieldKeyReleased

    private void ut_add_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ut_add_buttonActionPerformed
        addUser();
    }//GEN-LAST:event_ut_add_buttonActionPerformed

    private void wt_delete_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wt_delete_buttonActionPerformed
        streamer.getWordTracking().removeAll(wt_word_list.getSelectedValuesList());
        refreshWordList();
    }//GEN-LAST:event_wt_delete_buttonActionPerformed

    private void wt_add_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wt_add_buttonActionPerformed
        addWord();
    }//GEN-LAST:event_wt_add_buttonActionPerformed

    private void wt_add_textfieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_wt_add_textfieldKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            addWord();
        }
    }//GEN-LAST:event_wt_add_textfieldKeyReleased

    private void connect_toggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connect_toggleButtonActionPerformed
        if (connect_toggleButton.isSelected()) {
            startStreamer();
        } else {
            stopStreamer();
        }
    }//GEN-LAST:event_connect_toggleButtonActionPerformed
    private void startStreamer() {
        idWorkspace = projectController.getCurrentWorkspace().getId();
        streamer.start((Networklogic) network_logic_combo.getSelectedItem());
        connect_toggleButton.setText("Disconnect");
    }

    private void stopStreamer() {
        streamer.stop();
        connect_toggleButton.setText("Connect");
        connect_toggleButton.setSelected(false);
    }    
    private void addLocation(){
        if(!lt_add_sw_lat_textfield.getText().isEmpty() &&
                !lt_add_sw_long_textfield.getText().isEmpty()  &&
                !lt_add_ne_lat_textfield.getText().isEmpty()  && 
                !lt_add_ne_long_textfield.getText().isEmpty()  &&
                !lt_add_name_textfield.getText().isEmpty() 
                ) {
        TrackLocation location = new TrackLocation(Double.parseDouble(lt_add_sw_lat_textfield.getText()),
                                                    Double.parseDouble(lt_add_sw_long_textfield.getText()),
                                                    Double.parseDouble(lt_add_ne_lat_textfield.getText()),
                                                    Double.parseDouble(lt_add_ne_long_textfield.getText()),
                                                    lt_add_name_textfield.getText());
        lt_add_sw_lat_textfield.setText("");
        lt_add_sw_long_textfield.setText("");
        lt_add_ne_lat_textfield.setText("");
        lt_add_ne_long_textfield.setText("");
        lt_add_name_textfield.setText("");
        streamer.addLocation(location);
        refreshLocationList();
        } else {
           throw new IllegalArgumentException("Longitude, Latitude and Title fields shouldn't be empty");
        }
    }
    private void addFromList(){
        String username = ut_add_from_list_user_textfield.getText().trim().toLowerCase();
        String listname =  ut_add_from_list_listname_textfield.getText().trim();
        streamer.addFromList(username, listname);
        ut_add_from_list_listname_textfield.setText("");
        ut_add_from_list_user_textfield.setText("");
        refreshUserList();
    }
     private void refreshTracking() {
        refreshWordList();
        refreshUserList();
        refreshLocationList();
        refreshLanguageList();
    }

    private void addUser(){
        for(String user: ut_add_textfield.getText().toLowerCase().split(",")){
            streamer.addUser(user.trim());
        }
        ut_add_textfield.setText("");
        refreshUserList();
    }
    
    private void addWord() {
        for(String word : wt_add_textfield.getText().toLowerCase().split(",")){
            streamer.addWordTracking(word.trim());
        }
        wt_add_textfield.setText("");
        refreshWordList();
    }

    private void refreshWordList() {
        wordTrackingListModel.clear();
        for (String s : streamer.getWordTracking()) {
            wordTrackingListModel.add(0, s);
        }
    }
    private void refreshLanguageList(){
       languageTrackingListModel.clear();
       for(Language l: streamer.getLanguageFilter()) {
            languageTrackingListModel.addElement(l);
        }  
    }
    private void refreshLocationList(){
        lt_list_table.clearSelection();
        lt_list_table.updateUI();
        locationsTrackingTableModel.getDataVector().removeAllElements();
        for (Map.Entry<String, TrackLocation> s : streamer.getLocationTracking().entrySet()) {
            locationsTrackingTableModel.addRow(new Object[]{s.getKey(),s.getValue().toString()});
        }
    }
    private void refreshUserList() {
        ut_list_table.clearSelection();
        ut_list_table.updateUI();
        userTrackingTableModel.getDataVector().removeAllElements();
        for (Map.Entry<String, Long> entry : streamer.getUserTracking().entrySet()) {
            userTrackingTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton connect_toggleButton;
    private javax.swing.JButton credential_goto_twitter_button;
    private javax.swing.JButton crendential_button;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel lang_panel;
    private javax.swing.JButton language_add_button;
    private javax.swing.JComboBox<String> language_combo_box;
    private javax.swing.JButton language_delete_button;
    private javax.swing.JButton load_tracking_button;
    private javax.swing.JButton lt_add_button;
    private javax.swing.JTextField lt_add_name_textfield;
    private javax.swing.JTextField lt_add_ne_lat_textfield;
    private javax.swing.JTextField lt_add_ne_long_textfield;
    private javax.swing.JTextField lt_add_sw_lat_textfield;
    private javax.swing.JTextField lt_add_sw_long_textfield;
    private javax.swing.JButton lt_delete_button;
    private javax.swing.JScrollPane lt_list_scrollpane;
    private javax.swing.JTable lt_list_table;
    private javax.swing.JPanel lt_panel;
    private javax.swing.JComboBox<String> network_logic_combo;
    private javax.swing.JLabel network_logic_label;
    private javax.swing.JCheckBox random_sample_chk;
    private javax.swing.JButton save_tracking_button;
    private javax.swing.JTabbedPane tracking_tab_panel;
    private javax.swing.JButton ut_add_button;
    private javax.swing.JButton ut_add_from_list_button;
    private javax.swing.JTextField ut_add_from_list_listname_textfield;
    private javax.swing.JTextField ut_add_from_list_user_textfield;
    private javax.swing.JTextField ut_add_textfield;
    private javax.swing.JButton ut_delete_button;
    private javax.swing.JScrollPane ut_list_scrollpane;
    private javax.swing.JTable ut_list_table;
    private javax.swing.JPanel ut_panel;
    private javax.swing.JLabel warning_new_project_label;
    private javax.swing.JButton wt_add_button;
    private javax.swing.JTextField wt_add_textfield;
    private javax.swing.JButton wt_delete_button;
    private javax.swing.JList<String> wt_lang_list;
    private javax.swing.JPanel wt_panel;
    private javax.swing.JList<String> wt_word_list;
    private javax.swing.JScrollPane wt_word_list_scrollpane;
    private javax.swing.JScrollPane wt_word_list_scrollpane1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
