package org.gephi.desktop.streaming;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamingConnection;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

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
    private ConnectionState state;

    public StreamingConnectionNode(final StreamingConnection connection, final Report report) {
        super(Children.LEAF);
        setDisplayName(connection.getUrl().toString());
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
                if (state!=ConnectionState.CLOSED) {
                    try {
                        connection.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    setState(ConnectionState.CLOSED);
                }
            }
        };

        showReportAction = new AbstractAction("Show Report") {

            public void actionPerformed(ActionEvent e) {
                ReportPanel reportPanel = new ReportPanel();
                reportPanel.setData(report);
                DialogDescriptor dd = new DialogDescriptor(reportPanel, NbBundle.getMessage(StreamingController.class, "ReportPanel.title"));
                if (!DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    reportPanel.destroy();
                    return;
                }
                reportPanel.destroy();
                }
        };

        if (!connection.isClosed()) {
            state = ConnectionState.CONNECTED;
        } else {
            state = ConnectionState.CLOSED;
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
            return new Action[]{closeConnectionAction, showReportAction};
        } else {
            return new Action[]{showReportAction};
        }
    }

    private void setState(ConnectionState newstate) {
        if (newstate == state) return;
        if (state == ConnectionState.CLOSED) return;

        switch (newstate) {
            case CONNECTED:
            case ERROR:
            case CLOSED:
                 state = newstate;
                 fireIconChange();
                 break;
            case RECEIVING:
                state = newstate;
                fireIconChange();

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {}
                        setState(ConnectionState.CONNECTED);
                    }
                }.start();
                break;
        }
    }
}
