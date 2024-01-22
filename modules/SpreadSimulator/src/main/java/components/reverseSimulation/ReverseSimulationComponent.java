package components.reverseSimulation;

import components.reverseSimulation.buttons.ChangeModelButton;
import components.reverseSimulation.buttons.GetReportButton;
import components.reverseSimulation.buttons.ShowResultButton;
import components.reverseSimulation.buttons.StartSimulationButton;
import components.reverseSimulation.buttons.StepButton;
import components.reverseSimulation.buttons.UsePredictSimulationButton;
import components.reverseSimulation.buttons.UseReverseSeriesSimulationButton;
import components.simulation.Simulation;
import components.simulation.SimulationAll;
import components.simulation.SimulationRelativeEdges;
import components.simulation.SimulationRelativeFreeEdges;
import components.simulation.SimulationRelativeFreeNodes;
import components.simulation.SimulationRelativeNodes;
import components.simulationLogic.SimulationComponent;
import simulationModel.node.NodeRoleDecorator;
import simulationModel.SimulationModel;
import lombok.Getter;
import lombok.Setter;
import org.gephi.graph.api.Graph;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

@ConvertAsProperties(dtd = "-//Simulation//ReverseSimulation//EN", autostore = false)
@TopComponent.Description(preferredID = "ReverseSimulation",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "ReverseSimulation")
@ActionReference(path = "Menu/Window", position = 0)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ReverseSimulation",
        preferredID = "Simulation")
@Getter
@Setter
public class ReverseSimulationComponent extends TopComponent {

    private String modelName;
    private Graph graph;
    private Simulation currentSimulation;
    private Simulation lastStepSimulation;
    private List<Simulation> simulationList;
    private SimulationModel simulationModel;
    private int reverseSimulationState = 0;
    private List<NodeRoleDecorator> nodeRoles;

    public ReverseSimulationComponent() {
        initComponents();
        setName("Reverse Simulation");
        setToolTipText("Revers Simulation");
    }

    public void initComponents() {
        this.removeAll();
        setLayout(new FlowLayout());
        if (reverseSimulationState == 0) {
            JButton initButton = new JButton("Init");
            initButton.addActionListener(this::initButtonActionPerformed);
            add(initButton);
        }

        if (reverseSimulationState == 0) {
            return;
        }

        Label simulationName = new Label();
        switch (reverseSimulationState) {
            case 1:
                simulationName.setText("Reverse Series Simulation");
                break;
            case 2:
                simulationName.setText("Predict Simulation");
                break;
            default:
                break;
        }
        simulationName.setBounds(5,5, 400,75);

        add(simulationName);
        add(new UsePredictSimulationButton(this));
        add(new UseReverseSeriesSimulationButton(this));


        if (reverseSimulationState == 1) {
            add(new ChangeModelButton(this));

            if (nodeRoles == null || nodeRoles.isEmpty()) {
                return;
            }
            var modelName = new JLabel("Current model: " + getModelName());
            add(modelName);

            var modelStatisticInput = new ModelSimpleStatisticsDynamicInput(this).generate(nodeRoles);
            add(modelStatisticInput);

            add(new StepButton(currentSimulation, this));
            add(new StartSimulationButton(currentSimulation, this));
            add(new ShowResultButton(this));
            add(new GetReportButton(this));

            var stepLabel = new JLabel("Step: " + currentSimulation.getStep().toString());
            add(stepLabel);
        } else if (reverseSimulationState == 2) {
            JButton placeholderButton = new JButton("Placeholder");
            add(placeholderButton);
        }
    }



    private boolean wasRanSimulation() {
        Simulation simulation = SimulationComponent.getInstance().getCurrentSimulation();
        if (simulation != null) {
            reverseSimulationState = simulation.getStep() > 0? 1 : 0;
        }
        return false;
    }

    private void initButtonActionPerformed(ActionEvent e) {
        this.setSimulationModel(SimulationComponent.getInstance().getSimulationModel());
        this.setGraph(SimulationComponent.getInstance().getGraph());
        switch (simulationModel.getInteraction().getInteractionType()){
            case All:
                this.lastStepSimulation = currentSimulation;
                this.currentSimulation = new SimulationAll(graph, simulationModel);
                break;
            case RelativeEdges:
                this.lastStepSimulation = currentSimulation;
                this.currentSimulation = new SimulationRelativeEdges(graph, simulationModel);
                break;
            case RelativeFreeEdges:
                this.lastStepSimulation = currentSimulation;
                this.currentSimulation = new SimulationRelativeFreeEdges(graph, simulationModel);
                break;
            case RelativeNodes:
                this.lastStepSimulation = currentSimulation;
                this.currentSimulation = new SimulationRelativeNodes(graph, simulationModel);
                break;
            case RelativeFreeNodes:
                this.lastStepSimulation = currentSimulation;
                this.currentSimulation = new SimulationRelativeFreeNodes(graph, simulationModel);
                break;
            default:
                break;
        }
        wasRanSimulation();
        initComponents();
        revalidate();
        repaint();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
