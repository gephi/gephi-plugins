package components.reverseSimulation.buttons;


import components.reverseSimulation.ReverseSimulationComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UseReverseSeriesSimulationButton extends JButton {

    private final ReverseSimulationComponent reverseSimulationComponent;

    public UseReverseSeriesSimulationButton(ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Use Reverse Step Simulation");
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new UseReverseSeriesSimulationButton.ReverseStepSimulationListener());
    }

    private class ReverseStepSimulationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            reverseSimulationComponent.setReverseSimulationState(1);
            reverseSimulationComponent.initComponents();
            reverseSimulationComponent.revalidate();
            reverseSimulationComponent.repaint();
        }
    }
}
