package complexGenerator.BalancedTree;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class BalancedTreePanel extends JPanel {
    @Getter
    @Setter
    private JTextField rField, hField;

    @Getter
    private int rValue, hValue;
    private JLabel rLabel, hLabel;

    public BalancedTreePanel() {
        setLayout(new GridBagLayout());

        // Initialize components
        rLabel = new JLabel("Enter r:");
        hLabel = new JLabel("Enter h:");
        rField = new JTextField(10);
        hField = new JTextField(10);

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

        // Document filter to only allow digits
        DocumentFilter onlyDigitFilter = new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d*")) {
                    super.insertString(fb, offset, string, attr);
                    parseValues();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                    parseValues();
                }
            }
        };

        // Set the document filter on the text fields
        ((AbstractDocument) rField.getDocument()).setDocumentFilter(onlyDigitFilter);
        ((AbstractDocument) hField.getDocument()).setDocumentFilter(onlyDigitFilter);
    }

    private void parseValues() {
        rValue = parseValue(rField, "Invalid r - must be integer.");
        hValue = parseValue(hField, "Invalid h - must be integer.");
    }

    private int parseValue(JTextField value, String message) {
        try {
            return Integer.parseInt(value.getText());
        } catch (NumberFormatException ex) {
        }
        value.setText("0");
        return 0;
    }
}
