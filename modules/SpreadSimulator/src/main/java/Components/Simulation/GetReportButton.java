package Components.Simulation;

import Components.Simulation.Report.ReportGeneratorHelper;

import java.util.UUID;

import Components.Simulation.Simulation.Simulation;
import org.joda.time.DateTime;

import javax.swing.*;

public class GetReportButton extends JButton {
    private final SimulationComponent simulationComponent;
    private String fileName;

    public GetReportButton(SimulationComponent simulationComponent) {
        this.setText("Get Report");
        this.simulationComponent = simulationComponent;
        this.addActionListener(e -> GetReport());
    }

    public void GetReport(){
        UUID uuid = UUID.randomUUID();
        fileName = "SimulationReport_" + DateTime.now().toString("yyyy-MM-dd-HH-mm-ss");
        ReportGeneratorHelper.generateReport(simulationComponent.getCurrentSimulation().getReport(), fileName);
    }
}
