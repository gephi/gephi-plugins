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

    private Action connectAction;
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
        
        connectAction = new AbstractAction("Connect") {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	StreamingUIController controller = Lookup.getDefault().lookup(StreamingUIController.class);
                    	controller.connectToStream(connection.getStreamingEndpoint());
                    }
                });
            }
        };

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
            return new Action[]{connectAction, showReportAction, removeFromViewAction};
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
