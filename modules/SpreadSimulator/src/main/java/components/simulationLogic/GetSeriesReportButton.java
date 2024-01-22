package components.simulationLogic;

import components.simulation.Simulation;
import components.simulationLogic.report.ReportGeneratorHelper;
import components.simulationLogic.report.SimulationStepReport;
import org.joda.time.DateTime;

import javax.swing.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetSeriesReportButton extends JButton {
    private final SimulationComponent simulationComponent;
    private List<Simulation> simulationList;
    private String fileName;

    public GetSeriesReportButton(SimulationComponent simulationComponent, List<Simulation> simulationList) {
        this.setText("Get Series Report");
        this.simulationList = simulationList;
        this.simulationComponent = simulationComponent;
        this.addActionListener(e -> GetReport());
    }

    public void GetReport(){
        UUID uuid = UUID.randomUUID();
        fileName = "SimulationReport_" + DateTime.now().toString("yyyy-MM-dd-HH-mm-ss");
        List<List<SimulationStepReport>> listOfAllSimulations = simulationList.stream().map(Simulation::getReport).collect(Collectors.toList());
        listOfAllSimulations.add(simulationComponent.getCurrentSimulation().getReport());
        ReportGeneratorHelper.generateSeriesReport(listOfAllSimulations, fileName);
    }
}
