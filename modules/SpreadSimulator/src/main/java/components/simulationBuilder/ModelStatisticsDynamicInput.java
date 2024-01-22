package components.simulationBuilder;

import simulationModel.node.NodeRoleDecorator;
import simulationModel.node.NodeStateDecorator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
public class ModelStatisticsDynamicInput {

    public SimulationBuilderComponent component;

    public ModelStatisticsDynamicInput(SimulationBuilderComponent component){
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
            roleCoverageField.setText(role.getCoverage().toString());

            roleCoverageField.getDocument().addDocumentListener(new NodeRoleListener(role, roleCoverageField));
            panel.add(roleCoverageField, gbc);

            for (NodeStateDecorator state : role.getNodeStates()) {
                gbc.insets = new Insets(padding, padding, padding, padding);
                gbc.gridy = row++;
                gbc.gridx = 0;

                JLabel stateLabel = new JLabel("NodeState: " + state.getNodeState().getName());
                currentFont = stateLabel.getFont();
                stateLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize()));
                panel.add(stateLabel, gbc);

                gbc.gridx = 1;
                var addRuleButton = new AdvancedAssigmentButton(role.getNodeRole(), state, this);
                panel.add(addRuleButton, gbc);

                gbc.gridx = 0;
                gbc.gridy = row++;
                panel.add(new JLabel("Coverage:"), gbc);
                gbc.gridx = 1;
                JTextField stateCoverageField = new JTextField(10);
                stateCoverageField.setText(state.getCoverage().toString());
                stateCoverageField.getDocument().addDocumentListener(new NodeStateListener(state, stateCoverageField));
                panel.add(stateCoverageField, gbc);

                gbc.gridy = row++;
                gbc.gridx = 0;
                var advancedStateRules = component.advancedRules.get(role.getNodeRole().getName()+ "_" + state.getNodeState().getName());
                if (advancedStateRules != null && !advancedStateRules.isEmpty()) {
                    for (int i = 0; i < advancedStateRules.size(); i++) {
                        AdvancedRule rule = advancedStateRules.get(i);
                        gbc.gridy = row++;
                        gbc.gridx = 0;
                        panel.add(new JLabel(rule.toString()), gbc);
                        gbc.gridx = 1;
                        panel.add(new RemoveRuleButton(component, role, state, rule), gbc);
                    }
                }

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

    private class NodeRoleListener implements DocumentListener {

        private NodeRoleDecorator nodeRole;
        private JTextField coverage;

        public NodeRoleListener(NodeRoleDecorator nodeRole, JTextField coverage) {
            this.nodeRole = nodeRole;
            this.coverage = coverage;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            try {
                nodeRole.setCoverage(Double.parseDouble(coverage.getText()));
            } catch (NumberFormatException ex) {
            }
        }
    }

    private class NodeStateListener implements DocumentListener{

        private NodeStateDecorator nodeState;
        private JTextField coverage;

        public NodeStateListener(NodeStateDecorator nodeState, JTextField coverage) {
            this.nodeState = nodeState;
            this.coverage = coverage;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            try {
                nodeState.setCoverage(Double.parseDouble(coverage.getText()));
            } catch (NumberFormatException ex) {
            }
        }
    }
}
