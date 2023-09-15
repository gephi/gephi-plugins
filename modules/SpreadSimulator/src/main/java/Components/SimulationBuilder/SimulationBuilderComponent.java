package Components.SimulationBuilder;

import Helper.ApplySimulationHelper;
import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeRole;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.Node.NodeState;
import SimulationModel.Node.NodeStateDecorator;
import SimulationModel.SimulationModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ConvertAsProperties(dtd = "-//Simulation//SimulationBuilder//EN", autostore = false)
@TopComponent.Description(preferredID = "SimulationBuilder",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "SimulationBuilder")
@ActionReference(path = "Menu/Window", position = 1)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulationBuilderComponent",
        preferredID = "SimulationBuilder")
public class SimulationBuilderComponent extends TopComponent {

    private SimulationModel simulationModel;
    private List<NodeRoleDecorator> nodeRoles;

    public SimulationBuilderComponent() {
        simulationModel = new SimulationModel();
        initComponents();
        setName("Simulation Builder");
        setToolTipText("Simulation Builder");
    }

    private void initComponents() {
        this.removeAll();
        setLayout(new FlowLayout());

        JButton createButton = new JButton("Create");
        createButton.addActionListener(new CreateListener());

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new LoadListener());

        add(createButton);
        add(loadButton);

