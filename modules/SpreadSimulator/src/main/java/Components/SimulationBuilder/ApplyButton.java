package Components.SimulationBuilder;

import Helper.ApplySimulationHelper;
import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.SimulationModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ApplyButton extends JButton {

    private final SimulationBuilderComponent simulationBuilderComponent;

    public ApplyButton(SimulationBuilderComponent simulationBuilderComponent) {
        this.simulationBuilderComponent = simulationBuilderComponent;
        this.setText("Apply");
        this.addActionListener(new ApplyButtonActionListener(simulationBuilderComponent.getSimulationModel(), simulationBuilderComponent.getNodeRoles()));
    }

    private class ApplyButtonActionListener implements ActionListener {

        private final SimulationModel simulationModel;
        private final List<NodeRoleDecorator> nodeRoles;

        private ApplyButtonActionListener(SimulationModel simulationModel, List<NodeRoleDecorator> nodeRoles) {
            this.simulationModel = simulationModel;
            this.nodeRoles = nodeRoles;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            simulationModel.setNodeRoles(nodeRoles);
            simulationModel.setName(ApplySimulationHelper.GenerateName(nodeRoles));
            try {
                Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                ApplySimulationHelper.ClearModel(graph);
                ApplySimulationHelper.CrateModelColumns(graph);
                ApplySimulationHelper.Apply(graph, simulationModel);
            }
            catch (NullPointerException ex){
                JOptionPane.showMessageDialog(null, "Setup graph model first");
            }

        }

    }
}
