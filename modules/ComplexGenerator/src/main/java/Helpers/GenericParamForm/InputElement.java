package Helpers.GenericParamForm;

import lombok.AllArgsConstructor;

import javax.swing.*;
import java.util.List;


public class InputElement{
    public InputElement(JCheckBox checkBox, JLabel intLabel) {
        this.checkBox = checkBox;
        this.intLabel = intLabel;
    }

    public InputElement(JTextField intTextFields, JLabel intLabel) {
        this.intTextFields = intTextFields;
        this.intLabel = intLabel;
    }
    JTextField intTextFields;
    JCheckBox checkBox;
    JLabel intLabel;
}
