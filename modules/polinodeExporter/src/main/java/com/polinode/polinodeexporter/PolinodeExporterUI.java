/*
 Copyright Polinode, 2017
 * 
 
 Base on code from 
 Copyright 2008-2016 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 Portions Copyrighted 2011 Gephi Consortium.
 */
package com.polinode.polinodeexporter;

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
public class PolinodeExporterUI implements ExporterClassUI {

    private final LongTaskErrorHandler errorHandler;
    private boolean cancelled = true;

    public PolinodeExporterUI() {
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
        return "Polinode Network...";
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {
        //Create exporter
        final PolinodeExporter exporter = new PolinodeExporter();
        final Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        exporter.setWorkspace(currentWorkspace);

        //Create the settings panel
        final PolinodeExporterSettingsPanel settingsPanel = new PolinodeExporterSettingsPanel();
        settingsPanel.setup(exporter);
        final DialogDescriptor dd = new DialogDescriptor(settingsPanel, "Polinode Export");
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            settingsPanel.unsetup(true);

            //  Create a new executor and execute

            LongTaskExecutor executor = new LongTaskExecutor(true, "Polinode export");
            executor.setDefaultErrorHandler(errorHandler);
            executor.execute(exporter, new Runnable() {

                @Override
                public void run() {
                    Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
                    exporter.setWorkspace(currentWorkspace);
                    exporter.setNetworkName(settingsPanel.getNetworkName());
                    exporter.setNetworkDescription(settingsPanel.getNetworkDescription());
                    exporter.setPolinodePublicKey(settingsPanel.getPolinodePublicKey());
                    exporter.setPolinodePrivateKey(settingsPanel.getPolinodePrivateKey());
                    
                    //Execute export

                    cancelled = !exporter.execute();
                    
                    //If not cancelled, write a status line message

                    if (cancelled) {
                        StatusDisplayer.getDefault().setStatusText("Export to Polinode cancelled");
                    }
                    else {
                        StatusDisplayer.getDefault().setStatusText("Export to Polinode completed");
                    }
                }
            });
        } else {
            settingsPanel.unsetup(false);
        }
    }
}
