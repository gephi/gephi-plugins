package components.simulationBuilder;

import simulationModel.node.NodeRoleDecorator;
import simulationModel.node.NodeStateDecorator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemoveRuleButton extends JButton {
    private final AdvancedRule rule;
    private SimulationBuilderComponent component;
    private NodeStateDecorator state;
    private NodeRoleDecorator role;

    public RemoveRuleButton(SimulationBuilderComponent component, NodeRoleDecorator role,  NodeStateDecorator state, AdvancedRule rule) {
        this.component = component;
        this.rule = rule;
        this.state = state;
        this.role = role;
        this.setText("Remove");
        this.addActionListener(new Action(component, rule));
    }

    private class Action implements ActionListener{

        SimulationBuilderComponent component;
        AdvancedRule rule;

        public Action(SimulationBuilderComponent component, AdvancedRule rule) {
            this.component = component;
            this.rule = rule;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.advancedRules.get(role.getNodeRole().getName()+ "_" + state.getNodeState().getName()).remove(rule);
            component.initComponents();
            component.repaint();
            component.revalidate();
        }
    }
}
