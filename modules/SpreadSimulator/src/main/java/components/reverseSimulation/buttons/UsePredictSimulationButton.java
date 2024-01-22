package components.reverseSimulation.buttons;

import components.reverseSimulation.ReverseSimulationComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UsePredictSimulationButton extends JButton {

    private final ReverseSimulationComponent reverseSimulationComponent;

    public UsePredictSimulationButton(ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Use Predict Simulation");
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new UsePredictSimulationButton.PredictSimulationListener());
    }

    private class PredictSimulationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            reverseSimulationComponent.setReverseSimulationState(2);
            reverseSimulationComponent.initComponents();
            reverseSimulationComponent.revalidate();
            reverseSimulationComponent.repaint();
        }
    }
}
