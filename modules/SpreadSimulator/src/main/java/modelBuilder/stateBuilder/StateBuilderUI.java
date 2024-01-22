package modelBuilder.stateBuilder;

import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;

public class StateBuilderUI extends JPanel implements ManipulatorUI {
    StateBuilder modelBuilder;

    public StateBuilderUI(StateBuilder modelBuilder) {
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
        return new StateBuilderPanel(modelBuilder);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
