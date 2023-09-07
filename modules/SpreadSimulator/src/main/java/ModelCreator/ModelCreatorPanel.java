package ModelCreator;

import Helper.ObjectMapperHelper;
import SimulationModel.Node.NodeRole;
import SimulationModel.SimulationModel;
import SimulationModel.Transition.CustomDeserializer.TransitionDeserializer;
import SimulationModel.Transition.Transition;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
public class ModelCreatorPanel extends JPanel {
    private final JPanel folderListPanel;
    private final ModelCreator modelCreator;

    public ModelCreatorPanel(ModelCreator modelCreator) {
        this.modelCreator = modelCreator;
        this.modelCreator.setSimulationModel(new SimulationModel());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        folderListPanel = new JPanel();
        folderListPanel.setLayout(new BoxLayout(folderListPanel, BoxLayout.Y_AXIS));
        refreshFolderList();

        JPanel createFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createFolderPanel.add(new JLabel("Insert params and setup fire strategy:"));
        add(createFolderPanel);
        add(folderListPanel);
    }

    private void refreshFolderList() {
        folderListPanel.removeAll();

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
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JButton button = new JButton(subFolder.getName());
                button.addActionListener(new ModelListener(subFolder));
                rowPanel.add(button);

                File[] listOfFiles = subFolder.listFiles();
                StringBuilder fileListBuilder = new StringBuilder("State Machines: ");
                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        if (!file.isDirectory()) {
                            fileListBuilder.append(file.getName()).append(": ");
                        }
                    }
                }
                JLabel fileListLabel = new JLabel(fileListBuilder.toString());
                rowPanel.add(fileListLabel);

                folderListPanel.add(rowPanel);
            }
        } else {
            folderListPanel.add(new JLabel("No folders found in 'models/'"));
        }

        revalidate();
        repaint();
    }


    @Getter @Setter
    public class ModelListener implements ActionListener {

        private File subFolder;
        private ArrayList<FileFireStrategy> fireStrategyFiles;
        public ModelListener(File subFolder){
            this.subFolder = subFolder;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            var nodeRoles = new ArrayList<NodeRole>();

            ObjectMapper objectMapper = ObjectMapperHelper.CustomObjectMapperCreator();
            fireStrategyFiles = initFileFireStrategyArray();
            LoadNodeRolesFromFiles(nodeRoles, objectMapper, fireStrategyFiles);

            var model = modelCreator.getSimulationModel();
            model.setNodeRoles(nodeRoles);
            model.setName(subFolder.getName());
            modelCreator.setSimulationModel(model);

        }

        private void LoadNodeRolesFromFiles(ArrayList<NodeRole> nodeRoles, ObjectMapper objectMapper, ArrayList<FileFireStrategy> fireStrategyFiles) {
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
        }

        private ArrayList<FileFireStrategy> initFileFireStrategyArray() {
            var fireStrategyFiles = new ArrayList<FileFireStrategy>();

            File[] listOfFiles = subFolder.listFiles();
            if (listOfFiles == null)
                return fireStrategyFiles;

            for (File file : listOfFiles) {
                var fireStrategyFile = new FileFireStrategy();
                fireStrategyFile.setFile(file);
                fireStrategyFile.setCoverage(1.0 / listOfFiles.length);
                fireStrategyFile.setMinCoverage(1);
                fireStrategyFiles.add(fireStrategyFile);
            }
            return fireStrategyFiles;
        }
    }
}