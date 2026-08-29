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
package org.gephi.plugins.mcp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.plugins.mcp.api.GephiAPIServer;
import org.gephi.plugins.mcp.service.GephiControlService;
import org.gephi.plugins.mcp.ui.BindFailureNotifier;
import org.gephi.visualization.api.VisualizationController;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Module lifecycle for the Gephi AI plugin. Starts the local HTTP API when the
 * module is restored and stops it when the module closes. The server can also
 * be started and stopped from Tools, Gephi AI Server, which calls the static
 * entry points below. This class stays free of Swing; all user interface work
 * lives in the ui package.
 */
public class Installer extends ModuleInstall {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    static final int DEFAULT_PORT = 8080;
    static final String PREF_PORT = "port";

    /** The live module instance, published by restored() for the UI entry points. */
    private static volatile Installer instance;

    /**
     * Written by the MCP-Server-Starter thread and read by other threads,
     * including the event dispatch thread in stopNow(). Volatile so a stop can
     * never read a stale null and skip a live server.
     */
    private volatile GephiAPIServer server;

    /**
     * True once the module is closing or the user has stopped the server.
     * Checked before the delayed startup constructs the server, so a disable
     * that lands inside the startup delay cannot be followed by a start that
     * nothing can ever stop.
     */
    private volatile boolean stopped;

    @Override
    public void restored() {
        instance = this;
        stopped = false;
        Thread serverThread = new Thread(() -> {
            try {
                Thread.sleep(2000); // Let Gephi finish initializing before binding the port.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            String error = startNow();
            if (error != null) {
                BindFailureNotifier.notifyStartupFailure(error);
            }
            installClickListenerWhenVisualizationReady();
        }, "MCP-Server-Starter");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Override
    public void close() {
        stopped = true;
        stopNow();
    }

    @Override
    public boolean closing() {
        stopped = true;
        stopNow();
        return true;
    }

    // ─── Entry points for the Tools menu dialog ──────────────────────────

    /** True when the API server is currently listening. */
    public static boolean isServerRunning() {
        Installer i = instance;
        return i != null && i.isRunningNow();
    }

    /** The port the running server is bound to, or -1 when it is not running. */
    public static int getRunningPort() {
        Installer i = instance;
        GephiAPIServer s = (i == null) ? null : i.server;
        return (s != null && s.isAlive()) ? s.getListeningPort() : -1;
    }

    /**
     * The port the next start will bind: the persisted preference if set,
     * otherwise the gephi.mcp.port system property, otherwise 8080.
     */
    public static int getPreferredPort() {
        int fallback = Integer.getInteger("gephi.mcp.port", DEFAULT_PORT);
        return NbPreferences.forModule(Installer.class).getInt(PREF_PORT, fallback);
    }

    /** Persists the port. Takes effect the next time the server starts. */
    public static void setPreferredPort(int port) {
        NbPreferences.forModule(Installer.class).putInt(PREF_PORT, port);
    }

    /**
     * Starts the server on the preferred port if it is not already running.
     * Returns null on success, or a user-facing error message on failure.
     * Callers must invoke this off the event dispatch thread.
     */
    public static String requestStart() {
        Installer i = instance;
        if (i == null) {
            return NbBundle.getMessage(Installer.class, "Installer.error.moduleNotReady");
        }
        i.stopped = false;
        return i.startNow();
    }

    /** Stops the server if it is running, and cancels any pending delayed start. */
    public static void requestStop() {
        Installer i = instance;
        if (i == null) {
            return;
        }
        i.stopped = true;
        i.stopNow();
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────

    /** True when this instance's server is currently listening. */
    boolean isRunningNow() {
        GephiAPIServer s = server;
        return s != null && s.isAlive();
    }

    /**
     * Starts the server unless it is already running or the module has been
     * stopped. Returns null when the server is running afterwards, or when the
     * start was cancelled by a stop; returns a user-facing message when the
     * bind failed. Synchronized against stopNow() so a start and a stop can
     * never interleave.
     */
    synchronized String startNow() {
        if (stopped) {
            return null; // The module was disabled or stopped while the start was pending.
        }
        if (isRunningNow()) {
            return null;
        }
        int port = getPreferredPort();
        GephiAPIServer s = new GephiAPIServer(port);
        try {
            s.startServer();
        } catch (IOException e) {
            String detail = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.toString();
            LOGGER.log(Level.SEVERE, "Gephi AI server failed to bind port " + port, e);
            return NbBundle.getMessage(Installer.class, "Installer.error.bindFailed",
                    Integer.toString(port), detail);
        }
        server = s;
        if (stopped) {
            // A stop request arrived while the bind was in flight; honor it.
            stopNow();
            return null;
        }
        LOGGER.log(Level.INFO, "Gephi AI API listening on http://127.0.0.1:{0}",
                Integer.toString(port));
        return null;
    }

    /**
     * Installs the passive node click listener as soon as Gephi's visualization
     * controller is available, so clicks made before the first API call are
     * recorded rather than silently dropped. A single call after the bind would
     * not be enough: GephiControlService.ensureClickListener no-ops while the
     * controller is absent from the Lookup and never retries on its own, and at
     * two seconds after startup the controller may not exist yet. Polling until
     * the controller appears makes the install deterministic. Runs on the
     * daemon starter thread, bounded, and honors a module stop.
     */
    private void installClickListenerWhenVisualizationReady() {
        for (int i = 0; i < 120 && !stopped; i++) {
            if (Lookup.getDefault().lookup(VisualizationController.class) != null) {
                GephiControlService.getInstance().ensureClickListener();
                LOGGER.fine("Gephi AI click listener installed at startup");
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        LOGGER.fine("Gephi AI click listener not installed; visualization controller unavailable");
    }

    /**
     * Stops the server. Safe to call from any thread, including the event
     * dispatch thread; never blocks for more than three seconds.
     */
    private synchronized void stopNow() {
        final GephiAPIServer s = server;
        server = null;
        if (s == null) {
            return;
        }
        // Stop on a daemon watchdog so a slow or stuck socket close can never
        // block Gephi's shutdown, which runs on the event dispatch thread. Wait
        // at most three seconds, then continue regardless; the daemon threads
        // cannot keep the JVM alive.
        Thread stopper = new Thread(() -> {
            try {
                s.stopServer();
                LOGGER.info("Gephi AI API stopped");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error stopping the Gephi AI server", e);
            }
        }, "MCP-Server-Stopper");
        stopper.setDaemon(true);
        stopper.start();
        try {
            stopper.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (stopper.isAlive()) {
            LOGGER.warning("Gephi AI server stop exceeded three seconds; continuing shutdown");
        }
    }
}
