package pl.edu.wat.student.rzepinski.jakub;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;

@ServiceProvider(service = KleinbergGeneratorUI.class)
public class KleinbergGeneratorUI implements GeneratorUI {

    private static final String GRID_SIZE_LABEL = "n - grid size: ";
    private static final String CLUSTERING_COEFFICIENT_LABEL = "q - clustering coefficient: ";
    private static final String TORUS_LABEL = "torus: ";
    private static final int DEFAULT_GRID_SIZE = 10;
    private static final int DEFAULT_CLUSTERING_COEFFICIENT = 2;
    private JSpinner gridSizeSpinner;
    private JSpinner clusteringCoefficientSpinner;
    private JCheckBox torusCheckbox;
    private KleinbergGenerator generator;

    @Override
    public JPanel getPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        int padding = 5;
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        panel.add(new JLabel(GRID_SIZE_LABEL));
        gridSizeSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_GRID_SIZE, 2, null, 1));
        panel.add(gridSizeSpinner);

        panel.add(new JLabel(CLUSTERING_COEFFICIENT_LABEL));
        clusteringCoefficientSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_CLUSTERING_COEFFICIENT, 0, null, 1));
        panel.add(clusteringCoefficientSpinner);

        panel.add(new JLabel(TORUS_LABEL));
        torusCheckbox = new JCheckBox();
        panel.add(torusCheckbox);

        return panel;
    }

    @Override
    public void setup(Generator generator) {
        if (!(generator instanceof KleinbergGenerator)) {
            throw new IllegalArgumentException("Wrong generator type: " + generator.getClass());
        }
        this.generator = (KleinbergGenerator) generator;
        //TODO
    }

    @Override
    public void unsetup() {
        //TODO
    }
}