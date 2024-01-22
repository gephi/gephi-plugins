package components.reverseSimulation.buttons;

import components.reverseSimulation.ReverseSimulationComponent;
import components.simulation.Simulation;
import simulationModel.node.NodeStateDecorator;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StartSimulationButton extends JButton {

    private final Simulation simulation;
    private final ReverseSimulationComponent reverseSimulationComponent;
    private String roleAndStateName;
    private Integer numberOfNodesToStop;

    public StartSimulationButton(Simulation simulation, ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Start Simulation");
        this.simulation = simulation;
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new StartSimulationReverseSeriesListener());
    }

    private class StartSimulationReverseSeriesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            CustomInputDialog dialog = new CustomInputDialog(null);
            dialog.setVisible(true);
            dialog.dispose();
            if (dialog.isSuccessful()) {
                runSimulation();
            }
        }
    }

    private void runSimulation() {
        while(!stopCondition()) {
            simulation.Step();
        }

        reverseSimulationComponent.initComponents();
        reverseSimulationComponent.revalidate();
        reverseSimulationComponent.repaint();
    }

    private boolean stopCondition() {
        Optional<Double> coverage = reverseSimulationComponent.getCurrentSimulation().getNodeRoleDecoratorList().stream()
                .filter(nodeRoleDecorator -> roleAndStateName.split(":")[0]
                        .equals(nodeRoleDecorator.getNodeRole().getName()))
                .flatMap(nodeRoleDecorator -> nodeRoleDecorator.getNodeStates().stream())
                .filter(nodeStateDecorator -> roleAndStateName.split(":")[1]
                        .equals(nodeStateDecorator.getNodeState().getName()))
                .map(NodeStateDecorator::getCoverage)
                .findFirst();

        if (coverage.isPresent()) {
            double numberOfNodesInSimulation = reverseSimulationComponent.getCurrentSimulation().getGraph().getEdgeCount() * coverage.get();
            return numberOfNodesInSimulation <= numberOfNodesToStop;
        } else {
            return true;
        }
    }

    private class CustomInputDialog extends JDialog {
        private JComboBox<String> rolesChoseFromList;
        private JTextField numberOfNodesButton;
        @Getter
        private boolean successful = false;

        public CustomInputDialog(Frame parent) {
            super(parent, "End condition", true);

            setLayout(new GridLayout(4, 2));

            List<Pair<String, String>> stateAndRoleNames = reverseSimulationComponent
                    .getSimulationModel()
                    .getNodeRoles()
                    .stream()
                    .flatMap(e -> e.getNodeStates()
                            .stream()
                            .map(nodeStateDecorator -> Pair.of(e.getNodeRole().getName(), nodeStateDecorator.getNodeState().getName())))
                    .collect(Collectors.toList());
            DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
            comboBoxModel.addAll(stateAndRoleNames.stream().map(e -> e.first() + ":" + e.second()).collect(Collectors.toList()));


            rolesChoseFromList = new JComboBox<>(comboBoxModel);
            add(new JLabel("Choose a value:"));
            add(rolesChoseFromList);

            numberOfNodesButton = new JTextField(10);
            add(new JLabel("Number of nodes:"));
            add(numberOfNodesButton);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> onOk());
            add(okButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> setVisible(false));
            add(cancelButton);

            pack();
            setLocationRelativeTo(parent);
        }

        private void onOk() {
            try {
                roleAndStateName = rolesChoseFromList.getSelectedItem().toString();
                numberOfNodesToStop = Integer.parseInt(numberOfNodesButton.getText());

                if (numberOfNodesToStop <= 0) {
                    JOptionPane.showMessageDialog(this, "Values should be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                successful = true;
                setVisible(false);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid integers for steps and delay.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }
}
