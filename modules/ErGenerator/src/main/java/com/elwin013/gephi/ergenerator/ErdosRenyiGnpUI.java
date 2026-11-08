package com.elwin013.gephi.ergenerator;

import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_EDGE_CREATE_PROBABILITY;
import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_NUMBER_OF_NODES;
import static com.elwin013.gephi.ergenerator.Labels.*;

import java.awt.*;

import javax.swing.*;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ErdosRenyiGnpUI.class)
public class ErdosRenyiGnpUI implements GeneratorUI {
    private final JPanel panel;
    private final JSpinner noOfNodesSpinner;
    private final JSpinner edgeCreateProbabilitySpinner;
    private ErdosRenyiGnp generator;

    public ErdosRenyiGnpUI() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        int padding = 15;
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        panel.add(new JLabel(LEGEND_N_LABEL, SwingConstants.LEADING));
        panel.add(new JLabel(LEGEND_P_LABEL, SwingConstants.LEADING));
        panel.add(new JLabel(NO_OF_NODES, SwingConstants.LEFT));
        noOfNodesSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_NUMBER_OF_NODES, 2, null, 1));
        panel.add(noOfNodesSpinner);

        panel.add(new JLabel(EDGE_CREATE_PROBABILITY, SwingConstants.LEFT));
        edgeCreateProbabilitySpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_EDGE_CREATE_PROBABILITY, 0, 1, 0.05));
        edgeCreateProbabilitySpinner.setEditor(new JSpinner.NumberEditor(edgeCreateProbabilitySpinner, "0.#####"));
        panel.add(edgeCreateProbabilitySpinner);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setup(Generator generator) {
        if (!(generator instanceof ErdosRenyiGnp)) {
            throw new IllegalArgumentException("Wrong generator's type: " + generator.getClass() + ", only ErdosRenyiGnp accepted");
        }
        this.generator = (ErdosRenyiGnp) generator;

        noOfNodesSpinner.setValue(this.generator.getNoOfNodes());
        edgeCreateProbabilitySpinner.setValue(this.generator.getEdgeCreateProbability());
    }

    @Override
    public void unsetup() {
        this.generator.setNoOfNodes((Integer) noOfNodesSpinner.getValue());
        this.generator.setEdgeCreateProbability((Double) edgeCreateProbabilitySpinner.getValue());
    }
}
