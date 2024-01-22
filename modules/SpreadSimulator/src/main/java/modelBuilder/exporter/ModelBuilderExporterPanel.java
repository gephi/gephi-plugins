package modelBuilder.exporter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;

public class ModelBuilderExporterPanel extends JPanel {

    JTextField nameField;
    JTextField descField;

    ModelBuilderExporter modelBuilderExporter;

    public ModelBuilderExporterPanel(ModelBuilderExporter modelBuilderExporter) {
        this.modelBuilderExporter = modelBuilderExporter;
        ChooseFile(modelBuilderExporter);
        var nameLabel = new JLabel("Name");
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(new UpdateNameDocumentListener());
        var descLabel = new JLabel("Description");
        descField = new JTextField();
        descField.getDocument().addDocumentListener(new UpdateDescriptionDocumentListener());

        add(nameLabel);
        add(nameField);
        add(descLabel);
        add(descField);
    }

    private static void ChooseFile(ModelBuilderExporter modelBuilderExporter) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        var fileFilterExtenstion = new FileNameExtensionFilter("JSON files", "json", "JSON");
        fileChooser.addChoosableFileFilter(fileFilterExtenstion);
        fileChooser.setFileFilter(fileFilterExtenstion);
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        var path = fileChooser.getSelectedFile();

        if (path == null) {
            JOptionPane.showMessageDialog(null, "File not selected");
            return;
        }
        if (path.exists()) {
            modelBuilderExporter.setPath(path);
            return;
        }

        try {
            path.createNewFile();
            modelBuilderExporter.setPath(path);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot save model");
        }
    }

    private class UpdateNameDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            modelBuilderExporter.setModelName(nameField.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            modelBuilderExporter.setModelName(nameField.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            modelBuilderExporter.setModelName(nameField.getText());
        }
    }

    private class UpdateDescriptionDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            modelBuilderExporter.setModeDescription(descField.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            modelBuilderExporter.setModeDescription(descField.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            modelBuilderExporter.setModeDescription(descField.getText());
        }
    }
}
