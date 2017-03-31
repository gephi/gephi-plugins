/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bitnine.importer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.io.importer.spi.DatabaseImporterBuilder;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;

/**
 *
 * @author dehowefeng
 */

@ConvertAsProperties(dtd = "//org.bitnine//AgensGraphImport//EN", autostore = false)
@TopComponent.Description(preferredID = "AgensGraphImportPanel",
	persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@ActionID(category = "Window", id = "org.bitnine.AgensGraphImportPanel")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(displayName = "#CTL_AgensGraphImportPanel",
	preferredID = "AgensGraphImportPanel")
@TopComponent.Registration(mode = "editor", openAtStartup = true, roles = {"overview"})

public class AgensGraphImportPanel extends TopComponent /*javax.swing.JPanel*/ {
    
    private AgensGraphDatabaseManager databaseManager;
    private static String NEW_CONFIGURATION_NAME
            = NbBundle.getMessage(AgensGraphImportPanel.class,
                    "AgensGraphImportPanel.template.name");
    private boolean inited = false;
    
    private LongTaskExecutor executor;
    private LongTaskErrorHandler errorHandler;
    private ImportController controller;
    
    /**
     * Creates new form AgensGraphImportPanel
     */
    public AgensGraphImportPanel() {
        databaseManager = new AgensGraphDatabaseManager();
        
        initComponents();
        setName(
		NbBundle.getMessage(
				AgensGraphImportPanel.class,
				"CTL_AgensGraphImportPanel"));
        
    }
    static ValidationGroup group;
    
        public static ValidationPanel createValidationPanel(AgensGraphImportPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            throw new NullPointerException();
        }
        validationPanel.setInnerComponent(innerPanel);

        group = validationPanel.getValidationGroup();

        //Validators
        group.add(innerPanel.configNameTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.hostTextField, new AgensGraphImportPanel.HostOrFileValidator(innerPanel));
        group.add(innerPanel.dbTextField, new AgensGraphImportPanel.NotEmptyValidator(innerPanel));
        group.add(innerPanel.portTextField, new AgensGraphImportPanel.PortValidator(innerPanel));
        group.add(innerPanel.userTextField, new AgensGraphImportPanel.NotEmptyValidator(innerPanel));

        return validationPanel;
    }
        
    private void initDriverType() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                    hostLabel.setText(NbBundle.getMessage(AgensGraphImportPanel.class, "AgensGraphImportPanel.hostLabel.text"));
                    portTextField.setEnabled(true);
                    portLabel.setEnabled(true);
                    dbLabel.setEnabled(true);
                    dbTextField.setEnabled(true);
                    userLabel.setEnabled(true);
                    userTextField.setEnabled(true);
                    pwdLabel.setEnabled(true);
                    pwdTextField.setEnabled(true);
                    //group.validateAll(); //commented out for now
            }
        });
   
    }

    public Database getSelectedDatabase() {
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();

        populateEdgeListDatabase(item.db);

        // add configuration if user changed the template configuration
        if (item.equals(model.templateConfiguration)) {
            databaseManager.addDatabase(item.db);
        }

        databaseManager.persist();

        return item.db;
    }
    
    public SQLDriver getSelectedSQLDriver() {
        return (SQLDriver) new AgensGraphDriver();
    }

    /*public void setSQLDrivers(SQLDriver[] drivers) {
        DefaultComboBoxModel driverModel = new DefaultComboBoxModel(drivers);
        driverComboBox.setModel(driverModel);
    }*/

    public void setup() {
        configurationCombo.setModel(new AgensGraphImportPanel.ConfigurationComboModel());
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        if (model.getSelectedItem().equals(model.templateConfiguration)) {
            this.removeConfigurationButton.setEnabled(false);
        } else {
            this.removeConfigurationButton.setEnabled(true);
        }
        inited = true;
        group.validateAll();
    }
    
        private void populateForm(EdgeListDatabaseImpl db) {
        configNameTextField.setText(db.getName());
        dbTextField.setText(db.getDBName());
        hostTextField.setText(db.getHost());
        portTextField.setText(db.getPort() == 0 ? "" : "" + db.getPort());
        userTextField.setText(db.getUsername());
        pwdTextField.setText(db.getPasswd());
        nodeQueryTextField.setText(db.getNodeQuery());
        edgeQueryTextField.setText(db.getEdgeQuery());

        initDriverType();
    }

    private void populateEdgeListDatabase(EdgeListDatabaseImpl db) {
        db.setName(this.configNameTextField.getText());
        db.setDBName(this.dbTextField.getText());
        db.setHost(this.hostTextField.getText());
        db.setPasswd(new String(this.pwdTextField.getPassword()));
        db.setPort(!portTextField.getText().isEmpty()
                ? Integer.parseInt(portTextField.getText()) : 0);
        db.setUsername(this.userTextField.getText());
        db.setSQLDriver(this.getSelectedSQLDriver());
        db.setNodeQuery(this.nodeQueryTextField.getText());
        db.setEdgeQuery(this.edgeQueryTextField.getText());
        db.setNodeAttributesQuery("");
        db.setEdgeAttributesQuery("");
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JPanel1 = new javax.swing.JTabbedPane();
        connectionPanel = new javax.swing.JPanel();
        configurationLabel = new javax.swing.JLabel();
        configNameLabel = new javax.swing.JLabel();
        configNameTextField = new javax.swing.JTextField();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        removeConfigurationButton = new javax.swing.JButton();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        dbLabel = new javax.swing.JLabel();
        dbTextField = new javax.swing.JTextField();
        pwdLabel = new javax.swing.JLabel();
        graphPathLabel = new javax.swing.JLabel();
        graphPathTextField = new javax.swing.JTextField();
        testConnectionButton = new javax.swing.JButton();
        configurationCombo = new javax.swing.JComboBox<>();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        pwdTextField = new javax.swing.JPasswordField();
        queryPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        nodeQueryTextField = new javax.swing.JTextArea();
        nodeQueryLabel = new javax.swing.JLabel();
        edgeQueryLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        edgeQueryTextField = new javax.swing.JTextArea();
        executeButton = new javax.swing.JButton();

        configurationLabel.setText("Configuration:");

        configNameLabel.setText("Configuration Name:");

        configNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNameTextFieldActionPerformed(evt);
            }
        });

        hostLabel.setText("Host:");

        removeConfigurationButton.setText("x");
        removeConfigurationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeConfigurationButtonActionPerformed(evt);
            }
        });

        portLabel.setText("Port:");

        dbLabel.setText("Database:");

        pwdLabel.setText("Password:");

        graphPathLabel.setText("Graph Path:");

        testConnectionButton.setText("Test Connection");
        testConnectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionButtonActionPerformed(evt);
            }
        });

        configurationCombo.setModel(new AgensGraphImportPanel.ConfigurationComboModel());
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });

        userLabel.setText("Username:");

        javax.swing.GroupLayout connectionPanelLayout = new javax.swing.GroupLayout(connectionPanel);
        connectionPanel.setLayout(connectionPanelLayout);
        connectionPanelLayout.setHorizontalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connectionPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(testConnectionButton)
                .addGap(43, 43, 43))
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configNameLabel)
                    .addComponent(hostLabel)
                    .addComponent(portLabel)
                    .addComponent(dbLabel)
                    .addComponent(graphPathLabel)
                    .addComponent(configurationLabel)
                    .addComponent(pwdLabel)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(connectionPanelLayout.createSequentialGroup()
                        .addComponent(configurationCombo, 0, 459, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(configNameTextField)
                    .addComponent(portTextField)
                    .addComponent(dbTextField)
                    .addComponent(graphPathTextField)
                    .addComponent(hostTextField)
                    .addComponent(pwdTextField)
                    .addComponent(userTextField))
                .addGap(24, 24, 24))
        );
        connectionPanelLayout.setVerticalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configurationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeConfigurationButton)
                    .addComponent(configurationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configNameLabel)
                    .addComponent(configNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostLabel)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(portLabel)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbLabel)
                    .addComponent(dbTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userLabel)
                    .addComponent(userTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(connectionPanelLayout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(graphPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(graphPathLabel))
                        .addGap(38, 38, 38)
                        .addComponent(testConnectionButton))
                    .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(pwdLabel)
                        .addComponent(pwdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        JPanel1.addTab("Connection", connectionPanel);

        nodeQueryTextField.setColumns(20);
        nodeQueryTextField.setRows(5);
        jScrollPane1.setViewportView(nodeQueryTextField);

        nodeQueryLabel.setText("Node Query:");

        edgeQueryLabel.setText("Edge Query:");

        edgeQueryTextField.setColumns(20);
        edgeQueryTextField.setRows(5);
        jScrollPane2.setViewportView(edgeQueryTextField);

        javax.swing.GroupLayout queryPanelLayout = new javax.swing.GroupLayout(queryPanel);
        queryPanel.setLayout(queryPanelLayout);
        queryPanelLayout.setHorizontalGroup(
            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                    .addGroup(queryPanelLayout.createSequentialGroup()
                        .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nodeQueryLabel)
                            .addComponent(edgeQueryLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        queryPanelLayout.setVerticalGroup(
            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryPanelLayout.createSequentialGroup()
                .addComponent(nodeQueryLabel)
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(edgeQueryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(83, Short.MAX_VALUE))
        );

        JPanel1.addTab("Query", queryPanel);

        executeButton.setText("Execute");
        executeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(JPanel1)
            .addComponent(executeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(JPanel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(executeButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_configNameTextFieldActionPerformed

    private void testConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionButtonActionPerformed
            if (!portTextField.getText().isEmpty()) {
            try {
                Integer.parseInt(portTextField.getText());
            } catch (Exception e) {
                return;
            }
        }
        Connection conn = null;
        try {
            conn = getSelectedSQLDriver().getConnection(SQLUtils.getUrl(getSelectedSQLDriver(), hostTextField.getText(), (portTextField.getText().isEmpty() ? 0 : Integer.parseInt(portTextField.getText())), dbTextField.getText()), userTextField.getText(), new String(pwdTextField.getPassword()));
            String message = NbBundle.getMessage(AgensGraphImportPanel.class, "AgensGraphImportPanel.alert.connection_successful");
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
        } catch (SQLException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    Logger.getLogger("").info("Database connection terminated");
                } catch (Exception e) {
                    /* ignore close errors */ }
            }
        }    // TODO add your handling code here:
    }//GEN-LAST:event_testConnectionButtonActionPerformed

    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_configurationComboActionPerformed

    private void removeConfigurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeConfigurationButtonActionPerformed
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();

        if (databaseManager.removeDatabase(item.db)) {

            model.removeElement(item);
            databaseManager.persist();
            String message = NbBundle.getMessage(AgensGraphImportPanel.class,
                    "AgensGraphImportPanel.alert.configuration_removed", item.toString());
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
            model.setSelectedItem(model.getElementAt(0));

        } else {
            String message = NbBundle.getMessage(AgensGraphImportPanel.class,
                    "AgensGraphImportPanel.alert.configuration_unsaved");
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_removeConfigurationButtonActionPerformed

    private void executeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeButtonActionPerformed
        executeAgensImport();// TODO add your handling code here:
    }//GEN-LAST:event_executeButtonActionPerformed

    
    
    public void executeAgensImport(){

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        
        AgensGraphDatabaseImpl db = new AgensGraphDatabaseImpl();
        
        ImporterAgensGraph agensGraphImporter = new ImporterAgensGraph();
        Container container = importController.importDatabase(db, agensGraphImporter);
        
        
 
//Import database
        
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane JPanel1;
    private javax.swing.JLabel configNameLabel;
    private javax.swing.JTextField configNameTextField;
    private javax.swing.JComboBox<String> configurationCombo;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JLabel dbLabel;
    private javax.swing.JTextField dbTextField;
    private javax.swing.JLabel edgeQueryLabel;
    private javax.swing.JTextArea edgeQueryTextField;
    private javax.swing.JButton executeButton;
    private javax.swing.JLabel graphPathLabel;
    private javax.swing.JTextField graphPathTextField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel nodeQueryLabel;
    private javax.swing.JTextArea nodeQueryTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JLabel pwdLabel;
    private javax.swing.JPasswordField pwdTextField;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JButton removeConfigurationButton;
    private javax.swing.JButton testConnectionButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    public void initEvents() {
    }

    private class ConfigurationComboModel extends DefaultComboBoxModel {

        /**
         * The template configuration (will appear as "New Configuration")
         */
        ConfigurationComboItem templateConfiguration;

        public ConfigurationComboModel() {
            super();
            Collection<Database> configs = databaseManager.getEdgeListDatabases();
            for (Database db : configs) {
                EdgeListDatabaseImpl dbe = (EdgeListDatabaseImpl) db;
                ConfigurationComboItem item = new ConfigurationComboItem(dbe);
                this.insertElementAt(item, this.getSize());
            }

            // add template configuration option at end
            EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
            populateEdgeListDatabase(db);
            templateConfiguration = new ConfigurationComboItem(db);
            templateConfiguration.setConfigurationName(NEW_CONFIGURATION_NAME);
            this.insertElementAt(templateConfiguration, this.getSize());

            ConfigurationComboItem selected = (ConfigurationComboItem) this.getElementAt(0);
            this.setSelectedItem(selected);

            //driverComboBox.setSelectedItem(selected.db.getSQLDriver());
        }

        @Override
        public void setSelectedItem(Object anItem) {
            ConfigurationComboItem item = (ConfigurationComboItem) anItem;
            populateForm(item.db);
            super.setSelectedItem(anItem);
        }
    }

    private class ConfigurationComboItem {

        private final EdgeListDatabaseImpl db;
        private String configurationName;

        public ConfigurationComboItem(EdgeListDatabaseImpl db) {
            this.db = db;
            this.configurationName = db.getName();
        }

        public EdgeListDatabaseImpl getDb() {
            return db;
        }

        public void setConfigurationName(String configurationName) {
            this.configurationName = configurationName;
        }

        @Override
        public String toString() {
            String name = configurationName;
            if (name == null || name.isEmpty()) {
                name = SQLUtils.getUrl(db.getSQLDriver(), db.getHost(), db.getPort(), db.getDBName());
            }
            return name;
        }
    }

    private static class HostOrFileValidator implements Validator<String> {

        private AgensGraphImportPanel panel;

        public HostOrFileValidator(AgensGraphImportPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            } else {
                return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model);
            }
        }
    }

    private static class NotEmptyValidator implements Validator<String> {

        private AgensGraphImportPanel panel;

        public NotEmptyValidator(AgensGraphImportPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            } else {
                return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model);
            }
        }
    }

    private static class PortValidator implements Validator<String> {

        private AgensGraphImportPanel panel;

        public PortValidator(AgensGraphImportPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            } else {
                return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model)
                        && Validators.REQUIRE_VALID_INTEGER.validate(problems, compName, model)
                        && Validators.numberRange(1, 65535).validate(problems, compName, model);
            }
        }
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


