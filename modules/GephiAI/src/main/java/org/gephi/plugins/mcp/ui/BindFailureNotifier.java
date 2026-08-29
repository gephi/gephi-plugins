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

import java.awt.GraphicsEnvironment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Shows server startup failures to the user in a dialog, so a port clash is
 * never silent. Kept in the ui package so the module lifecycle classes stay
 * free of user interface code.
 */
public final class BindFailureNotifier {

    private BindFailureNotifier() {
    }

    /**
     * Shows the message in an error dialog. Safe to call from any thread;
     * DialogDisplayer.notifyLater queues the dialog for the event dispatch
     * thread.
     */
    public static void notifyStartupFailure(final String message) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        NotifyDescriptor descriptor =
                new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        descriptor.setTitle(NbBundle.getMessage(BindFailureNotifier.class,
                "BindFailureNotifier.title"));
        DialogDisplayer.getDefault().notifyLater(descriptor);
    }
}
