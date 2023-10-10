package Components.Simulation.Report;

import ConfigLoader.ConfigLoader;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.Label;

import java.io.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

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

        String path = ConfigLoader.getProperty("reports.path");

        File directory = new File(path + filename);
        if (!directory.exists()) {
            directory.mkdir();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + filename + "/" + filename + ".csv"))) {
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

    public static void GenerateExcelJXL(List<SimulationStepReport> reports, String filename) {
        try {
            File directory = new File("reports/"+filename);
            if (!directory.exists()) {
                directory.mkdir();
            }

            WritableWorkbook workbook = Workbook.createWorkbook(new File("reports/"+filename+"/" + filename + ".xls"));
            WritableSheet sheet = workbook.createSheet("Simulation Report", 0);

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

            // Write headers to Excel
            Label label = new Label(0, 0, "step");
            sheet.addCell(label);

            int columnIndex = 1;
            for (String header : csvData.keySet()) {
                label = new Label(columnIndex, 0, header);
                sheet.addCell(label);
                columnIndex++;
            }

            // Write data rows to Excel
            int rowIndex = 1;
            for (SimulationStepReport report : reports) {
                label = new Label(0, rowIndex, report.getStep().toString());
                sheet.addCell(label);

                Map<String, String> rowData = new LinkedHashMap<>(csvData); // clone the initial structure
                for (SimulationStepReport.NodeRoleReport roleReport : report.getRoleReports()) {
                    for (SimulationStepReport.NodeRoleReport.StateElement stateElement : roleReport.getStatesReport()) {
                        String headerBase = roleReport.getNodeRoleName() + "-" + stateElement.getNodeStateName();
                        rowData.put(headerBase + "_num", stateElement.getNumberOfNodes().toString());
                        rowData.put(headerBase + "_coverage", stateElement.getCoverage().toString());
                    }
                }

                columnIndex = 1;
                for (String value : rowData.values()) {
                    label = new Label(columnIndex, rowIndex, value);
                    sheet.addCell(label);
                    columnIndex++;
                }
                rowIndex++;
            }

            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
