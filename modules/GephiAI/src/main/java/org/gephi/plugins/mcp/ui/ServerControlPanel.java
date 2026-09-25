/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.mcp.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.gephi.plugins.mcp.Installer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Control panel for the Gephi AI server: shows whether the server is running
 * and at which URL, lets the user start and stop it, and lets the user change
 * the port. The port is persisted through Installer.setPreferredPort and takes
 * effect on the next start. Start and stop run off the event dispatch thread.
 */
public final class ServerControlPanel extends JPanel {

    private static final RequestProcessor RP = new RequestProcessor("GephiAI-ServerControl", 1);

    static final int MIN_PORT = 1024;
    static final int MAX_PORT = 65535;

    private final JLabel statusLabel = new JLabel();
    private final JTextField portField = new JTextField(6);
    private final JButton startButton = new JButton(msg("ServerControlPanel.start"));
    private final JButton stopButton = new JButton(msg("ServerControlPanel.stop"));
    private final JLabel messageLabel = new JLabel(" ");

    ServerControlPanel() {
        super(new GridBagLayout());
        portField.setText(Integer.toString(Installer.getPreferredPort()));
        startButton.addActionListener(e -> onStart());
        stopButton.addActionListener(e -> onStop());
        buildLayout();
        refresh();
    }

    /** Opens the control dialog. Must be called on the event dispatch thread. */
    public static void showDialog() {
        ServerControlPanel panel = new ServerControlPanel();
        String close = msg("ServerControlPanel.close");
        // The four argument constructor is used deliberately: the longer ones
        // carry HelpCtx in their signatures, which lives in org-openide-util-ui,
        // a module this plugin does not depend on.
        DialogDescriptor descriptor = new DialogDescriptor(
                panel, msg("ServerControlPanel.title"), true, (ActionListener) null);
        descriptor.setOptions(new Object[]{close});
        DialogDisplayer.getDefault().notify(descriptor);
    }

    private void buildLayout() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 4);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        add(statusLabel, c);

        c.gridy = 1;
        c.gridwidth = 1;
        add(new JLabel(msg("ServerControlPanel.portLabel")), c);
        c.gridx = 1;
        add(portField, c);
        c.gridx = 2;
        add(startButton, c);
        c.gridx = 3;
        add(stopButton, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        add(new JLabel(msg("ServerControlPanel.portHint")), c);

        c.gridy = 3;
        add(messageLabel, c);
    }

    /**
     * Parses a port field value. Returns the port when it is a usable number
     * between MIN_PORT and MAX_PORT, and -1 otherwise. Static and free of
     * Swing so it can be unit tested.
     */
    static int parsePort(String text) {
        if (text == null) {
            return -1;
        }
        try {
            int port = Integer.parseInt(text.trim());
            return (port >= MIN_PORT && port <= MAX_PORT) ? port : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void onStart() {
        int port = parsePort(portField.getText());
        if (port < 0) {
            messageLabel.setText(msg("ServerControlPanel.portInvalid"));
            return;
        }
        Installer.setPreferredPort(port);
        setBusy(true);
        RP.post(() -> {
            final String error = Installer.requestStart();
            SwingUtilities.invokeLater(() -> {
                messageLabel.setText(error == null ? " " : error);
                setBusy(false);
                refresh();
            });
        });
    }

    private void onStop() {
        setBusy(true);
        RP.post(() -> {
            Installer.requestStop();
            SwingUtilities.invokeLater(() -> {
                messageLabel.setText(" ");
                setBusy(false);
                refresh();
            });
        });
    }

    private void setBusy(boolean busy) {
        startButton.setEnabled(!busy);
        stopButton.setEnabled(!busy);
        portField.setEnabled(!busy);
    }

    private void refresh() {
        if (Installer.isServerRunning()) {
            String url = "http://127.0.0.1:" + Installer.getRunningPort();
            statusLabel.setText(NbBundle.getMessage(ServerControlPanel.class,
                    "ServerControlPanel.status.running", url));
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            statusLabel.setText(msg("ServerControlPanel.status.stopped"));
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private static String msg(String key) {
        return NbBundle.getMessage(ServerControlPanel.class, key);
    }
}
