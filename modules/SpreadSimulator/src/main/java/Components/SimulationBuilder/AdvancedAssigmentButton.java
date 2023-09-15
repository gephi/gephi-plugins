package Components.SimulationBuilder;

import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeStateDecorator;
import org.gephi.graph.api.GraphController;
import org.gephi.statistics.plugin.Degree;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class AdvancedAssigmentButton extends JButton {
    private NodeRole nodeRole;
    private NodeStateDecorator nodeStateDecorator;

    AdvancedAssigmentButton(NodeRole nodeRole, NodeStateDecorator nodeStateDecorator) {
        this.nodeRole = nodeRole;
        this.nodeStateDecorator = nodeStateDecorator;
        this.addActionListener(new AdvancedAssigmentButtonListner());
        setText("Options");
    }

    private class AdvancedAssigmentButtonListner implements ActionListener {

        public AdvancedAssigmentButtonListner() {

        }

        JTextField numOfNodesInput;
        JComboBox centralityRateDropdown;

        @Override
        public void actionPerformed(ActionEvent e){
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

            var centralityRateLabel = new JLabel("Select Centrality Rate:");
            centralityRateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            var centralityRateOptions = new String[] {
                    "Closeness", "Betweenness", "Degree", "Eigenvector", "Prestige"
            };
            centralityRateDropdown = new JComboBox<>(centralityRateOptions);
            centralityRateDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

            var applyButton = new JButton("Apply");
            applyButton.addActionListener(new ApplyChangesListener());

            var buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(applyButton);

            mainPanel.add(numOfNodesPanel);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(centralityRateLabel);
            mainPanel.add(Box.createVerticalStrut(5));
            mainPanel.add(centralityRateDropdown);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(buttonPanel);

            dialog.add(mainPanel);

            dialog.setTitle("Advanced options: " + nodeStateDecorator.getNodeState().getName());
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(null);
            dialog.setModal(true);
            dialog.setVisible(true);
        }


        private class ApplyChangesListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    Integer.parseInt(numOfNodesInput.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid number for 'Number of nodes'.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }

                var graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                var graph = graphModel.getGraph();
                var centralityMethod = centralityRateDropdown.getSelectedItem().toString();
                var numOfNodesString = numOfNodesInput.getText();
                var numOfNodes = Integer.valueOf(numOfNodesString);
                switch (centralityMethod) {
                    case "Degree":
                        var degree = new Degree();
                        degree.execute(graph);
                        var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());
                        var nodeRoleNodes = nodes.stream()
                                .filter(node -> node.getAttribute("NodeRole") == nodeRole.getName().toString())
                                .collect(Collectors.toList());
                        nodeRoleNodes
                                .sort(Comparator.comparingInt(node -> Integer.parseInt(node.getAttribute("Degree").toString())));
                        Collections.reverse(nodeRoleNodes);
                        for (int i = 0; i < numOfNodes; i++) {
                            var chosenOne = nodeRoleNodes.get(i);
                            chosenOne.setAttribute("NodeState", nodeStateDecorator.getNodeState().getName());
                            chosenOne.setColor(nodeStateDecorator.getColor());
                        }
                        break;
                    case "Betweenness":
                        break;

                }
            }
        }
    }
}