        if (nodeRoles != null && nodeRoles.size() > 0) {
            add(generateInputFieldsForRolesAndStates());


            var apply = new Button("Apply");
            apply.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    simulationModel.setNodeRoles(nodeRoles);
                    simulationModel.setName(generateName());
                    try {
                        Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                        ApplySimulationHelper.ClearModel(graph);
                        ApplySimulationHelper.CrateModelColumns(graph);
                        ApplySimulationHelper.Apply(graph, simulationModel);
                    }
                    catch (NullPointerException ex){
                        JOptionPane.showMessageDialog(null, "Setup graph model first");
                    }

                }
            });

            add(apply);

            var save = new Button("Save");
            save.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    File folder = new File("simulations/");
                    if (!folder.exists()) {
                        if (!folder.mkdir()) {
                            JOptionPane.showMessageDialog(null,"Nie można utworzyć folderu 'simulations/'");
                            return;
                        }
                    }

                    simulationModel.setNodeRoles(nodeRoles);
                    simulationModel.setName(generateName());
                    var mapper = ObjectMapperHelper.CustomObjectMapperCreator();
                    try {
                        File jsonFile = new File("simulations/"+ simulationModel.getName() + ".json");
                        mapper.writeValue(jsonFile, simulationModel);
                        JOptionPane.showMessageDialog(null,"Model saved as " + simulationModel.getName() + ".json");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,"Unexpected error");
                        throw new RuntimeException(ex);
                    }
                }
            });

            add(save);
        }
    }

    private String generateName(){
        var name = "SIMULATION-";
        for (NodeRoleDecorator role: nodeRoles) {
            name += role.getNodeRole().getName();
            name += "-";
            name += role.getCoverage().toString();
            name += "-states-";
            for (NodeStateDecorator state: role.getNodeStates()) {
                name += state.getNodeState().getName();
                name += "-";
                name += state.getCoverage().toString();
            }
            name += "_";
        }
        return name;
    }

    private class CreateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showModelFolders();
        }
    }

    private void showModelFolders() {
        JDialog customDialog = new JDialog();
        var folderListPanel = new JPanel();
        folderListPanel.setLayout((new BoxLayout(folderListPanel, BoxLayout.Y_AXIS)));
        folderListPanel.setSize(400, 300);
        File folder = new File("models/");
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                folderListPanel.add(new JLabel("Nie można utworzyć folderu 'models/'"));
                return;
            }
        }

        File[] listOfFolders = folder.listFiles(File::isDirectory);
        if (listOfFolders != null && listOfFolders.length > 0) {
            for (File subFolder : listOfFolders) {
                JPanel rowPanel = new JPanel();
                rowPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                rowPanel.setSize(390, 10);

                JButton button = new JButton(subFolder.getName());
                button.setPreferredSize(new Dimension(390, 40));
                button.addActionListener(new ModelListener(subFolder, customDialog));
                rowPanel.add(button);

                folderListPanel.add(rowPanel);
            }
        } else {
            folderListPanel.add(new JLabel("No folders found in 'models/'"));
        }

        JScrollPane scrollPane = new JScrollPane(folderListPanel);

        customDialog.setTitle("Select simulation folder");
        customDialog.setSize(400, 300);
        customDialog.getContentPane().add(scrollPane);
        customDialog.setLocationRelativeTo(this);
        customDialog.setVisible(true);
    }

    private class ModelListener implements ActionListener {

        private File subFolder;
        private JDialog dialog;

        public ModelListener(File subFolder, JDialog dialog) {
            this.dialog = dialog;
            this.subFolder = subFolder;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            nodeRoles = new ArrayList<>();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(subFolder);

            var mapper = ObjectMapperHelper.CustomObjectMapperCreator();


            File[] listOfFiles = subFolder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (!file.isDirectory()) {
                        try {
                            var content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                            var nodeRole = mapper.readValue(content, NodeRole.class);
                            var nodeSourceStates = nodeRole.getTransitionMap().stream().map(x -> x.getSourceState().getName()).distinct().collect(Collectors.toList());
                            var nodeDestinationStates = nodeRole.getTransitionMap().stream().map(x -> x.getSourceState().getName()).distinct().collect(Collectors.toList());
                            var nodeStates = nodeSourceStates;
                            nodeStates.addAll(nodeDestinationStates);
                            nodeStates = nodeStates.stream().distinct().collect(Collectors.toList());
                            List<NodeState> finalNodeStates = nodeStates.stream().map(state -> new NodeState(state)).collect(Collectors.toList());
                            var nodeStatesDecorator = finalNodeStates.stream().map(x -> new NodeStateDecorator(1.0 / finalNodeStates.size(), 1, x)).collect(Collectors.toList());
                            var nodeRoleDecorator = new NodeRoleDecorator(1.0 / listOfFiles.length, 1, nodeRole, nodeStatesDecorator);
                            nodeRoles.add(nodeRoleDecorator);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            initComponents();
            dialog.dispose();
        }
    }

    private class LoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, "load button clicked");
        }
    }

    public JScrollPane generateInputFieldsForRolesAndStates() {

        var panel = new JPanel();

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        int row = 0;
        int padding = 4;

        for (NodeRoleDecorator role : nodeRoles) {
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
            roleCoverageField.setText(role.getCoverage().toString());

            panel.add(roleCoverageField, gbc);

            gbc.gridy = row++;
            gbc.gridx = 0;
            panel.add(new JLabel("MinCoverage:"), gbc);
            gbc.gridx = 1;
            JTextField roleMinCoverageField = new JTextField(10); // 10 columns wide
            roleMinCoverageField.setText(role.getMinCoverage().toString());

            roleCoverageField.getDocument().addDocumentListener(new NodeRoleListener(role, roleCoverageField, roleMinCoverageField));
            roleMinCoverageField.getDocument().addDocumentListener(new NodeRoleListener(role, roleCoverageField, roleMinCoverageField));
            panel.add(roleMinCoverageField, gbc);

            for (NodeStateDecorator state : role.getNodeStates()) {
                gbc.insets = new Insets(padding, padding, padding, padding);
                gbc.gridy = row++;
                gbc.gridx = 0;

                JLabel stateLabel = new JLabel("NodeState: " + state.getNodeState().getName());
                currentFont = stateLabel.getFont();
                stateLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize())); // Wytłuszczenie i zwiększenie rozmiaru o 2 punkty
                panel.add(stateLabel, gbc);

                gbc.gridx = 1;
                var advancedButton = new AdvancedAssigmentButton(role.getNodeRole(), state);
                panel.add(advancedButton, gbc);

                gbc.gridx = 0;
                gbc.gridy = row++;
                panel.add(new JLabel("Coverage:"), gbc);
                gbc.gridx = 1;
                JTextField stateCoverageField = new JTextField(10); // 10 columns wide
                stateCoverageField.setText(state.getCoverage().toString());
                panel.add(stateCoverageField, gbc);

                gbc.gridy = row++;
                gbc.gridx = 0;
                panel.add(new JLabel("MinCoverage:"), gbc);
                gbc.gridx = 1;
                JTextField stateMinCoverageField = new JTextField(10);
                stateMinCoverageField.setText(state.getMinCoverage().toString());

                stateCoverageField.getDocument().addDocumentListener(new NodeStateListener(state, stateCoverageField, stateMinCoverageField));
                stateMinCoverageField.getDocument().addDocumentListener(new NodeStateListener(state, stateCoverageField, stateMinCoverageField));

                panel.add(stateMinCoverageField, gbc);
            }
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;

        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    private class NodeRoleListener implements DocumentListener{

        private NodeRoleDecorator nodeRole;
        private JTextField coverage;
        private JTextField minCoverage;

        public NodeRoleListener(NodeRoleDecorator nodeRole, JTextField coverage, JTextField minCoverage) {
            this.nodeRole = nodeRole;
            this.coverage = coverage;
            this.minCoverage = minCoverage;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            try {
                nodeRole.setCoverage(Double.parseDouble(coverage.getText()));
                nodeRole.setMinCoverage(Integer.parseInt(minCoverage.getText()));
            } catch (NumberFormatException ex) {
            }
        }
    }

    private class NodeStateListener implements DocumentListener{

        private NodeStateDecorator nodeState;
        private JTextField coverage;
        private JTextField minCoverage;

        public NodeStateListener(NodeStateDecorator nodeState, JTextField coverage, JTextField minCoverage) {
            this.nodeState = nodeState;
            this.coverage = coverage;
            this.minCoverage = minCoverage;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            try {
                nodeState.setCoverage(Double.parseDouble(coverage.getText()));
                nodeState.setMinCoverage(Integer.parseInt(minCoverage.getText()));
            } catch (NumberFormatException ex) {
            }
        }
    }


    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}
