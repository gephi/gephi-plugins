package Components.Simulation;

import Components.Simulation.Report.ReportGeneratorHelper;
import org.joda.time.DateTime;

import javax.swing.*;
import java.awt.*;

public class GetReportButton extends JButton {
    private final SimulationComponent simulationComponent;
    private final Simulation simulation;
    private String fileName;

    public GetReportButton(Simulation simulation, SimulationComponent simulationComponent) {
        this.setText("Get Report");
        this.simulation = simulation;
        this.simulationComponent = simulationComponent;
        this.addActionListener(e -> GetReport());
    }

    public void GetReport(){
        fileName = "SimulationReport_"+ DateTime.now().toString();
        ReportGeneratorHelper.GenerateCSV(simulation.getReport(), fileName);
    }
}
