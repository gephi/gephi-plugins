/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package uk.ac.ox.oii.sigmaexporter;

import org.gephi.desktop.io.export.spi.ExporterClassUI;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Export action placed in the "Export..." menu which exports the current graph
 * to a SQLite database file.
 * <p>
 * This implements the {@link ExporterClassUI} interface, which is a little
 * different from {@link ExporterUI}. Unlike the <code>ExporterUI</code> interface
 * which can add "Options" to a file export the <code>ExporterClassUI</code> will
 * simply add a new line to the "Export..." menu and let the implementation
 * control the rest.
 * <p>
 * The <code>action()</code> creates a new exporter and configure it with
 * the settings panel. The export process is wrapped in a {@link LongTaskExecutor}
 * which executes {@link LongTask} in a separated thread and supports progress
 * and cancel.
 * 
 * 
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterClassUI.class)
public class SigmaExporterUI implements ExporterClassUI {

    private final LongTaskErrorHandler errorHandler;
    private boolean cancelled = true;

    public SigmaExporterUI() {
        //Create a generic error handler called if the task raises an exception
        errorHandler = new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                cancelled = true;
                String message = t.getCause().getMessage();
                if (message == null || message.isEmpty()) {
                    message = t.getMessage();
                }
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        };
    }

    @Override
    public String getName() {
        return "Sigma.js template...";
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {
        //Create exporter
        final SigmaExporter exporter = new SigmaExporter();
        final Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        exporter.setWorkspace(currentWorkspace);

        //Create the settings panel
        SigmaSettingsPanel settingPanel = new SigmaSettingsPanel();
        settingPanel.setup(exporter);
        final DialogDescriptor dd = new DialogDescriptor(settingPanel, "Sigma.js Export");
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            //This line will write the file path from the panel to the exporter's <code>setPath()<code> method.
            settingPanel.unsetup(true);

            //Create a new executor and execute
            LongTaskExecutor executor = new LongTaskExecutor(true, "Sigma.js export");
            executor.setDefaultErrorHandler(errorHandler);
            executor.execute(exporter, new Runnable() {

                @Override
                public void run() {
                    //Get the current workspace and set it to the exporter
                    //Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
                    //exporter.setWorkspace(currentWorkspace);
                    
                    //Execute export
                    cancelled = !exporter.execute();
                    
                    //If not cancelled, write a status line message
                    if (!cancelled) {
                        StatusDisplayer.getDefault().setStatusText("Export to Sigma.js template completed");
                    }
                }
            });
        } else {
            settingPanel.unsetup(false);
        }
    }
}
