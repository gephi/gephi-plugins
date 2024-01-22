package components.reverseSimulation.buttons;

import components.reverseSimulation.NodeHelper;
import components.reverseSimulation.ReverseSimulationComponent;
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
import java.util.Arrays;
import java.util.stream.Collectors;

public class GetReportButton extends JButton {
    private final ReverseSimulationComponent reverseSimulationComponent;

    public GetReportButton(ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Get Report");
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new GetReport());
    }

    private class GetReport implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            OptionDialog optionDialog = new OptionDialog(null, reverseSimulationComponent, "Option Dialog");
            optionDialog.setVisible(true);
            optionDialog.dispose();
            if (optionDialog.isSuccessful()) {
                showReport(optionDialog.getExaminedStateAndRole());
            }
            reverseSimulationComponent.initComponents();
            reverseSimulationComponent.revalidate();
            reverseSimulationComponent.repaint();
        }

        private void showReport(String examinedStateAndRole) {
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
            Object[][] predictData = generatePredictData(examinedStateAndRole.split(":")[1]);
            JTable predictTable = new JTable(new DefaultTableModel(predictData, predictColumnNames));
            JScrollPane predictScrollPane = new JScrollPane(predictTable);
            setPreferredTableSize(predictScrollPane, predictTable);
            panel.add(predictScrollPane, gbc);

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

        private Object[][] generatePredictData(String stateName) {
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
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
