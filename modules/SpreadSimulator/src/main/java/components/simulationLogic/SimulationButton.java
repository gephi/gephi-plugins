package components.simulationLogic;

import components.simulation.Simulation;
import configLoader.ConfigLoader;
import javax.swing.*;
import java.awt.*;

public class SimulationButton extends JButton {
    private final SimulationComponent simulationComponent;
    private final Simulation simulation;
    private Integer conductSteps;
    private Boolean visualization;
    private Integer delay;

    public SimulationButton(Simulation simulation, SimulationComponent simulationComponent) {
        this.setText(ConfigLoader.buttonLabelRunSimulation);
        this.simulation = simulation;
        this.simulationComponent = simulationComponent;
        this.addActionListener(e -> openInputDialogAndRunSimulation());
    }

    private void openInputDialogAndRunSimulation() {
        CustomInputDialog dialog = new CustomInputDialog(null);
        dialog.setVisible(true);
        dialog.dispose();
        if (dialog.isSuccessful()) {
            runSimulation();
        }
    }

    private void runSimulation() {
        if (visualization) {
            for (int i = 0; i < conductSteps; i++) {
                simulation.Step();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        else{
            for (int i = 0; i < conductSteps; i++) {
                simulation.Step();
            }
        }

        simulationComponent.initComponents();
        simulationComponent.revalidate();
        simulationComponent.repaint();
    }

    private class CustomInputDialog extends JDialog {
        private JTextField stepsField;
        private JCheckBox visualizationCheckbox;
        private JTextField delayField;
        private boolean successful = false;

        public CustomInputDialog(Frame parent) {
            super(parent, "Input Parameters", true);

            setLayout(new GridLayout(4, 2));

            stepsField = new JTextField(10);
            visualizationCheckbox = new JCheckBox("Visualization");
            delayField = new JTextField(10);
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> onOk());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> setVisible(false));

            add(new JLabel("Conduct Steps:"));
            add(stepsField);
            add(new JLabel("Visualization:"));
            add(visualizationCheckbox);
            add(new JLabel("Delay:"));
            add(delayField);
            add(okButton);
            add(cancelButton);

            pack();
            setLocationRelativeTo(parent);
        }

        private void onOk() {
            try {
                conductSteps = Integer.parseInt(stepsField.getText());
                visualization = visualizationCheckbox.isSelected();
                delay = Integer.parseInt(delayField.getText());

                if (conductSteps <= 0 || (delay <= 0 && visualization)) {
                    JOptionPane.showMessageDialog(this, "Values should be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                successful = true;
                setVisible(false);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid integers for steps and delay.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSuccessful() {
            return successful;
        }
    }
}
