package modelBuilder.transitionBuilder;

import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;

public class TransitionBuilderUI extends JPanel implements ManipulatorUI {
    TransitionBuilder transitionBuilder;

    public TransitionBuilderUI(TransitionBuilder transitionBuilder) {
        this.transitionBuilder = transitionBuilder;
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
        return new TransitionBuilderPanel(transitionBuilder);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
