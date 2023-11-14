package ec.loxa.sna.gephi.websiteexport.ui;

import ec.loxa.sna.gephi.websiteexport.WebSiteExporter;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.io.export.spi.ExporterClassUI;
import org.gephi.lib.validation.DialogDescriptorWithValidation;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author jorgaf
 */
@ServiceProvider(service = ExporterClassUI.class)
public class WebSiteExporterUI implements ExporterClassUI {
 private final ExporterSettings settings = new ExporterSettings();
    private final LongTaskListener longTaskListener;
    private final LongTaskErrorHandler errorHandler;
    private boolean cancelled = true;
    //private String filePath;

    public WebSiteExporterUI() {
        longTaskListener = new LongTaskListener() {

            @Override
            public void taskFinished(LongTask task) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (!cancelled) {
                            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                    getMessage("WebSiteExporterUI.messageFinish.description"),
                                    getMessage("WebSiteExporterUI.messageFinish.Title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
            }
        };

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
        return getMessage("WebSiteExporterUI.menu.name");
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {
        final WebSiteExporter wsExporter = new WebSiteExporter();
        settings.load(wsExporter);

        WebSiteSettingsPanel wsPanelSettings = new WebSiteSettingsPanel();
        wsPanelSettings.setup(wsExporter);

        ValidationPanel validationPanel =
                WebSiteSettingsPanel.createValidationPanel(wsPanelSettings);

        final DialogDescriptor dialogDescriptor = DialogDescriptorWithValidation.dialog(validationPanel,
            getMessage("WebSiteExporterUI.dialogdescriptor.description"));
        Object result = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (result == NotifyDescriptor.OK_OPTION) {
            wsPanelSettings.unsetup(true);
            settings.save(wsExporter);
            if (wsPanelSettings.getSelectedWorkspaces().length > 0) {
                wsExporter.setSelectedWorkspaces(wsPanelSettings.getSelectedWorkspaces());

                LongTaskExecutor executor = new LongTaskExecutor(true, "WebSiteExporter");
                executor.setLongTaskListener(longTaskListener);
                executor.setDefaultErrorHandler(errorHandler);
                executor.execute(wsExporter, new Runnable() {

                    @Override
                    public void run() {

                        cancelled = !wsExporter.execute();
                    }
                });
            } else {
                JOptionPane.showMessageDialog(validationPanel, 
                        getMessage("WebSiteExporterUI.Errormsg.WSSelected.Description"), 
                        getMessage("WebSiteExporterUI.Errormsg.WSSelected.Title"), 
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            wsPanelSettings.unsetup(false);
        }
    }

    private String getMessage(String key) {
        return NbBundle.getMessage(WebSiteExporterUI.class, key);
    }

    private static class ExporterSettings {

        final String LAST_ATTRIBUTES = "GEXFExporterUI_Last_Attributes";
        final String LAST_COLORS = "GEXFExporterUI_Last_Colors";
        final String LAST_DYNAMIC = "GEXFExporterUI_Last_Dynamic";
        final String LAST_POSITION = "GEXFExporterUI_Last_Position";
        final String LAST_SIZE = "GEXFExporterUI_Last_Size";
                
        private boolean attribures = true;
        private boolean colors = true;
        private boolean dynamic = true;
        private boolean position = true;
        private boolean size = true;
                

        public void save(WebSiteExporter exporter) {
            this.attribures = exporter.isExportAttributes();
            this.colors = exporter.isExportColors();
            this.dynamic = exporter.isExportDynamic();
            this.position = exporter.isExportPosition();
            this.position = exporter.isExportSize();                        

            NbPreferences.forModule(WebSiteExporterUI.class).putBoolean(LAST_ATTRIBUTES, attribures);
            NbPreferences.forModule(WebSiteExporterUI.class).putBoolean(LAST_ATTRIBUTES, colors);
            NbPreferences.forModule(WebSiteExporterUI.class).putBoolean(LAST_ATTRIBUTES, dynamic);
            NbPreferences.forModule(WebSiteExporterUI.class).putBoolean(LAST_ATTRIBUTES, position);
            NbPreferences.forModule(WebSiteExporterUI.class).putBoolean(LAST_ATTRIBUTES, size);            
        }

        public void load(WebSiteExporter exporter) {
            attribures = NbPreferences.forModule(WebSiteExporterUI.class).getBoolean(LAST_ATTRIBUTES, attribures);
            colors = NbPreferences.forModule(WebSiteExporterUI.class).getBoolean(LAST_ATTRIBUTES, colors);
            dynamic = NbPreferences.forModule(WebSiteExporterUI.class).getBoolean(LAST_ATTRIBUTES, dynamic);
            position = NbPreferences.forModule(WebSiteExporterUI.class).getBoolean(LAST_ATTRIBUTES, position);
            size = NbPreferences.forModule(WebSiteExporterUI.class).getBoolean(LAST_ATTRIBUTES, size);            

            exporter.setExportAttributes(attribures);
            exporter.setExportColors(colors);
            exporter.setExportDynamic(dynamic);
            exporter.setExportPosition(position);
            exporter.setExportSize(size);
        }
    }   
}
