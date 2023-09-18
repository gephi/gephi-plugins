package Components.SimulationBuilder;

import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeStateDecorator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.Hits;
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
        this.addActionListener(new AdvancedAssigmentButtonListener());
        setText("Options");
    }

    private class AdvancedAssigmentButtonListener implements ActionListener {

        public AdvancedAssigmentButtonListener() {

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
                    "Closeness", "Betweenness", "Degree", "Eigenvector", "Prestige", "HITS"
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
                        DegreeStatisticOption(graph, numOfNodes);
                        break;
                    case "Eigenvector":
                        EigenvectorStatisticOption(graph, numOfNodes);
                        break;
                    case "HITS":
                        HITSStatisticOption(graph, numOfNodes);
                    default:
                        JOptionPane.showMessageDialog(null, "Not implemented method yet.");

                }
            }

            private void DegreeStatisticOption(Graph graph, Integer numOfNodes) {
                var degree = new Degree();
                degree.execute(graph);
                var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());
                nodes.sort(Comparator.comparingInt(node -> Integer.parseInt(node.getAttribute("Degree").toString())));
                Collections.reverse(nodes);
                for (int i = 0; i < numOfNodes; i++) {
                    var chosenOne = nodes.get(i);
                    chosenOne.setAttribute("NodeRole", nodeRole.getName());
                    chosenOne.setAttribute("NodeState", nodeStateDecorator.getNodeState().getName());
                    chosenOne.setColor(nodeStateDecorator.getColor());
                }
            }
            private void HITSStatisticOption(Graph graph, Integer numOfNodes) {
                var hits = new Hits();
                hits.execute(graph);
                var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());
                nodes.sort(Comparator.comparingInt(node -> Integer.parseInt(node.getAttribute("Eigenvector Centrality").toString())));
                Collections.reverse(nodes);
                for (int i = 0; i < numOfNodes; i++) {
                    var chosenOne = nodes.get(i);
                    chosenOne.setAttribute("NodeRole", nodeRole.getName());
                    chosenOne.setAttribute("NodeState", nodeStateDecorator.getNodeState().getName());
                    chosenOne.setColor(nodeStateDecorator.getColor());
                }
            }
            private void EigenvectorStatisticOption(Graph graph, Integer numOfNodes) {
                var eigenvector = new EigenvectorCentrality();
                eigenvector.setDirected(false);
                eigenvector.execute(graph);
                var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());

                nodes.sort(Comparator.comparingInt(node -> Integer.parseInt(node.getAttribute("Eigenvector Centrality").toString())));
                Collections.reverse(nodes);
                for (int i = 0; i < numOfNodes; i++) {
                    var chosenOne = nodes.get(i);
                    chosenOne.setAttribute("NodeRole", nodeRole.getName());
                    chosenOne.setAttribute("NodeState", nodeStateDecorator.getNodeState().getName());
                    chosenOne.setColor(nodeStateDecorator.getColor());
                }
            }
        }
    }
}
