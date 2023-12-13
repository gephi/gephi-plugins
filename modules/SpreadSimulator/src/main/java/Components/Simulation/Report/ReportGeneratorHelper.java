package Components.Simulation.Report;

import ConfigLoader.ConfigLoader;
import it.unimi.dsi.fastutil.Pair;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.gephi.statistics.plugin.ChartUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportGeneratorHelper {

    public static void generateCSV(List<SimulationStepReport> reports, String filename) {
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

        File directory = new File(ConfigLoader.folderReports + filename);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ConfigLoader.folderReports + filename + "/" + filename + ".csv"))) {
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

    public static void generateExcelJXL(List<SimulationStepReport> reports, String filename) {
        try {
            File directory = new File(ConfigLoader.folderReports + filename);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            WritableWorkbook workbook = Workbook.createWorkbook(new File(ConfigLoader.folderReports + filename + "/" + filename + ".xls"));
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

    public static void generateSeriesReport(List<List<SimulationStepReport>> reports, String fileName) {
        ArrayList<ArrayList<List<Pair<String, Map<Integer, Integer>>>>> allSeries = new ArrayList<>();
        ArrayList<List<Pair<String, Map<Integer, Integer>>>> oneSeries = new ArrayList<>();

        for (int l = 0; l < reports.size(); l++) {
            int roleCount = reports.get(l).get(0).getRoleReports().size();
            for (int i = 0; i < roleCount; i++) {
                List<Pair<String, Map<Integer, Integer>>> resultForRole = getValuesFromReport(reports.get(l), i);
                oneSeries.add(resultForRole);
            }
            allSeries.add(oneSeries);
            oneSeries = new ArrayList<>();
        }

        List<List<Pair<String, Map<Integer, Integer>>>> roles = new ArrayList<>();
        for (int i = 0; i < allSeries.get(0).size(); i++) {
            int finalI = i;
            roles.add(allSeries.stream()
                    .map(series -> series.get(finalI))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }

        List<List<Pair<String, Map<Integer, Double>>>> allAvgValues = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++) {
            List<Pair<String, Map<Integer, Double>>> valuesForRole = new ArrayList<>();
            List<String> nodesNames = roles.get(i)
                    .stream()
                    .map(Pair::first)
                    .distinct()
                    .collect(Collectors.toList());
            for (int l = 0; l < nodesNames.size(); l++) {
                int finalL = l;
                Map<Integer, Double> values;
                values = roles.get(i)
                        .stream()
                        .filter(step -> Objects.equals(step.first(), nodesNames.get(finalL)))
                        .flatMap(step -> step.second().entrySet().stream())
                        .collect(Collectors.groupingBy(
                                entry -> entry.getKey(),
                                Collectors.averagingInt(entry -> entry.getValue())
                        ));
                Pair<String, Map<Integer, Double>> valuesWithNodeName = Pair.of(nodesNames.get(finalL), values);
                valuesForRole.add(valuesWithNodeName);
            }
            allAvgValues.add(valuesForRole);
        }
        allAvgValues.size();

        JFrame graphFrame = new JFrame("Frame");
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.PAGE_AXIS));

        List<JFreeChart> chartList = new ArrayList<>(List.of());

        for(int i=0; i<allAvgValues.size(); i++) {
            List<XYSeries> listOfSeries = allAvgValues.get(i).stream().map(pair -> ChartUtils.createXYSeries(pair.value(), pair.key())).collect(Collectors.toList());

            XYSeriesCollection dataset = new XYSeriesCollection();
            for (XYSeries series : listOfSeries) {
                dataset.addSeries(series);
            }

            JFreeChart chart = ChartFactory.createXYLineChart(
                    reports.get(i).get(0).getRoleReports().get(i).getNodeRoleName(),
                    "Time",
                    "Count",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);

            chartList.add(chart);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(1200, 800));
            graphPanel.add(chartPanel);
        }

//        Button section
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveAsCSVButton = new JButton("Save as CSV");
        JButton saveAsXLSXButton = new JButton("Save as XLSX");
        JButton saveAsImageButton = new JButton("Save as IMAGE");

        saveAsCSVButton.addActionListener(e -> {
            generateCSV(reports, fileName);
            System.out.println("SAVED AS CSV");
        });

        saveAsXLSXButton.addActionListener(e -> {
            generateExcelJXL(report, fileName);
            System.out.println("SAVED AS XLSX");
        });

        saveAsImageButton.addActionListener(e -> {
            for (JFreeChart chart : chartList) {
                try {
                    File directory = new File(ConfigLoader.folderReports + fileName);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    ChartUtilities.saveChartAsPNG(new File(ConfigLoader.folderReports + fileName + "/" + "chart_" + (chartList.indexOf(chart) + 1) + ".png"), chart, 1200, 800);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            System.out.println("SAVED AS IMAGE");
        });

        buttonPanel.add(saveAsCSVButton);
        buttonPanel.add(saveAsXLSXButton);
        buttonPanel.add(saveAsImageButton);

        // add the panel to this frame
        graphFrame.setSize(400, 400);
        graphFrame.add(graphPanel);
        graphPanel.add(buttonPanel, BorderLayout.SOUTH);
        graphFrame.pack();
        graphFrame.setLocationRelativeTo(null);
        graphFrame.setVisible(true);

    }

    public static void generateReport(List<SimulationStepReport> report, String fileName) {
        JFrame graphFrame = new JFrame("Frame");
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.PAGE_AXIS));

        int roleCount = report.get(0).getRoleReports().size();

        List<JFreeChart> chartList = new ArrayList<>(List.of());
        for (int i = 0; i < roleCount; i++) {
            List<Pair<String, Map<Integer, Integer>>> resultForRole = getValuesFromReport(report, i);

            List<XYSeries> listOfSeries = resultForRole.stream().map(pair -> ChartUtils.createXYSeries(pair.value(), pair.key())).collect(Collectors.toList());

            XYSeriesCollection dataset = new XYSeriesCollection();
            for (XYSeries series : listOfSeries) {
                dataset.addSeries(series);
            }

            JFreeChart chart = ChartFactory.createXYLineChart(
                    report.get(0).getRoleReports().get(i).getNodeRoleName(),
                    "Time",
                    "Count",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);

            chartList.add(chart);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(1200, 800));
            graphPanel.add(chartPanel);
        }

