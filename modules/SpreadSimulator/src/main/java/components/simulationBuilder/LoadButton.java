package components.simulationBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadButton  extends JButton {

    private SimulationBuilderComponent simulationBuilderComponent;

    public LoadButton(SimulationBuilderComponent simulationBuilderComponent){
        this.simulationBuilderComponent = simulationBuilderComponent;
        this.setText("Load");
        this.addActionListener(new LoadListener());
    }

    private class LoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, "load button clicked");
        }
    }
}
