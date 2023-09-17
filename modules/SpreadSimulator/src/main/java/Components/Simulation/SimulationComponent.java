package Components.Simulation;

import Helper.ApplySimulationHelper;
import SimulationModel.Node.NodeRoleDecorator;
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
    private Simulation simulation;

    public SimulationComponent() {
        initComponents();
        setName("Simulation");
        setToolTipText("Simulation");
    }

    public void initComponents() {
        this.removeAll();
        setLayout(new FlowLayout());
        JButton initButton = new JButton("Init");
        initButton.addActionListener(this::initButtonActionPerformed);
        add(initButton);

        if (!isGraphValid()) {
            return;
        }

        add(generateInputFieldsForRolesAndStates());
        add(new StepButton(simulation, this));
        add(new SimulationButton(simulation, this));
        add(new GetReportButton(simulation, this));
    }

    private boolean isGraphValid() {
        return graph != null && ApplySimulationHelper.ValidateGraph(graph);
    }

    private void initButtonActionPerformed(ActionEvent e) {
        try {
            graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            if (!ApplySimulationHelper.ValidateGraph(graph)) {
                JOptionPane.showMessageDialog(null, "This is not a valid graph model");
            } else {
                simulation = new Simulation(graph);
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

        var stepLabel = new JLabel("Step: " + simulation.getStep().toString());
        panel.add(stepLabel);
        int row = 1;
        int padding = 4;
        for (NodeRoleDecorator role : simulation.nodeRoleDecoratorList) {
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

    }

    private void addStateToPanel(JPanel panel, NodeStateDecorator state, GridBagConstraints gbc, int row, int padding) {
        gbc.insets = new Insets(padding, padding, padding, padding);
        gbc.gridy = row++;
        gbc.gridx = 0;

        JLabel stateLabel = new JLabel("NodeState: " + state.getNodeState().getName());
        var currentFont = stateLabel.getFont();
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
