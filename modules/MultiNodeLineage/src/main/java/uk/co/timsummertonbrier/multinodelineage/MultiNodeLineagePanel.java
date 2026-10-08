
package uk.co.timsummertonbrier.multinodelineage;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MultiNodeLineagePanel extends JPanel {
    
    private static final long serialVersionUID = 1759842236L;
    
    private final JTextField originNodeIdsTextField = new JTextField();
    
    public MultiNodeLineagePanel() {
        initComponents();
    }
    
    public List<String> getOriginNodeIds() {
        String allOriginNodeIds = originNodeIdsTextField.getText();
        if (allOriginNodeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(allOriginNodeIds.split(","));
    }
    
    private void initComponents() {        
        setPreferredSize(new Dimension(450, 160));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        addText("Identifies the ancestors and descendants of a set of nodes. Attributes are added to the nodes to record the results. These attributes can be used for further analysis (e.g filtering).");
        addGap();
        addText("Enter the ID(s) of the origin node(s) you wish to process. Separate multiple IDs with a comma, e.g: node1,node2,node3");
        addGap();
        add(originNodeIdsTextField);
    }
    
    private void addText(String text) {
        add(new JLabel("<html><body style='width: 300px'>" + text));
    }
    
    private void addGap() {
        add(Box.createRigidArea(new Dimension(0, 20)));
    }
    
}
