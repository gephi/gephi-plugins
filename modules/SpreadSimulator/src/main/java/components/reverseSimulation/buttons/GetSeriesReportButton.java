package components.reverseSimulation.buttons;

import components.reverseSimulation.NodeHelper;
import components.reverseSimulation.ReverseSimulationComponent;
import components.reverseSimulation.model.NodeData;
import configLoader.ConfigLoader;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetSeriesReportButton extends JButton {
    private final ReverseSimulationComponent reverseSimulationComponent;

    public GetSeriesReportButton(ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Get Series Report");
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new GetSeriesReport());
    }

    private class GetSeriesReport implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            OptionDialog optionDialog = new OptionDialog(null, reverseSimulationComponent, "Option Dialog");
            optionDialog.setVisible(true);
            if (optionDialog.isSuccessful()) {
                Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
                var lastStepNodeData = Arrays.stream(graph.getNodes().toArray()).map(NodeData::new).collect(Collectors.toList());
                var fullList = Stream.of(reverseSimulationComponent.getLastStepSimulationList(), List.of(lastStepNodeData)).flatMap(Collection::stream).collect(Collectors.toList());
                showReport(optionDialog.getExaminedStateAndRole(), fullList);
            }
            optionDialog.dispose();
            reverseSimulationComponent.initComponents();
            reverseSimulationComponent.revalidate();
            reverseSimulationComponent.repaint();
        }

        private void showReport(String examinedStateAndRole, List<List<NodeData>> fullList) {
            JFrame frame = new JFrame("Reverse Step Simulation Report");
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 0.0;

            JLabel initialLabel = new JLabel("Initial Nodes");
            Font labelFont = initialLabel.getFont();
            initialLabel.setFont(new Font(labelFont.getName(), Font.BOLD, 16));
            initialLabel.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(initialLabel, gbc);

            gbc.gridy++;
            gbc.weighty = 1.0;
            String[] initialColumnNames = {"Node Number", "Node Degree", "Node Betweenees", "Node Closeness"};
            Object[][] initialData = generateInitialData(examinedStateAndRole.split(":")[1]);
            JTable initialTable = new JTable(new DefaultTableModel(initialData, initialColumnNames));
            JScrollPane initialScrollPane = new JScrollPane(initialTable);
            setPreferredTableSize(initialScrollPane, initialTable);
            panel.add(initialScrollPane, gbc);

            gbc.gridy++;
            gbc.weighty = 0;
            JLabel predicLabel = new JLabel("Predict Nodes");
            Font predictFont = predicLabel.getFont();
            predicLabel.setFont(new Font(predictFont.getName(), Font.BOLD, 16));
            predicLabel.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(predicLabel, gbc);

            gbc.gridy++;
            gbc.weighty = 1;

            String[] predictColumnNames = {"Node Number", "Distance To Closest Initial", "Distance To Farthest Initial", "Avg Distance To All Initial"};
            for (int i = 0; i < fullList.size(); i++) {
                JLabel seriesLabel = new JLabel("Series: " + (i + 1));
                Font seriesFont = seriesLabel.getFont();
                seriesLabel.setFont(new Font(seriesFont.getName(), Font.PLAIN, 12));
                panel.add(seriesLabel, gbc);
                gbc.gridy++;

                Object[][] predictData = generatePredictData(examinedStateAndRole.split(":")[1], fullList.get(i));
                JTable predictTable = new JTable(new DefaultTableModel(predictData, predictColumnNames));
                JScrollPane predictScrollPane = new JScrollPane(predictTable);
                setPreferredTableSize(predictScrollPane, predictTable);
                panel.add(predictScrollPane, gbc);
                gbc.gridy++;
            }

            JScrollPane mainScrollPane = new JScrollPane(panel);
            frame.setSize(1200, 800);
            frame.add(mainScrollPane);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        private void setPreferredTableSize(JScrollPane scrollPane, JTable table) {
            JTableHeader header = table.getTableHeader();
            int columnCount = header.getColumnModel().getColumnCount();
            int headerWidth = 0;

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                TableColumn column = header.getColumnModel().getColumn(columnIndex);
                TableCellRenderer headerRenderer = column.getHeaderRenderer();
                if (headerRenderer == null) {
                    headerRenderer = header.getDefaultRenderer();
                }
                Component headerComponent = headerRenderer.getTableCellRendererComponent(
                        table, column.getHeaderValue(), false, false, 0, columnIndex);
                headerWidth += headerComponent.getPreferredSize().width;
            }

            Dimension tableSize = table.getPreferredSize();
            Dimension headerSize = new Dimension(600, tableSize.height);
            scrollPane.getViewport().setPreferredSize(headerSize);
        }

        private Object[][] generatePredictData(String stateName, List<NodeData> nodesData) {
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();

            nodesData.forEach(nodeData -> {
                List.of(graph.getNodes().toArray())
                        .get(nodeData.getNodeStroeId())
                        .setAttribute(ConfigLoader.colNameNodeState, nodeData.getNodeCurrnetState());
            });

            var predictNodes = Arrays.stream(graph.getNodes().toArray())
                    .filter(node -> node.getAttribute(ConfigLoader.colNameNodeState).equals(stateName))
                    .collect(Collectors.toList());
            var initialNodes = Arrays.stream(graph.getNodes().toArray())
                    .filter(node -> node.getAttribute(ConfigLoader.colNameRootState).equals(stateName))
                    .collect(Collectors.toList());

            String[][] result = new String[predictNodes.size()][4];

            for (int i = 0; i < predictNodes.size(); i++) {
                Node currentNode = predictNodes.get(i);
//                Node Number
                result[i][0] = String.valueOf(i + 1);
//                Distance To Closest Initial
                result[i][1] = String.valueOf(NodeHelper.getDistanceToClosestNode(currentNode, initialNodes, graph));
//                Distance To Farthest Initial
                result[i][2] = String.valueOf(NodeHelper.getDistanceToFarthestNode(currentNode, initialNodes, graph));
//                Avg Distance To All Initials
                result[i][3] = String.valueOf(NodeHelper.getAvgDistanceToNodes(currentNode, initialNodes, graph));
            }

            return result;
        }

        private Object[][] generateInitialData(String stateName) {
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            var nodes = Arrays.stream(graph.getNodes().toArray())
                    .filter(node -> node.getAttribute(ConfigLoader.colNameRootState).equals(stateName))
                    .collect(Collectors.toList());
            var betweeneesColumn = NodeHelper.getBetweeneesColumn(graph);

            String[][] result = new String[nodes.size()][4];

            for (int i = 0; i < nodes.size(); i++) {
                Node currentNode = nodes.get(i);
                result[i][0] = String.valueOf(i + 1);
                result[i][1] = String.valueOf(NodeHelper.getNodeDegree(currentNode, graph));
                result[i][2] = String.valueOf(currentNode.getAttribute(betweeneesColumn));
                result[i][3] = String.valueOf(NodeHelper.getNodeCloseness(currentNode, graph));
            }

            return result;
        }

    }


}
