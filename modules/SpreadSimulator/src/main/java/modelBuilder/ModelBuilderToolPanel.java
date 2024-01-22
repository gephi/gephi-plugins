package modelBuilder;


import configLoader.ConfigLoader;

import javax.swing.*;

public class ModelBuilderToolPanel extends JPanel {
    private JLabel statusLabel;

    public ModelBuilderToolPanel() {
        this.initComponents();
    }

    public void setStatus(String status) {
        this.statusLabel.setText(status);
    }

    private void initComponents() {
        this.statusLabel = new JLabel();
        this.statusLabel.setFont(this.statusLabel.getFont().deriveFont(10.0F));
        this.statusLabel.setText(ConfigLoader.modelBuilderToolInfoStatusCreate);
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
    }
}
