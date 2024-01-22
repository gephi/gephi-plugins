package components.modelLoader;


import configLoader.ConfigLoader;
import helper.ObjectMapperHelper;
import simulationModel.node.NodeRole;
import org.codehaus.jackson.map.ObjectMapper;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@ConvertAsProperties(dtd = "-//Simulation//ModelLoader//EN", autostore = false)
@TopComponent.Description(preferredID = "ModelLoader",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "ModelLoader")
@ActionReference(path = "Menu/Window", position = 0)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ModelLoaderComponent",
        preferredID = "ModelLoader")
public class ModelLoaderComponent extends TopComponent {

    private JPanel folderListPanel;

    public ModelLoaderComponent() {
        initComponents();
        setName("Model Loader");
        setToolTipText("Model Loader");
    }

    private void initComponents() {
        this.removeAll();
        setLayout(new BorderLayout()); // Use BorderLayout for the main container

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Name:"));

        JTextField newFolderTextField = new JTextField(10);
        inputPanel.add(newFolderTextField);

        JButton createButton = new JButton("Create Model");
        inputPanel.add(createButton);
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderName = newFolderTextField.getText().trim();
                if (!folderName.isEmpty()) {
                    File newFolder = new File(ConfigLoader.folderSimulationBuilderModels + folderName);
                    if (!newFolder.exists()) {
                        if (newFolder.mkdirs()) {
                            refreshFolderList();
                            SwingUtilities.getWindowAncestor(ModelLoaderComponent.this).pack();  // Nowa linia
                        } else {
                            JOptionPane.showMessageDialog(null, "Couldn't create model.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Model already exists.");
                    }
                }
            }
        });

        add(inputPanel, BorderLayout.NORTH); // Add inputPanel to the top

        // Rest of your folder list panel code remains unchanged
        folderListPanel = new JPanel();
        folderListPanel.setLayout(new BoxLayout(folderListPanel, BoxLayout.Y_AXIS));
        refreshFolderList();

        JScrollPane scrollPane = new JScrollPane(folderListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER); // Add scrollPane to the center
    }

    private void refreshFolderList() {
        folderListPanel.removeAll();

        File folder = new File(ConfigLoader.folderSimulationBuilderModels);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                folderListPanel.add(new JLabel("Nie można utworzyć folderu " + ConfigLoader.folderSimulationBuilderModels));
                return;
            }
        }

        File[] listOfFolders = folder.listFiles(File::isDirectory);

        if (listOfFolders != null && listOfFolders.length > 0) {
            for (File subFolder : listOfFolders) {
                JPanel rowPanel = new JPanel();
                rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS));

                JLabel label = new JLabel("Simulation model: " + subFolder.getName());
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                rowPanel.add(label);

                JButton button = new JButton("Load JSON");
                button.addActionListener(new ModelListener(subFolder));
                button.setAlignmentX(Component.LEFT_ALIGNMENT);
                rowPanel.add(button);

                File[] listOfFiles = subFolder.listFiles();
                JLabel stateMachinesLabel = new JLabel("StateMachines: ");
                Font currentFont = stateMachinesLabel.getFont();
                stateMachinesLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize()));
                rowPanel.add(stateMachinesLabel);
                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        if (!file.isDirectory()) {
                            JLabel fileListLabel = new JLabel(file.getName());
                            rowPanel.add(fileListLabel);
                        }
                    }
                }
                folderListPanel.add(rowPanel);
                JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                separator.setAlignmentX(Component.LEFT_ALIGNMENT);
                folderListPanel.add(separator);
            }
        } else {
            folderListPanel.add(new JLabel("No folders found in " + ConfigLoader.folderSimulationBuilderModels));
        }

        revalidate();
        repaint();
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
                ObjectMapper objectMapper = ObjectMapperHelper.CustomObjectMapperCreator();

                try {
                    var nodeRole = objectMapper.readValue(content, NodeRole.class);

                    try {
                        // Zapisuje obiekt do pliku JSON
                        File jsonFile = new File(ConfigLoader.folderSimulationBuilderModels+ subFolder.getName() + "/"+ nodeRole.getName() + ".json");
                        objectMapper.writeValue(jsonFile, nodeRole);

                    } catch (IOException exx) {
                        exx.printStackTrace();
                    }
                    refreshFolderList();
                    SwingUtilities.getWindowAncestor(ModelLoaderComponent.this).pack();
                    JOptionPane.showMessageDialog(null, "Model has been loaded");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
