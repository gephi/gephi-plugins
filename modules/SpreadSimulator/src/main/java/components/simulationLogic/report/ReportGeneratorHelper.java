package components.simulationLogic.report;

import configLoader.ConfigLoader;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
        Integer roleCount = reports.get(0).get(0).getRoleReports().size();
        Integer seriesCount = reports.size();

        Map<Integer, List<SimulationStepReport>> sortedStep = reports.stream().flatMap(Collection::stream)
                .collect(Collectors.groupingBy(
                        SimulationStepReport::getStep,
                        Collectors.toList()
                ));

        List<Map<String, Map<String, Double>>> allSteps = sortedStep.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(step -> step.stream()
                        .flatMap(simulationStepReport -> simulationStepReport.getRoleReports().stream()
                                .flatMap(roleReport -> roleReport.getStatesReport().stream()
                                        .map(state -> new AbstractMap.SimpleEntry<>(
                                                new AbstractMap.SimpleEntry<>(roleReport.getNodeRoleName(), state.getNodeStateName()),
                                                state)))
                        )
                        .collect(Collectors.groupingBy(
                                entry -> entry.getKey().getKey(),
                                Collectors.groupingBy(
                                        entry -> entry.getKey().getValue(),
                                        Collectors.summingDouble(entry -> entry.getValue().getNumberOfNodes())
                                )
                        ))
                ).collect(Collectors.toList());

        List<Map<String, Map<String, Double>>> groupedByRoleName = allSteps.stream()
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(
                                        Map.Entry::getValue,
                                        Collectors.toList()
                                )))
                        .values()
                        .stream()
                        .map(list -> list.stream()
                                .map(innerMap -> {
                                    Map<String, Map<String, Double>> tempMap = new HashMap<>();
                                    tempMap.put(map.keySet().iterator().next(), innerMap);
                                    return tempMap;
                                })
                                .collect(Collectors.toList()))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Set<String> uniqueKeys = allSteps.stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());

        List<List<Map<String, Map<String, Double>>>> sortedByRoleName = uniqueKeys.stream()
                .map(key -> groupedByRoleName.stream()
                        .map(map -> map.getOrDefault(key, null))
                        .filter(Objects::nonNull)
                        .map(filteredMap -> Map.of(key, filteredMap))
                        .collect(Collectors.toList())
                )
                .collect(Collectors.toList());

        List<List<Map<String, Map<String, Double>>>> transformed = sortedByRoleName.stream()
                .map(list -> list.stream()
                        .map(map -> map.entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue().entrySet().stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        stateEntry -> stateEntry.getValue() / seriesCount
                                                ))
                                ))
                        )
                        .collect(Collectors.toList())
                )
                .collect(Collectors.toList());

        List<SimulationStepReport> exampleReport = new ArrayList<>(reports.get(0));

        exampleReport.forEach(simulationStepReport -> {
            int step = simulationStepReport.getStep();

            simulationStepReport.getRoleReports().forEach(roleReport -> {
                String nodeRoleName = roleReport.getNodeRoleName();

                roleReport.getStatesReport().forEach(statesReport -> {
                    String nodeStateName = statesReport.getNodeStateName();
                    Double newValue = findValue(transformed, nodeRoleName, nodeStateName, step);

                    if (newValue != null) {
                        statesReport.setNumberOfNodes(newValue);
                    }
                });
            });
        });



