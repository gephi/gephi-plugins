package Components.SimulationBuilder;

import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeStateDecorator;
import org.codehaus.jackson.map.ObjectMapper;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
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

            descendingCheckbox = new JCheckBox("descending");

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
            mainPanel.add(descendingCheckbox);
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
                    case "Random":
                        RandomNStrategy(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Random-Random":
                        RandomRandomStrategy(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Closeness":
                        GraphDistanceClosenessStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Harmonic Closeness":
                        GraphDistanceHarmonicClosenessStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Betwenness":
                        GraphDistanceBetweenessStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Degree":
                        DegreeStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Eigenvector":
                        EigenvectorStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "HITS - authority":
                        HITSAuthorityStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                    case "HITS - hub":
                        HITSHubStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Eccentricity":
                        GraphDistanceEccentricityStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    case "Modularity":
                        GraphDistanceModularityStatisticOption(graph, numOfNodes, !descendingCheckbox.isSelected());
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Not implemented method yet.");
                        throw new NotImplementedException();
                }
                JOptionPane.showMessageDialog(null, "Changes have been imposed.");
            }

            private void RandomNStrategy(Graph graph, Integer numOfNodes, Boolean descending) {
                Node[] nodes = graph.getNodes().toArray();
                var rnd = new Random();
                for (int i = 0; i < numOfNodes; i++) {
                    var index = rnd.nextInt(nodes.length);
                    var selectedNode = nodes[index];
                    selectedNode.setAttribute("NodeRole", nodeRole.getName());
                    selectedNode.setAttribute("NodeState", nodeStateDecorator.getNodeState().getName());
                    selectedNode.setColor(nodeStateDecorator.getColor());
                }
            }

            private void RandomRandomStrategy(Graph graph, Integer numOfNodes, Boolean descending) {
                Node[] nodes = graph.getNodes().toArray();
                var rnd = new Random();
                for (int i = 0; i < numOfNodes; i++) {
                    var index = rnd.nextInt(nodes.length);
                    var selectedNode = nodes[index];
                    var neighbours = graph.getNeighbors(selectedNode).toArray();
                    index = rnd.nextInt(neighbours.length);
                    selectedNode = neighbours[index];
                    selectedNode.setAttribute("NodeRole", nodeRole.getName());
                    selectedNode.setAttribute("NodeState", nodeStateDecorator.getNodeState().getName());
                    selectedNode.setColor(nodeStateDecorator.getColor());
                }
            }

            private void GraphDistanceClosenessStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var eigenvector = new GraphDistance();
                eigenvector.setDirected(false);
                eigenvector.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "closnesscentrality");
            }

            private void GraphDistanceHarmonicClosenessStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var eigenvector = new GraphDistance();
                eigenvector.setDirected(false);
                eigenvector.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "harmonicclosnesscentrality");
            }

            private void GraphDistanceBetweenessStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var eigenvector = new GraphDistance();
                eigenvector.setDirected(false);
                eigenvector.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "betweenesscentrality");
            }

            private void DegreeStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var degree = new Degree();
                degree.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "Degree");
            }

            private void EigenvectorStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var eigenvector = new EigenvectorCentrality();
                eigenvector.setDirected(false);
                eigenvector.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "eigencentrality");
            }

            private void HITSAuthorityStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var hits = new Hits();
                hits.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "Authority");
            }

            private void HITSHubStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var hits = new Hits();
                hits.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "Hub");
            }

            private void GraphDistanceEccentricityStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var eigenvector = new GraphDistance();
                eigenvector.setDirected(false);
                eigenvector.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "eccentricity");
            }

            private void GraphDistanceModularityStatisticOption(Graph graph, Integer numOfNodes, Boolean descending) {
                var eigenvector = new Modularity();
                eigenvector.execute(graph);
                StatisticsOptions(graph, numOfNodes, descending, "modularity_class");
            }


            private void StatisticsOptions(Graph graph, Integer numOfNodes, Boolean descending, String attributeName) {
                var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());
                nodes.sort(Comparator.comparingDouble(node -> Double.parseDouble(node.getAttribute(attributeName).toString())));
                if (descending) {
                    Collections.reverse(nodes);
                }
                for (int i = 0; i < numOfNodes; i++) {
                    var chosenOne = nodes.get(i);
                    chosenOne.setAttribute("NodeRole", nodeRole.getName());
                    chosenOne.setAttribute("NodeState", nodeStateDecorator.getNodeState().getName());
                    var mapper = ObjectMapperHelper.CustomObjectMapperCreator();
                    try {
                        chosenOne.setAttribute("TransitionMap", mapper.writeValueAsString(nodeRole.getTransitionMap()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    chosenOne.setColor(nodeStateDecorator.getColor());
                }
            }
        }
    }
}
