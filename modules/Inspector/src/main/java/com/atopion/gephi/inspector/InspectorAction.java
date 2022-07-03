/*
 * Copyright 2020 atopion.com
 * Authors : Timon Vogt
 * Website : https://www.atopion.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atopion.gephi.inspector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.BooleanStateAction;

@ActionID(
        category = "Tools",
        id = "com.atopion.gephi.inspector.InspectorAction"
)
@ActionRegistration(
        displayName = "#CTL_InspectorAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 0),
    @ActionReference(path = "Shortcuts", name = "DO-I")
})
@Messages("CTL_InspectorAction=Inspector")
public final class InspectorAction extends BooleanStateAction {

    public InspectorAction() {
        super();
        
        // Initial state is false
        setBooleanState(false);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        setBooleanState(!getBooleanState());
        
        // Plugins main method
        toggleInspector();
    }

    @Override
    public String getName() {
        return "Inspector";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        return new org.openide.awt.Actions.CheckboxMenuItem(this, false);
    }
    
    
    
    private Timer timer = null;
    
    private void toggleInspector() {
        final int delay = 500; // Timer run delay in ms. Shorter value means more CPU load but more reactive UI.
        
        // The user has just activated the inspector.
        if(getBooleanState()) {
            timer = new Timer(delay, runner);
            timer.start();
        }
        
        // The user has just deactivated the inspector
        else {
            nodePropertiesFrame.remove(); // To make sure it isn't stuck.
            timer.stop();
            timer = null;
        }
    }
    
    private final ActionListener runner = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (VizController.getInstance().getSelectionManager() != null) {
                // Query the currently selected Nodes
                List<Node> nodes =
                    VizController.getInstance().getSelectionManager().getSelectedNodes();

                // If the mouse currently hovers over a node, only one is selected.
                // In all other cases, remove the frame and end.
                if (nodes.size() != 1) {
                    nodePropertiesFrame.remove();
                    return;
                }

                // The mouse currently hovers over a node.
                // First, calculate the mouse location.
                int[] mousePosition = calculateMouseLocation();

                // Then display the nodePropertiesFrame using the currently selected node.
                // The window will be located 20px left and 20px down of the mouse.
                nodePropertiesFrame.show(nodes.get(0), mousePosition[0] + 20,
                    mousePosition[1] + 20);
            }
        }
        
        
        private int[] calculateMouseLocation() {
            // Since javax.swing measures the y coordinate top - down and 
            // Gephi's VizController measures it bottom - up, we need to 
            // convert the y coordinate using Gephi's viewportHeight.
            
            // Additionally, the mouse position is relative to Gephi's drawable,
            // therefore we need its location on screen.
            
            int[] mousePosition = new int[2];

            float scale = VizController.getInstance().getDrawable().getGlobalScale();
            float[] mouse = VizController.getInstance().getGraphIO().getMousePosition();
            Point screenLocation = VizController.getInstance().getDrawable().getLocationOnScreen();
            int viewportHeight = VizController.getInstance().getDrawable().getViewportHeight();
            
            // The x coordinate
            mousePosition[0] = screenLocation.x + (int) (mouse[0] / scale);
            
            // The y coordinate
            mousePosition[1] = screenLocation.y + (int) ((viewportHeight - (int) mouse[1]) / scale);
            
            return mousePosition;
        }
    };
    
    private final NodePropertiesFrame nodePropertiesFrame = new NodePropertiesFrame();
    
    // The node properties will be displayed in their own frame.
    class NodePropertiesFrame extends JFrame {
        private JPanel contentPanel;
        
        public NodePropertiesFrame() {
            super();
            this.setType(Type.UTILITY);
            this.setUndecorated(true);
            this.setAlwaysOnTop(false);
            this.setFocusableWindowState(false);
            this.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);
            this.setOpacity(0.75f);
            this.setVisible(false);
            
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                }
            });
            
            contentPanel = new JPanel();
            contentPanel.setLayout(new GridLayout(0, 2));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contentPanel.setBackground(new Color(0, 0, 0));
            
            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add(contentPanel, BorderLayout.CENTER);
        }
        
        // Makes the frame invisible
        public void remove() {
            this.setVisible(false);
        }
        
        // Updates the frame's content with a new node and displays it at a given location.
        public void show(Node node, int x, int y) {
            this.contentPanel.removeAll();
            Set<String> keys = node.getAttributeKeys();
            
            // For each attribute of the node, one row will be displayed,
            // containing the column name and the nodes value (or "null" if none).
            for(String key : keys) {
                Object attr = node.getAttribute(key);
                JLabel keyLabel = new JLabel();
                JLabel dataLabel = new JLabel();
                
                keyLabel.setForeground(Color.WHITE);
                keyLabel.setFont(new Font("Arial", Font.BOLD, 13));
                
                dataLabel.setForeground(Color.WHITE);
                dataLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                
                // Making use of HTML to force the JLabel to use multiple lines
                // if the text is particularly long and exceeds the maximum
                // label width of 200.
                String keyText = key + " : ";
                int keyTextLength = keyLabel.getFontMetrics(keyLabel.getFont()).stringWidth(keyText);
                keyLabel.setText("<html><body style='width: " +
                        (keyTextLength > 200 ? 200 : keyTextLength) +
                        "px'>" + keyText + "</body></html>");

                String dataText = attr != null ? attr.toString() : "null";
                int dataTextLength = dataLabel.getFontMetrics(dataLabel.getFont()).stringWidth(dataText);
                dataLabel.setText("<html><body style='width: " +
                        (dataTextLength > 200 ? 200 : dataTextLength) +
                        "px'>" + dataText + "</body></html>");


                this.contentPanel.add(keyLabel);
                this.contentPanel.add(dataLabel);
            }
            
            // The GridLayoutManager will make the window just as big as it
            // needs to be.
            this.pack();
            this.setLocation(x, y);
            this.setVisible(true);
        }
    }
}