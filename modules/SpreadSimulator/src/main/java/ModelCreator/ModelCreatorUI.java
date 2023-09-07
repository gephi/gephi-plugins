package ModelCreator;

import SimulationModel.SimulationModel;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;

public class ModelCreatorUI extends JPanel implements ManipulatorUI {

    private SimulationModel simulationModel;
    private ModelCreator modelCreator;
    private DialogControls dialogControls;

    public ModelCreatorUI(SimulationModel simulationModel) {
        this.simulationModel = simulationModel;
    }

    @Override
    public void setup(Manipulator manipulator, DialogControls dialogControls) {
        this.modelCreator = (ModelCreator) manipulator;
        this.dialogControls = dialogControls;
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
        return new ModelCreatorPanel(modelCreator);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
