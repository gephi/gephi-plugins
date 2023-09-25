package ModelBuilder.StateBuilder;

import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;

public class ModelBuilderUI extends JPanel implements ManipulatorUI {
    ModelBuilder modelBuilder;

    public ModelBuilderUI(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    @Override
    public void setup(Manipulator manipulator, DialogControls dialogControls) {
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
        return new ModelBuilderPanel(modelBuilder);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
