/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.neo4j.ui;

import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbPreferences;

/**
 *
 * @author Martin Škurla
 */
public class RemoteDatabasePanel extends javax.swing.JPanel {

    private static String URL = "Neo4jRemoteDatabasePanel_Url";
    private static String LOGIN = "Neo4jRemoteDatabasePanel_Login";
    private static String PASSWD = "Neo4jRemoteDatabasePanel_Passwd";

    public RemoteDatabasePanel() {
        initComponents();

        String url = NbPreferences.forModule(RemoteDatabasePanel.class).get(URL, "");
        String login = NbPreferences.forModule(RemoteDatabasePanel.class).get(LOGIN, "");
        String passwd = NbPreferences.forModule(RemoteDatabasePanel.class).get(PASSWD, "");
        remoteDatabaseUrlTextField.setText(url);
        loginTextField.setText(login);
        passwordTextField.setText(passwd);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();
        remoteDatabaseUrlLabel = new javax.swing.JLabel();
        remoteDatabaseUrlTextField = new javax.swing.JTextField();
        loginLabel = new javax.swing.JLabel();
        loginTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();

        contentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.contentPanel.border.title"))); // NOI18N

        remoteDatabaseUrlLabel.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.remoteDatabaseUrlLabel.text")); // NOI18N

        remoteDatabaseUrlTextField.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.remote database URL.text")); // NOI18N
        remoteDatabaseUrlTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.remote database URL.toolTipText")); // NOI18N
        remoteDatabaseUrlTextField.setName("remote database URL"); // NOI18N

        loginLabel.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.loginLabel.text")); // NOI18N

        loginTextField.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.login.text")); // NOI18N
        loginTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.login.toolTipText")); // NOI18N
        loginTextField.setName("login"); // NOI18N

        passwordLabel.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.passwordLabel.text")); // NOI18N

        passwordTextField.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.passwordTextField.text")); // NOI18N
        passwordTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.passwordTextField.toolTipText")); // NOI18N

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel)
                    .addComponent(loginLabel)
                    .addComponent(remoteDatabaseUrlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loginTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(remoteDatabaseUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(passwordTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                .addContainerGap())
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(remoteDatabaseUrlLabel)
                    .addComponent(remoteDatabaseUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loginLabel)
                    .addComponent(loginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel loginLabel;
    private javax.swing.JTextField loginTextField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JLabel remoteDatabaseUrlLabel;
    private javax.swing.JTextField remoteDatabaseUrlTextField;
    // End of variables declaration//GEN-END:variables

    public ValidationPanel createValidationPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);
        ValidationGroup group = validationPanel.getValidationGroup();

        //Validators
        group.add(remoteDatabaseUrlTextField, Validators.URL_MUST_BE_VALID);
        group.add(loginTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(passwordTextField, Validators.REQUIRE_NON_EMPTY_STRING);

        return validationPanel;
    }

    public String getRemoteUrl() {
        NbPreferences.forModule(RemoteDatabasePanel.class).put(URL, remoteDatabaseUrlTextField.getText().trim());
        return remoteDatabaseUrlTextField.getText().trim();
    }

    public String getLogin() {
        NbPreferences.forModule(RemoteDatabasePanel.class).put(LOGIN, loginTextField.getText().trim());
        return loginTextField.getText().trim();
    }

    public String getPassword() {
        NbPreferences.forModule(RemoteDatabasePanel.class).put(PASSWD, passwordTextField.getText().trim());
        return passwordTextField.getText().trim();
    }
}
