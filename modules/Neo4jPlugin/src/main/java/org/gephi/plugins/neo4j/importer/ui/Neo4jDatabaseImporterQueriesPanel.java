package org.gephi.plugins.neo4j.importer.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Neo4jDatabaseImporterQueriesPanel extends javax.swing.JPanel {
    private JLabel nodeQueryLabel;
    private JButton checkNodeQUeryButton;
    private JLabel edgeQueryLabel;
    private JTextArea nodeQuery;
    private JTextArea edgeQuery;
    private JButton checkEdgeQueryButton;

    public Neo4jDatabaseImporterQueriesPanel() {
        checkNodeQUeryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        checkEdgeQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }
}
