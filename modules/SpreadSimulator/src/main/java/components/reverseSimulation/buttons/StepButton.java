package components.reverseSimulation.buttons;

import components.reverseSimulation.ReverseSimulationComponent;
import components.simulation.Simulation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StepButton extends JButton {

    private final Simulation simulation;
    private final ReverseSimulationComponent reverseSimulationComponent;

    public StepButton(Simulation simulation, ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Step");
        this.simulation = simulation;
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new StepButton.StartSimulationReverseSeriesListener());
    }

    private class StartSimulationReverseSeriesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            simulation.Step();
            reverseSimulationComponent.initComponents();
            reverseSimulationComponent.revalidate();
            reverseSimulationComponent.repaint();
        }
    }
}
