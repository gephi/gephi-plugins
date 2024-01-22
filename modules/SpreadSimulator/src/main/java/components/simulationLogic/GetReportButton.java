package components.simulationLogic;

import components.simulationLogic.report.ReportGeneratorHelper;
import org.joda.time.DateTime;

import javax.swing.*;
import java.util.UUID;

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
