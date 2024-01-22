package modelBuilder.stateBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class StateBuilderPanel extends JPanel {
    private StateBuilder modelBuilder;
    private JTextField nameField;
    private JTextField descriptionField;
    private JLabel nameLabel;
    private JLabel descriptionLabel;

    public StateBuilderPanel(StateBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        nameField = new JTextField(20);
        descriptionField = new JTextField(20);

        nameLabel = new JLabel("Name: ");
        descriptionLabel = new JLabel("Description: ");

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                modelBuilder.setName(nameField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                modelBuilder.setName(nameField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                modelBuilder.setName(nameField.getText());
            }
        });

        descriptionField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                modelBuilder.setDescription(descriptionField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                modelBuilder.setDescription(descriptionField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                modelBuilder.setDescription(descriptionField.getText());
            }
        });

        add(nameLabel);
        add(nameField);
        add(descriptionLabel);
        add(descriptionField);
    }

}
