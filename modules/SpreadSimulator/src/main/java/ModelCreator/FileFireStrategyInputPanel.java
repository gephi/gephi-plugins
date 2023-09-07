package ModelCreator;

import Helper.ObjectMapperHelper;
import ModelCreator.FileFireStrategy;
import SimulationModel.Node.NodeRole;
import org.codehaus.jackson.map.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

class FileFireStrategyInputPanel extends JPanel {
    private ArrayList<FileFireStrategy> fireStrategyFiles;
    private ModelCreator modelCreator;

    public FileFireStrategyInputPanel(ModelCreator modelCreator, ArrayList<FileFireStrategy> fireStrategyFiles) {
        this.fireStrategyFiles = fireStrategyFiles;
        this.modelCreator = modelCreator;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (FileFireStrategy fileStrategy : fireStrategyFiles) {
            JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            filePanel.add(new JLabel(fileStrategy.getFile().getName()));

            JTextField coverageField = new JTextField(5);
            JTextField minCoverageField = new JTextField(5);

            filePanel.add(new JLabel("Coverage:"));
            filePanel.add(coverageField);

            filePanel.add(new JLabel("minCoverage:"));
            filePanel.add(minCoverageField);

            JButton updateButton = new JButton("Aktualizuj");
            updateButton.addActionListener(e -> {
                try {
                    double coverage = Double.parseDouble(coverageField.getText());
                    int minCoverage = Integer.parseInt(minCoverageField.getText());

                    fileStrategy.setCoverage(coverage);
                    fileStrategy.setMinCoverage(minCoverage);

                    ObjectMapper objectMapper = ObjectMapperHelper.CustomObjectMapperCreator();
                    var nodeRoles = LoadNodeRolesFromFiles(objectMapper, fireStrategyFiles);

                    var model = modelCreator.getSimulationModel();
                    model.setNodeRoles(nodeRoles);
                    modelCreator.setSimulationModel(model);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Proszę wprowadzić poprawne wartości.");
                }
            });

            filePanel.add(updateButton);
            add(filePanel);


        }
    }

    private ArrayList<NodeRole> LoadNodeRolesFromFiles(ObjectMapper objectMapper, ArrayList<FileFireStrategy> fireStrategyFiles) {
        var nodeRoles = new ArrayList<NodeRole>();
        for (FileFireStrategy fileFireStrategy : fireStrategyFiles) {
            if (!fileFireStrategy.getFile().isDirectory()) {
                String content = null;
                try {
                    content = new String(Files.readAllBytes(Paths.get(fileFireStrategy.getFile().getAbsolutePath())));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                try {
                    var nodeRole = objectMapper.readValue(content, NodeRole.class);
                    nodeRole.setCoverage(fileFireStrategy.getCoverage());
                    nodeRole.setMinCoverage(fileFireStrategy.getMinCoverage());
                    nodeRoles.add(nodeRole);

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return nodeRoles;
    }

}
