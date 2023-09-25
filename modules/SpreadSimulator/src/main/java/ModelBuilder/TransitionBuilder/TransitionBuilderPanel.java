package ModelBuilder.TransitionBuilder;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class TransitionBuilderPanel extends JPanel {
    private Graph graph;
    private List<Node> states;
    private TransitionBuilder modelBuilder;

    private JLabel sourceNodeLabel;
    private JLabel destinationNodeLabel;
    private JLabel transitionTypeLabel;
    private JLabel probabilityLabel;
    private JLabel provNeighbourLabel;

    private String[] nodeList;
    private String[] transitionTypes;


    private JComboBox<String> sourceNodeDropdown;
    private JComboBox<String> destinationNodeDropdown;
    private JComboBox<String> transitionTypeDropdown;

    private JScrollPane scrollPane;

    private JTextField probabilityField;
    public TransitionBuilderPanel(TransitionBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        var nodes = List.of(graph.getNodes().toArray());
        states = nodes.stream().filter(x -> x.getLabel().equals("State")).collect(Collectors.toList());

        nodeList = states.stream().map(x -> x.getAttribute("NodeState").toString()).toArray(String[]::new);

        sourceNodeLabel = new JLabel("Source node:");
        sourceNodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourceNodeDropdown = new JComboBox<>(nodeList);
        sourceNodeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        destinationNodeLabel = new JLabel("Destination node:");
        destinationNodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        destinationNodeDropdown = new JComboBox<>(nodeList);
        destinationNodeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        transitionTypes = new String[] {"No Condition Probability", "Condition Probability"};
        transitionTypeLabel = new JLabel("Transition Type:");
        transitionTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        transitionTypeDropdown = new JComboBox<>(transitionTypes);
        transitionTypeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        transitionTypeDropdown.addActionListener(new TransitionTypeDropdownActionListener());

        probabilityLabel = new JLabel("Probability:");
        probabilityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        probabilityField = new JTextField(20);
        probabilityField.setAlignmentX(Component.LEFT_ALIGNMENT);

        provNeighbourLabel = new JLabel("Provocative Neighbours:");
        JList<String> list = new JList<>(nodeList);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollPane = new JScrollPane(list);

        add(sourceNodeLabel);
        add(sourceNodeDropdown);
        add(destinationNodeLabel);
        add(destinationNodeDropdown);
        add(transitionTypeLabel);
        add(transitionTypeDropdown);
        add(probabilityLabel);
        add(probabilityField);
    }

    private class TransitionTypeDropdownActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(transitionTypeDropdown.getSelectedItem().equals("Condition Probability")){
                add(provNeighbourLabel);
                add(scrollPane);
                revalidate();
                repaint();
            }
            if(transitionTypeDropdown.getSelectedItem().equals("No Condition Probability")){
                remove(provNeighbourLabel);
                remove(scrollPane);
                revalidate();
                repaint();
            }
        }
    }
}
