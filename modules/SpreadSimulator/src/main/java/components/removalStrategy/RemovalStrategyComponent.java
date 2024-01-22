package components.removalStrategy;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.*;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

@ConvertAsProperties(dtd = "-//Simulation//RemovalStrategy//EN", autostore = false)
@TopComponent.Description(preferredID = "RemovalStrategy",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "RemovalStrategy")
@ActionReference(path = "Menu/Window", position = 4)
@TopComponent.OpenActionRegistration(displayName = "#CTL_RemovalStrategy",
        preferredID = "RemovalStrategy")
public class RemovalStrategyComponent extends TopComponent {

    private Graph graph;
    JTextField numOfNodesInput;
    JComboBox centralityRateDropdown;
    JCheckBox ascendingCheckbox;

    public RemovalStrategyComponent() {
        initComponents();
        setName("Removal Strategy");
        setToolTipText("Removal Strategy");
    }

    public void initComponents() {
        this.removeAll();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

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
        constraints.insets = new Insets(0, 0, 0, 0);
        numOfNodesPanel.add(numOfNodesInput, constraints);

        var centralityRateLabel = new JLabel("Select Strategy:");
        centralityRateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        var centralityRateOptions = new String[]{
                "Random", "Random-Random", "Closeness", "Harmonic Closeness", "Betweenness", "Degree",
                "Eigenvector", "HITS - hub", "HITS - authority", "Eccentricity", "Modularity"
        };
        centralityRateDropdown = new JComboBox<>(centralityRateOptions);
        centralityRateDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        ascendingCheckbox = new JCheckBox("ascending");

        var applyButton = new JButton("Apply");
        applyButton.addActionListener(new ApplyChangesListener());

        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(applyButton);

        contentPanel.add(numOfNodesPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(centralityRateLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(centralityRateDropdown);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(ascendingCheckbox);
        contentPanel.add(buttonPanel);

        add(contentPanel, BorderLayout.NORTH);

        setVisible(true);
    }



    private class ApplyChangesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                Integer.parseInt(numOfNodesInput.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number for 'Number of nodes'.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }

            graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();

            var graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
            var graph = graphModel.getGraph();
            var centralityMethod = centralityRateDropdown.getSelectedItem().toString();
            var numOfNodesString = numOfNodesInput.getText();
            var numOfNodes = Integer.valueOf(numOfNodesString);
            switch (centralityMethod) {
                case "Random":
                    RandomNStrategy(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "Random-Random":
                    RandomRandomStrategy(graph, numOfNodes, !ascendingCheckbox.isSelected());
                case "Closeness":
                    GraphDistanceClosenessStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "Harmonic Closeness":
                    GraphDistanceHarmonicClosenessStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "Betwenness":
                    GraphDistanceBetweenessStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "Degree":
                    DegreeStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "Eigenvector":
                    EigenvectorStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "HITS - authority":
                    HITSAuthorityStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                case "HITS - hub":
                    HITSHubStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "Eccentricity":
                    GraphDistanceEccentricityStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                case "Modularity":
                    GraphDistanceModularityStatisticOption(graph, numOfNodes, !ascendingCheckbox.isSelected());
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Not implemented method yet.");
                    throw new NotImplementedException();
            }
            JOptionPane.showMessageDialog(null, "Changes have been imposed.");
        }

        private void RandomNStrategy(Graph graph, Integer numOfNodes, Boolean ascending) {
            Node[] nodes = graph.getNodes().toArray();
            var rnd = new Random();
            for (int i = 0; i < numOfNodes; i++) {
                var index = rnd.nextInt(nodes.length);
                var chosenOne = nodes[index];
                graph.removeNode(chosenOne);
            }
        }

        private void RandomRandomStrategy(Graph graph, Integer numOfNodes, Boolean ascending) {
            Node[] nodes = graph.getNodes().toArray();
            var rnd = new Random();
            for (int i = 0; i < numOfNodes; i++) {
                var index = rnd.nextInt(nodes.length);
                var chosenOne = nodes[index];
                var neighbours = graph.getNeighbors(chosenOne).toArray();
                index = rnd.nextInt(neighbours.length);
                chosenOne = neighbours[index];
                graph.removeNode(chosenOne);
            }
        }

        private void GraphDistanceClosenessStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "closnesscentrality");
        }

        private void GraphDistanceHarmonicClosenessStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "harmonicclosnesscentrality");
        }

        private void GraphDistanceBetweenessStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "betweenesscentrality");
        }

        private void DegreeStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var degree = new Degree();
            degree.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "Degree");
        }

        private void EigenvectorStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var eigenvector = new EigenvectorCentrality();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "eigencentrality");
        }

        private void HITSAuthorityStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var hits = new Hits();
            hits.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "Authority");
        }

        private void HITSHubStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var hits = new Hits();
            hits.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "Hub");
        }

        private void GraphDistanceEccentricityStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "eccentricity");
        }

        private void GraphDistanceModularityStatisticOption(Graph graph, Integer numOfNodes, Boolean ascending) {
            var eigenvector = new Modularity();
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, ascending, "modularity_class");
        }


        private void StatisticsOptions(Graph graph, Integer numOfNodes, Boolean ascending, String attributeName) {
            var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());
            nodes.sort(Comparator.comparingDouble(node -> Double.parseDouble(node.getAttribute(attributeName).toString())));
            if (ascending) {
                Collections.reverse(nodes);
            }
            for (int i = 0; i < numOfNodes; i++) {
                var chosenOne = nodes.get(i);
                graph.removeNode(chosenOne);
            }
        }
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
