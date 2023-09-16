package Components.Simulation;

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
        JButton initButton = new JButton("Init");
        initButton.addActionListener(this::initButtonActionPerformed);
        add(initButton);

        if (!isGraphValid()) {
            return;
        }

        nodeRoleDecoratorList = generateNodeDecoratorList();
        add(generateInputFieldsForRolesAndStates());
        add(new StepButton(graph, nodeRoleDecoratorList));
    }

    private boolean isGraphValid() {
        return graph != null && ApplySimulationHelper.ValidateGraph(graph);
    }

    private List<NodeRoleDecorator> generateNodeDecoratorList() {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        var nodesCount = nodes.size();
        var nodeRoles = nodes.stream()
                .map(node -> node.getAttribute("NodeRole").toString())
                .distinct()
                .collect(Collectors.toList());

        return nodeRoles.stream().map(nodeRole -> new NodeRoleDecorator(new NodeRole(nodeRole)))
                .peek(this::populateNodeRoleDecorator)
                .collect(Collectors.toList());
    }

    private void populateNodeRoleDecorator(NodeRoleDecorator nodeRoleDecorator) {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        var nodesCount = nodes.size();

        var nodeStates = nodes.stream()
                .filter(node -> node.getAttribute("NodeRole").equals(nodeRoleDecorator.getNodeRole().getName()))
                .map(node -> node.getAttribute("NodeState").toString())
                .distinct()
                .map(nodeState -> new NodeStateDecorator(new NodeState(nodeState)))
                .collect(Collectors.toList());

        nodeStates.forEach(state -> state.setColor(getStateColor(state)));

        nodeRoleDecorator.setNodeStates(nodeStates);
        var nodeRoleCount = nodes.stream()
                .filter(node -> node.getAttribute("NodeRole").equals(nodeRoleDecorator.getNodeRole().getName()))
                .count();

        nodeRoleDecorator.setMinCoverage((int) nodeRoleCount);
        nodeRoleDecorator.setCoverage((double)nodeRoleCount / nodesCount);

        nodeStates.forEach(nodeState -> {
            var nodeStateCount = nodes.stream()
                    .filter(node -> node.getAttribute("NodeRole").equals(nodeRoleDecorator.getNodeRole().getName()))
                    .filter(node -> node.getAttribute("NodeState").equals(nodeState.getNodeState().getName()))
                    .count();

            nodeState.setCoverage((double)nodeStateCount / nodeRoleCount);
            nodeState.setMinCoverage((int) nodeStateCount);
        });
    }

    private Color getStateColor(NodeStateDecorator state) {
        var nodes = Arrays.asList(graph.getNodes().toArray());
        return nodes.stream().filter(node -> node.getAttribute("NodeState").equals(state.getNodeState().getName())).collect(Collectors.toList()).get(0).getColor();
    }

    private void initButtonActionPerformed(ActionEvent e) {
        try {
            graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            if (!ApplySimulationHelper.ValidateGraph(graph)) {
                JOptionPane.showMessageDialog(null, "This is not a valid graph model");
            } else {
                initComponents();
                revalidate();
                repaint();
            }
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Set up graph model first");
        }
    }

    private JScrollPane generateInputFieldsForRolesAndStates() {
        var panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        int row = 0;
        int padding = 4;
        for (NodeRoleDecorator role : nodeRoleDecoratorList) {
            addRoleToPanel(panel, role, gbc, row, padding);
            row += 3;
            for (NodeStateDecorator state : role.getNodeStates()) {
                addStateToPanel(panel, state, gbc, row, padding);
                row += 3;
            }
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    private void addRoleToPanel(JPanel panel, NodeRoleDecorator role, GridBagConstraints gbc, int row, int padding) {
        // ... [existing code for adding NodeRole to the panel]
    }

    private void addStateToPanel(JPanel panel, NodeStateDecorator state, GridBagConstraints gbc, int row, int padding) {
        // ... [existing code for adding NodeState to the panel]
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
