package components.reverseSimulation.buttons;

import components.reverseSimulation.ReverseSimulationComponent;
import components.simulation.Simulation;
import configLoader.ConfigLoader;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import simulationModel.node.NodeStateDecorator;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimulationSeriesButton extends JButton {
    private final ReverseSimulationComponent reverseSimulationComponent;
    private Simulation simulation;

    public SimulationSeriesButton(Simulation simulation, ReverseSimulationComponent reverseSimulationComponent) {
        this.setText(ConfigLoader.buttonLabelRunSimulationSeries);
        this.simulation = simulation;
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(e -> openInputDialogAndRunSimulation());
    }

    private void openInputDialogAndRunSimulation() {
        OptionDialog dialog = new OptionDialog(null, reverseSimulationComponent, "Stop condition");
        dialog.setVisible(true);
        dialog.dispose();
        if (dialog.isSuccessful()) {
            for(int i = 1; i < dialog.getConductSimulations(); i++){
                runSimulation(dialog.getExaminedStateAndRole(), dialog.nodesNumberToStop);
                this.simulation = reverseSimulationComponent.NewSeries(simulation);
            }
            runSimulation(dialog.getExaminedStateAndRole(), dialog.nodesNumberToStop);
        }
        reverseSimulationComponent.initComponents();
        reverseSimulationComponent.repaint();
        reverseSimulationComponent.revalidate();
    }

    private void runSimulation(String examinedStateAndRole, int nodesToStop) {
        while(!stopCondition(examinedStateAndRole, nodesToStop)) {
            simulation.Step();
        }

        reverseSimulationComponent.initComponents();
        reverseSimulationComponent.revalidate();
        reverseSimulationComponent.repaint();
    }

    private boolean stopCondition(String examinedStateAndRole, int nodesToStop) {
        Optional<Double> coverage = reverseSimulationComponent.getCurrentSimulation().getNodeRoleDecoratorList().stream()
                .filter(nodeRoleDecorator -> examinedStateAndRole.split(":")[0]
                        .equals(nodeRoleDecorator.getNodeRole().getName()))
                .flatMap(nodeRoleDecorator -> nodeRoleDecorator.getNodeStates().stream())
                .filter(nodeStateDecorator -> examinedStateAndRole.split(":")[1]
                        .equals(nodeStateDecorator.getNodeState().getName()))
                .map(NodeStateDecorator::getCoverage)
                .findFirst();

        if (coverage.isPresent()) {
            double numberOfNodesInSimulation = reverseSimulationComponent.getCurrentSimulation().getGraph().getEdgeCount() * coverage.get();
            return numberOfNodesInSimulation <= nodesToStop;
        } else {
            return true;
        }
    }

    @Setter
    @Getter
    public class OptionDialog extends JDialog {

        private String examinedStateAndRole;
        private JComboBox<String> rolesChoseFromList;
        private JTextField simulationNumberFiled;
        private JTextField numberOfNodesButton;
        private int conductSimulations;
        private int nodesNumberToStop;
        private boolean successful = false;

        public OptionDialog(Frame parent, ReverseSimulationComponent reverseSimulationComponent, String name) {
            super(parent, name, true);
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

            simulationNumberFiled = new JTextField(10);
            add(new JLabel("Conduct Simulations:"));
            add(simulationNumberFiled);

            rolesChoseFromList = new JComboBox<>(comboBoxModel);
            add(new JLabel("Choose a value:"));
            add(rolesChoseFromList);

            numberOfNodesButton = new JTextField(10);
            add(new JLabel("Number of nodes:"));
            add(numberOfNodesButton);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> onOk());
            add(okButton);

            pack();
            setLocationRelativeTo(parent);
        }

        private void onOk() {
            try {
                examinedStateAndRole = Objects.requireNonNull(rolesChoseFromList.getSelectedItem()).toString();
                conductSimulations = Integer.parseInt(simulationNumberFiled.getText());
                nodesNumberToStop = Integer.parseInt(numberOfNodesButton.getText());
                successful = true;
                setVisible(false);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Some error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }
}
