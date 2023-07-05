package complexGenerator.BalancedTree;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BalancedTreePanel extends JPanel {
    @Getter
    @Setter
    private JTextField rField, hField;

    @Getter
    private int rValue, hValue;
    private JLabel rLabel, hLabel, messageLabel;

    public BalancedTreePanel()  {
        setLayout(new GridBagLayout());

        // Initialize components
        rLabel = new JLabel("Enter r:");
        hLabel = new JLabel("Enter h:");
        rField = new JTextField(10);
        hField = new JTextField(10);
        messageLabel = new JLabel();

        // Add components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(rLabel, constraints);

        constraints.gridx = 1;
        add(rField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        add(hLabel, constraints);

        constraints.gridx = 1;
        add(hField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        add(messageLabel, constraints);
    }

    public boolean parseValues(){
        String rText = rField.getText();
        String hText = hField.getText();
        try {
            rValue = Integer.parseInt(rText);
            hValue = Integer.parseInt(hText);
            return true;
        } catch (NumberFormatException ex) {
            messageLabel.setText("Please enter valid integers.");
            return false;
        }
    }
}
