package org.gephi.plugins.neo4j.importer.ui;

import org.gephi.plugins.neo4j.importer.Neo4jDatabaseImporter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Neo4jDatabaseImporterConnectionPanel extends javax.swing.JPanel {

    private JTextField dbUrl;
    private JLabel dbUrlLabel;
    private JTextField dbUsername;
    private JPasswordField dbPassword;
    private JLabel dbUsernameLabel;
    private JLabel dbPasswordLabel;
    private JButton checkConnectivity;
    private JTextField dbName;
    private JLabel dbNameLabel;
    private JLabel error;
    private JLabel success;


    public Neo4jDatabaseImporterConnectionPanel() {

        checkConnectivity.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                checkConnectivity.setEnabled(false);
                dbUsername.setEnabled(false);
                dbPassword.setEnabled(false);
                dbUrl.setEnabled(false);
                dbName.setEnabled(false);
                try {
                    error.setVisible(false);
                    success.setVisible(false);
                    // TODO: need to check inputs
                    Neo4jDatabaseImporter.checkConnection(dbUrl.getText(), dbUsername.getText(), dbPassword.getText(), dbName.getText());
                    success.setVisible(true);
                } catch (Exception e) {
                    error.setVisible(true);
                    error.setText(e.getMessage());
                }
                finally {
                    checkConnectivity.setEnabled(true);
                    dbUsername.setEnabled(true);
                    dbPassword.setEnabled(true);
                    dbUrl.setEnabled(true);
                    dbName.setEnabled(true);
                }
            }
        });
    }
}
