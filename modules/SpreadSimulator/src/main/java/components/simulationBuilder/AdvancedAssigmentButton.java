package components.simulationBuilder;

import simulationModel.node.NodeRole;
import simulationModel.node.NodeStateDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class AdvancedAssigmentButton extends JButton {
    private NodeRole nodeRole;
    private NodeStateDecorator nodeStateDecorator;
    private  ModelStatisticsDynamicInput dynamicInput;

    AdvancedAssigmentButton(NodeRole nodeRole, NodeStateDecorator nodeStateDecorator,  ModelStatisticsDynamicInput dynamicInput) {
        this.nodeRole = nodeRole;
        this.nodeStateDecorator = nodeStateDecorator;
        this.addActionListener(new AdvancedAssigmentButtonListener());
        this.dynamicInput = dynamicInput;

        setText("Add rule");
    }

    private class AdvancedAssigmentButtonListener implements ActionListener {

        public AdvancedAssigmentButtonListener() {

        }

        JTextField numOfNodesInput;
        JComboBox centralityRateDropdown;
        JCheckBox descendingCheckbox;

        @Override
        public void actionPerformed(ActionEvent e) {
            var dialog = new JDialog();

            var mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            var numOfNodesPanel = new JPanel(new GridBagLayout());
            var constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            var numOfNodesLabel = new JLabel("Number of nodes:");
            numOfNodesInput = new JTextField(5);
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.insets = new Insets(0, 0, 0, 5);
            numOfNodesPanel.add(numOfNodesLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.insets = new Insets(0, 0, 0, 0);
            numOfNodesPanel.add(numOfNodesInput, constraints);

            var centralityRateLabel = new JLabel("Select Strategy:");
            centralityRateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            var centralityRateOptions = new String[]{
                    "Random", "Random-Random","Closeness", "Harmonic Closeness", "Betweenness", "Degree", "Eigenvector", "HITS - hub", "HITS - authority", "Eccentricity", "Modularity"
            };
            centralityRateDropdown = new JComboBox<>(centralityRateOptions);
            centralityRateDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

            descendingCheckbox = new JCheckBox("ascending");

            var applyButton = new JButton("Apply");
            applyButton.addActionListener(new SaveChangesListener());

            var buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(applyButton);


            mainPanel.add(numOfNodesPanel);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(centralityRateLabel);
            mainPanel.add(Box.createVerticalStrut(5));
            mainPanel.add(centralityRateDropdown);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(descendingCheckbox);
            mainPanel.add(buttonPanel);

            dialog.add(mainPanel);

            dialog.setTitle("Advanced options: " + nodeStateDecorator.getNodeState().getName());
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(null);
            dialog.setModal(true);
            dialog.setVisible(true);
        }

        private class SaveChangesListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Integer.parseInt(numOfNodesInput.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid number for 'Number of nodes'.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
                var centralityMethod = centralityRateDropdown.getSelectedItem().toString();
                var numOfNodesString = numOfNodesInput.getText();
                var numOfNodes = Integer.valueOf(numOfNodesString);
                var list = dynamicInput.component.advancedRules.get(nodeRole.getName()+ "_" + nodeStateDecorator.getNodeState().getName());
                if(list == null){
                    list = new ArrayList<AdvancedRule>();
                    dynamicInput.component.advancedRules.put(nodeRole.getName()+ "_" + nodeStateDecorator.getNodeState().getName(), list);
                }
                var rule = new AdvancedRule(centralityMethod, numOfNodes, descendingCheckbox.isSelected());
                list.add(rule);
                dynamicInput.component.initComponents();
                dynamicInput.component.repaint();
                dynamicInput.component.revalidate();
            }
        }

    }
}
