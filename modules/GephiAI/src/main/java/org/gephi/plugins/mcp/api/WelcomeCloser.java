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

package org.gephi.plugins.mcp.api;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Closes Gephi's Welcome window when work starts through the API, so it does not sit over the
 * graph the assistant is building. Gephi closes it the same way, by disposing the window that
 * holds its panel. Only in the first minutes after startup, and only until one has been closed:
 * a Welcome the person reopens later from the Help menu is theirs.
 */
final class WelcomeCloser {

    static final String WELCOME_PANEL = "org.gephi.desktop.welcome.WelcomeTopComponent";
    static final long WINDOW_MS = 5 * 60_000;

    private static final Logger LOGGER = Logger.getLogger(WelcomeCloser.class.getName());
    private static final long STARTED = System.currentTimeMillis();
    private static volatile boolean closedOne;

    private WelcomeCloser() {
    }

    /** Close the Welcome window if it is open and this is still the start of the session. */
    static void closeIfOpen() {
        if (!shouldTry(System.currentTimeMillis() - STARTED, closedOne)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                for (Window window : Window.getWindows()) {
                    if (window.isShowing() && contains(window, WELCOME_PANEL)) {
                        window.dispose();
                        closedOne = true;
                    }
                }
            } catch (RuntimeException e) {
                LOGGER.log(Level.FINE, "Could not close the Welcome window", e);
            }
        });
    }

    static boolean shouldTry(long uptimeMs, boolean alreadyClosedOne) {
        return !alreadyClosedOne && uptimeMs <= WINDOW_MS;
    }

    /** True when a component of the named class sits anywhere inside {@code container}. */
    static boolean contains(Container container, String className) {
        for (Component child : container.getComponents()) {
            if (child.getClass().getName().equals(className)) {
                return true;
            }
            if (child instanceof Container && contains((Container) child, className)) {
                return true;
            }
        }
        return false;
    }
}
