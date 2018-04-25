package pl.edu.wat.student.rzepinski.jakub;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;

import static pl.edu.wat.student.rzepinski.jakub.KleinbergGenerator.DEFAULT_CLUSTERING_COEFFICIENT;
import static pl.edu.wat.student.rzepinski.jakub.KleinbergGenerator.DEFAULT_GRID_SIZE;

@ServiceProvider(service = KleinbergGeneratorUI.class)
public class KleinbergGeneratorUI implements GeneratorUI {

    private static final String GRID_SIZE_LABEL = "n - grid size: ";
    private static final String CLUSTERING_COEFFICIENT_LABEL = "q - clustering coefficient: ";
    private static final String TORUS_MODE_LABEL = "torus: ";
    private JSpinner gridSizeSpinner;
    private JSpinner clusteringCoefficientSpinner;
    private JCheckBox torusModeCheckbox;
    private KleinbergGenerator generator;
    private final JPanel panel;

    public KleinbergGeneratorUI() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        int padding = 5;
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        panel.add(new JLabel(GRID_SIZE_LABEL));
        gridSizeSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_GRID_SIZE, 2, null, 1));
        panel.add(gridSizeSpinner);

        panel.add(new JLabel(CLUSTERING_COEFFICIENT_LABEL));
        clusteringCoefficientSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_CLUSTERING_COEFFICIENT, 0, null, 1));
        panel.add(clusteringCoefficientSpinner);

        panel.add(new JLabel(TORUS_MODE_LABEL));
        torusModeCheckbox = new JCheckBox();
        torusModeCheckbox.setSelected(KleinbergGenerator.DEFAULT_TORUS_MODE);
        panel.add(torusModeCheckbox);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setup(Generator generator) {
        if (!(generator instanceof KleinbergGenerator)) {
            throw new IllegalArgumentException("Wrong generator type: " + generator.getClass());
        }
        this.generator = (KleinbergGenerator) generator;

        gridSizeSpinner.setValue(this.generator.getGridSize());
        clusteringCoefficientSpinner.setValue(this.generator.getClusteringCoefficient());
        torusModeCheckbox.setSelected(this.generator.isTorusMode());
    }

    @Override
    public void unsetup() {
        this.generator.setGridSize((Integer) gridSizeSpinner.getValue());
        this.generator.setClusteringCoefficient((Integer) clusteringCoefficientSpinner.getValue());
        this.generator.setTorusMode(torusModeCheckbox.isSelected());
    }
}