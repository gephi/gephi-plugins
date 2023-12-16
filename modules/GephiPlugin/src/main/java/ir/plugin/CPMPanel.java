/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.plugin;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Mir Saman Tajbakhsh
 */
public class CPMPanel extends JPanel {

    JTextField kvalue;

    @SuppressWarnings("unchecked")
    public CPMPanel() {
        //this.setLayout(null);
        JLabel jXHeader1 = new JLabel();

        jXHeader1.setText("Enter the value for k (clique size, ex: k = 3 will find triangualrs). Higher values of k may take more time for computation. This algorithm is NP-Hard, so use it carefully."); // NOI18N

        JLabel label = new JLabel("Enter value of k here:");
        label.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 13));
        this.add(label);
        kvalue = new JTextField();
        this.add(kvalue);
        Insets insets = this.getInsets();

        Dimension size = label.getPreferredSize();
        label.setBounds(20 + insets.left, 30 + insets.top, size.width, size.height);

        Dimension size1 = kvalue.getPreferredSize();
        kvalue.setBounds(20 + insets.left, 130 + insets.top, size1.width + 20, size1.height);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jXHeader1, GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(label)
                        .addContainerGap(354, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(kvalue)
                        .addContainerGap(382, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(jXHeader1, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(kvalue)
                        .addContainerGap(187, Short.MAX_VALUE))
        );
    }

    public int getK() {
        int i = 0;
        try {
            i = Integer.valueOf(kvalue.getText());
        } catch (Exception ex) {
            return 0;
        }
        return i;
    }

    public void setK(int k) {
        this.kvalue.setText(String.valueOf(k));
    }
}
