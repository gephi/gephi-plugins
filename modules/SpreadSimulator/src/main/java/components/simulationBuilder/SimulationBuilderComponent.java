package components.simulationBuilder;

import simulationModel.node.NodeRoleDecorator;
import simulationModel.SimulationModel;
import lombok.Getter;
import lombok.Setter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

@ConvertAsProperties(dtd = "-//Simulation//SimulationBuilder//EN", autostore = false)
@TopComponent.Description(preferredID = "SimulationBuilder",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "SimulationBuilder")
@ActionReference(path = "Menu/Window", position = 1)
@TopComponent.OpenActionRegistration(displayName = "#CTL_SimulationBuilderComponent",
        preferredID = "SimulationBuilder")
public class SimulationBuilderComponent extends TopComponent {

    @Getter
    private SimulationModel simulationModel;
    @Setter
    @Getter
    private List<NodeRoleDecorator> nodeRoles;
    public HashMap<String, List<AdvancedRule>> advancedRules;

    public SimulationBuilderComponent() {
        simulationModel = new SimulationModel();
        advancedRules = new HashMap<>();
        initComponents();
        setName("Simulation Builder");
        setToolTipText("Simulation Builder");
    }

    public void initComponents() {
        this.removeAll();
        setLayout(new FlowLayout());
        JButton createButton = new CreateButton(this);
        JButton loadButton = new LoadButton(this);

        add(createButton);
        add(loadButton);

        if (nodeRoles == null || nodeRoles.isEmpty()) {
            return;
        }

        var modelStatisticInput = new ModelStatisticsDynamicInput(this).generate(nodeRoles);
        add(modelStatisticInput);

        var apply = new ApplyButton(this);
        add(apply);

        var save = new SaveButton(this);
        add(save);

        var paint = new PaintButton(this);
        add(paint);

        var interactionLabel = new JLabel("Select Interaction Strategy:");
        interactionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(interactionLabel);

        var interactionDropdown = new InteractionDropdown().generate(this);
        interactionDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(interactionDropdown);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}
