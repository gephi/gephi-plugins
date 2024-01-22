package components.reverseSimulation.buttons;

import components.reverseSimulation.ReverseSimulationComponent;
import configLoader.ConfigLoader;
import helper.ApplySimulationHelper;
import helper.ObjectMapperHelper;
import simulationModel.node.NodeRole;
import simulationModel.node.NodeRoleDecorator;
import simulationModel.node.NodeState;
import simulationModel.node.NodeStateDecorator;
import lombok.Setter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;

import javax.swing.*;
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

public class ChangeModelButton extends JButton {

    private final ReverseSimulationComponent reverseSimulationComponent;
    @Setter
    private final String modelName;

    public ChangeModelButton(ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Change model");
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new ChangeModelListener());
        modelName = "";
    }

    private class ChangeModelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showModelFolders();
            reverseSimulationComponent.initComponents();
            reverseSimulationComponent.revalidate();
            reverseSimulationComponent.repaint();
        }
    }

    private void showModelFolders() {
        JDialog customDialog = new JDialog();
        var folderListPanel = new JPanel();
        folderListPanel.setLayout((new BoxLayout(folderListPanel, BoxLayout.Y_AXIS)));
        folderListPanel.setSize(400, 300);
        File folder = new File(ConfigLoader.folderSimulationBuilderModels);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                folderListPanel.add(new JLabel("Cannot create folder " + ConfigLoader.folderSimulationBuilderModels));
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
            folderListPanel.add(new JLabel("No folders found in " + ConfigLoader.folderSimulationBuilderModels));
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
            var nodeRoles = new ArrayList<NodeRoleDecorator>();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(subFolder);

            var mapper = ObjectMapperHelper.CustomObjectMapperCreator();


            File[] listOfFiles = subFolder.listFiles();
            String fileName = "";
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (!file.isDirectory()) {
                        try {
                            var content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                            var nodeRole = mapper.readValue(content, NodeRole.class);
                            var nodeSourceStates = nodeRole.getTransitionMap().stream().map(x -> x.getSourceState().getName()).distinct().collect(Collectors.toList());
                            var nodeDestinationStates = nodeRole.getTransitionMap().stream().map(x -> x.getDestinationState().getName()).distinct().collect(Collectors.toList());
                            var nodeStates = nodeSourceStates;
                            nodeStates.addAll(nodeDestinationStates);
                            nodeStates = nodeStates.stream().distinct().collect(Collectors.toList());
                            List<NodeState> finalNodeStates = nodeStates.stream().map(NodeState::new).collect(Collectors.toList());
                            var nodeStatesDecorator = finalNodeStates.stream().map(x -> new NodeStateDecorator(1.0 / finalNodeStates.size(), 1, x)).collect(Collectors.toList());
                            var nodeRoleDecorator = new NodeRoleDecorator(1.0 / listOfFiles.length, 1, nodeRole, nodeStatesDecorator);
                            nodeRoles.add(nodeRoleDecorator);
                            fileName = file.getName().replace(".josn", "");
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            reverseSimulationComponent.setModelName(fileName);
            reverseSimulationComponent.setNodeRoles(nodeRoles);
            reverseSimulationComponent.getSimulationModel().setNodeRoles(nodeRoles);
            reverseSimulationComponent.getSimulationModel().setName(ApplySimulationHelper.GenerateName(nodeRoles));
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            ApplySimulationHelper.Apply(graph, reverseSimulationComponent.getSimulationModel());
            reverseSimulationComponent.initComponents();
            dialog.dispose();
            reverseSimulationComponent.revalidate();
            reverseSimulationComponent.repaint();
        }
    }
}
