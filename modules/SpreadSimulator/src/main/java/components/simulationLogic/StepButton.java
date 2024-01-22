package components.simulationLogic;


import components.simulation.Simulation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StepButton extends JButton {

    private final SimulationComponent simulationComponent;
    private final Simulation simulation;
    public StepButton(Simulation simulation, SimulationComponent simulationComponent) {
        this.setText("Step Simulation");
        this.simulation = simulation;
        this.simulationComponent = simulationComponent;
        this.addActionListener(new StepButtonListener());
    }
    private class StepButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            simulation.Step();
            simulationComponent.initComponents();
            simulationComponent.revalidate();
            simulationComponent.repaint();
        }
    }
}
