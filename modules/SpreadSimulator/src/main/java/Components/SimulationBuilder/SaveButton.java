package Components.SimulationBuilder;

import ConfigLoader.ConfigLoader;
import Helper.ApplySimulationHelper;
import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.SimulationModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SaveButton extends JButton {
    private final SimulationBuilderComponent simulationBuilderComponent;

    public SaveButton(SimulationBuilderComponent simulationBuilderComponent) {
        this.simulationBuilderComponent = simulationBuilderComponent;
        this.setText("Save");
        this.addActionListener(new SaveButtonActionListener(simulationBuilderComponent.getSimulationModel(), simulationBuilderComponent.getNodeRoles()));
    }

    private class SaveButtonActionListener implements ActionListener {
        private final SimulationModel simulationModel;
        private final List<NodeRoleDecorator> nodeRoles;
        private SaveButtonActionListener(SimulationModel simulationModel, List<NodeRoleDecorator> nodeRoles) {
            this.simulationModel = simulationModel;
            this.nodeRoles = nodeRoles;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            File folder = new File(ConfigLoader.getProperty("folder.simulationBuilder.simulations"));
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    JOptionPane.showMessageDialog(null,"Nie można utworzyć folderu " + ConfigLoader.getProperty("folder.simulationBuilder.simulations"));
                    return;
                }
            }

            simulationModel.setNodeRoles(nodeRoles);
            simulationModel.setName(ApplySimulationHelper.GenerateName(nodeRoles));
            var mapper = ObjectMapperHelper.CustomObjectMapperCreator();
            try {
                File jsonFile = new File(ConfigLoader.getProperty("folder.simulationBuilder.simulations")+ simulationModel.getName() + ".json");
                mapper.writeValue(jsonFile, simulationModel);
                JOptionPane.showMessageDialog(null,"Model saved as " + simulationModel.getName() + ".json");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,"Unexpected error");
                throw new RuntimeException(ex);
            }
        }
    }

}
