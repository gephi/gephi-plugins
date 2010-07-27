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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.gephi.streaming.api.Report;

import org.gephi.streaming.api.StreamingConnection;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class StreamingModel {

    private static final Logger logger = Logger.getLogger(StreamingModel.class.getName());

    private Image clientImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/gephiclient.png", true);
    private Image masterImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/gephimaster.png", true);
    
    private boolean serverRunning;
    private String serverContext;
    
    private Children masterChildren = new Children.Array();

    private Node masterNode;
    private Node clientNode;
    
    public StreamingModel() {

        clientNode = new ClientNode();
        masterNode = new MasterNode();

        masterChildren.add(new Node[]{new StreamingServerNode()});
    }

    public void addConnection(StreamingConnection connection, Report report) {
        StreamingConnectionNode node = new StreamingConnectionNode(connection, report);
        addConnectionNode(node);
    }
    
    public void addConnectionNode(StreamingConnectionNode node) {
        clientNode.getChildren().add(new Node[]{node});
    }

    public void removeConnectionNode(StreamingConnectionNode node) {
        clientNode.getChildren().remove(new Node[]{node});
    }

    Map<String, Node> connectedMap = new HashMap<String, Node>();

    public void addConnected(String client) {
        Node node = new AbstractNode(Children.LEAF);
        node.setDisplayName(client);
        masterNode.getChildren().add(new Node[]{node});
        connectedMap.put(client, node);
    }

    public void removeConnected(String client) {
        Node node = connectedMap.remove(client);
        if (node!=null) {
            masterNode.getChildren().remove(new Node[]{node});
        }
    }

    public boolean isServerRunning() {
        return serverRunning;
    }

    public void setServerRunning(boolean serverRunning) {
        this.serverRunning = serverRunning;
    }

    /**
     * @return the serverContext
     */
    public String getServerContext() {
        return serverContext;
    }

    /**
     * @param serverContext the serverContext to set
     */
    public void setServerContext(String serverContext) {
        this.serverContext = serverContext;
    }

    public Node getClientNode() {
        return clientNode;
    }

    public Node getMasterNode() {
        return masterNode;
    }

    public class ClientNode extends AbstractNode {

        private final Action addConnectionAction;

        public ClientNode() {
            super(new Children.Array());
            setDisplayName("Client");

            addConnectionAction = new AbstractAction("Connect to Stream") {

                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            StreamingUIController controller = Lookup.getDefault().lookup(StreamingUIController.class);
                            controller.connectToStream();
                        }
                    });
                }
            };
        }
        @Override
        public Action[] getActions(boolean popup) {
            return new Action[]{addConnectionAction};
        }
        @Override
        public Image getIcon(int type) {
            return clientImage;
        }

        @Override
        public Image getOpenedIcon(int i) {
            return getIcon(i);
        }
        @Override
        public String getHtmlDisplayName() {
            return "<b>Client<b>";
        }

        public void addConnectionNode(StreamingConnectionNode node) {
            getChildren().add(new Node[]{node});
        }

        public void removeConnectionNode(StreamingConnectionNode node) {
            getChildren().remove(new Node[]{node});
        }
    }

    private class MasterNode extends AbstractNode {

        public MasterNode() {
            super(masterChildren);
            setDisplayName("Master");
        }
        @Override
        public Action[] getActions(boolean popup) {
            return new Action[]{};
        }
        @Override
        public Image getIcon(int type) {
            return masterImage;
        }
        @Override
        public Image getOpenedIcon(int i) {
            return getIcon(i);
        }
        @Override
        public String getHtmlDisplayName() {
            return "<b>Master<b>";
        }
    }
}
