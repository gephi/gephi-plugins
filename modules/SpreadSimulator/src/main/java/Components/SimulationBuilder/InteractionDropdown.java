package Components.SimulationBuilder;

import SimulationModel.Interaction.AllInteraction;
import SimulationModel.Interaction.InteractionType;
import SimulationModel.Interaction.RelativeFreeInteraction;
import SimulationModel.Interaction.RelativeInteraction;
import SimulationModel.SimulationModel;
import org.openide.util.NotImplementedException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InteractionDropdown {

    public JComboBox generate(SimulationBuilderComponent simulationBuilderComponent){
        var interactionOptions = new String[]{
                "All", "Relative", "RelativeFree"
        };

        var combobox = new JComboBox<>(interactionOptions);
        combobox.addActionListener(new InteractionDropdownListener(combobox, simulationBuilderComponent.getSimulationModel()));

        return combobox;
    }

    private class InteractionDropdownListener implements ActionListener {
        public InteractionDropdownListener(JComboBox interaction, SimulationModel simulationModel) {
            this.interaction = interaction;
            this.simulationModel = simulationModel;
        }

        JComboBox interaction;
        private final SimulationModel simulationModel;

        @Override
        public void actionPerformed(ActionEvent e) {
            var interactionItem = interaction.getSelectedItem().toString();
            switch (interactionItem) {
                case "All":
                    var allInteraction =  new AllInteraction();
                    allInteraction.setInteractionType(InteractionType.All);
                    simulationModel.setInteraction(allInteraction);
                    break;
                case "Relative":
                    var relativeInteraction =  new RelativeInteraction();
                    relativeInteraction.setInteractionType(InteractionType.Relative);
                    //todo: wprowadź interakcjie
                    simulationModel.setInteraction(relativeInteraction);
                    break;
                case "RelativeFree":
                    var relativeFreeInteraction =  new RelativeFreeInteraction();
                    relativeFreeInteraction.setInteractionType(InteractionType.RelativeFree);
                    //todo: wprowadź interakcjie
                    simulationModel.setInteraction(relativeFreeInteraction);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Not implemented method yet.");
                    throw new NotImplementedException();
            }
        }
    }

    private class RelativeInteractionListener implements DocumentListener {
        private final SimulationModel simulationModel;
        private final JTextField percentageField;
        public RelativeInteractionListener(SimulationModel simulationModel, JTextField percentageField) {
            this.simulationModel = simulationModel;
            this.percentageField = percentageField;
        }
        private void Change() {
            var percentage = Double.parseDouble(percentageField.getText());
            var interaction = (RelativeInteraction) simulationModel.getInteraction();
            interaction.setPercentage(percentage);
            simulationModel.setInteraction(interaction);
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            Change();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            Change();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            Change();
        }
    }

    private class RelativeFreeInteractionListener implements DocumentListener {
        private final SimulationModel simulationModel;
        private final JTextField numberField;
        public RelativeFreeInteractionListener(SimulationModel simulationModel, JTextField numberField) {
            this.simulationModel = simulationModel;
            this.numberField = numberField;
        }
        private void Change() {
            var number = Integer.parseInt(numberField.getText());
            var interaction = (RelativeFreeInteraction) simulationModel.getInteraction();
            interaction.setNumber(number);
            simulationModel.setInteraction(interaction);
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            Change();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            Change();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            Change();
        }
    }
}
