package org.gephi.desktop.streaming;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class StreamingMasterNode extends AbstractNode {

    private Image icon;
    private Action[] actions;
    private boolean masterStarted;
    private Action startMasterAction;
    private Action stopMasterAction;

    public StreamingMasterNode() {
        super(Children.LEAF);
        setDisplayName("Master Server");

        masterStarted = false;
        icon = StreamingTopComponent.disconnectedImage;

        startMasterAction = new AbstractAction("Start") {

            public void actionPerformed(ActionEvent e) {
                if (!masterStarted) {
                    StreamingController controller = Lookup.getDefault().lookup(StreamingController.class);
                    controller.startMaster();
                    actions = new Action[]{stopMasterAction};
                    masterStarted = true;

                    icon = StreamingTopComponent.connectedImage;
                    fireIconChange();
                }
            }
        };

        stopMasterAction = new AbstractAction("Stop") {

            public void actionPerformed(ActionEvent e) {
                if (masterStarted) {
                    StreamingController controller = Lookup.getDefault().lookup(StreamingController.class);
                    controller.stopMaster();
                    actions = new Action[]{startMasterAction};
                    masterStarted = false;

                    icon = StreamingTopComponent.disconnectedImage;
                    fireIconChange();
                }
            }
        };

        actions = new Action[]{startMasterAction};
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public Action[] getActions(boolean popup) {
        return actions;
    }
}
