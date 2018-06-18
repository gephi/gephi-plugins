package KatzCentrality;

import org.gephi.graph.api.GraphController;
import org.jdesktop.swingx.JXHeader;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;

public class KatzCentralityPanel extends JPanel {

    private ButtonGroup buttonGroup1;
    private JRadioButton directedRadioButton;
    private JXHeader header;
    private JTextField iterationTextField;
    private JLabel jLabel1;
    private JRadioButton undirectedRadioButton;

    public KatzCentralityPanel() {
        initComponents();
        //Disable directed if the graph is undirecteds
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if(graphController.getGraphModel().isUndirected()){
//            directedRadioButton.setEnabled(false);
        }
    }

    private void initComponents() {
        buttonGroup1 = new ButtonGroup();
        header = new JXHeader();
        iterationTextField = new JTextField();
        jLabel1 = new JLabel();
        directedRadioButton = new JRadioButton();
        undirectedRadioButton = new JRadioButton();

        header.setTitle("Katz Centrality");
        iterationTextField.setMinimumSize(new Dimension(30, 27));

        jLabel1.setText("Liczba");

        buttonGroup1.add(directedRadioButton);
        directedRadioButton.setText("Directed");

        buttonGroup1.add(undirectedRadioButton);
        undirectedRadioButton.setText("Undirected");
//        undirectedRadioButton.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
////                undirectedRadioButtonActionPerformed(evt);
//            }
//        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(header, GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(directedRadioButton)
                                        .addComponent(undirectedRadioButton)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addGap(8, 8, 8)
                                                .addComponent(iterationTextField, GroupLayout.PREFERRED_SIZE,
                                                        174, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(header, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(directedRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(undirectedRadioButton)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(iterationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1))
                                .addContainerGap(66, Short.MAX_VALUE))
        );
    }
}
