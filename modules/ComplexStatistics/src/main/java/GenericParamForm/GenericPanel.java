package GenericParamForm;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GenericPanel<TParams extends Params> extends JPanel {
    @Getter @Setter
    private TParams tParams;
    Map<String, InputElement> mapParams;
    protected abstract void CreateParamObject();

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

    DocumentFilter doubleFilter = new DocumentFilter() {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("[0-9]*\\.?[0-9]*")) {
                super.insertString(fb, offset, string, attr);
                parseValues();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text.matches("[0-9]*\\.?[0-9]*")) {
                super.replace(fb, offset, length, text, attrs);
                parseValues();
            }
        }
    };

    ItemListener checkboxListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            parseValues();
        }
    };


    public GenericPanel() {
        CreateParamObject();

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets = new Insets(5, 10, 5, 10);

        constraints.gridwidth = 5;
        String shortDescription = tParams.ShortDescription();
        int length = shortDescription.length();
        int approxLineLength = 30; // assuming 30 characters per line
        int rows = (length / approxLineLength) + 1;
        JTextArea shortDescriptionTextArea = new JTextArea(shortDescription, rows, approxLineLength);
        shortDescriptionTextArea.setEditable(false);
        shortDescriptionTextArea.setWrapStyleWord(true);
        shortDescriptionTextArea.setLineWrap(true);
        shortDescriptionTextArea.setPreferredSize(null);
        add(shortDescriptionTextArea, constraints);
        constraints.anchor = GridBagConstraints.CENTER;

        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridwidth = 5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        JLabel separatorLabel = new JLabel();
        separatorLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        add(separatorLabel, constraints);
        constraints.fill = GridBagConstraints.NONE;


        AtomicInteger gridYIterator = new AtomicInteger(2); // Starts from 2 because 0 and 1 are taken by short descriptions

        constraints.gridwidth = 1;
        var description = tParams.Descritpion();
        description.forEach(x -> {
            constraints.gridy = gridYIterator.get();
            constraints.gridx = 0;
            constraints.anchor = GridBagConstraints.WEST;
            add(new JLabel(x.toString()), constraints);
            gridYIterator.getAndIncrement();

        });

        mapParams = new HashMap<>();

        gridYIterator.set(2);
        var allFields = tParams.getClass().getDeclaredFields();
        Arrays.stream(allFields).forEach(field -> {

            if (field.getType().equals(Integer.class)) {

                var fieldName = field.getName();

                var fieldValue = "0";
                try {
                    fieldValue = field.get(tParams).toString();
                } catch (IllegalAccessException e) {
                }

                var newLabel = new JLabel(fieldName);
                var newField = new JTextField();

                newField.setText(fieldValue);
                newField.setColumns(6);

                var newInputElement = new InputElement(newField, newLabel);
                mapParams.put(fieldName, newInputElement);

                constraints.gridy = gridYIterator.get();
                constraints.gridx = 2;
                constraints.anchor = GridBagConstraints.EAST;
                add(newLabel, constraints);

                constraints.gridx = 3;
                constraints.anchor = GridBagConstraints.WEST;
                add(newField, constraints);

                gridYIterator.getAndIncrement();

                ((AbstractDocument) newField.getDocument()).setDocumentFilter(onlyDigitFilter);
            } else if (field.getType().equals(Double.class)) {

                var fieldName = field.getName();
                var fieldValue = "0";
                try {
                    fieldValue = field.get(tParams).toString();
                } catch (IllegalAccessException e) {
                }

                var newLabel = new JLabel(fieldName);
                var newField = new JTextField();

                newField.setText(fieldValue);
                newField.setColumns(6);

                var newInputElement = new InputElement(newField, newLabel);
                mapParams.put(fieldName, newInputElement);

                constraints.gridy = gridYIterator.get();
                constraints.gridx = 2;
                constraints.anchor = GridBagConstraints.EAST;
                add(newLabel, constraints);

                constraints.gridx = 3;
                constraints.anchor = GridBagConstraints.WEST;
                add(newField, constraints);

                gridYIterator.getAndIncrement();
                ((AbstractDocument) newField.getDocument()).setDocumentFilter(doubleFilter);
            } else if (field.getType().equals(Boolean.class)) {
                var fieldName = field.getName();
                var newLabel = new JLabel(fieldName);
                var newField = new JCheckBox();
                var newInputElement = new InputElement(newField, newLabel);
                mapParams.put(fieldName, newInputElement);

                constraints.gridy = gridYIterator.get();
                constraints.gridx = 2;
                constraints.anchor = GridBagConstraints.EAST;
                add(newLabel, constraints);

                constraints.gridx = 3;
                constraints.anchor = GridBagConstraints.WEST;
                add(newField, constraints);

                gridYIterator.getAndIncrement();

                newField.addItemListener(checkboxListener);
            }  else {
                throw new IllegalStateException("Unexpected value: " + field.getType());
            }
        });

        JPanel separator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.drawLine(0, 0, 0, getHeight());
            }
        };
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.VERTICAL;
        add(separator, constraints);
    }

    private void parseValues(){
        var allFields = tParams.getClass().getDeclaredFields();
        Arrays.stream(allFields).forEach(field -> {
            InputElement element = mapParams.get(field.getName());
            try{
                if (field.getType().equals(Integer.class)) {
                    var textValue = element.intTextFields.getText();
                    field.set(tParams, Integer.valueOf(textValue));
                }
                else if (field.getType().equals(Double.class)) {
                    var textValue = element.intTextFields.getText();
                    field.set(tParams, Double.valueOf(textValue));
                }
                else if (field.getType().equals(Boolean.class)) {
                    Boolean value = element.checkBox.isSelected();
                    field.set(tParams, value);
                }
            } catch (IllegalAccessException e) {

            }
        });
    }

}