// Generowanie raportu
        JFrame graphFrame = new JFrame("Frame");
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.PAGE_AXIS));

        List<JFreeChart> chartList = new ArrayList<>(List.of());
        for (int i = 0; i < roleCount; i++) {
            List<Pair<String, Map<Integer, Double>>> resultForRole = getValuesFromReport(exampleReport, i);

            List<XYSeries> listOfSeries = resultForRole.stream().map(pair -> ChartUtils.createXYSeries(pair.value(), pair.key())).collect(Collectors.toList());

            XYSeriesCollection dataset = new XYSeriesCollection();
            for (XYSeries series : listOfSeries) {
                dataset.addSeries(series);
            }

            JFreeChart chart = ChartFactory.createXYLineChart(
                    exampleReport.get(0).getRoleReports().get(i).getNodeRoleName(),
                    "Time",
                    "Count",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);

            chartList.add(chart);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(1200, 800));
            graphPanel.add(chartPanel);

            //            Charts for each searies
            for (XYSeries series : listOfSeries) {
                XYSeriesCollection datasetForEach = new XYSeriesCollection();
                datasetForEach.addSeries(series);

                JFreeChart chartForSeries = ChartFactory.createXYLineChart(
                        exampleReport.get(0).getRoleReports().get(i).getNodeRoleName(),
                        "Time",
                        "Count",
                        datasetForEach,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false);

                chartList.add(chartForSeries);

                ChartPanel panelForSeries = new ChartPanel(chartForSeries);
                panelForSeries.setPreferredSize(new Dimension(1200, 800));
                graphPanel.add(panelForSeries);
            }
        }



//        Button section
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveAsCSVButton = new JButton("Save as CSV");
        JButton saveAsXLSXButton = new JButton("Save as XLSX");
        JButton saveAsImageButton = new JButton("Save as IMAGE");

        saveAsCSVButton.addActionListener(e -> {
            generateCSV(exampleReport, fileName);
            System.out.println("SAVED AS CSV");
        });

        saveAsXLSXButton.addActionListener(e -> {
            generateExcelJXL(exampleReport, fileName);
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

    public static Double findValue (List<List<Map<String, Map<String, Double>>>> transformed, String nodeRoleName, String roleReportName, int step) {
        return transformed.stream().filter(nodeRole -> nodeRole.get(step - 1).containsKey(nodeRoleName))
                .map(e -> e.get(step - 1))
                .map(e -> e.values().stream().map(reportName -> reportName.get(roleReportName)).collect(Collectors.toList()).get(0))
                .collect(Collectors.toList()).get(0);


    }
    public static void generateReport(List<SimulationStepReport> report, String fileName) {
        JFrame graphFrame = new JFrame("Frame");
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.PAGE_AXIS));

        int roleCount = report.get(0).getRoleReports().size();

        List<JFreeChart> chartList = new ArrayList<>(List.of());
        for (int i = 0; i < roleCount; i++) {
            List<Pair<String, Map<Integer, Double>>> resultForRole = getValuesFromReport(report, i);

            List<XYSeries> listOfSeries = resultForRole.stream().map(pair -> ChartUtils.createXYSeries(pair.value(), pair.key())).collect(Collectors.toList());

//            One Chart for all series
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
                    true,
                    false);

            chartList.add(chart);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(1200, 800));
            graphPanel.add(chartPanel);

            //            Charts for each searies
            for (XYSeries series : listOfSeries) {
                XYSeriesCollection datasetForEach = new XYSeriesCollection();
                datasetForEach.addSeries(series);

                JFreeChart chartForSeries = ChartFactory.createXYLineChart(
                        report.get(0).getRoleReports().get(i).getNodeRoleName(),
                        "Time",
                        "Count",
                        datasetForEach,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false);

                chartList.add(chartForSeries);
                ChartPanel panelForSeries = new ChartPanel(chartForSeries);
                panelForSeries.setPreferredSize(new Dimension(1200, 800));
                graphPanel.add(panelForSeries);
            }
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

    private static List<Pair<String, Map<Integer, Double>>> getValuesFromReport(List<SimulationStepReport> report, int roleNumber) {
        List<SimulationStepReport.NodeRoleReport> correctRoleList = report.stream().map(step -> step.getRoleReports().get(roleNumber)).collect(Collectors.toList());
        int statesNumber = correctRoleList.get(0).getStatesReport().size();

        List<Pair<String, Map<Integer, Double>>> listOfMapValues = new ArrayList<>();
        for (int i = 0; i < statesNumber; i++) {

            Map<Integer, Double> valuesMap = new HashMap<>();
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
                        .orElse(0.0));
            }
            Pair<String, Map<Integer, Double>> mapOfValues = Pair.of(stateName, valuesMap);
            listOfMapValues.add(mapOfValues);
        }

        return listOfMapValues;
    }


}
