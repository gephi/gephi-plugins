package Simulation;


import Helper.ApplySimulationHelper;
import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.Node.NodeState;
import SimulationModel.Node.NodeStateDecorator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ConvertAsProperties(dtd = "-//Simulation//Simulation//EN", autostore = false)
@TopComponent.Description(preferredID = "Simulation",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "Simulation")
@ActionReference(path = "Menu/Window", position = 0)
@TopComponent.OpenActionRegistration(displayName = "#CTL_Simulation",
        preferredID = "Simulation")
public class SimulationComponent extends TopComponent {

    private Graph graph;
    private List<NodeRoleDecorator> nodeRoleDecoratorList;

    public SimulationComponent() {
        initComponents();
        setName("Simulation");
        setToolTipText("Simulation");
    }

    private void initComponents() {
        this.removeAll();
        setLayout(new FlowLayout());
        var initButton = new JButton("Init");
        initButton.addActionListener(new InitButtonListener());
        add(initButton);

        if(graph == null || !ApplySimulationHelper.ValidateGraph(graph))
            return;

        nodeRoleDecoratorList = GenerateNodeDecoratorList();
        add(generateInputFieldsForRolesAndStates());

    }

    private List<NodeRoleDecorator> GenerateNodeDecoratorList() {
        var nodes = Arrays.stream(graph.getNodes().toArray()).collect(Collectors.toList());
        var nodesCount = nodes.stream().count();
        var nodeRoles = nodes.stream().map(node -> node.getAttribute("NodeRole").toString()).distinct().collect(Collectors.toList());
        var nodeRolesDecorator = nodeRoles.stream().map(nodeRole -> new NodeRoleDecorator(new NodeRole(nodeRole))).collect(Collectors.toList());
        for (NodeRoleDecorator nodeRoleDecorator: nodeRolesDecorator) {
            var nodeStates = nodes.stream()
                    .filter(node -> node.getAttribute("NodeRole") == nodeRoleDecorator.getNodeRole().getName())
                    .map(node -> node.getAttribute("NodeState").toString())
                    .distinct()
                    .map(node -> new NodeStateDecorator(new NodeState(node)))
                    .collect(Collectors.toList());

            nodeRoleDecorator.setNodeStates(nodeStates);

            var nodeRoleCount = nodes.stream()
                    .filter(node -> node.getAttribute("NodeRole") == nodeRoleDecorator.getNodeRole().getName())
                    .count();
            nodeRoleDecorator.setMinCoverage((int) nodeRoleCount);
            nodeRoleDecorator.setCoverage((double)nodeRoleCount/nodesCount);

            for (NodeStateDecorator nodeState: nodeStates) {
                var nodeStateCount = nodes.stream()
                        .filter(node -> node.getAttribute("NodeRole") == nodeRoleDecorator.getNodeRole().getName())
                        .filter(node -> node.getAttribute("NodeState") == nodeState.getNodeState().getName())
                        .count();
                nodeState.setCoverage((double)nodeStateCount/nodeRoleCount);
                nodeState.setMinCoverage((int)nodeStateCount);
            }

        }
        return nodeRolesDecorator;
    }

    private class InitButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                if(!ApplySimulationHelper.ValidateGraph(graph)){
                    JOptionPane.showMessageDialog(null, "This is not valid graph model");
                }
                else{
                    initComponents();
                    revalidate();
                    repaint();
                }
            }
            catch (NullPointerException ex){
                JOptionPane.showMessageDialog(null, "Setup graph model first");
            }
        }
    }

    public JScrollPane generateInputFieldsForRolesAndStates() {

        var panel = new JPanel();

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        int row = 0;
        int padding = 4;

        for (NodeRoleDecorator role : nodeRoleDecoratorList) {
            gbc.insets = new Insets(padding, padding, padding, padding);
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel nodeLabel = new JLabel("NodeRole: " + role.getNodeRole().getName());
            Font currentFont = nodeLabel.getFont();
            nodeLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize()+5)); // Wytłuszczenie i zwiększenie rozmiaru o 2 punkty
            panel.add(nodeLabel, gbc);

            gbc.gridy = row++;
            panel.add(new JLabel("Coverage:"), gbc);
            gbc.gridx = 1;
            JTextField roleCoverageField = new JTextField(10); // 10 columns wide
            roleCoverageField.setEditable(false);
            roleCoverageField.setText(role.getCoverage().toString());

            panel.add(roleCoverageField, gbc);

            gbc.gridy = row++;
            gbc.gridx = 0;
            panel.add(new JLabel("Count:"), gbc);
            gbc.gridx = 1;
            JTextField roleMinCoverageField = new JTextField(10);
            roleMinCoverageField.setEditable(false);
            roleMinCoverageField.setText(role.getMinCoverage().toString());

            panel.add(roleMinCoverageField, gbc);

            for (NodeStateDecorator state : role.getNodeStates()) {
                gbc.insets = new Insets(padding, padding, padding, padding);
                gbc.gridy = row++;
                gbc.gridx = 0;

                JLabel stateLabel = new JLabel("NodeState: " + state.getNodeState().getName());
                currentFont = stateLabel.getFont();
                stateLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize())); // Wytłuszczenie i zwiększenie rozmiaru o 2 punkty
                panel.add(stateLabel, gbc);

                gbc.gridy = row++;
                panel.add(new JLabel("Coverage:"), gbc);
                gbc.gridx = 1;
                JTextField stateCoverageField = new JTextField(10); // 10 columns wide
                stateCoverageField.setEditable(false);
                stateCoverageField.setText(state.getCoverage().toString());
                panel.add(stateCoverageField, gbc);

                gbc.gridy = row++;
                gbc.gridx = 0;
                panel.add(new JLabel("Count:"), gbc);
                gbc.gridx = 1;
                JTextField stateMinCoverageField = new JTextField(10); // 10 columns wide
                stateMinCoverageField.setText(state.getMinCoverage().toString());
                stateMinCoverageField.setEditable(false);

                panel.add(stateMinCoverageField, gbc);
            }
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL; // Dodajemy wypełnienie poziome dla separatora
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;

        }


        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
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