//      Button section
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveAsCSVButton = new JButton("Save as CSV");
        JButton saveAsXLSXButton = new JButton("Save as XLSX");
        JButton saveAsImageButton = new JButton("Save as IMAGE");

        saveAsCSVButton.addActionListener(e -> {
            generateCSV(report, fileName);
            System.out.println("SAVED AS CSV");
        });

        saveAsXLSXButton.addActionListener(e -> {
            generateExcelJXL(report, fileName);
            System.out.println("SAVED AS XLSX");
        });

        saveAsImageButton.addActionListener(e -> {
            for (JFreeChart chart : chartList) {
                try {
                    File directory = new File(ConfigLoader.folderReports + fileName);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    ChartUtilities.saveChartAsPNG(new File(ConfigLoader.folderReports + fileName + "/" + "chart_" + (chartList.indexOf(chart) + 1) + ".png"), chart, 1200, 800);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            System.out.println("SAVED AS IMAGE");
        });

        buttonPanel.add(saveAsCSVButton);
        buttonPanel.add(saveAsXLSXButton);
        buttonPanel.add(saveAsImageButton);

        // add the panel to this frame
        graphFrame.setSize(400, 400);
        graphFrame.add(graphPanel);
        graphPanel.add(buttonPanel, BorderLayout.SOUTH);
        graphFrame.pack();
        graphFrame.setLocationRelativeTo(null);
        graphFrame.setVisible(true);
    }

    private static List<Pair<String, Map<Integer, Integer>>> getValuesFromReport(List<SimulationStepReport> report, int roleNumber) {
        List<SimulationStepReport.NodeRoleReport> correctRoleList = report.stream().map(step -> step.getRoleReports().get(roleNumber)).collect(Collectors.toList());
        int statesNumber = correctRoleList.get(0).getStatesReport().size();

        List<Pair<String, Map<Integer, Integer>>> listOfMapValues = new ArrayList<>();
        for (int i = 0; i < statesNumber; i++) {

            Map<Integer, Integer> valuesMap = new HashMap<>();
            int finalI = i;
            String stateName = correctRoleList.get(roleNumber).getStatesReport().get(finalI).getNodeStateName();

            for (int j = 0; j < correctRoleList.size(); j++) {
                valuesMap.put(j + 1, correctRoleList
                        .get(j)
                        .getStatesReport()
                        .stream()
                        .filter(stateElement -> Objects.equals(stateElement.getNodeStateName(), stateName))
                        .findFirst()
                        .map(SimulationStepReport.NodeRoleReport.StateElement::getNumberOfNodes)
                        .orElse(0));
            }
            Pair<String, Map<Integer, Integer>> mapOfValues = Pair.of(stateName, valuesMap);
            listOfMapValues.add(mapOfValues);
        }

        return listOfMapValues;
    }
}
