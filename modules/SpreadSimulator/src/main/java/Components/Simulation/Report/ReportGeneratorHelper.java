package Components.Simulation.Report;

import java.io.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ReportGeneratorHelper {

    public static void GenerateCSV(List<SimulationStepReport> reports, String filename) {
        // LinkedHashMap to maintain insertion order
        Map<String, String> csvData = new LinkedHashMap<>();

        // Get unique headers
        for (SimulationStepReport report : reports) {
            for (SimulationStepReport.NodeRoleReport roleReport : report.getRoleReports()) {
                for (SimulationStepReport.NodeRoleReport.StateElement stateElement : roleReport.getStatesReport()) {
                    String header = roleReport.getNodeRoleName() + "-" + stateElement.getNodeStateName();
                    if (!csvData.containsKey(header + "_num")) {
                        csvData.put(header + "_num", "");
                        csvData.put(header + "_coverage", "");
                    }
                }
            }
        }

        File directory = new File("reports");
        if (!directory.exists()) {
            directory.mkdir();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("reports/"+filename))) {
            // Write the headers
            writer.write("step," + String.join(",", csvData.keySet()));
            writer.newLine();

            // Write the data rows
            for (SimulationStepReport report : reports) {
                Map<String, String> rowData = new LinkedHashMap<>(csvData); // clone the initial structure

                for (SimulationStepReport.NodeRoleReport roleReport : report.getRoleReports()) {
                    for (SimulationStepReport.NodeRoleReport.StateElement stateElement : roleReport.getStatesReport()) {
                        String headerBase = roleReport.getNodeRoleName() + "-" + stateElement.getNodeStateName();
                        rowData.put(headerBase + "_num", stateElement.getNumberOfNodes().toString());
                        rowData.put(headerBase + "_coverage", stateElement.getCoverage().toString());
                    }
                }

                writer.write(report.getStep() + "," + String.join(",", rowData.values()));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
