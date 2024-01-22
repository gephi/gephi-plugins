package components.simulationBuilder;

import simulationModel.interaction.*;
import simulationModel.SimulationModel;
import org.openide.util.NotImplementedException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InteractionDropdown {

    public JComboBox generate(SimulationBuilderComponent simulationBuilderComponent){
        var interactionOptions = new String[]{
                "All", "RelativeNodes", "RelativeFreeNodes", "RelativeEdges", "RelativeFreeEdges"
        };

        var combobox = new JComboBox<>(interactionOptions);
        combobox.addActionListener(new InteractionDropdownListener(combobox, simulationBuilderComponent));

        return combobox;
    }

    private class InteractionDropdownListener implements ActionListener {
        private final SimulationBuilderComponent simulationBuilderComponent;
        private final JComboBox interaction;
        private final SimulationModel simulationModel;
        private final JLabel label;
        private final JTextField numberField;
        private final JTextField percentageField;
        private DocumentListener numberFieldListener;
        private DocumentListener percentageFieldListener;

        public InteractionDropdownListener(JComboBox interaction, SimulationBuilderComponent simulationBuilderComponent) {
            this.interaction = interaction;
            this.simulationModel = simulationBuilderComponent.getSimulationModel();
            var allInteraction =  new AllInteraction();
            allInteraction.setInteractionType(InteractionType.All);
            this.simulationModel.setInteraction(allInteraction);
            this.simulationBuilderComponent = simulationBuilderComponent;
            numberField = new JTextField();
            percentageField = new JTextField();
            label = new JLabel();
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            var interactionItem = interaction.getSelectedItem().toString();
            simulationBuilderComponent.remove(numberField);
            simulationBuilderComponent.remove(percentageField);
            numberField.getDocument().removeDocumentListener(numberFieldListener);
            percentageField.getDocument().removeDocumentListener(percentageFieldListener);
            simulationBuilderComponent.remove(label);
            switch (interactionItem) {
                case "All":
                    var allInteraction =  new AllInteraction();
                    allInteraction.setInteractionType(InteractionType.All);
                    simulationModel.setInteraction(allInteraction);
                    break;
                case "RelativeNodes":
                    label.setText("Percentage of nodes: ");
                    var relativeInteraction =  new RelativeNodesInteraction();
                    relativeInteraction.setInteractionType(InteractionType.RelativeNodes);
                    simulationModel.setInteraction(relativeInteraction);
                    simulationBuilderComponent.add(label);
                    simulationBuilderComponent.add(percentageField);
                    percentageFieldListener = new RelativeNodesInteractionListener(simulationModel, percentageField);
                    percentageField.getDocument().addDocumentListener(percentageFieldListener);
                    break;
                case "RelativeFreeNodes":
                    label.setText("Number of nodes: ");
                    var relativeFreeInteraction =  new RelativeFreeNodesInteraction();
                    relativeFreeInteraction.setInteractionType(InteractionType.RelativeFreeNodes);
                    simulationBuilderComponent.add(label);
                    simulationModel.setInteraction(relativeFreeInteraction);
                    simulationBuilderComponent.add(numberField);
                    numberFieldListener = new RelativeFreeNodesInteractionListener(simulationModel, numberField);
                    numberField.getDocument().addDocumentListener(numberFieldListener);
                    break;
                case "RelativeEdges":
                    label.setText("Percentage of edges: ");
                    var relativeEdgesInteraction =  new RelativeNodesInteraction();
                    relativeEdgesInteraction.setInteractionType(InteractionType.RelativeEdges);
                    simulationBuilderComponent.add(label);
                    simulationModel.setInteraction(relativeEdgesInteraction);
                    simulationBuilderComponent.add(percentageField);
                    percentageFieldListener = new RelativeEdgesInteractionListener(simulationModel, percentageField);
                    percentageField.getDocument().addDocumentListener(percentageFieldListener);
                    break;
                case "RelativeFreeEdges":
                    label.setText("Number of edges: ");
                    var relativeFreeEdgesInteraction =  new RelativeFreeNodesInteraction();
                    relativeFreeEdgesInteraction.setInteractionType(InteractionType.RelativeFreeEdges);
                    simulationBuilderComponent.add(label);
                    simulationModel.setInteraction(relativeFreeEdgesInteraction);
                    simulationBuilderComponent.add(numberField);
                    numberFieldListener = new RelativeFreeEdgesInteractionListener(simulationModel, numberField);
                    numberField.getDocument().addDocumentListener(numberFieldListener);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Not implemented method yet.");
                    throw new NotImplementedException();
            }
            simulationBuilderComponent.revalidate();
            simulationBuilderComponent.repaint();
        }
    }


    private abstract class CustomListener implements DocumentListener {
        protected final SimulationModel simulationModel;
        protected final JTextField percentageField;
        public CustomListener(SimulationModel simulationModel, JTextField percentageField) {
            this.simulationModel = simulationModel;
            this.percentageField = percentageField;
        }
        protected abstract void Change();
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

    private class RelativeNodesInteractionListener extends CustomListener {
        public RelativeNodesInteractionListener(SimulationModel simulationModel, JTextField percentageField) {
            super(simulationModel, percentageField);
        }
        @Override
        protected void Change() {
            var percentage = Double.parseDouble(percentageField.getText());
            var interaction = (RelativeNodesInteraction) simulationModel.getInteraction();
            interaction.setPercentage(percentage);
            simulationModel.setInteraction(interaction);
        }
    }

    private class RelativeEdgesInteractionListener extends CustomListener {
        public RelativeEdgesInteractionListener(SimulationModel simulationModel, JTextField percentageField) {
            super(simulationModel, percentageField);
        }
        @Override
        protected void Change() {
            var percentage = Double.parseDouble(percentageField.getText());
            var interaction = (RelativeEdgesInteraction) simulationModel.getInteraction();
            interaction.setPercentage(percentage);
            simulationModel.setInteraction(interaction);
        }
    }

    private class RelativeFreeNodesInteractionListener extends CustomListener {
        public RelativeFreeNodesInteractionListener(SimulationModel simulationModel, JTextField percentageField) {
            super(simulationModel, percentageField);
        }
        @Override
        protected void Change() {
            var number = Integer.parseInt(percentageField.getText());
            var interaction = (RelativeFreeNodesInteraction) simulationModel.getInteraction();
            interaction.setNumber(number);
            simulationModel.setInteraction(interaction);
        }
    }

    private class RelativeFreeEdgesInteractionListener extends CustomListener {
        public RelativeFreeEdgesInteractionListener(SimulationModel simulationModel, JTextField percentageField) {
            super(simulationModel, percentageField);
        }
        @Override
        protected void Change() {
            var number = Integer.parseInt(percentageField.getText());
            var interaction = (RelativeFreeEdgesInteraction) simulationModel.getInteraction();
            interaction.setNumber(number);
            simulationModel.setInteraction(interaction);
        }
    }
}
