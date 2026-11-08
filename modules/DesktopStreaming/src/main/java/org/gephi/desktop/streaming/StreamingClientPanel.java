/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
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

package org.gephi.desktop.streaming;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.gephi.streaming.api.StreamingEndpoint;
import org.gephi.streaming.api.StreamType;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * A JPanel to fill StreamingEndpoint information in order to connect to a stream.
 * @see StreamingEndpoint
 *
 * @author panisson
 */
public class StreamingClientPanel extends javax.swing.JPanel {

    // Default selected type is JSON
    private static final String DEFAULT_FORMAT = "JSON";

    private ComboBoxModel streamTypeComboBoxModel;

    /** Creates new form StreamingClientPanel */
    public StreamingClientPanel() {

        streamTypeComboBoxModel = new DefaultComboBoxModel(getStreamTypes());

        // Sets the default selected format
        for (int i=0; i<streamTypeComboBoxModel.getSize(); i++) {
            StreamType type = (StreamType)streamTypeComboBoxModel.getElementAt(i);
            if (type.getType().equalsIgnoreCase(DEFAULT_FORMAT)) {
                streamTypeComboBoxModel.setSelectedItem(type);
                break;
            }
        }

        initComponents();
    }

    /**
     * Create a StreamingEndpoint instance from the filled information.
     *
     * @return the StreamingEndpoint to connect to
     */
    public StreamingEndpoint getGraphStreamingEndpoint() {
        StreamingEndpoint endpoint = new StreamingEndpoint();
        endpoint.setStreamType((StreamType)streamTypeComboBox.getSelectedItem());
        try {
            endpoint.setUrl(new URL(streamUrlTextField.getText()));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (basicAuthCheckBox.isEnabled()) {
            endpoint.setUser(usernameTextField.getText());
            endpoint.setPassword(new String(passwordField.getPassword()));
        }
        return endpoint;
    }

    private static StreamType[] getStreamTypes() {
        Collection<? extends StreamType> streamTypes = Lookup.getDefault().lookupAll(StreamType.class);
        return streamTypes.toArray(new StreamType[0]);
    }

     public static ValidationPanel createValidationPanel(final StreamingClientPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            throw new NullPointerException();
        }
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();
        group.add(innerPanel.streamUrlTextField, StringValidators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.streamUrlTextField, StringValidators.URL_MUST_BE_VALID);

        return validationPanel;
     }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        streamUrlLabel = new javax.swing.JLabel();
        streamUrlTextField = new javax.swing.JTextField();
        streamTypeLabel = new javax.swing.JLabel();
        streamTypeComboBox = new javax.swing.JComboBox();
        basicAuthCheckBox = new javax.swing.JCheckBox();
        usernameLabel = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        passwordLabel = new javax.swing.JLabel();

        streamUrlLabel.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.streamUrlLabel.text")); // NOI18N

        streamUrlTextField.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.sourceURL.text")); // NOI18N
        streamUrlTextField.setName("sourceURL"); // NOI18N

        streamTypeLabel.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.streamTypeLabel.text")); // NOI18N

        streamTypeComboBox.setModel(streamTypeComboBoxModel);

        basicAuthCheckBox.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.basicAuthCheckBox.text")); // NOI18N
        basicAuthCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                basicAuthCheckBoxStateChanged(evt);
            }
        });

        usernameLabel.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.usernameLabel.text")); // NOI18N
        usernameLabel.setEnabled(false);

        usernameTextField.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.usernameTextField.text")); // NOI18N
        usernameTextField.setEnabled(false);

        passwordField.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.passwordField.text")); // NOI18N
        passwordField.setEnabled(false);

        passwordLabel.setText(org.openide.util.NbBundle.getMessage(StreamingClientPanel.class, "StreamingClientPanel.passwordLabel.text")); // NOI18N
        passwordLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(streamUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                    .addComponent(streamUrlLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(streamTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(streamTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(basicAuthCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(usernameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(passwordLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(streamUrlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(streamUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(streamTypeLabel)
                    .addComponent(streamTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(basicAuthCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void basicAuthCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_basicAuthCheckBoxStateChanged
        usernameLabel.setEnabled(basicAuthCheckBox.isSelected());
        usernameTextField.setEnabled(basicAuthCheckBox.isSelected());
        passwordLabel.setEnabled(basicAuthCheckBox.isSelected());
        passwordField.setEnabled(basicAuthCheckBox.isSelected());
}//GEN-LAST:event_basicAuthCheckBoxStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox basicAuthCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JComboBox streamTypeComboBox;
    private javax.swing.JLabel streamTypeLabel;
    private javax.swing.JLabel streamUrlLabel;
    private javax.swing.JTextField streamUrlTextField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables

}
