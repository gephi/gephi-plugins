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
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.gephi.desktop.streaming.StreamingModel.ClientNode;
import org.gephi.streaming.api.StreamingConnection;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * A Node representing the connection to the stream.
 *
 * @see Node
 *
 * @author panisson
 */
public class StreamingConnectionNode extends AbstractNode {

    private enum ConnectionState {
        CONNECTED,
        CLOSED,
        ERROR,
        RECEIVING
    }

    private static Image connectedImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_connected.png", true);
    private static Image closedImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_disconnected.png", true);
    private static Image sendrecImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_sendrec.png", true);
    private static Image errorImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_error.png", true);

    private Action closeConnectionAction;
    private Action showReportAction;
    private Action removeFromViewAction;
    private Action synchronizeAction;
    private ConnectionState state;
    private Timer timer;
    private final StreamingConnection connection;

    /**
     * Create a Node from the given connection.
     *
     * @param connection - the connection to take the information
     */
    public StreamingConnectionNode(final StreamingConnection connection) {
        super(Children.LEAF);
        this.connection = connection;
        timer = new Timer();
        setDisplayName(connection.getStreamingEndpoint().getUrl().toString());
        connection.addStatusListener(
        new StreamingConnection.StatusListener() {

            public void onConnectionClosed(StreamingConnection connection) {
                setState(ConnectionState.CLOSED);
            }
            
            public void onDataReceived(StreamingConnection connection) {
                setState(ConnectionState.RECEIVING);
            }

            public void onError(StreamingConnection connection) {
                setState(ConnectionState.ERROR);
            }
        });

        closeConnectionAction = new AbstractAction("Close connection") {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        closeConnection();
                    }
                });
            }
        };

        showReportAction = new AbstractAction("Show Report") {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        StreamingUIController controller = Lookup.getDefault().lookup(StreamingUIController.class);
                        controller.showReport(connection);
                    }
                });
            }
        };

        removeFromViewAction = new AbstractAction("Remove from view") {

            public void actionPerformed(ActionEvent e) {
                ClientNode parent = (ClientNode)getParentNode();
                parent.removeConnectionNode(StreamingConnectionNode.this);
            }
        };

        synchronizeAction = new AbstractAction("Synchronize") {
            public void actionPerformed(ActionEvent e) {
                ClientNode parent = (ClientNode)getParentNode();
                parent.removeConnectionNode(StreamingConnectionNode.this);
                StreamingUIController controller = Lookup.getDefault().lookup(StreamingUIController.class);
                controller.synchronize(connection);
            }
        };

        if (!connection.isClosed()) {
            state = ConnectionState.CONNECTED;
        } else {
            state = ConnectionState.CLOSED;
        }
    }

    /**
     * Close the underlying connection and put the state in CLOSED.
     */
    public void closeConnection() {
        if (state!=ConnectionState.CLOSED) {
            try {
                connection.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            setState(ConnectionState.CLOSED);
        }
    }

    @Override
    public Image getIcon(int type) {
        switch (state) {
            case CONNECTED: return connectedImage;
            case RECEIVING: return sendrecImage;
            case ERROR: return errorImage;
            case CLOSED: return closedImage;
            default: return closedImage;
        }
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public Action[] getActions(boolean popup) {
        if (state != ConnectionState.CLOSED) {
            return new Action[]{synchronizeAction, closeConnectionAction, showReportAction};
        } else {
            return new Action[]{showReportAction, removeFromViewAction};
        }
    }

    /**
     * Sets the state of this node.
     * @param newstate the new state
     */
    private void setState(ConnectionState newstate) {
        if (newstate == state) return;
        if (state == ConnectionState.CLOSED) return;

        switch (newstate) {
            case CONNECTED:
            case ERROR:
                 state = newstate;
                 fireIconChange();
                 break;
            case CLOSED:
                 state = newstate;
                 fireIconChange();
                 timer.cancel();
                 timer = null;
                 break;
            case RECEIVING:
                state = newstate;
                fireIconChange();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        setState(ConnectionState.CONNECTED);
                    }
                }, 100);
                break;
        }
    }
}
