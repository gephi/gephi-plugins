/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.streaming;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

public class StreamingServerNode extends AbstractNode {

    private static Image connectedImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_connected.png", true);
    private static Image disconnectedImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_disconnected.png", true);

    private boolean masterStarted;
    private Action startMasterAction;
    private Action stopMasterAction;

    public StreamingServerNode() {
        super(Children.LEAF);
        setDisplayName("Master Server");

        masterStarted = false;

        startMasterAction = new AbstractAction("Start") {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!masterStarted) {
                            StreamingController controller =
                                    Lookup.getDefault().lookup(StreamingController.class);
                            controller.startMaster();
                            masterStarted = true;
                            fireIconChange();
                        }
                    }
                });

            }
        };

        stopMasterAction = new AbstractAction("Stop") {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (masterStarted) {
                            StreamingController controller =
                                    Lookup.getDefault().lookup(StreamingController.class);
                            controller.stopMaster();
                            masterStarted = false;
                            fireIconChange();
                        }
                    }
                });

            }
        };
    }

    @Override
    public Image getIcon(int type) {
        if (masterStarted) {
            return connectedImage;
        } else {
            return disconnectedImage;
        }
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public Action[] getActions(boolean popup) {
        if (masterStarted) {
            return new Action[]{stopMasterAction};
        } else {
            return new Action[]{startMasterAction};
        }
    }
}
