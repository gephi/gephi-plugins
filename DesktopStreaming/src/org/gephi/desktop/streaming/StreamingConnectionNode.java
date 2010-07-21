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

    public static Image connectedImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_connected.png", true);
    public static Image disconnectedImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_disconnected.png", true);
    public static Image sendrecImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_sendrec.png", true);
    public static Image errorImage = ImageUtilities.loadImage("org/gephi/desktop/streaming/resources/dot_error.png", true);

    private Image icon;
    private Action[] actions;

    private Action closeConnectionAction;
    private Action showReportAction;
    private ConnectionState lastState;

    public StreamingConnectionNode(final StreamingConnection connection, final Report report) {
        super(Children.LEAF);
        setDisplayName(connection.getUrl().toString());
        connection.addStatusListener(
        new StreamingConnection.StatusListener() {

            public void onConnectionClosed(StreamingConnection connection) {
                switchToClosed();
            }
            
            public void onDataReceived(StreamingConnection connection) {
                switchToReceiving();
            }

            public void onError(StreamingConnection connection) {
                switchToError();
            }
        });

        closeConnectionAction = new AbstractAction("Close connection") {

            public void actionPerformed(ActionEvent e) {
                if (lastState!=ConnectionState.CLOSED) {
                    try {
                        connection.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    switchToClosed();
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

        actions = new Action[]{closeConnectionAction, showReportAction};

        if (!connection.isClosed()) {
            lastState = ConnectionState.CONNECTED;
            icon = connectedImage;
        } else {
            lastState = ConnectionState.CLOSED;
            icon = disconnectedImage;
        }
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

    private void switchToClosed() {
        if (lastState!=ConnectionState.CLOSED) {
            lastState = ConnectionState.CLOSED;
            icon = disconnectedImage;
            fireIconChange();
            actions = new Action[]{showReportAction};
        }
    }

    private void switchToReceiving() {
        if (lastState!=ConnectionState.RECEIVING) {
            lastState = ConnectionState.RECEIVING;
            icon = sendrecImage;
            fireIconChange();
            actions = new Action[]{closeConnectionAction, showReportAction};

            new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {}
                    switchToConnected();
                }
            }.start();
        }
    }

    private void switchToConnected() {
        if (lastState!=ConnectionState.CONNECTED && lastState!=ConnectionState.CLOSED) {
            lastState = ConnectionState.CONNECTED;
            icon = connectedImage;
            fireIconChange();
            actions = new Action[]{closeConnectionAction, showReportAction};
        }
    }

        private void switchToError() {
        if (lastState!=ConnectionState.ERROR && lastState!=ConnectionState.CLOSED) {
            lastState = ConnectionState.ERROR;
            icon = errorImage;
            fireIconChange();
            actions = new Action[]{closeConnectionAction, showReportAction};
        }
    }
}
