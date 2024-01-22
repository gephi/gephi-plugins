package simulationModelBuilder;

import helper.ObjectMapperHelper;
import simulationModel.SimulationModel;
import org.codehaus.jackson.map.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SimulationModelBuilderPanel extends JPanel {
    private JCheckBox clearModelCheckbox;
    private JButton loadJsonButton;
    private JButton submitButton;
    private String content;
    private SimulationModelBuilder simulationModelBuilder;

    public SimulationModelBuilderPanel(SimulationModelBuilder simulationModel) {
        this.simulationModelBuilder = simulationModel;
        setLayout(new FlowLayout());

        clearModelCheckbox = new JCheckBox("Clear Model");
        loadJsonButton = new JButton("Load JSON");
        submitButton = new JButton("Submit");

        add(clearModelCheckbox);
        add(loadJsonButton);
        add(submitButton);

        clearModelCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clearModelCheckbox.isSelected()) {
                    loadJsonButton.setEnabled(false);
                    // Add code here to clear your model if applicable
                } else {
                    loadJsonButton.setEnabled(true);
                }
            }
        });

        loadJsonButton.addActionListener(new JsonLoaderListener());

        submitButton.addActionListener(new SubmitListener());
    }

    private class SubmitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (clearModelCheckbox.isSelected()) {
                JOptionPane.showMessageDialog(null, "Model cleared, action submitted.");
            } else if (content != null) {

                ObjectMapper objectMapper = ObjectMapperHelper.CustomObjectMapperCreator();

                try {
                    var simulator = objectMapper.readValue(content, SimulationModel.class);
                    simulationModelBuilder.setSimulationModel(simulator);
                    JOptionPane.showMessageDialog(null, "Model has been loaded");

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Model occurred mapping errors");
            }
        }
    }

    private class JsonLoaderListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
                } catch (IOException ex) {
                    // JOptionPane.showMessageDialog(null, "An error occurred while reading the file or parsing the JSON.");
                }

            }
        }
    }
}
