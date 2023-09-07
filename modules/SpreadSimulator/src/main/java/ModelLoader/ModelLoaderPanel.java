package ModelLoader;

import SimulationModel.Node.NodeRole;
import SimulationModel.Transition.CustomDeserializer.TransitionDeserializer;
import SimulationModel.Transition.Transition;
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
import javax.swing.filechooser.FileNameExtensionFilter;

public class ModelLoaderPanel extends JPanel {
    private final JPanel folderListPanel;

    public ModelLoaderPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        folderListPanel = new JPanel();
        folderListPanel.setLayout(new BoxLayout(folderListPanel, BoxLayout.Y_AXIS));
        refreshFolderList();

        JTextField newFolderTextField = new JTextField(20);
        JButton createButton = new JButton("Create Model");

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderName = newFolderTextField.getText().trim();
                if (!folderName.isEmpty()) {
                    File newFolder = new File("models/" + folderName);
                    if (!newFolder.exists()) {
                        if (newFolder.mkdir()) {
                            refreshFolderList();
                            SwingUtilities.getWindowAncestor(ModelLoaderPanel.this).pack();  // Nowa linia
                        } else {
                            JOptionPane.showMessageDialog(null, "Couldn't create model.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Model already exists.");
                    }
                }
            }
        });

        JPanel createFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createFolderPanel.add(new JLabel("Create new model:"));
        createFolderPanel.add(newFolderTextField);
        createFolderPanel.add(createButton);
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
                JLabel label = new JLabel("Simulation model: " + subFolder.getName());
                JButton button = new JButton("Load JSON");
                button.addActionListener(new ModelListener(subFolder));
                rowPanel.add(label);
                rowPanel.add(button);

                File[] listOfFiles = subFolder.listFiles();
                StringBuilder fileListBuilder = new StringBuilder("State Machines: ");
                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        if (!file.isDirectory()) {
                            fileListBuilder.append(file.getName()).append(", ");
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


    private class ModelListener implements ActionListener {

        private File subFolder;
        public ModelListener(File subFolder){
            this.subFolder = subFolder;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(subFolder);
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));

            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String content = null;
                try {
                    content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                SimpleModule module =
                        new SimpleModule("TransitionCustomDeserializer", new Version(1, 0, 0, null));
                module.addDeserializer(Transition.class, new TransitionDeserializer());
                objectMapper.registerModule(module);

                try {
                    var nodeRole = objectMapper.readValue(content, NodeRole.class);

                    try {
                        // Zapisuje obiekt do pliku JSON
                        File jsonFile = new File("models/"+ subFolder.getName() + "/"+ nodeRole.getName() + ".json");
                        objectMapper.writeValue(jsonFile, nodeRole);

                    } catch (IOException exx) {
                        exx.printStackTrace();
                    }
                    refreshFolderList();
                    SwingUtilities.getWindowAncestor(ModelLoaderPanel.this).pack();
                    JOptionPane.showMessageDialog(null, "Model has been loaded");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}