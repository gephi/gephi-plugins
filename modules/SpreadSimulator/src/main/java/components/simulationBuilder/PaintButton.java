package components.simulationBuilder;

import helper.ApplySimulationHelper;
import simulationModel.node.NodeRoleDecorator;
import simulationModel.SimulationModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PaintButton extends JButton {

    private final SimulationBuilderComponent simulationBuilderComponent;

    public PaintButton(SimulationBuilderComponent simulationBuilderComponent) {
        this.simulationBuilderComponent = simulationBuilderComponent;
        this.setText("Paint");
        this.addActionListener(new PaintButtonActionListener(simulationBuilderComponent.getSimulationModel(), simulationBuilderComponent.getNodeRoles()));
    }

    private class PaintButtonActionListener implements ActionListener {

        private final SimulationModel simulationModel;
        private final List<NodeRoleDecorator> nodeRoles;

        private PaintButtonActionListener(SimulationModel simulationModel, List<NodeRoleDecorator> nodeRoles) {
            this.simulationModel = simulationModel;
            this.nodeRoles = nodeRoles;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                var nodes = List.of(graph.getNodes().toArray());
                ApplySimulationHelper.GenerateColorPaintings(nodeRoles);
                ApplySimulationHelper.PaintGraph(nodes, nodeRoles);
            }
            catch (NullPointerException ex){
                JOptionPane.showMessageDialog(null, "Setup graph model first");
            }
        }
    }

}
