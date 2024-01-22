package modelBuilder.exporter;

import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;

public class ModelBuilderExporterUI implements ManipulatorUI {
    private ModelBuilderExporter modelBuilderExporter;
    @Override
    public void setup(Manipulator manipulator, DialogControls dialogControls) {
        modelBuilderExporter = (ModelBuilderExporter) manipulator;
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
        return new ModelBuilderExporterPanel(modelBuilderExporter);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}