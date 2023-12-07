package Components.Simulation;

import Components.Simulation.Report.ReportGeneratorHelper;
import org.joda.time.DateTime;

import javax.swing.*;
import java.util.UUID;

public class GetSeriesReportButton extends JButton {
    private final SimulationComponent simulationComponent;
    private String fileName;

    public GetSeriesReportButton(SimulationComponent simulationComponent) {
        this.setText("Get Series Report");
        this.simulationComponent = simulationComponent;
        this.addActionListener(e -> GetReport());
    }

    public void GetReport(){
        UUID uuid = UUID.randomUUID();
        fileName = "SimulationReport_" + DateTime.now().toString("yyyy-MM-dd-HH-mm-ss");
        ReportGeneratorHelper.generateReport(simulationComponent.getCurrentSimulation().getReport(), fileName);
    }
}
