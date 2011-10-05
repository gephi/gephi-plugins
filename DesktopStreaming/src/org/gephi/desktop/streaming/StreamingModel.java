/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
