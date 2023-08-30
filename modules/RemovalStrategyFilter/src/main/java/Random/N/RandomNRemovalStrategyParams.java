package Random.N;

import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class RandomNRemovalStrategyParams extends JPanel {

    private FilterProperty N;
    private FilterProperty Seed;

    private JTextField nField;
    private JTextField seedField;

    private Filter Filter;

    public RandomNRemovalStrategyParams(Filter filter) {
        Filter = filter;
        N = filter.getProperties()[0];
        Seed = filter.getProperties()[1];



        // Initialize text fields with default values
        nField = new JTextField(N.getValue().toString());
        seedField = new JTextField(Seed.getValue().toString());

        // Apply DocumentFilter to JTextField to allow only integers
        ((AbstractDocument) nField.getDocument()).setDocumentFilter(new IntFilter());
        ((AbstractDocument) seedField.getDocument()).setDocumentFilter(new IntFilter());


        // Add DocumentListener to update N and Seed
        nField.getDocument().addDocumentListener(new MyDocumentListener() {
            @Override
            public void update(DocumentEvent e) {
                try {
                    N.setValue(Integer.parseInt(nField.getText()));
                } catch (NumberFormatException ex) {
                    // Handle invalid input
                }
            }
        });

        seedField.getDocument().addDocumentListener(new MyDocumentListener() {
            @Override
            public void update(DocumentEvent e) {
                try {
                    Seed.setValue(Integer.parseInt(seedField.getText()));
                } catch (NumberFormatException ex) {
                    // Handle invalid input
                }
            }
        });

        // Add labels and fields to the panel
        add(new JLabel("N:"));
        add(nField);

        add(new JLabel("Seed:"));
        add(seedField);
    }

    abstract class MyDocumentListener implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        public abstract void update(DocumentEvent e);
    }

    // DocumentFilter that allows only integer input
    class IntFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("\\d*")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text.matches("\\d*")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
