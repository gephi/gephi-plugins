package Components.SimulationBuilder;

import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeStateDecorator;
import org.gephi.graph.api.GraphController;
import org.gephi.statistics.plugin.Degree;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class AdvancedAssigmentButton extends JButton {
    private NodeRole nodeRole;
    private NodeStateDecorator nodeStateDecorator;
    AdvancedAssigmentButton(NodeRole nodeRole, NodeStateDecorator nodeStateDecorator){
        this.nodeRole = nodeRole;
        this.nodeStateDecorator = nodeStateDecorator;
        this.addActionListener(new AdvancedAssigmentButtonListner());
        setText("Options");
    }

    private class AdvancedAssigmentButtonListner implements ActionListener {

        public AdvancedAssigmentButtonListner() {

        }

        JTextField numOfNodesInput;
        JList centralityRateList;
        @Override
        public void actionPerformed(ActionEvent e) {
            var dialog = new JDialog();

            var panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            var numOfNodesLabel = new JLabel("Number of nodes");
            numOfNodesInput = new JTextField(10);

            var centralityRateLabel = new JLabel("Centrality Rate");
            var centralityRateListModel = new DefaultListModel<>();
            centralityRateListModel.addElement("Closeness");
            centralityRateListModel.addElement("Betweenness");
            centralityRateListModel.addElement("Degree");
            centralityRateListModel.addElement("Closeness");
            centralityRateListModel.addElement("Prestige");
            centralityRateList = new JList<>(centralityRateListModel);
            centralityRateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            var applyButton = new JButton("Apply");
            applyButton.addActionListener(new ApplyChangesListener());

            panel.add(numOfNodesLabel);
            panel.add(numOfNodesInput);
            panel.add(centralityRateLabel);
            panel.add(new JScrollPane(centralityRateList));
            panel.add(applyButton);

            dialog.add(panel);

            dialog.setTitle("Advanced options: " + nodeStateDecorator.getNodeState().getName());
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }


        private class ApplyChangesListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                var graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
                var graph = graphModel.getGraph();
                var centralityMethod = centralityRateList.getSelectedValue().toString();
                var numOfNodesString = numOfNodesInput.getText();
                var numOfNodes = Integer.valueOf(numOfNodesString);
                switch (centralityMethod){
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
