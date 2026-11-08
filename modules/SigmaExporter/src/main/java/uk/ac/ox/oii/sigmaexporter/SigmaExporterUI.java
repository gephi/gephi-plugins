/*
 Copyright Scott A. Hale, 2016
 * 
 
 Base on code from 
 Copyright 2008-2016 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

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
