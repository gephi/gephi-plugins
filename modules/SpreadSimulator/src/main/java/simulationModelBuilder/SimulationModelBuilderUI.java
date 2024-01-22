package simulationModelBuilder;

import simulationModel.SimulationModel;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;

import javax.swing.*;

public class SimulationModelBuilderUI extends JPanel implements ManipulatorUI {

    private SimulationModel simulationModel;
    private SimulationModelBuilder simulationModelBuilder;
    private DialogControls dialogControls;

    public SimulationModelBuilderUI(SimulationModel simulationModel) {
        this.simulationModel = simulationModel;
    }

    @Override
    public void setup(Manipulator manipulator, DialogControls dialogControls) {
        this.simulationModelBuilder = (SimulationModelBuilder) manipulator;
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
        return new SimulationModelBuilderPanel(simulationModelBuilder);
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
