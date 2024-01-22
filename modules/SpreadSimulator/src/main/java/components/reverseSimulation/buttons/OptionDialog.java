package components.reverseSimulation.buttons;

import components.reverseSimulation.ReverseSimulationComponent;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class OptionDialog extends JDialog {

    private String examinedStateAndRole;
    private JComboBox<String> rolesChoseFromList;
    private boolean successful = false;

    public OptionDialog(Frame parent, ReverseSimulationComponent reverseSimulationComponent, String name) {
        super(parent, name, true);
        setLayout(new GridLayout(3, 2));

        List<Pair<String, String>> stateAndRoleNames = reverseSimulationComponent
                .getSimulationModel()
                .getNodeRoles()
                .stream()
                .flatMap(e -> e.getNodeStates()
                        .stream()
                        .map(nodeStateDecorator -> Pair.of(e.getNodeRole().getName(), nodeStateDecorator.getNodeState().getName())))
                .collect(Collectors.toList());
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addAll(stateAndRoleNames.stream().map(e -> e.first() + ":" + e.second()).collect(Collectors.toList()));

        rolesChoseFromList = new JComboBox<>(comboBoxModel);
        add(new JLabel("Choose a value:"));
        add(rolesChoseFromList);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> onOk());
        add(okButton);

        pack();
        setLocationRelativeTo(parent);
    }

    private void onOk() {
        try {
            examinedStateAndRole = rolesChoseFromList.getSelectedItem().toString();
            successful = true;
            setVisible(false);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Some error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
