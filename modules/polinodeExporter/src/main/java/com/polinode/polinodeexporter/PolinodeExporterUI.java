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

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
public class PolinodeExporterUI implements ExporterClassUI, ActionListener {

    private final LongTaskErrorHandler errorHandler;
    private boolean cancelled = true;

    private PolinodeExporterSettingsPanel settingsPanel;
    private DialogDescriptor dialogDescriptor;
    private Dialog dialog;

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
        return "To Polinode...";
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {
        //Create the settings panel
        settingsPanel = new PolinodeExporterSettingsPanel(this);
        settingsPanel.setup();
        
//        public DialogDescriptor(Object innerPane, String title, boolean modal, Object[] options, Object initialValue, int optionsAlign, HelpCtx helpCtx, ActionListener bl) {
    
        dialogDescriptor = new DialogDescriptor(settingsPanel, 
                "Export to Polinode", 
                true, 
                new Object[]{
                    PolinodeExporterSettingsPanel.BUTTON_DETAILEDINSTRUCTIONS,
                    PolinodeExporterSettingsPanel.BUTTON_CREATEPOLINODEACCOUNT,
                    PolinodeExporterSettingsPanel.BUTTON_CANCEL,
                    PolinodeExporterSettingsPanel.BUTTON_OK},
                PolinodeExporterSettingsPanel.BUTTON_OK,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                this);
        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true); 
    }

    public void actionPerformed(ActionEvent event) {
        if( event.getActionCommand().equals(PolinodeExporterSettingsPanel.BUTTON_OK) ) {
            
            //  validate settings

            String errorMessage = "";
            if( this.settingsPanel.getNetworkName().trim().length()==0 ) {
                errorMessage += "Network name must not be blank\n";
            }
            if( !this.settingsPanel.getPublicPrivateSelected() ) {
                errorMessage += "Please choose either Public or Private\n";
            }
            if( this.settingsPanel.getPolinodePublicKey().trim().length()==0 ) {
                errorMessage += "API Public Key must not be blank\n";
            }
            if( this.settingsPanel.getPolinodePrivateKey().trim().length()==0 ) {
                errorMessage += "API Private Key must not be blank\n";
            }
            
            if( errorMessage.length()==0 ) {

                //  no problem - proceed with close
                
                closeDialog(true);

                //  Create a new executor and execute

                final PolinodeExporter exporter = new PolinodeExporter();
                LongTaskExecutor executor = new LongTaskExecutor(true, "Polinode export");
                executor.setDefaultErrorHandler(errorHandler);
                executor.execute(exporter, new Runnable() {

                    @Override
                    public void run() {

                        Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
                        exporter.setWorkspace(currentWorkspace);
                        exporter.setNetworkName(settingsPanel.getNetworkName());
                        exporter.setNetworkDescription(settingsPanel.getNetworkDescription());
                        exporter.setIsNetworkPublic(settingsPanel.getIsNetworkPublic());
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
            }
            else {
                //  display error message
                
                final String innerErrorMessage = errorMessage;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(null,
                            innerErrorMessage,
                            "Export to Polinode",
                            JOptionPane.ERROR_MESSAGE);
                        }
                    });
            }
        }
        else if( event.getActionCommand().equals(PolinodeExporterSettingsPanel.BUTTON_CANCEL) ) {
            closeDialog(false);
        }
        else if( event.getActionCommand().equals(PolinodeExporterSettingsPanel.BUTTON_DETAILEDINSTRUCTIONS) ) {
            try {
                Desktop.getDesktop().browse(new URL("https://support.polinode.com/hc/en-us/articles/115002415352").toURI());
            } catch (Exception e) {
                Logger.getLogger(PolinodeExporter.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        else if( event.getActionCommand().equals(PolinodeExporterSettingsPanel.BUTTON_CREATEPOLINODEACCOUNT) ) {
            try {
                Desktop.getDesktop().browse(new URL("https://app.polinode.com/free-trial").toURI());
            } catch (Exception e) {
                Logger.getLogger(PolinodeExporter.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    void closeDialog(boolean save) {
        if( dialog != null) {

            settingsPanel.unsetup(save);

            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
    }
}
