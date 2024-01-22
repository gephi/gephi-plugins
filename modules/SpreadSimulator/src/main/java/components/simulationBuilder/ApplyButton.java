package components.simulationBuilder;

import configLoader.ConfigLoader;
import helper.ApplySimulationHelper;
import simulationModel.node.NodeRoleDecorator;
import simulationModel.SimulationModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.stream.Collectors;

public class ApplyButton extends JButton {

    private final SimulationBuilderComponent simulationBuilderComponent;

    public ApplyButton(SimulationBuilderComponent simulationBuilderComponent) {
        this.simulationBuilderComponent = simulationBuilderComponent;
        this.setText("Apply");
        this.addActionListener(new ApplyButtonActionListener(this.simulationBuilderComponent, simulationBuilderComponent.getNodeRoles()));
    }

    private class ApplyButtonActionListener implements ActionListener {

        private final SimulationBuilderComponent component;
        private final List<NodeRoleDecorator> nodeRoles;
        private final SimulationModel simulationModel;

        private ApplyButtonActionListener(SimulationBuilderComponent component, List<NodeRoleDecorator> nodeRoles) {
            this.component = component;
            this.nodeRoles = nodeRoles;
            this.simulationModel = component.getSimulationModel();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            simulationModel.setNodeRoles(nodeRoles);
            simulationModel.setName(ApplySimulationHelper.GenerateName(nodeRoles));
            try {
                Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                ApplySimulationHelper.ClearModel(graph);
                ApplySimulationHelper.CrateModelColumns(graph);
                ApplySimulationHelper.Apply(graph, simulationModel);

                var rules = component.advancedRules.keySet();
                for (var rule: rules) {
                    var names = rule.split("_");
                    var rulesList =component.advancedRules.get(rule);
                    for(var rul : rulesList){
                        ExecuteRule(rul, names[0], names[1]);
                    }
                }
            }
            catch (NullPointerException ex){
                JOptionPane.showMessageDialog(null, "Setup graph model first");
            }
        }
        private void ExecuteRule(AdvancedRule rule, String roleName, String stateName){
            var graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
            var graph = graphModel.getGraph();
            String centralityMethod = rule.rule;
            Integer numOfNodes = rule.coverage;
            boolean ascending = rule.ascending;
            switch (centralityMethod) {
                case "Random":
                    RandomNStrategy(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Random-Random":
                    RandomRandomStrategy(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Closeness":
                    GraphDistanceClosenessStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Harmonic Closeness":
                    GraphDistanceHarmonicClosenessStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Betwenness":
                    GraphDistanceBetweenessStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Degree":
                    DegreeStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Eigenvector":
                    EigenvectorStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "HITS - authority":
                    HITSAuthorityStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                case "HITS - hub":
                    HITSHubStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Eccentricity":
                    GraphDistanceEccentricityStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                case "Modularity":
                    GraphDistanceModularityStatisticOption(graph, numOfNodes, ascending, roleName, stateName);
                    break;
                default:
                    throw new NotImplementedException();
            }
        }

        private void RandomNStrategy(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            Node[] nodes = graph.getNodes().toArray();
            var rnd = new Random();
            for (int i = 0; i < numOfNodes; i++) {
                var index = rnd.nextInt(nodes.length);
                var selectedNode = nodes[index];
                selectedNode.setAttribute(ConfigLoader.colNameNodeRole, nodeRoleName);
                selectedNode.setAttribute(ConfigLoader.colNameNodeState, nodeStateName);
            }
        }

        private void RandomRandomStrategy(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            Node[] nodes = graph.getNodes().toArray();
            var rnd = new Random();
            for (int i = 0; i < numOfNodes; i++) {
                var index = rnd.nextInt(nodes.length);
                var selectedNode = nodes[index];
                var neighbours = graph.getNeighbors(selectedNode).toArray();
                index = rnd.nextInt(neighbours.length);
                selectedNode = neighbours[index];
                selectedNode.setAttribute(ConfigLoader.colNameNodeRole, nodeRoleName);
                selectedNode.setAttribute(ConfigLoader.colNameNodeState, nodeStateName);
            }
        }

        private void GraphDistanceClosenessStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "closnesscentrality", nodeRoleName, nodeStateName);
        }

        private void GraphDistanceHarmonicClosenessStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "harmonicclosnesscentrality", nodeRoleName, nodeStateName);
        }

        private void GraphDistanceBetweenessStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "betweenesscentrality", nodeRoleName, nodeStateName);
        }

        private void DegreeStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var degree = new Degree();
            degree.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "Degree", nodeRoleName, nodeStateName);
        }

        private void EigenvectorStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var eigenvector = new EigenvectorCentrality();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "eigencentrality", nodeRoleName, nodeStateName);
        }

        private void HITSAuthorityStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var hits = new Hits();
            hits.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "Authority", nodeRoleName, nodeStateName);
        }

        private void HITSHubStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var hits = new Hits();
            hits.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "Hub", nodeRoleName, nodeStateName);
        }

        private void GraphDistanceEccentricityStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var eigenvector = new GraphDistance();
            eigenvector.setDirected(false);
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "eccentricity", nodeRoleName, nodeStateName);
        }

        private void GraphDistanceModularityStatisticOption(Graph graph, Integer numOfNodes, Boolean descending, String nodeRoleName, String nodeStateName) {
            var eigenvector = new Modularity();
            eigenvector.execute(graph);
            StatisticsOptions(graph, numOfNodes, descending, "modularity_class", nodeRoleName, nodeStateName);
        }


        private void StatisticsOptions(Graph graph, Integer numOfNodes, Boolean descending, String attributeName, String nodeRoleName, String nodeStateName) {
            var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());
            nodes.sort(Comparator.comparingDouble(node -> Double.parseDouble(node.getAttribute(attributeName).toString())));
            if (descending) {
                Collections.reverse(nodes);
            }
            for (int i = 0; i < numOfNodes; i++) {
                var chosenOne = nodes.get(i);
                chosenOne.setAttribute(ConfigLoader.colNameNodeRole, nodeRoleName);
                chosenOne.setAttribute(ConfigLoader.colNameNodeState, nodeStateName);
            }
        }
    }
}
