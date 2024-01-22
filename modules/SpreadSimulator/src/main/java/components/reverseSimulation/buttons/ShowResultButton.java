package components.reverseSimulation.buttons;

import components.reverseSimulation.ReverseSimulationComponent;
import configLoader.ConfigLoader;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

public class ShowResultButton extends JButton {
    private final ReverseSimulationComponent reverseSimulationComponent;

    public ShowResultButton(ReverseSimulationComponent reverseSimulationComponent) {
        this.setText("Show Results");
        this.reverseSimulationComponent = reverseSimulationComponent;
        this.addActionListener(new ResultsListener());
    }

    private class ResultsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            var nodes = List.of(graph.getNodes().toArray());
            OptionDialog optionDialog = new OptionDialog(null, reverseSimulationComponent, "Option Dialog");
            optionDialog.setVisible(true);
            optionDialog.dispose();
            if (optionDialog.isSuccessful()) {
                paintNodes(nodes, optionDialog.getExaminedStateAndRole().split(":")[1]);
                showDialog(optionDialog.getExaminedStateAndRole().split(":")[1]);
            }
        }
    }

    private void showDialog(String stateName) {
        JFrame graphFrame = new JFrame("Frame Dialog");
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new GridLayout(5, 4));

        ImageIcon circleIcon = createCircleIcon(40);

        graphPanel.add(createOtherLabel(circleIcon));
        graphPanel.add(new JLabel("+"));
        graphPanel.add(createOtherLabel(circleIcon));
        graphPanel.add(new JLabel("="));
        graphPanel.add(new JLabel(createSquareIcon(40,Color.GRAY)));

        graphPanel.add(createOtherLabel(circleIcon));
        graphPanel.add(new JLabel("+"));
        graphPanel.add(createExaminedLabel(circleIcon, stateName));
        graphPanel.add(new JLabel("="));
        graphPanel.add(new JLabel(createSquareIcon(40,Color.YELLOW)));

        graphPanel.add(createExaminedLabel(circleIcon, stateName));
        graphPanel.add(new JLabel("+"));
        graphPanel.add(createOtherLabel(circleIcon));
        graphPanel.add(new JLabel("="));
        graphPanel.add(new JLabel(createSquareIcon(40,Color.RED)));

        graphPanel.add(createExaminedLabel(circleIcon, stateName));
        graphPanel.add(new JLabel("+"));
        graphPanel.add(createExaminedLabel(circleIcon, stateName));
        graphPanel.add(new JLabel("="));
        graphPanel.add(new JLabel(createSquareIcon(40,Color.GREEN)));

        graphFrame.setSize(400, 400);
        graphFrame.add(graphPanel);
        graphFrame.pack();
        graphFrame.setLocationRelativeTo(null);
        graphFrame.setVisible(true);
    }

    private JLabel createOtherLabel(ImageIcon circleIcon) {
        JLabel legendImageLabelOther = new JLabel(circleIcon);
        legendImageLabelOther.setHorizontalTextPosition(SwingConstants.CENTER);
        legendImageLabelOther.setText("other");
        return legendImageLabelOther;
    }

    private JLabel createExaminedLabel(ImageIcon circleIcon, String stateName) {
        JLabel legendImageLabelOther = new JLabel(circleIcon);
        legendImageLabelOther.setHorizontalTextPosition(SwingConstants.CENTER);
        legendImageLabelOther.setText(stateName);
        return legendImageLabelOther;
    }

    private ImageIcon createCircleIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.GRAY);
        g.fillOval(0, 0, size, size);
        g.dispose();

        return new ImageIcon(image);
    }

    private ImageIcon createSquareIcon(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, size, size);
        g.dispose();

        return new ImageIcon(image);
    }

    private void paintNodes(List<Node> nodes, String stateName) {
        nodes.forEach(node -> {
            if (node.getAttribute(ConfigLoader.colNameRootState).equals(stateName)
                    && node.getAttribute(ConfigLoader.colNameNodeState).equals(stateName)) {
                node.setColor(Color.green);
            } else if (node.getAttribute(ConfigLoader.colNameRootState).equals(stateName)) {
                node.setColor(Color.red);
            } else if (node.getAttribute(ConfigLoader.colNameNodeState).equals(stateName)) {
                node.setColor(Color.yellow);
            } else {
                node.setColor(Color.GRAY);
            }
        });
    }

}
