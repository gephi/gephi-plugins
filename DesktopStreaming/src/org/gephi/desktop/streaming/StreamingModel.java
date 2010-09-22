/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
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

import org.gephi.streaming.api.StreamingConnection;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Object used to group all information about the GUI state: The Client node,
 * the Master node, the outgoing streaming connections and the ingoing server
 * connections.
 *
 * @author panisson
 *
 */
public class StreamingModel {

    private static final Logger logger = Logger.getLogger(StreamingModel.class.getName());

    private Image clientImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/gephiclient.png", true);
    private Image masterImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/gephimaster.png", true);
    private Image masterconnImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/masterconnection.jpg", true);
    
    private boolean masterRunning;
    private String serverContext;
    
    private MasterNode masterNode;
    private Node clientNode;

    /**
     * Create a StreamingModel object with information about the GUI state:
     * The Client node,
     * the Master node, the outgoing streaming connections and the ingoing server
     * connections.
     */
    public StreamingModel() {
        clientNode = new ClientNode();
        masterNode = new MasterNode();
    }

    /**
     * Add a StreamingConnection to the list of outgoing connections.
     * @param connection
     */
    public void addConnection(StreamingConnection connection) {
        StreamingConnectionNode node = new StreamingConnectionNode(connection);
        clientNode.getChildren().add(new Node[]{node});
    }

    /**
     * Remove all outgoing connections
     */
    public void removeAllConnections() {
        for (Node node: clientNode.getChildren().getNodes()) {
            StreamingConnectionNode connNode = (StreamingConnectionNode)node;
            connNode.closeConnection();
        }
        clientNode.getChildren().remove(clientNode.getChildren().getNodes());
    }

    private Map<String, Node> connectedMap = new HashMap<String, Node>();

    /**
     * Add an ingoing client connection to the master list
     * @param client the client description to add
     */
    public void addConnected(String client) {
        Node node = new AbstractNode(Children.LEAF) {
            @Override
            public Action[] getActions(boolean popup) {
                return new Action[]{};
            }
            @Override
            public Image getIcon(int type) {
                return masterconnImage;
            }

            @Override
            public Image getOpenedIcon(int i) {
                return getIcon(i);
            }
        };
        node.setDisplayName(client);
        masterNode.getChildren().add(new Node[]{node});
        connectedMap.put(client, node);
    }

    /**
     * Remove an ingoing client connection to the master list
     * @param client the client description to remove
     */
    public void removeConnected(String client) {
        Node node = connectedMap.remove(client);
        if (node!=null) {
            masterNode.getChildren().remove(new Node[]{node});
        }
    }

    /**
     * Used to verify if the master is running.
     * @return true if the master is running, false otherwise
     */
    public boolean isMasterRunning() {
        return masterRunning;
    }

    /**
     * Used to set the information about the master is running.
     *
     * @param masterRunning true if the master is running, false otherwise
     */
    public void setMasterRunning(boolean masterRunning) {
        this.masterRunning = masterRunning;
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

    /**
     * Used to get the node that represents the Client
     * @return the Client node
     */
    public Node getClientNode() {
        return clientNode;
    }

    /**
     * Used to get the node that represents the Master
     * @return the Master node
     */
    public MasterNode getMasterNode() {
        return masterNode;
    }

    /**
     * A class that represents the Client node information.
     */
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

    /**
     * A class that represents the Master node information.
     */
    public class MasterNode extends AbstractNode {

        StreamingServerNode streamingServerNode;

        public MasterNode() {
            super(new Children.Array());
            setDisplayName("Master");
            streamingServerNode = new StreamingServerNode();
            this.getChildren().add(new Node[]{streamingServerNode});
        }

        public StreamingServerNode getStreamingServerNode() {
            return streamingServerNode;
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
