package com.elwin013.gephi.ergenerator;

import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_NUMBER_OF_EDGES;
import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_NUMBER_OF_NODES;
import static com.elwin013.gephi.ergenerator.Labels.*;

import java.awt.*;

import javax.swing.*;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ErdosRenyiGnmUI.class)
public class ErdosRenyiGnmUI implements GeneratorUI {
    private final JPanel panel;
    private final JSpinner noOfNodesSpinner;
    private final JSpinner noOfEdgesSpinner;
    private ErdosRenyiGnm generator;

    public ErdosRenyiGnmUI() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        int padding = 15;
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        panel.add(new JLabel(LEGEND_N_LABEL, SwingConstants.LEADING));
        panel.add(new JLabel(LEGEND_M_LABEL, SwingConstants.LEADING));
        panel.add(new JLabel(NO_OF_NODES, SwingConstants.LEFT));
        noOfNodesSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_NUMBER_OF_NODES, 2, null, 1));
        panel.add(noOfNodesSpinner);

        panel.add(new JLabel(NO_OF_EDGES, SwingConstants.LEFT));
        noOfEdgesSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_NUMBER_OF_EDGES, 0, Integer.MAX_VALUE, 1));
        panel.add(noOfEdgesSpinner);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setup(Generator generator) {
        if (!(generator instanceof ErdosRenyiGnm)) {
            throw new IllegalArgumentException("Wrong generator's type: " + generator.getClass() + ", only ErdosRenyiGnm accepted");
        }
        this.generator = (ErdosRenyiGnm) generator;

        noOfNodesSpinner.setValue(this.generator.getNoOfNodes());
        noOfEdgesSpinner.setValue(this.generator.getNoOfEdges());
    }

    @Override
    public void unsetup() {
        this.generator.setNoOfNodes((Integer) noOfNodesSpinner.getValue());
        this.generator.setNoOfEdges((Integer) noOfEdgesSpinner.getValue());
    }
}
