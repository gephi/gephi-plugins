/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.columns.merge.ui;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.columns.merge.ColumnCalculator;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Table;
import org.openide.util.NbPreferences;
import org.gephi.graph.api.Column;

/**
 *
 * @author XGG3
 * @editor oscarfont
 */
public class ColumnCalculatorUI extends javax.swing.JPanel implements ManipulatorUI {

    private ColumnCalculator manipulator;
    private DialogControls dialogControls;
    private Table table;
    
    /**
     * Creates new form ColumnCalculatorUI
     */
    public ColumnCalculatorUI() {
        initComponents();
        formulaTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                refreshOkButton();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshOkButton();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshOkButton();
            }
            
            private void refreshOkButton() {
                String formulaText = formulaTextField.getText();
                String columnText = titleTextField.getText();
                dialogControls.setOkButtonEnabled((formulaText != null && columnText != null) && (!formulaText.isEmpty() && !columnText.isEmpty()) && table != null && !table.hasColumn(columnText));//Title not empty and not repeated.
            }
        });
    }
    
    public String generateSelectedColumnsLabel(ColumnCalculator mani){
        
        String outputLabel = "<html><table><tr><td style='width:150px'>Column Name</td><td style='width:150px'>Column Variable</td></tr>";
        
        Column[] columnas = manipulator.getColumns();
        for (Column columna : columnas){
            String columnTitle = columna.getTitle();
            String columnIndex = columna.getId();
            outputLabel += "<tr><td style='width:150px'>" + columnTitle + "</td><td style='width:150px'>" + columnIndex + "</td></tr>"; 
        }
        
        outputLabel += "</table></html>";
                
        return outputLabel;
    }
    
    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        //Receive our manipulator instance:
        this.manipulator = (ColumnCalculator) m; //We know the type of manipulator we are going to receive so cast is safe
        //And an object to control the dialog if necessary 
        this.table = this.manipulator.getTable();
        //(for now it only is able to enable/disable the Ok button of the dialog for validation purposes)
        this.dialogControls = dialogControls;
        
        String tableDescription = generateSelectedColumnsLabel(this.manipulator);
        selectedColumns.setText(tableDescription);
    }

    @Override
    public void unSetup() {
        //Called when the dialog is closed, canceled or accepted. Pass necessary data to the manipulator:
        //manipulator.setSomeOption(someValue);
        //TODO..
        //Añadimos titulo de la nueva columna y formula al manipulator
        manipulator.setColumnTitle(titleTextField.getText());
        manipulator.setCustomFormula(formulaTextField.getText());
        //Enviamos a la calse ColumnCalculator el titulo de la columna y la formula introducida
        NbPreferences.forModule(ColumnCalculator.class).put(ColumnCalculator.COLUMN_TITLE_SAVED_PREFERENCES, manipulator.getColumnTitle());
        NbPreferences.forModule(ColumnCalculator.class).put(ColumnCalculator.CUSTOM_FORMULA_SAVED_PREFERENCES, manipulator.getCustomFormula());
    }

    @Override
    public String getDisplayName() {
        //Provide title for the dialog:
        return manipulator.getName();//For example, the manipulator name
    }
    
    @Override
    public JPanel getSettingsPanel() {
        //Provide the JPanel to create the UI dialog
        //A good practice is to extend JPanel and just return this object
        return this;
    }
    
    @Override
    public boolean isModal() {
        return true;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        formulaLabel = new javax.swing.JLabel();
        formulaTextField = new javax.swing.JTextField();
        titleDescriptionLabel = new javax.swing.JLabel();
        selectedColumnsLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        selectedColumns = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(formulaLabel, org.openide.util.NbBundle.getMessage(ColumnCalculatorUI.class, "ColumnCalculatorUI.formulaLabel.text")); // NOI18N

        formulaTextField.setText(org.openide.util.NbBundle.getMessage(ColumnCalculatorUI.class, "ColumnCalculatorUI.formulaTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(titleDescriptionLabel, org.openide.util.NbBundle.getMessage(ColumnCalculatorUI.class, "ColumnCalculatorUI.titleDescriptionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectedColumnsLabel, org.openide.util.NbBundle.getMessage(ColumnCalculatorUI.class, "ColumnCalculatorUI.selectedColumnsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(titleLabel, org.openide.util.NbBundle.getMessage(ColumnCalculatorUI.class, "ColumnCalculatorUI.titleLabel.text")); // NOI18N

        titleTextField.setText(org.openide.util.NbBundle.getMessage(ColumnCalculatorUI.class, "ColumnCalculatorUI.titleTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectedColumns, org.openide.util.NbBundle.getMessage(ColumnCalculatorUI.class, "ColumnCalculatorUI.selectedColumns.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedColumns)
                    .addComponent(selectedColumnsLabel)
                    .addComponent(titleDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(titleLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(formulaLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(formulaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(selectedColumnsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectedColumns)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formulaLabel)
                    .addComponent(formulaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(76, 76, 76))
        );
    }// </editor-fold>//GEN-END:initComponents

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel formulaLabel;
    private javax.swing.JTextField formulaTextField;
    private javax.swing.JLabel selectedColumns;
    private javax.swing.JLabel selectedColumnsLabel;
    private javax.swing.JLabel titleDescriptionLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    // End of variables declaration//GEN-END:variables


}
