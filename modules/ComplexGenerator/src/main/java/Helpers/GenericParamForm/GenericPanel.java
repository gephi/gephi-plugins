package Helpers.GenericParamForm;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.*;
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

    public GenericPanel() {
        CreateParamObject();
        setLayout(new GridBagLayout());

        // Initialize components
        mapParams = new HashMap<>();

        var allFields = tParams.getClass().getDeclaredFields();

        AtomicInteger gridYIterator = new AtomicInteger(1);
        GridBagConstraints constraints = new GridBagConstraints();

        Arrays.stream(allFields).forEach(field -> {
            var fieldName = field.getName();
            var newLabel = new JLabel("Enter " + fieldName + ": ");
            var newField = new JTextField(10);

            var newInputElement = new InputElement(newField, newLabel);
            mapParams.put(fieldName, newInputElement);

            constraints.gridy = gridYIterator.get();;
            constraints.gridx = 0;
            add(newLabel, constraints);
            constraints.gridx = 1;
            add(newField, constraints);

            gridYIterator.getAndIncrement();


            if (field.getType().equals(Integer.class)) {
                ((AbstractDocument) newField.getDocument()).setDocumentFilter(onlyDigitFilter);
            }
            else if (field.getType().equals(Boolean.class)) {
                //todo
            }
            else if (field.getType().equals(Double.class)) {
                //todo
            }
            else {
                throw new IllegalStateException("Unexpected value: " + field.getType());
            }

        });
        constraints.gridwidth = 4;
        constraints.anchor = GridBagConstraints.CENTER;
    }

    private void parseValues(){
        var allFields = tParams.getClass().getDeclaredFields();
        Arrays.stream(allFields).forEach(field -> {
            InputElement element = mapParams.get(field.getName());
            var textValue = element.intTextFields.getText();
            try{
                if (field.getType().equals(Integer.class)) {
                    field.set(tParams, Integer.valueOf(textValue));
                }
                else if (field.getType().equals(Boolean.class)) {
                    //todo
                }
                else if (field.getType().equals(Double.class)) {
                    //todo
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
