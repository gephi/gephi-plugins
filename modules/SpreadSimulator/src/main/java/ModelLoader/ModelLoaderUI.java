package ModelLoader;

import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;

public class ModelLoaderUI extends JPanel implements ManipulatorUI {

    private ModelLoader simulationModelBuilder;

    @Override
    public void setup(Manipulator manipulator, DialogControls dialogControls) {
        this.simulationModelBuilder = (ModelLoader) manipulator;
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
        return new ModelLoaderPanel();
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
