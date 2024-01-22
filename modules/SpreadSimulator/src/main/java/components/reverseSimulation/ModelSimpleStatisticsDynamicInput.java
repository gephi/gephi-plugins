package components.reverseSimulation;

import components.simulationLogic.SimulationComponent;
import simulationModel.node.NodeRoleDecorator;
import simulationModel.node.NodeStateDecorator;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ModelSimpleStatisticsDynamicInput {

    public ReverseSimulationComponent component;

    public ModelSimpleStatisticsDynamicInput(ReverseSimulationComponent component){
        this.component = component;
    }

    public JScrollPane generate(List<NodeRoleDecorator> nodeRoles){
        var panel = new JPanel();

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        int row = 0;
        int padding = 4;

        for (NodeRoleDecorator role : nodeRoles) {
            gbc.insets = new Insets(padding, padding, padding, padding);
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel nodeLabel = new JLabel("NodeRole: " + role.getNodeRole().getName());
            Font currentFont = nodeLabel.getFont();
            nodeLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize()+5)); // Wytłuszczenie i zwiększenie rozmiaru o 2 punkty
            panel.add(nodeLabel, gbc);

            gbc.gridy = row++;
            panel.add(new JLabel("Coverage:"), gbc);
            gbc.gridx = 1;
            JTextField roleCoverageField = new JTextField(10);
            Optional<NodeRoleDecorator> optionalRole;
            if(component.getCurrentSimulation() == null) {
                optionalRole = SimulationComponent.getInstance()
                        .getCurrentSimulation()
                        .getNodeRoleDecoratorList()
                        .stream()
                        .filter(nodeRoleDecorator -> nodeRoleDecorator.getNodeRole().getName().equals(role.getNodeRole().getName()))
                        .findFirst();
            } else {
                optionalRole = component.getCurrentSimulation()
                        .getNodeRoleDecoratorList()
                        .stream()
                        .filter(nodeRoleDecorator -> nodeRoleDecorator.getNodeRole().getName().equals(role.getNodeRole().getName()))
                        .findFirst();
            }
            roleCoverageField.setText(String.valueOf(optionalRole.map(NodeRoleDecorator::getCoverage).orElse((double) -1)));
            roleCoverageField.setEditable(false);
            panel.add(roleCoverageField, gbc);

            for (NodeStateDecorator state : role.getNodeStates()) {
                gbc.insets = new Insets(padding, padding, padding, padding);
                gbc.gridy = row++;
                gbc.gridx = 0;

                JLabel stateLabel = new JLabel("NodeState: " + state.getNodeState().getName());
                currentFont = stateLabel.getFont();
                stateLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize()));
                panel.add(stateLabel, gbc);

                gbc.gridx = 0;
                gbc.gridy = row++;
                panel.add(new JLabel("Coverage:"), gbc);
                gbc.gridx = 1;
                JTextField stateCoverageField = new JTextField(10);
                Optional<NodeStateDecorator> optionalNodeState = optionalRole.map(optional -> optional.getNodeStates()
                        .stream()
                        .filter(optionalState -> optionalState.getNodeState().getName().equals(state.getNodeState().getName()))
                        .findFirst().orElse(new NodeStateDecorator()));
                stateCoverageField.setText(String.valueOf(optionalNodeState.map(NodeStateDecorator::getCoverage).orElse((double) -1)));
                stateCoverageField.setEditable(false);
                panel.add(stateCoverageField, gbc);
            }
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(new JSeparator(), gbc);
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }
}
