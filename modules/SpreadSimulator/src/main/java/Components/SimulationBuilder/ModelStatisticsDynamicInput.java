package Components.SimulationBuilder;

import SimulationModel.Node.NodeRoleDecorator;
import SimulationModel.Node.NodeStateDecorator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
public class ModelStatisticsDynamicInput {

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
            JTextField roleCoverageField = new JTextField(10); // 10 columns wide
            roleCoverageField.setText(role.getCoverage().toString());

            panel.add(roleCoverageField, gbc);

            gbc.gridy = row++;
            gbc.gridx = 0;
            panel.add(new JLabel("MinCoverage:"), gbc);
            gbc.gridx = 1;
            JTextField roleMinCoverageField = new JTextField(10); // 10 columns wide
            roleMinCoverageField.setText(role.getMinCoverage().toString());

            roleCoverageField.getDocument().addDocumentListener(new NodeRoleListener(role, roleCoverageField, roleMinCoverageField));
            roleMinCoverageField.getDocument().addDocumentListener(new NodeRoleListener(role, roleCoverageField, roleMinCoverageField));
            panel.add(roleMinCoverageField, gbc);

            for (NodeStateDecorator state : role.getNodeStates()) {
                gbc.insets = new Insets(padding, padding, padding, padding);
                gbc.gridy = row++;
                gbc.gridx = 0;

                JLabel stateLabel = new JLabel("NodeState: " + state.getNodeState().getName());
                currentFont = stateLabel.getFont();
                stateLabel.setFont(currentFont.deriveFont(currentFont.getStyle() | Font.BOLD, currentFont.getSize())); // Wytłuszczenie i zwiększenie rozmiaru o 2 punkty
                panel.add(stateLabel, gbc);

                gbc.gridx = 1;
                var advancedButton = new AdvancedAssigmentButton(role.getNodeRole(), state);
                panel.add(advancedButton, gbc);

                gbc.gridx = 0;
                gbc.gridy = row++;
                panel.add(new JLabel("Coverage:"), gbc);
                gbc.gridx = 1;
                JTextField stateCoverageField = new JTextField(10); // 10 columns wide
                stateCoverageField.setText(state.getCoverage().toString());
                panel.add(stateCoverageField, gbc);

                gbc.gridy = row++;
                gbc.gridx = 0;
                panel.add(new JLabel("MinCoverage:"), gbc);
                gbc.gridx = 1;
                JTextField stateMinCoverageField = new JTextField(10);
                stateMinCoverageField.setText(state.getMinCoverage().toString());

                stateCoverageField.getDocument().addDocumentListener(new NodeStateListener(state, stateCoverageField, stateMinCoverageField));
                stateMinCoverageField.getDocument().addDocumentListener(new NodeStateListener(state, stateCoverageField, stateMinCoverageField));

                panel.add(stateMinCoverageField, gbc);
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
        private JTextField minCoverage;

        public NodeRoleListener(NodeRoleDecorator nodeRole, JTextField coverage, JTextField minCoverage) {
            this.nodeRole = nodeRole;
            this.coverage = coverage;
            this.minCoverage = minCoverage;
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
                nodeRole.setMinCoverage(Integer.parseInt(minCoverage.getText()));
            } catch (NumberFormatException ex) {
            }
        }
    }

    private class NodeStateListener implements DocumentListener{

        private NodeStateDecorator nodeState;
        private JTextField coverage;
        private JTextField minCoverage;

        public NodeStateListener(NodeStateDecorator nodeState, JTextField coverage, JTextField minCoverage) {
            this.nodeState = nodeState;
            this.coverage = coverage;
            this.minCoverage = minCoverage;
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
                nodeState.setMinCoverage(Integer.parseInt(minCoverage.getText()));
            } catch (NumberFormatException ex) {
            }
        }
    }


}
