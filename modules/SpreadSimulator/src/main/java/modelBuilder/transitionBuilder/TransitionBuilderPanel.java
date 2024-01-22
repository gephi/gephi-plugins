package modelBuilder.transitionBuilder;

import simulationModel.transition.TransitionType;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class TransitionBuilderPanel extends JPanel {
    private Graph graph;
    private List<Node> states;
    private TransitionBuilder transitionBuilder;

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

    private JList<String> provNeighbourList;

    public TransitionBuilderPanel(TransitionBuilder transitionBuilder){
        this(transitionBuilder, null, null);
    }

    public TransitionBuilderPanel(TransitionBuilder transitionBuilder, String source, String destination) {
        this.transitionBuilder = transitionBuilder;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        var nodes = List.of(graph.getNodes().toArray());
        states = nodes.stream().filter(x -> x.getLabel().equals("State")).collect(Collectors.toList());

        nodeList = states.stream().map(x -> x.getAttribute("NodeState").toString()).toArray(String[]::new);

        source = source != null ? source : nodeList[0];
        destination = destination != null ? destination : nodeList[1];


        String finalSource = source;
        var sourceState = states.stream().filter(x -> x.getAttribute("NodeState").toString().equals(finalSource)).findFirst().get();
        String finalDestination = destination;
        var destinationState = states.stream().filter(x -> x.getAttribute("NodeState").toString().equals(finalDestination)).findFirst().get();

        sourceNodeLabel = new JLabel("Source node:");
        sourceNodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourceNodeDropdown = new JComboBox<>(nodeList);
        sourceNodeDropdown.setSelectedItem(source);
        sourceNodeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourceNodeDropdown.addActionListener(new SourceNodeDropdownActionListener());
        this.transitionBuilder.setSourceNode(sourceState);

        destinationNodeLabel = new JLabel("Destination node:");
        destinationNodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        destinationNodeDropdown = new JComboBox<>(nodeList);
        destinationNodeDropdown.setSelectedItem(destination);
        destinationNodeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        destinationNodeDropdown.addActionListener(new DestinationNodeDropdownActionListener());
        this.transitionBuilder.setDestinationNode(destinationState);

        transitionTypes = new String[] {"No Condition Probability", "Condition Probability"};
        transitionTypeLabel = new JLabel("Transition Type:");
        transitionTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        transitionTypeDropdown = new JComboBox<>(transitionTypes);
        transitionTypeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        transitionTypeDropdown.addActionListener(new TransitionTypeDropdownActionListener());
        this.transitionBuilder.setTransitionType(TransitionType.noConditionProbability);


        probabilityLabel = new JLabel("Probability:");
        probabilityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        probabilityField = new JTextField(20);
        probabilityField.setAlignmentX(Component.LEFT_ALIGNMENT);
        ((AbstractDocument) probabilityField.getDocument()).setDocumentFilter(new DoubleFilter());
        probabilityField.getDocument().addDocumentListener(new ProbabilityFieldDocumentListener());
        this.transitionBuilder.setProbability(0.);

        provNeighbourLabel = new JLabel("Provocative Neighbours:");
        provNeighbourList = new JList<>(nodeList);
        provNeighbourList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        provNeighbourList.addListSelectionListener(new ProvNeighbourListSectionListener());
        this.transitionBuilder.setProvocativeNeighbours(null);
        scrollPane = new JScrollPane(provNeighbourList);

        add(sourceNodeLabel);
        add(sourceNodeDropdown);
        add(destinationNodeLabel);
        add(destinationNodeDropdown);
        add(transitionTypeLabel);
        add(transitionTypeDropdown);
        add(probabilityLabel);
        add(probabilityField);
    }

    private class SourceNodeDropdownActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var sourceNode = states.stream().filter(x -> x.getAttribute("NodeState").toString().equals(sourceNodeDropdown.getSelectedItem().toString())).findFirst().get();
            transitionBuilder.setSourceNode(sourceNode);
        }
    }

    private class DestinationNodeDropdownActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            var destinationNode = states.stream().filter(x -> x.getAttribute("NodeState").toString().equals(destinationNodeDropdown.getSelectedItem().toString())).findFirst().get();
            transitionBuilder.setDestinationNode(destinationNode);
        }
    }


    private class TransitionTypeDropdownActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(transitionTypeDropdown.getSelectedItem().equals("Condition Probability")){
                transitionBuilder.setTransitionType(TransitionType.conditionProbability);
                add(provNeighbourLabel);
                add(scrollPane);
                revalidate();
                repaint();
            }
            if(transitionTypeDropdown.getSelectedItem().equals("No Condition Probability")){
                transitionBuilder.setTransitionType(TransitionType.noConditionProbability);
                remove(provNeighbourLabel);
                remove(scrollPane);
                transitionBuilder.setProvocativeNeighbours(null);
                revalidate();
                repaint();
            }
        }
    }

    private class ProbabilityFieldDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            Update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            Update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            Update();
        }

        private void Update() {
            transitionBuilder.setProbability(Double.parseDouble(probabilityField.getText()));
        }
    }

    class DoubleFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string.matches("[\\d.]*")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text.matches("[\\d.]*")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    private class ProvNeighbourListSectionListener implements javax.swing.event.ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            var selectedValues = provNeighbourList.getSelectedValuesList();
            transitionBuilder.setProvocativeNeighbours(selectedValues);
        }
    }
}
