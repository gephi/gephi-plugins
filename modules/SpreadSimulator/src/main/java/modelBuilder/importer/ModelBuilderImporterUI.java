package modelBuilder.importer;

import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;

public class ModelBuilderImporterUI implements ManipulatorUI {
    private ModelBuilderImporter modelBuilderExporter;
    @Override
    public void setup(Manipulator manipulator, DialogControls dialogControls) {
        modelBuilderExporter = (ModelBuilderImporter) manipulator;
    }

    @Override
    public void unSetup() {

    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public JPanel getSettingsPanel() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            var fileFilterExtenstion = new FileNameExtensionFilter("JSON files", "json", "JSON");
            fileChooser.addChoosableFileFilter(fileFilterExtenstion);
            fileChooser.setFileFilter(fileFilterExtenstion);
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            var path = fileChooser.getSelectedFile();

            if (path == null) {
                JOptionPane.showMessageDialog(null, "File not selected");
                return null;
            }
            if (path.exists()) {
                modelBuilderExporter.setPath(path);
                return null;
            }

            try {
                path.createNewFile();
                modelBuilderExporter.setPath(path);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Cannot open model");
            }
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}